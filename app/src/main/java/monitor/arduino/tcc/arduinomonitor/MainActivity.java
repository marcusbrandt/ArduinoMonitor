package monitor.arduino.tcc.arduinomonitor;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

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
    ListView listview;
    Button btnDownload;
    BarChart mChart;
    private Typeface mTf;

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

    private void setData(ArrayList<EnergyMonitor> energyMonitors) {
        String tensaoS;
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



            tensaoS = energyMonitors.get(i).getTensao();
            if(tensaoS.equals("$tensao$")) {
                tensaoS = "0.0";
            }

            float tensao = Float.valueOf(tensaoS);

            count++;
            media+=tensao;

            if(minutos!=minutosAux){

                media=media/count;
                Log.d("Brandt", tensaoS);
                Log.d("Brandt", String.valueOf(date));
                Log.d("Brandt", ""+minutos);
                xVals.add("" + minutos);
                yVals1.add(new BarEntry(media, i));
                minutosAux = minutos;
                count=0;
                media=0;
            }


        }

        BarDataSet set1 = new BarDataSet(yVals1, "Minutos");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        // data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(10f);
        data.setValueTypeface(mTf);

        mChart.setData(data);
    }

    public void buttonClickHandler(View view) {
        new DownloadWebpageTask(new AsyncResult() {
            @Override
            public void onResult(JSONObject object) {
                processJson(object);
            }
        }).execute("https://spreadsheets.google.com/tq?key=1XvVQycX7L_Yg9d39dMxlDo_MQNyG--SklzW7nl_BQIc");

    }

    private void processJson(JSONObject object) {

        try {
            JSONArray rows = object.getJSONArray("rows");

            for (int r = 0; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                String tensao = columns.getJSONObject(1).getString("v");
                String corrente = columns.getJSONObject(2).getString("v");
                String potenciaAtiva = columns.getJSONObject(3).getString("v");
                String potenciaAparente = columns.getJSONObject(4).getString("v");
                String fatorDePotencia = columns.getJSONObject(5).getString("v");
                String energiaInstantanea = columns.getJSONObject(6).getString("v");
                String hora = columns.getJSONObject(0).getString("f");

                EnergyMonitor energyMonitor = new EnergyMonitor(tensao, corrente, potenciaAtiva, potenciaAparente, fatorDePotencia, energiaInstantanea, hora);
                teams.add(energyMonitor);
            }

            setData(teams);

//            final TeamsAdapter adapter = new TeamsAdapter(this, R.layout.team, teams);
//            listview.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (e == null)
            return;

        RectF bounds = mChart.getBarBounds((BarEntry) e);
        PointF position = mChart.getPosition(e, YAxis.AxisDependency.LEFT);
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




