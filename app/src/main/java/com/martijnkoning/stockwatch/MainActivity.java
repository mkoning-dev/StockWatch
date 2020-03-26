package com.martijnkoning.stockwatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private final List<Stock> stockList = new ArrayList<>();
    private final List<Stock> tempList = new ArrayList<>();
    TreeMap<String, String> stockMap = new TreeMap<>();

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private StockAdapter stockAdapter;

    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);

        stockAdapter = new StockAdapter(stockList, this);

        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        if (doNetCheck())
            new NameDownloader(this).execute();
        else
            noNetworkDialog('n');

        databaseHandler = new DatabaseHandler(this);

        ArrayList<Stock> list = databaseHandler.loadStocks();
        tempList.clear();
        tempList.addAll(list);

        if (doNetCheck()) {
            for (int i = 0; i < tempList.size(); i++) {
                new StockDownloader(this).execute(tempList.get(i).getSymbol());
            }
        } else {
            for (int i = 0; i < tempList.size(); i++) {
                tempList.get(i).setPercentage();
                tempList.get(i).setPriceChange();
                tempList.get(i).setPrice();
            }
            stockList.addAll(tempList);
            sortStocks();
            stockAdapter.notifyDataSetChanged();
        }

    }


    // This is called in the onPostExecute of the NameDownloader
    public void updateData(TreeMap<String, String> sList) {
        stockMap.putAll(sList);
        //stockAdapter.notifyDataSetChanged();

        databaseHandler.dumpDbToLog();

    }

    // This is called in the onPostExecute of StockDownloader
    public void createStock(Stock stock) {
        if (stock != null) {
            stockList.add(stock);

            // only update DB when new stocks are added
            if (stockList.size() > tempList.size()) {
                //Toast.makeText(MainActivity.this, "Adding to DB", Toast.LENGTH_SHORT).show();
                databaseHandler.addStock(stock);
            }
            sortStocks();
            stockAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Stock not found", Toast.LENGTH_SHORT).show();
        }
    }

    // Check for internet connection
    private boolean doNetCheck() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }

    public void sortStocks() {
        Collections.sort(stockList, new Comparator<Stock>() {
            public int compare(Stock s1, Stock s2) {
                return s1.getSymbol().compareTo(s2.getSymbol());
            }
        });
    }

    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }


    //
    // Buttons and such
    //

    @Override
    public void onClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);

        Intent i = new Intent(Intent.ACTION_VIEW);
        String mwURL = "https://www.marketwatch.com/tools/quotes/lookup.asp?lookup=";
        i.setData(Uri.parse(mwURL + stockList.get(pos).getSymbol()));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.delete);

        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                databaseHandler.deleteStock(stockList.get(pos).getSymbol());
                stockList.remove(pos);
                stockAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol " + stockList.get(pos).getSymbol() + "?");

        AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }

    private void doRefresh() {
        if (doNetCheck()) {
                tempList.clear();
                tempList.addAll(stockList);
                stockList.clear();
                for (int i = 0; i < tempList.size(); i++) {
                    new StockDownloader(this).execute(tempList.get(i).getSymbol());
                }
        } else
            noNetworkDialog('r');

        swiper.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_stock, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add) {
            if (doNetCheck()) {
                // If no symbols/names were downloaded at the start of the app, do so now
                if (stockMap != null) {
                    new NameDownloader(this).execute();
                }
                selectDialog();
            } else
                noNetworkDialog('a');

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //
    // Dialog related code below
    //

    // The Select Symbol dialog
    public void selectDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol:");

        builder.setView(view);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText et = view.findViewById(R.id.input);

                String symbol = et.getText().toString();

                TreeMap<String, String> filteredMap = filterSymbol(symbol);

                if (filteredMap.size() == 0) {
                    // If symbol not found, search company name
                    TreeMap<String, String> tempMap = filterName(symbol);
                    if (tempMap.size() == 0) {
                        // If no company name found either, show symbol not found dialog
                        stockDialog(symbol, 'n');
                    } else if (tempMap.size() == 1) {
                        // If one match is found in company names, check if it already exists
                        boolean exists = false;
                        for (int i = 0; i < stockList.size(); i++) {
                            if (stockList.get(i).getSymbol().equals(tempMap.firstKey()))
                                exists = true;
                        }
                        if (exists) // If stock already exists in list, show duplicate dialog
                            stockDialog(symbol, 'd');
                        else // Otherwise add it to the list
                            new StockDownloader(MainActivity.this).execute(tempMap.firstKey());
                    } else {
                        // If more than one matching company name has been found, go to list dialog
                        listDialog(tempMap);
                    }
                } else if (filteredMap.size() == 1) {
                    // If one symbol has been found, check if it already exists
                    boolean exists = false;
                    for (int i = 0; i < stockList.size(); i++) {
                        if (stockList.get(i).getSymbol().equals(symbol))
                            exists = true;
                    }
                    if (exists) // If stock already exists in list, show duplicate dialog
                        stockDialog(symbol, 'd');
                    else // Otherwise, add it to the list
                        new StockDownloader(MainActivity.this).execute(filteredMap.firstKey());
                } else if (!symbol.equals("")) {
                    // If more than one result has been found and the symbol entered is not blank,
                    // go to list dialog
                    listDialog(filteredMap);
                }

            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    // The dialog shown if multiple results are returned after a symbol search
    public void listDialog(SortedMap<String, String> filteredMap) {
        final String[] sArray = new String[filteredMap.size()];

        int index = 0;

        for (String i : filteredMap.keySet()) {
            if (Objects.equals(filteredMap.get(i), ""))
                sArray[index] = i;
            else
                sArray[index] = i + " - " + filteredMap.get(i);
            index++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");

        builder.setItems(sArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String selected = sArray[which];
                String symbol;
                if (selected.contains(" "))
                    symbol = selected.substring(0, selected.indexOf(" "));
                else
                    symbol = selected;

                boolean exists = false;
                for (int i = 0; i < stockList.size(); i++) {
                    if (stockList.get(i).getSymbol().equals(symbol))
                        exists = true;
                }

                if (exists)
                    stockDialog(symbol, 'd');
                else
                    new StockDownloader(MainActivity.this).execute(symbol);
            }
        });
        builder.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    // Shows the No Network Connection dialog if there is no internet connection
    // when attempting to add a stock or refresh the list
    public void noNetworkDialog(char caller) { // caller: 'a' for add, 'r' for refresh
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("No Network Connection");

        if (caller == 'a')
            builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
        else if (caller == 'r')
            builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");
        else if (caller == 'n')
            builder.setMessage("Please refresh the app when the internet connection is restored");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Shows the Duplicate Stock or Symbol Not Found dialog
    public void stockDialog(String symbol, char reason) { // reason: 'd' for duplicate, 'n' for not found
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (reason == 'd') {
            builder.setIcon(R.drawable.duplicate);
            builder.setTitle("Duplicate Stock");
            builder.setMessage("Stock symbol " + symbol + " is already displayed");
        } else if (reason == 'n') {
            builder.setTitle("Symbol Not Found: " + symbol);
            builder.setMessage("Please enter a valid stock symbol");
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Filters stock list by strings containing string provided by user
    public TreeMap<String, String> filterName(String name) {
        TreeMap<String, String> tempMap = new TreeMap<>();
        if (name.length() > 0) {
            for (String i : stockMap.keySet()) {
                if (Objects.requireNonNull(stockMap.get(i)).contains(name))
                    tempMap.put(i, stockMap.get(i));
            }
            return tempMap;
        }
        return tempMap;
    }

    // Filters stock list by strings containing string provided by user
    public TreeMap<String, String> filterSymbol(String name) {
        TreeMap<String, String> tempMap = new TreeMap<>();
        if (name.length() > 0) {
            for (String i : stockMap.keySet()) {
                if (Objects.requireNonNull(i.contains(name)))
                    tempMap.put(i, stockMap.get(i));
            }
            return tempMap;
        }
        return stockMap;
    }


}
