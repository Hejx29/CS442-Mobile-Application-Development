package com.example.multinotes;


//A separate Note class used to represent note data
public class Note {

    private String SaveDate;
    private String noteTitle;
    private String noteText;
    private String trimText;

    public Note(String t, String n, String l){
        noteTitle = t;
        noteText = n;
        SaveDate = l;

        if(n.length() > 80){
            trimText = noteText.substring(0, 79) + "...";
        }
        else{
            trimText = noteText;
        }
    }


    public String getLastSaveDate() {
        return SaveDate;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public String getNoteText() {
        return noteText;
    }

    public String getTrimText() {
        return this.trimText; }
}
