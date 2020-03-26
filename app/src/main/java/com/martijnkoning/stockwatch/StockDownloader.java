package com.martijnkoning.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockDownloader extends AsyncTask<String, Integer, String> {

    private static final String URL1 = "https://cloud.iexapis.com/stable/stock/";
    private static final String URL2 = "/quote?token=";
    private static final String KEY = "pk_acdf88d35f1541e9bd5a5207273ab03a";

    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    private static final String TAG = "StockDownloader";

    StockDownloader(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    protected void onPostExecute(String s) {
        Stock stock = parseJSON(s);
        mainActivity.createStock(stock);
    }


    @Override
    protected String doInBackground(String... strings) {
        Uri dataUri = Uri.parse(URL1 + strings[0] + URL2 + KEY);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            Log.d(TAG, "doInBackground: ResponseCode: " + conn.getResponseCode());

            if (conn.getResponseCode() == 404)
                return null;

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }

        return sb.toString();
    }

    private Stock parseJSON(String s) {

        try {
            JSONObject jStock = new JSONObject(s);
            String symbol = jStock.getString("symbol");
            String companyName = jStock.getString("companyName");
            String latestPriceStr = jStock.getString("latestPrice");
            String changeStr = jStock.getString("change");
            String changePercentStr = jStock.getString("changePercent");

            double latestPrice;
            double change;
            double changePercent;

            if (latestPriceStr.equals("null"))
                latestPrice = 0;
            else
                latestPrice = Double.parseDouble(latestPriceStr);

            if (changeStr.equals("null"))
                change = 0;
            else
                change = Double.parseDouble(changeStr);

            if (changePercentStr.equals("null"))
                changePercent = 0;
            else
                changePercent = Double.parseDouble(changePercentStr);

            return new Stock(symbol, companyName, latestPrice, change, changePercent);
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


}
