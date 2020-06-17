package me.nakeeb.notes.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import me.nakeeb.notes.R;
import me.nakeeb.notes.helper.Utils;

public class AddNoteActivity extends AppCompatActivity {

    // UI
    ImageButton addBtn;
    EditText noteET;
    ProgressBar progressBar;

    // Firebase
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        noteET = findViewById(R.id.noteET);
        addBtn = findViewById(R.id.addBtn);
        progressBar = findViewById(R.id.progressBar);

        noteET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    addBtn.setEnabled(false);
                }
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNote(noteET.getText().toString().trim());
            }
        });
    }

    private void addNote(String note) {

        if (!Utils.isNetworkConnected(this)) {
            Toast.makeText(this, "Internet unavailable!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.GONE);

        Map<String, Object> noteObj = new HashMap<>();
        noteObj.put("content", note);

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("notes")
                .document()
                .set(noteObj)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("AddResp", "DocumentSnapshot successfully written!");

                        Toast.makeText(getBaseContext(), "Note is added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("AddResp", "Error writing document", e);

                        Toast.makeText(getBaseContext(), "An error occurred, Try again later!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        addBtn.setVisibility(View.VISIBLE);
                    }
                });
    }

}
