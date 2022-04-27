package com.gallery.art;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gallery.art.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


public class RegisterActivity extends AppCompatActivity {

    String selected, otp, sent;
    boolean b= false;
    EditText fullName,email,password,phone,countryCode;
    Button registerBtn,goToLogin;
    private RadioGroup radioGroup;
    boolean valid = true;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    PhoneAuthProvider.ForceResendingToken force;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        fullName = findViewById(R.id.registerName);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        phone = findViewById(R.id.registerPhone);
        countryCode= findViewById(R.id.country_code);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);
        radioGroup= findViewById(R.id.userGroup);
        radioGroup.clearCheck();
        progressDialog = new ProgressDialog(this);

        mCallbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(RegisterActivity.this,"otp sent", Toast.LENGTH_LONG).show();
                b = true;

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("OTP error", e.getLocalizedMessage());

            }
            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                sent = s;force=forceResendingToken;

            }
        };

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId)
                    {
                        RadioButton radioButton = (RadioButton)group.findViewById(checkedId);
                    }
                });
    }

    public void register(View view){
        Toast.makeText(RegisterActivity.this, "Clicked on login", Toast.LENGTH_SHORT).show();


        if (new CheckNetwork(RegisterActivity.this).isNetworkAvailable()) {


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("login");
            Log.i("TAG FB", databaseReference.getDatabase().toString());
            // databaseReference.child("login").addListenerForSingleValueEvent
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Timer(progressDialog, RegisterActivity.this).count();
            databaseReference.child("login").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.i("dataSNap#####111",dataSnapshot.getKey());
                    Log.i("dataSNap#####111",dataSnapshot.toString());
                    Log.i("dataSNap#####111", String.valueOf(dataSnapshot.getChildrenCount()));
                    Log.i("dataSNap#####111", String.valueOf(dataSnapshot.getChildren()));
                    if (dataSnapshot.child(phone.getText().toString()).exists()) {
                        Toast.makeText(RegisterActivity.this, "User already exists. Navigating to Login", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, SignInActivity.class));
                    } else {
                        String num = "+"+ countryCode.getText().toString()+ phone.getText().toString();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(num, 60, TimeUnit.SECONDS, RegisterActivity.this, mCallbacks);
                        LayoutInflater li = LayoutInflater.from(RegisterActivity.this);
                        final View promptsView = li.inflate(R.layout.otp, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                RegisterActivity.this);
                        alertDialogBuilder.setView(promptsView);
                        alertDialogBuilder.setMessage("OTP message");
                        final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);
                        userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                        progressDialog.dismiss();
                        alertDialogBuilder
                                .setCancelable(false)
                                .setNeutralButton("Resend OTP", (dialog, which) -> {
                                    PhoneAuthProvider.getInstance().verifyPhoneNumber(num, 60, TimeUnit.SECONDS, RegisterActivity.this, mCallbacks, force);
                                    register(view);
                                })
                                .setPositiveButton("OK",
                                        (dialog, id) -> {
                                            // get user input and set it to result
                                            if (!TextUtils.isEmpty(userInput.getText().toString())) {
                                                progressDialog.show();
                                                otp = userInput.getText().toString();
                                                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(sent, otp);
                                                mAuth.signInWithCredential(credential)
                                                        .addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                Log.i("success", "success");
                                                                int selectedId = radioGroup.getCheckedRadioButtonId();
                                                                if (selectedId == -1) {
                                                                    Toast.makeText(RegisterActivity.this, "No answer has been selected", Toast.LENGTH_SHORT).show();
                                                                }
                                                                RadioButton radioButton = (RadioButton) radioGroup.findViewById(selectedId);
                                                                User user= new User(email.getText().toString(),phone.getText().toString(),fullName.getText().toString(),radioButton.getText().toString(),password.getText().toString());
                                                                databaseReference.child(user.getPhone()).setValue(user);
                                                                progressDialog.dismiss();
                                                                preferences.setLoggedInUser(RegisterActivity.this, phone.getText().toString());
                                                                startActivity(new Intent(RegisterActivity.this, UserActivity.class));
                                                            } else {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(RegisterActivity.this, "OTP wrong", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });

                        //PhoneAuthProvider.verifyPhoneNumber(phone.getText().toString(),);
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });
        }else{
            Toast.makeText(RegisterActivity.this, "Check connection", Toast.LENGTH_LONG).show();
        }

    }


    public void goToLogin(View view) {
        startActivity(new Intent(this, SignInActivity.class));
    }
}