package com.example.stockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DataBaseHandler extends SQLiteOpenHelper {


    private static final String TAG = "DataBaseHandler";

    // DB Name
    private static final String dbName = "StockDB";

    // If you change the database schema, you must increment the database version.
    private static final int dbVersion = 1;

    private SQLiteDatabase database;

    //Constructor
    //Open Database
    public DataBaseHandler(Context context){
        super(context, dbName, null, dbVersion);
        database = getWritableDatabase();
    }

    //Table and columns for stockTable
    private static final String stockTable = "stockTable";
    private static final String symbol = "SYMBOL";
    private static final String name = "NAME";


    //Make SQL Creating Command into String
    private static String createCommand = "CREATE TABLE " + stockTable + " (" + symbol + " TEXT not null unique, " + name + " TEXT not null)";

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVer, int newVer){
        return;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //DB creation Done in onCreate
        database.execSQL(createCommand);
    }



    public void addStock(String symbolValue, String nameValue){

        //Sample method to add a stock to the DB by professor
        ContentValues stockValues = new ContentValues();
        stockValues.put(symbol, symbolValue);
        stockValues.put(name, nameValue);

        //add to database
        //long l =
        database.insert(stockTable, null, stockValues);

        Log.d(TAG, "addStock: Add Complete!");
    }


    public void deleteStock(String symbol){
        //DB Delete (Sample method to delete a stock from the DB):
        int cnt = database.delete(stockTable, symbol + " = ?", new String[] { symbol });

        Log.d(TAG, "delStock: deleting stock" + symbol);
    }

    //DB Load All (Sample method to get all stock-company entries from the DB):
    //Returns the stocks that are in the database
    public ArrayList<String[]> getStocks(){
        //Init ArrayList to be returned
        ArrayList<String[]> stockArrayList = new ArrayList<>();

        //Cursor object used to make queries
        Cursor c = database.query(
                stockTable,// The table to query
                new String[]{symbol, name}, // The columns to return
                null,//The columns for the WHERE clause
                null,// The values for the WHERE clause
                null, // don't group the rows
                null,// don't filter by row groups
                null);// don't filter by row groups

        //if Cursor not null
        if(c!=null){
            if(c.moveToFirst()){ //moves cursor to first row
                for(int i = 0; i < c.getCount(); i++){
                    //get the values for s=symbol n=name, add to array list, move the cursor
                    String s = c.getString(0);
                    String n = c.getString(1);
                    stockArrayList.add(new String[]{s, n});
                    Log.d(TAG, "getStocks: Added another stock to array list");
                    c.moveToNext();
                }
                c.close();
            }
        }
        Log.d(TAG, "getStocks: adding Done");
        return stockArrayList;
    }

    //Dumps Database content to log
    public void dumpLog(){
        //Cursor object used to make queries
        Cursor c = database.query(stockTable,
                new String[]{symbol, name},
                null,
                null,
                null,
                null,
                null);

        //Cursor not null
        if(c!=null){
            if(c.moveToFirst()) { //moves cursor to first row
                for (int i = 0; i < c.getCount(); i++) {
                    //get the values for s = symbol n =name
                    String s = c.getString(0);
                    String n = c.getString(1);

                    //data gets logged here then after, cursor mvoes
                    Log.d(TAG, "dumpLog: SYMBOL:" + s + " NAME:" + n);
                    c.moveToNext();
                }
                c.close();
            }
        }
    }

    //Close Database
    public void shutDown(){
        database.close();
    }
}
