package com.example.stockwatch;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private final List<Stock> stockList = new ArrayList<>(); //main content
    private SwipeRefreshLayout swiper;
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private int stockSelected = 0;

    //DataBase variables
    private ArrayList<String[]> dbStocks;
    private DataBaseHandler dbHandler;

    private HashMap<String, String> stockMap;

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


        dbHandler = new DataBaseHandler(MainActivity.this);
        dbStocks = dbHandler.getStocks();

        //If internet connected,  download ths stocks' data
        if(connected()){
            for(int i=0; i < dbStocks.size(); i++){

                String[] Str = {dbStocks.get(i)[0], dbStocks.get(i)[1]};
                GetFinancialData getFinancialData = new GetFinancialData(this,-1,Str,handler);
                Thread t = new Thread(getFinancialData);
                t.start();

            }
            //Get name
            GetNameData getNameData = new GetNameData(this);
            Thread t1 = new Thread(getNameData);
            t1.start();
        }
        else{
            for(int i=0; i < dbStocks.size(); i++){
                Stock currStock = new Stock(dbStocks.get(i)[0], dbStocks.get(i)[1],0,0,0);
                stockList.add(currStock);
            }
        }
        stockAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume(){
        dbHandler.dumpLog();

        super.onResume();
    }

    @Override
    protected void onDestroy(){
        dbHandler.shutDown();

        super.onDestroy();
    }

    //check connectivity of internet
    public boolean connected(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    public void doRefresh(){
        //If connected, update data
        if(connected()){

            //update
            for(int i=0; i < stockList.size(); i++){
                String[] Str = {stockList.get(i).getSymbol(), stockList.get(i).getCompany()};
                GetFinancialData getFinancialData = new GetFinancialData(this,i,Str,handler);
                Thread t = new Thread(getFinancialData);
                t.start();
            }
            Toast.makeText(this, "Stocks Updated!", Toast.LENGTH_SHORT).show();
        }
        //otherwise, prompt user that we do not have a connection
        else{
            AlertDialog.Builder ADB = new AlertDialog.Builder(MainActivity.this);
            ADB.setTitle("No Network Connection");
            ADB.setMessage("Stocks Cannot Be Updated Without A Network Connection!");
            AlertDialog AD = ADB.create();
            AD.show();
        }
        //notify stock adapter that changes occured
        stockAdapter.notifyDataSetChanged();
        swiper.setRefreshing(false);
    }

    @Override
    public void onClick(View v){
        if(connected()){
            //get the stock selected, use the URL and stock symbol to go to the marketwatch website
            stockSelected = recyclerView.getChildAdapterPosition(v);
            String tempSymbol = stockList.get(stockSelected).getSymbol();
            String url = "https://www.marketwatch.com/investing/stock/" + tempSymbol;

            //Set up intent to go to browser
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
        else{
            AlertDialog.Builder ADB = new AlertDialog.Builder(MainActivity.this);
            ADB.setTitle("No Network Connection");
            ADB.setMessage("Cannot open browser without network connection!");
            AlertDialog AD = ADB.create();
            AD.show();
        }
    }

    @Override
    public boolean onLongClick(View v){
        AlertDialog.Builder ADB = new AlertDialog.Builder(this);
        final int position = recyclerView.getChildLayoutPosition(v);

        //Dialog Box YES = Delete, No = Don't Delete
        ADB.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stockList.remove(position);
                //changes have been made to arraylist
                //notify adapter!
                stockAdapter.notifyDataSetChanged();
            }
        });

        ADB.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        TextView symbol = v.findViewById(R.id.symbol);
        ADB.setTitle("Delete Stock");
        ADB.setIcon(R.drawable.baseline_delete_black_18dp);
        ADB.setMessage("Delete Stock Symbol: " + symbol.getText().toString() + " ?");
        AlertDialog AD = ADB.create();
        AD.show();
        stockAdapter.notifyDataSetChanged();;
        return true;
    }

    //Menu Stuff
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Menu item is pressed
        if(item.getItemId() == R.id.add){
            if(connected()){
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                final View v = layoutInflater.inflate(R.layout.dialog, null);

                //Make dialog box
                AlertDialog.Builder ADB = new AlertDialog.Builder(this);
                ADB.setView(v);
                ADB.setTitle("Stock Selection");
                ADB.setMessage("Please Enter A Stock Symbol:  ");

                final EditText userInput = v.findViewById(R.id.userInput);
                userInput.setFilters(new InputFilter[]
                        {new InputFilter.AllCaps()});
                userInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                //Positive Button to search
                ADB.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputSymbol = userInput.getText().toString();
                        search(inputSymbol);
                    }
                });

                ADB.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                //Present the dialog box
                AlertDialog AD = ADB.create();
                AD.show();
            }
            else{
                AlertDialog.Builder ADB = new AlertDialog.Builder(MainActivity.this);
                ADB.setTitle("No Internet Connection");
                ADB.setMessage("Stocks Cannot Be Added Without A Internet Connection!");
                AlertDialog AD = ADB.create();
                AD.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void generateHashMap(HashMap<String,String> HM){
        this.stockMap = HM;
    }

    public void search(String userInput){
        if(!userInput.isEmpty()){

            for(int i=0; i < stockList.size(); i++){
                if(stockList.get(i).getSymbol().equals(userInput)){

                    //If the stock already is in the list
                    AlertDialog.Builder ADB = new AlertDialog.Builder(this);
                    ADB.setTitle("Duplicate Stock");
                    ADB.setIcon(R.drawable.warning);
                    ADB.setMessage("Stock Symbol " + userInput + " is already displayed.");
                    AlertDialog AD = ADB.create();
                    AD.show();
                    return;
                }
            }

            //We also need to check for substrings from the user's input to company or stock symbol
            final List<String> items = new ArrayList<>();

            //For Each item in the hashmap
            for(Map.Entry<String,String> entry: stockMap.entrySet()){
                //Get stock symbol and company name
                String k = entry.getKey();
                String v = entry.getValue();

                //check for substrings
                if(k.contains(userInput) || v.contains(userInput)){
                    //Add this stock symbol and name to the list for the user to pick from
                    String tmp = k + " - " + v;
                    items.add(k);
                }
            }
            //search for existing stock
            if(items.size() > 0){
                final CharSequence[] listPrompt = new CharSequence[items.size()];
                for(int i=0; i< items.size(); i++){
                    listPrompt[i] = items.get(i) + " - " + stockMap.get(items.get(i));
                }

                //Another Alert Dialog prompting the user with the stocks to pick from
                AlertDialog.Builder ADB = new AlertDialog.Builder(this);
                ADB.setTitle("Select A Stock");
                ADB.setItems(listPrompt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Download the stock information for the newly added stock
                        String[] Str = {items.get(which), stockMap.get(items.get(which))};
                        GetFinancialData getFinancialData = new GetFinancialData(MainActivity.this,-1,Str,handler);
                        Thread t = new Thread(getFinancialData);
                        t.start();
                        dbHandler.addStock(items.get(which), stockMap.get(items.get(which)));
                    }
                });

                ADB.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog AD = ADB.create();
                AD.show();
            }

            //Stock doesn't exist (or at least has not been found by the search)
            else{
                //Alert Dialog Box to tell user it can't be found
                AlertDialog.Builder ADB = new AlertDialog.Builder(this);
                ADB.setTitle("Symbol Not Found: " + userInput);
                ADB.setMessage("Data for stock symbol");

                AlertDialog AD = ADB.create();
                AD.show();
            }
        }
    }
    public void downloadFailed() {
        stockList.clear();
        stockAdapter.notifyDataSetChanged();
    }





    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == 1) {

                int pos = msg.arg1;
//                if(msg.arg1 == -1) return;;
                //Simply add a stock
                Stock s = (Stock) msg.obj;
                Log.d("TZ", "handleMessage: postExecute: add");
                Log.d("TZ", "handleMessage: " + s);
                stockList.add(s);
                stockAdapter.notifyDataSetChanged();
            }else if(msg.what == 2 ){
                Log.d("TAG", "updateStock: ");

                int pos = msg.arg1;
                Stock s = (Stock) msg.obj;
                stockList.set(pos, s);
                stockAdapter.notifyDataSetChanged();
            }
        }
    };

    }




