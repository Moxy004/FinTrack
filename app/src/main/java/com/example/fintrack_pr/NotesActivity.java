package com.example.fintrack_pr;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack_pr.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class NotesActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private NoteAdapter adapter;
    private List<Note> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        dbHelper = new DatabaseHelper(this);
        noteList = dbHelper.getAllNotes();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(noteList, this::showEditDeleteDialog);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddNote = findViewById(R.id.fab_add_note);
        fabAddNote.setOnClickListener(v -> showAddNoteDialog());
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Note");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_edit_note, null);
        builder.setView(customLayout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            EditText etTitle = customLayout.findViewById(R.id.etTitle);
            EditText etContent = customLayout.findViewById(R.id.etContent);
            Note note = new Note(0, etTitle.getText().toString(), etContent.getText().toString());
            dbHelper.addNote(note);
            refreshNotes();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditDeleteDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit/Delete Note");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_edit_note, null);
        EditText etTitle = customLayout.findViewById(R.id.etTitle);
        EditText etContent = customLayout.findViewById(R.id.etContent);
        etTitle.setText(note.title);
        etContent.setText(note.content);

        builder.setView(customLayout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            note.title = etTitle.getText().toString();
            note.content = etContent.getText().toString();
            dbHelper.updateNote(note);
            refreshNotes();
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            dbHelper.deleteNoteById(note.id);
            refreshNotes();
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void refreshNotes() {
        noteList.clear();
        noteList.addAll(dbHelper.getAllNotes());
        adapter.notifyDataSetChanged();
    }
}
