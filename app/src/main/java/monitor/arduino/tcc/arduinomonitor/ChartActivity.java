package monitor.arduino.tcc.arduinomonitor;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChartActivity extends Activity {

    private Spinner spinner;
    private Typeface mTf;
    private ArrayList<EnergyMonitor> teams;
    private ArrayList<String> spinnerDays;
    private BarChart mChart;
    private ArrayList<ArrayList<EnergyMonitor>> days;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grafico);

        spinner = (Spinner) findViewById(R.id.spinner_days_chart);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(days!=null)
                    setData(days.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btnReturn = (Button) findViewById(R.id.btn_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        teams = DataManager.data;
        findDays(teams);

        mChart = (BarChart) findViewById(R.id.bar_chart);
//        mChart.setOnChartValueSelectedListener(this);

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

//        mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);


        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setTypeface(mTf);
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

}
