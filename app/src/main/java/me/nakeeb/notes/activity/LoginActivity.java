package me.nakeeb.notes.activity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import me.nakeeb.notes.R;
import me.nakeeb.notes.helper.Utils;

public class LoginActivity extends AppCompatActivity {

    EditText emailET, passwordET;
    Button loginBtn;
    ProgressBar progressBar;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailET = findViewById(R.id.username);
        passwordET = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);
        progressBar = findViewById(R.id.loading);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                loginUser(emailET.getText().toString().trim(), passwordET.getText().toString().trim());
            }
        });

        emailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!Utils.isUserNameValid(s.toString().trim())) {
                    emailET.setError(getString(R.string.invalid_username));
                    loginBtn.setEnabled(false);
                }
                else if (Utils.isPasswordValid(passwordET.getText().toString().trim())) {
                    loginBtn.setEnabled(true);
                }
            }
        });

        passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!Utils.isPasswordValid(s.toString().trim())) {
                    passwordET.setError(getString(R.string.invalid_password));
                    loginBtn.setEnabled(false);
                }
                else if (Utils.isUserNameValid(emailET.getText().toString().trim())) {
                    loginBtn.setEnabled(true);
                }
            }
        });
    }

    private void openNotes() {
        startActivity(new Intent(this, NotesActivity.class));
        finish();
    }

    private void loginUser(final String email, final String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LoginRes", "signInWithEmail:success");

                            openNotes();
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginRes", "Login:failure", task.getException());
                            registerUser(email, password);
                        }
                    }
                });
    }

    private void registerUser(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("RegisterRes", "createUserWithEmail:success");
                            Toast.makeText(getBaseContext(), "Account is created successfully", Toast.LENGTH_SHORT).show();
                            openNotes();
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w("RegisterRes", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getBaseContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}