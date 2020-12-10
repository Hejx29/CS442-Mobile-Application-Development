package com.example.multinotes;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multinotes.R;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteViewHolder>{
    private static final String TAG = "NoteAdapter";
    private List<Note> noteList;
    private MainActivity mainActivity;

    public NoteAdapter(List<Note> nl, MainActivity ma){
        this.noteList = nl;
        mainActivity = ma;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        Log.d(TAG, "onCreateViewHolder: ");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_list_row, parent, false);
        itemView.setOnClickListener((View.OnClickListener) mainActivity);
        itemView.setOnLongClickListener((View.OnLongClickListener) mainActivity);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position){
        Note N = noteList.get(position);
        holder.lastSaveDate.setText(N.getLastSaveDate());
        holder.noteTitle.setText(N.getNoteTitle());
        holder.trimText.setText(N.getTrimText());
    }

    @Override
    public int getItemCount(){
        return noteList.size();
    }
}
