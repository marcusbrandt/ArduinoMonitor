package monitor.arduino.tcc.arduinomonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener{
    private static final String DEBUG_TAG = "HttpExample";
    ArrayList<EnergyMonitor> teams = new ArrayList<EnergyMonitor>();
    ArrayList<EnergyMonitor> daysEnergy = new ArrayList<EnergyMonitor>();
    ArrayList<Date> datesSpinner = new ArrayList<Date>();
    Button btnDownload;
    BarChart mChart;
    Spinner spinner;
    private Typeface mTf;
    private ArrayList<ArrayList<EnergyMonitor>> days;
    private ArrayList<String> spinnerDays;
    private ProgressDialog ringProgressDialog;

//    protected String[] mMonths = new String[] {
//            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            btnDownload.setEnabled(true);
        } else {
            btnDownload.setEnabled(false);
        }

        spinner = (Spinner) findViewById(R.id.spinner_days);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setData(days.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ringProgressDialog = new ProgressDialog(MainActivity.this);
        ringProgressDialog.setTitle("Por favor aguarde!");
        ringProgressDialog.setMessage("Fazendo o download das informações...");
        ringProgressDialog.setCancelable(false);

        mChart = (BarChart) findViewById(R.id.bar_chart);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);

        mChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // mChart.setDrawBarShadow(true);

        // mChart.setDrawXLabels(false);

        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

        mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);


        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(mTf);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });

//        setData(12, 50);


    }

    public void buttonClickHandler(View view) {
        ringProgressDialog.show();
        new DownloadWebpageTask(new AsyncResult() {
            @Override
            public void onResult(JSONObject object) {
                processJson(object);
            }
        }).execute("https://spreadsheets.google.com/tq?key=1g5t3auIznGoja909lVHAPC5xl1UwMZenoaWwZdfduUM");

    }

    private void processJson(JSONObject object) {
        String tensao = null;
        String corrente =  null;
        String potenciaAtiva =  null;
        String potenciaAparente =  null;
        String fatorDePotencia =  null;
        String energiaInstantanea = null;
        String hora = null;

        try {
            JSONArray rows = object.getJSONArray("rows");

            for (int r = 0; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");


                if (!columns.isNull(1) && !columns.isNull(2) && !columns.isNull(2)
                        && !columns.isNull(3) && !columns.isNull(4) && !columns.isNull(5)
                        && !columns.isNull(6)){
                    tensao = columns.getJSONObject(1).getString("v");
                    corrente = columns.getJSONObject(2).getString("v");
                    potenciaAtiva = columns.getJSONObject(3).getString("v");
                    potenciaAparente = columns.getJSONObject(4).getString("v");
                    fatorDePotencia = columns.getJSONObject(5).getString("v");
                    energiaInstantanea = columns.getJSONObject(6).getString("v");
                    hora = columns.getJSONObject(0).getString("f");

                    EnergyMonitor energyMonitor = new EnergyMonitor(tensao, corrente, potenciaAtiva, potenciaAparente, fatorDePotencia, energiaInstantanea, hora);
                    teams.add(energyMonitor);
                }

            }

            findDays(teams);

            setData(days.get(0));
            ringProgressDialog.dismiss();

//            final TeamsAdapter adapter = new TeamsAdapter(this, R.layout.team, teams);
//            listview.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void findDays(ArrayList<EnergyMonitor> energys){

        days = new ArrayList<ArrayList<EnergyMonitor>>();
        ArrayList<EnergyMonitor> auxDays = new ArrayList<EnergyMonitor>();
        spinnerDays = new ArrayList<String>();
        String data;


        data = energys.get(0).getHora();

        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.US).parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayAux = calendar.get(Calendar.DAY_OF_MONTH);
        int dayActual;
        spinnerDays.add(data.substring(0, 10));



        for(EnergyMonitor energy:energys){

            data = energy.getHora();
            try {
                date = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.US).parse(data);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar = Calendar.getInstance();
            calendar.setTime(date);
            dayActual = calendar.get(Calendar.DAY_OF_MONTH);

            if(dayActual!=dayAux){
                days.add(auxDays);
                auxDays = new ArrayList<EnergyMonitor>();
                dayAux = dayActual;
                spinnerDays.add(data.substring(0,10));
            }


            auxDays.add(energy);
        }

        days.add(auxDays);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerDays);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }


    private void setData(ArrayList<EnergyMonitor> energyMonitors) {
        String dadoS;
        String hora;
        float media = 0.0f;
        int count = 0;
        int minutos = 0;
        int minutosAux = 0;

        ArrayList<String> xVals = new ArrayList<String>();
        int size = energyMonitors.size();
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < size; i++) {
            hora = energyMonitors.get(i).getHora();
            Date date = null;
            try {
                date = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.US).parse(hora);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            minutos = calendar.get(Calendar.MINUTE);



            dadoS = energyMonitors.get(i).getEnergiaInstantanea();
            if(dadoS.equals("$energiainst$")) {
                dadoS = "0.0";
            }

            float dado = Float.valueOf(dadoS);

            count++;
            media+=dado;

            if(minutos!=minutosAux){

                media=media/count;
                xVals.add(hora.substring(10,hora.length()-3));
                yVals1.add(new BarEntry(media, i));
                minutosAux = minutos;
                count=0;
                media=0;
            }


        }

        BarDataSet set1 = new BarDataSet(yVals1, "Energia Instantânea (HH:MM)");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        // data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(10f);
        data.setValueTypeface(mTf);

        mChart.setData(data);
        mChart.invalidate();
    }



    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (e == null)
            return;

//        RectF bounds = mChart.getBarBounds((BarEntry) e);
//        PointF position = mChart.getPosition(e, YAxis.AxisDependency.LEFT);
//
//        Log.i("bounds", bounds.toString());
//        Log.i("position", position.toString());
//
//        Log.i("x-index",
//                "low: " + mChart.getLowestVisibleXIndex() + ", high: "
//                        + mChart.getHighestVisibleXIndex());
    }

    @Override
    public void onNothingSelected() {

    }
}




