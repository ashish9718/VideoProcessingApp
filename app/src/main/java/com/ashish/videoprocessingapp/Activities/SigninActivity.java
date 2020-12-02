package com.ashish.videoprocessingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class SigninActivity extends AppCompatActivity {

    private String pattern="[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";

    private TextView dont_have_an_acc ;

    private EditText email,pass;

    private Button signinBtn;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        dont_have_an_acc=findViewById(R.id.sign_in_register_here);

        email=findViewById(R.id.sign_in_email);
        pass=findViewById(R.id.sign_in_password);

        signinBtn=findViewById(R.id.sign_in_btn);

        progressBar=findViewById(R.id.sign_in_progressBar);

        firebaseAuth=FirebaseAuth.getInstance();

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

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEmailandPass();
            }
        });

        dont_have_an_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SigninActivity.this, SignupActivity.class));
                finish();
            }
        });

    }

    private void checkInputs() {
        if(!TextUtils.isEmpty(email.getText())){
            if(!TextUtils.isEmpty(pass.getText()) && pass.length()>=6){
                signinBtn.setEnabled(true);
            }else{
                signinBtn.setEnabled(false);
            }
        }else{
            signinBtn.setEnabled(false);
        }
    }

    private void checkEmailandPass() {
        if(email.getText().toString().matches(pattern)){
            if(pass.getText().length()>=6){

                progressBar.setVisibility(View.VISIBLE);
                signinBtn.setEnabled(false);

                firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),pass.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    startActivity(new Intent(SigninActivity.this, MainActivity.class));
                                    finish();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    signinBtn.setEnabled(true);
                                    String error=task.getException().getMessage();
                                    Toast.makeText(SigninActivity.this, error,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }else {
                pass.setError("Password must be 6 characters !");
            }
        }else {
            email.setError("Invalid Email!");
        }
    }

}