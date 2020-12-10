package com.example.stockwatch;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetFinancialData implements Runnable {

    private static final String TAG = "GetFinancialData";
    private MainActivity mainActivity;
    private String DATA_URL = "https://cloud.iexapis.com/stable/stock/";
    private String symbol;
    private String name;
    private int position;
    private Handler handler;
    public GetFinancialData(MainActivity mainActivity, int pos, String[] StrArgs, Handler handler) {
        this.mainActivity = mainActivity;
        this.position = pos;
        this.symbol = StrArgs[0];
        this.name = StrArgs[1];
        this.handler = handler;
    }

    @Override
    public void run() {

        DATA_URL= DATA_URL + this.symbol + "/quote?token=sk_b700e68ef2d2422d983c42dcd438bbe3";
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

    private void handleResults(String s) {

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

        double latestPrice = 0.0;
        double change = 0.0;
        double changePercent = 0.0;

        try{
            JSONObject jo = new JSONObject(s);
            latestPrice = Double.valueOf(jo.getDouble("latestPrice"));
            change = jo.getDouble("change");
            changePercent = Double.valueOf(jo.getDouble("changePercent"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Make that new stock object
        Stock stock = new Stock(this.symbol, this.name, latestPrice, change, changePercent);

        //If stock exists UPDATE STOCK s
        if(this.position > -1){
            Message message = new Message();
            message.what = 2;
            message.obj = stock;
            message.arg1 = this.position;
            handler.sendMessage(message);
            Log.d("TZ message", "onPostExecute: update");
         }
        //If stock doesn't exist ADD STOCK s
        else{
            Message message = new Message();
            message.what = 1;
            message.obj = stock;
            handler.sendMessage(message);
            Log.d("TZ message", "onPostExecute: add");

        }
    }





}
