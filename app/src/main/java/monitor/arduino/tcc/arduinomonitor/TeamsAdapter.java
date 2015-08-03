package monitor.arduino.tcc.arduinomonitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kstanoev on 1/14/2015.
 */
public class TeamsAdapter extends ArrayAdapter<EnergyMonitor> {

    Context context;
    private ArrayList<EnergyMonitor> teams;

    public TeamsAdapter(Context context, int textViewResourceId, ArrayList<EnergyMonitor> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.teams = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.team, null);
        }
        EnergyMonitor o = teams.get(position);
        if (o != null) {
            TextView pos = (TextView) v.findViewById(R.id.position);
            TextView name = (TextView) v.findViewById(R.id.name);
            TextView wins = (TextView) v.findViewById(R.id.wins);
            TextView draws = (TextView) v.findViewById(R.id.draws);
            TextView losses = (TextView) v.findViewById(R.id.losses);
            TextView points = (TextView) v.findViewById(R.id.points);

            pos.setText(String.valueOf(o.getTensao()));
            name.setText(String.valueOf(o.getCorrente()));
            wins.setText(String.valueOf(o.getPotenciaAtiva()));
            draws.setText(String.valueOf(o.getPotenciaAparente()));
            losses.setText(String.valueOf(o.getFatorDePotencia()));
            points.setText(String.valueOf(o.getEnergiaInstantanea()));
        }
        return v;
    }
}
