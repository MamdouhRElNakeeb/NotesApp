package me.nakeeb.notes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import me.nakeeb.notes.helper.NotesRVAdapter;
import me.nakeeb.notes.R;
import me.nakeeb.notes.helper.Utils;
import me.nakeeb.notes.model.Note;

public class NotesActivity extends AppCompatActivity {

    SwipeRefreshLayout refreshLayout;
    RecyclerView notesRV;
    ImageButton addBtn, logoutBtn;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser;

    NotesRVAdapter notesRVAdapter;
    ArrayList<Note> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else
            setUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserNotes();
    }

    private void setUI() {

        notesRV = findViewById(R.id.notesRV);
        refreshLayout = findViewById(R.id.swipeRL);
        addBtn = findViewById(R.id.addBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserNotes();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), AddNoteActivity.class));
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
                finish();
            }
        });


        // Set RecyclerView
        notesRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        notesRVAdapter = new NotesRVAdapter(this, notes);
        notesRV.setAdapter(notesRVAdapter);

        // Swipe listener
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                int position = viewHolder.getAdapterPosition();
                removeUserNote(position, notes.get(position));
                notes.remove(position);
                notesRVAdapter.notifyDataSetChanged();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(notesRV);


        getUserNotes();
    }

    private void getUserNotes() {

        if (!Utils.isNetworkConnected(this)) {
            Toast.makeText(this, "Internet unavailable!", Toast.LENGTH_SHORT).show();
            return;
        }

        refreshLayout.setRefreshing(true);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            notes = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("NotesResp", document.getId() + " => " + document.getData());
                                notes.add(new Note(document.getId(), document.getData().get("content").toString()));
                            }

                            notesRVAdapter.setNotes(notes);
                            notesRVAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);
                        }
                        else {
                            Log.w("NotesResp", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void removeUserNote(final int position, final Note note) {

        db.collection("users")
                .document(currentUser.getUid())
                .collection("notes")
                .document(note.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("NoteRemoveResp", "DocumentSnapshot successfully deleted!");
                        Toast.makeText(getBaseContext(), "Note is removed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("NoteRemoveResp", "Error deleting document", e);

                        notes.add(position, note);
                        Toast.makeText(getBaseContext(), "An error occurred, Try again later!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}