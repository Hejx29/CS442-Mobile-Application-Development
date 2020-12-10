package com.example.stockwatch;
import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class GetNameData implements Runnable {


    private MainActivity mainActivity;
    private String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    //private String requestMethod = "GET";

    private static final String TAG = "GetFinancialData";

    public GetNameData(MainActivity ma) {
        this.mainActivity = ma;
    }

    @Override
    public void run() {
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                handleResults(null);
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            handleResults(null);
            return;
        }

        handleResults(sb.toString());

    }
    private void handleResults(final String s) {

        if (s == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.downloadFailed();
                }
            });
            return;
        }

        final HashMap<String, String> symbolNameMap = parseJSON(s);
        mainActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            mainActivity.generateHashMap(symbolNameMap);
        }
        });
    }
    private HashMap<String, String> parseJSON(String str){
        HashMap<String, String> parsedMap = new HashMap<>();

        try{
            JSONArray jArray = new JSONArray(str);

            //Store each stock's company name and symbol into a hashmap
            for(int i = 0; i < jArray.length(); i++){
                JSONObject stock = jArray.getJSONObject(i);
                String name = stock.getString("name");
                if(!name.isEmpty()){
                    String symbol = stock.getString("symbol");
                    parsedMap.put(symbol, name);
                }
            }
            return parsedMap;
        }
        catch(JSONException e){
            e.printStackTrace();
            return null;
        }
    }
}





