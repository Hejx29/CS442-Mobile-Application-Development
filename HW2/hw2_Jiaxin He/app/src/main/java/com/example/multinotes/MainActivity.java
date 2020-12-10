package com.example.multinotes;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multinotes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity // NOTE the interfaces here!
        implements View.OnClickListener, View.OnLongClickListener {

    private List<Note> noteList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;

    private int noteToEdit = 0;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        noteAdapter = new NoteAdapter(noteList,this);
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Load is initiated in the onCreate method.
        this.loadJSONFile();
        if(noteList.size()>0){
            getSupportActionBar().setTitle(getString(R.string.app_name) + "(" + noteList.size() + ")");
        }
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {

        //Save is performed in the onPause method.
        this.writeJSONFile();
        Log.d(TAG, "onPause: writeJSONFile");

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case(R.id.about):
                Intent toAbout = new Intent(this, AboutActivity.class);
                startActivity(toAbout);
                return super.onOptionsItemSelected(item);
            case(R.id.addNote):
                Intent toEdit = new Intent(this, EditActivity.class);
                startActivityForResult(toEdit, 1);
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getPresentTime(){
        Date Cal = Calendar.getInstance().getTime();
        SimpleDateFormat SDF = new SimpleDateFormat("EEE MMM d, h:mm a");
        String SaveDate = SDF.format(Cal).toString();
        return SaveDate;
    }

    private void writeJSONFile() {
        try{
            FileOutputStream output = getApplicationContext().openFileOutput(getString(R.string.noteFile), Context.MODE_PRIVATE);

            JsonWriter JW = new JsonWriter(new OutputStreamWriter(output, getString(R.string.encoding)));
            JW.setIndent("  ");
            JW.beginArray();

            for(int i=0; i<noteList.size(); i++){
                JW.beginObject();
                JW.name("SaveDate").value(noteList.get(i).getLastSaveDate());
                JW.name("noteTitle").value(noteList.get(i).getNoteTitle());
                JW.name("noteText").value(noteList.get(i).getNoteText());
                JW.endObject();
            }
            JW.endArray();
            JW.close();
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadJSONFile() {
        try{
            InputStream input = getApplicationContext().openFileInput(getString(R.string.noteFile));
            BufferedReader BR = new BufferedReader(new InputStreamReader(input, getString(R.string.encoding)));

            StringBuilder SB = new StringBuilder();
            String line;
            while((line = BR.readLine()) != null) {
                SB.append(line);
            }

            JSONArray jsonArray = new JSONArray(SB.toString());
            for(int i=0; i<jsonArray.length(); i++){

                JSONObject noteObject = jsonArray.getJSONObject(i);
                String lastSaveDate = noteObject.getString("SaveDate");
                String noteTitle = noteObject.getString("noteTitle");
                String noteText = noteObject.getString("noteText");
                noteList.add(new Note(noteTitle, noteText, lastSaveDate));
            }
            noteAdapter.notifyDataSetChanged();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        noteToEdit = recyclerView.getChildLayoutPosition(view);
        Intent toEdit = new Intent(this, EditActivity.class);

        Note currNote = noteList.get(noteToEdit);
        toEdit.putExtra("TITLE", currNote.getNoteTitle());
        toEdit.putExtra("NOTETEXT", currNote.getNoteText());

        startActivityForResult(toEdit, 2);
        noteAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onLongClick(View view) {
        AlertDialog.Builder ADB = new AlertDialog.Builder(this);
        final int position = recyclerView.getChildLayoutPosition(view);
        ADB.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noteList.remove(position);

                if(noteList.size() > 0){
                    getSupportActionBar().setTitle(getString(R.string.app_name) + "(" + noteList.size() + ")");
                    noteAdapter.notifyDataSetChanged();
                }
                //Approving dialog results in note deleted from list.
                //If no notes.
                else{
                    getSupportActionBar().setTitle(getString(R.string.app_name));
                    noteAdapter.notifyDataSetChanged();
                }
            }
        });

        ADB.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cancelling dialog leaves the list unchanged
            }
        });

        TextView noteTitle = view.findViewById(R.id.noteTitle);
        ADB.setTitle("Delete '" + noteTitle.getText().toString() + "'?");
        AlertDialog AD = ADB.create();
        AD.show();
        noteAdapter.notifyDataSetChanged();
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            //After creating new Note
            case 1:
                if(resultCode==0){
                    noteList.add(0, new Note(data.getStringExtra("NEW-TITLE"), data.getStringExtra("NEW-NOTETEXT"), getPresentTime()));
                    getSupportActionBar().setTitle(getString(R.string.app_name) + "(" + noteList.size() + ")");
                    noteAdapter.notifyDataSetChanged();
                }
                break;

            //Existing Note Edited
            case 2:
                if(resultCode==0){
                    noteList.remove(noteToEdit);
                    noteList.add(0, new Note(data.getStringExtra("TITLE"), data.getStringExtra("NOTETEXT"), getPresentTime()));
                    getSupportActionBar().setTitle(getString(R.string.app_name) + "(" + noteList.size() + ")");
                    noteAdapter.notifyDataSetChanged();
                }

                //Note Wasn't Edited
                else if(resultCode==-1){
                    noteToEdit = -1;
                }
                break;
        }
    }
}