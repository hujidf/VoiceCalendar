package com.example.calendar;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;


import android.content.Intent;


import android.speech.RecognizerIntent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import android.widget.ImageButton;

public class NewActivity extends AppCompatActivity {

    private EditText noteEditText;
    private Button saveButton;
    private NoteAdapter noteAdapter;
    private ImageButton buttonMicrophone;
    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private String selectedDate;
    private static final int RECOGNIZER_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        noteEditText = findViewById(R.id.noteEditText);
        saveButton = findViewById(R.id.saveButton);
        buttonMicrophone = findViewById(R.id.buttonMicrophone);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        databaseHelper = new DatabaseHelper(this);

        saveButton.setOnClickListener(v -> showTimePickerDialog());



        selectedDate = getIntent().getStringExtra("selectedDate");

        setTitle("Дела на " + selectedDate);

        List<Note> noteList = databaseHelper.getAllNotesForDate(selectedDate); // Используйте переданную дату
        noteAdapter = new NoteAdapter(this, noteList);
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        buttonMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Назовите событие...");
                startActivityForResult(intent, RECOGNIZER_RESULT);

            }
        });


        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                removeNote(position);
            }
        });


    }


    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String selectedTime = hourOfDay + ":" + minute;
                        saveNoteToDatabase(selectedTime);
                    }
                }, hour, minute, true);


        timePickerDialog.show();
    }




    private void saveNoteToDatabase(String selectedTime) {
        String note = noteEditText.getText().toString().trim();
        String title = "Title";
        String selectedDate = getIntent().getStringExtra("selectedDate");

        long result = databaseHelper.addNoteWithTime(note, title, selectedTime, selectedDate);

        if (result != -1) {
            Toast.makeText(NewActivity.this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
            List<Note> updatedNoteList = databaseHelper.getAllNotesForDate(selectedDate);
            noteAdapter.setNoteList(updatedNoteList);
            finish();
        } else {
            Toast.makeText(NewActivity.this, "Ошибка при сохранении заметки", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String text = matches.get(0).toString();
        noteEditText.setText(text);

        super.onActivityResult(requestCode, resultCode, data);
    }



    private void removeNote(int position) {
        // Получите ID записи по позиции
        int noteId = noteAdapter.getNoteIdAtPosition(position);

        // Удалите запись из базы данных
        databaseHelper.deleteNoteById(noteId);

        // Удалите запись из списка адаптера
        noteAdapter.removeNoteAtPosition(position);
    }

}



