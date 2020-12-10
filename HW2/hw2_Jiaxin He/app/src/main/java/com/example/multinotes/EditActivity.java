package com.example.multinotes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {

    private EditText  noteTitle;
    private EditText editNoteText;
    private String titlevalue = "";
    private String notevalue = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        noteTitle = findViewById(R.id.NoteTitle);
        editNoteText = findViewById(R.id.editNoteText);
        editNoteText.setMovementMethod(new ScrollingMovementMethod());

        Intent i = getIntent();
        if(getIntent().getExtras() != null){
            titlevalue = i.getStringExtra("TITLE");
            noteTitle.setText(titlevalue);
            notevalue = i.getStringExtra("NOTETEXT");
            editNoteText.setText(notevalue);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.save){
            Intent toMain = new Intent();
            //Notes with no title are not saved (Toast message informs the user)
            String Untitl = noteTitle.getText().toString().trim();
            if(Untitl.isEmpty()){
                setResult(-1, toMain);
                finish();
                Toast.makeText(this, "Untitled Note wasn't saved.", Toast.LENGTH_LONG).show();
            }
            //Save Note

            else{
                String noteTitleValue = noteTitle.getText().toString();
                String editNoteTextValue = editNoteText.getText().toString();
                Intent i = getIntent();
                //Edit An Existing Old Note
                if(i.getExtras() != null){
                    String title = noteTitle.getText().toString().trim();
                    String noteText = editNoteText.getText().toString().trim();
                    //No Changes To Existing Old Note
                    if(noteText.equals(i.getStringExtra("NOTETEXT")) && title.equals(i.getStringExtra("TITLE"))){
                        setResult(-1, toMain);
                        finish();
                    }
                    //Changes To Existing Old Note
                    else{
                        toMain.putExtra("TITLE", noteTitleValue);
                        toMain.putExtra("NOTETEXT", editNoteTextValue);
                        setResult(0, toMain);
                        finish();
                    }
                }
                //Make New Note
                else{
                    toMain.putExtra("NEW-TITLE", noteTitleValue);
                    toMain.putExtra("NEW-NOTETEXT", editNoteTextValue);
                    setResult(0, toMain);
                    finish();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNode(){
        //Another alert dialog
        AlertDialog.Builder bulider = new AlertDialog.Builder(this);
        final String titlenote = noteTitle.getText().toString().trim();
        final String noteText = editNoteText.getText().toString().trim();
        //User wants to save the note
        bulider.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent newIntent = new Intent();
                newIntent.putExtra("NEW-TITLE", titlenote);
                newIntent.putExtra("NEW-NOTETEXT", noteText);
                setResult(0, newIntent);
                finish();
            }
        });

        bulider.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent newIntent = new Intent();
                setResult(-1, newIntent);
                finish();
            }
        });

        //Dialog Box
        bulider.setTitle("Your note is not saved!");
        bulider.setMessage("Save '" + titlenote + "' note?");
        AlertDialog AD = bulider.create();
        AD.show();
    }
    @Override
    public void onBackPressed(){
        //Notes with no title are not saved (Toast message informs the user)
        if(noteTitle.getText().toString().trim().isEmpty()){
            Intent toMain = new Intent();
            setResult(-1, toMain);
            Toast.makeText(this, "Untitled Note wasn't saved.", Toast.LENGTH_SHORT).show();
            finish();
        }
        //Note with title
        else{
            Intent intent1 = new Intent();
            Intent intent2 = getIntent();
            //User wants to edit note
            if(intent2.getExtras() != null) {
                String title = noteTitle.getText().toString().trim();
                String noteText = editNoteText.getText().toString().trim();
                //User made no edits
                if (noteText.equals(intent2.getStringExtra("NOTETEXT")) && title.equals(intent2.getStringExtra("TITLE"))) {
                    setResult(-1, intent1);
                    finish();
                }
                //Show dialog box if user did make edits
                else {
                    AlertDialog.Builder ADB = new AlertDialog.Builder(this);
                    //User wants to save note
                    ADB.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intdata = new Intent();
                            Intent i = getIntent();
                            //Editting An Old Note
                            String title = noteTitle.getText().toString().trim();
                            String noteText = editNoteText.getText().toString().trim();
                            if(i.getExtras() != null){

                                //No Changes To Old Note
                                if(noteText.equals(i.getStringExtra("NOTETEXT")) && title.equals(i.getStringExtra("TITLE"))){
                                    setResult(-1, intdata);
                                    finish();
                                }
                                //Changes To Old Note
                                else{
                                    intdata.putExtra("TITLE", title);
                                    intdata.putExtra("NOTETEXT", noteText);
                                    setResult(0, intdata);
                                    finish();
                                }
                            }
                            //New Note
                            else{
                                intdata.putExtra("NEW-TITLE", title);
                                intdata.putExtra("NEW-NOTETEXT", noteText);
                                setResult(0, intdata);
                                finish();
                            }
                        }
                    });

                    ADB.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent toMain = new Intent();
                            setResult(-1, toMain);
                            finish();
                        }
                    });

                    //Dialog Box
                    ADB.setTitle("Note is not saved!");
                    ADB.setMessage("Save '" + noteTitle.getText().toString().trim() + "' note?");
                    AlertDialog AD = ADB.create();
                    AD.show();
                }
            }
            //Save New Note
            else{
                saveNode();
            }
        }

    }

}


