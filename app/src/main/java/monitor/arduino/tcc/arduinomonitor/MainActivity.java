package monitor.arduino.tcc.arduinomonitor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "HttpExample";
    ArrayList<EnergyMonitor> teams = new ArrayList<EnergyMonitor>();
    ListView listview;
    Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listview = (ListView) findViewById(R.id.listview);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            btnDownload.setEnabled(true);
        } else {
            btnDownload.setEnabled(false);
        }
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

                EnergyMonitor energyMonitor = new EnergyMonitor(tensao, corrente, potenciaAtiva, potenciaAparente, fatorDePotencia, energiaInstantanea);
                teams.add(energyMonitor);
            }

            final TeamsAdapter adapter = new TeamsAdapter(this, R.layout.team, teams);
            listview.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}




