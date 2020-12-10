package com.example.multinotes;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.multinotes.R;

public class NoteViewHolder extends RecyclerView.ViewHolder {
    public TextView lastSaveDate;
    public TextView noteTitle;
    public TextView trimText;
    public NoteViewHolder(View v) {
        super(v);
        lastSaveDate = v.findViewById(R.id.lastSaveDate);
        noteTitle = v.findViewById(R.id.noteTitle);
        trimText = v.findViewById(R.id.textContents);
    }
}
