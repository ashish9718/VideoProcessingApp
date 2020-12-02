package com.ashish.videoprocessingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish.videoprocessingapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private String pattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";

    private TextView alreadyHaveAcc;
    private EditText email, name, pass;
    private Button signupBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);



        email = findViewById(R.id.sign_up_email);
        name = findViewById(R.id.sign_up_name);
        pass = findViewById(R.id.sign_up_password);
        alreadyHaveAcc = findViewById(R.id.sign_in_page);
        signupBtn = findViewById(R.id.sign_up_btn);
        progressBar = findViewById(R.id.sign_up_progressBar);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        alreadyHaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, SigninActivity.class));
                finish();
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEmailandPass();
            }
        });

    }

    private void checkEmailandPass() {
        if (email.getText().toString().matches(pattern)) {

            progressBar.setVisibility(View.VISIBLE);
            signupBtn.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                final Map<String, Object> userdata = new HashMap<>();
                                userdata.put("name", name.getText().toString());
                                userdata.put("email", email.getText().toString());

                                firebaseFirestore.collection("USERS").document(mAuth.getUid())
                                        .set(userdata)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                                    finish();

                                                } else {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // If sign in fails, display a message to the user.
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                signupBtn.setEnabled(true);
                                signupBtn.setTextColor(Color.rgb(255, 255, 255));
                            }
                        }
                    });

        } else {
            email.setError("Invalid Email!");
        }
    }


    private void checkInputs() {
        if (!TextUtils.isEmpty(email.getText())) {
            if (!TextUtils.isEmpty(name.getText())) {
                if (!TextUtils.isEmpty(pass.getText()) && pass.length() >= 6) {
                    signupBtn.setEnabled(true);
                    signupBtn.setTextColor(Color.rgb(255, 255, 255));

                } else {
                    signupBtn.setEnabled(false);
                    signupBtn.setTextColor(Color.argb(50, 255, 255, 255));
                }
            } else {
                signupBtn.setEnabled(false);
                signupBtn.setTextColor(Color.argb(50, 255, 255, 255));
            }
        } else {
            signupBtn.setEnabled(false);
            signupBtn.setTextColor(Color.argb(50, 255, 255, 255));
        }
    }

}