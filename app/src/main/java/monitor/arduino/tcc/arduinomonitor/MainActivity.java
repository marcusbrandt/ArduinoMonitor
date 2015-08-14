package monitor.arduino.tcc.arduinomonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{
    private static final String DEBUG_TAG = "DataChart";
    ArrayList<EnergyMonitor> teams = new ArrayList<EnergyMonitor>();
    Button btnDownload;
    Button btnChart;
    BarChart mChart;
    Spinner spinner;
    private ArrayList<ArrayList<EnergyMonitor>> days;
    private ArrayList<String> spinnerDays;
    private ProgressDialog ringProgressDialog;
    private ListView listViewPotencia;
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

        btnChart = (Button) findViewById(R.id.btn_Chart);
        btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ChartActivity.class);
                startActivity(myIntent);
            }
        });

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

        listViewPotencia = (ListView) findViewById(R.id.chart_list);

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
            ringProgressDialog.dismiss();
            setData(days.get(0));
            DataManager.data = teams;

            if(teams.size() > 0)
                btnChart.setEnabled(true);
            else
                btnChart.setEnabled(false);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void findDays(ArrayList<EnergyMonitor> energys){

        days = new ArrayList<ArrayList<EnergyMonitor>>();
        ArrayList<EnergyMonitor> auxDays = new ArrayList<EnergyMonitor>();
        spinnerDays = new ArrayList<String>();
        String data;

        days.clear();
        spinnerDays.clear();
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
        ArrayList<EnergyMonitor> energy = new ArrayList<EnergyMonitor>();
        energy.add(energyMonitors.get(energyMonitors.size()-1));
        final TeamsAdapter adapter = new TeamsAdapter(this, R.layout.team, energy);
        listViewPotencia.setAdapter(adapter);
        listViewPotencia.invalidate();
    }


}




