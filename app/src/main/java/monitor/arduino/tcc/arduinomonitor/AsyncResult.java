package monitor.arduino.tcc.arduinomonitor;

import org.json.JSONObject;

interface AsyncResult
{
    void onResult(JSONObject object);
}