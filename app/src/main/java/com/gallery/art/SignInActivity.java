package com.gallery.art;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.gallery.art.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {
    private EditText username, password;
    private Button login, singup;
    Switch active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        singup = findViewById(R.id.signup);
        active = findViewById(R.id.active);

        login.setOnClickListener(v -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Log.i("TAG FB",databaseReference.getDatabase().toString());
            databaseReference.child("login").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String input1 = username.getText().toString();
                    String input2 = password.getText().toString();
                    if (dataSnapshot.child(input1).exists()) {
                        if (dataSnapshot.child(input1).child("password").getValue(String.class).equals(input2)) {
                            if (active.isChecked()) {
                                if (dataSnapshot.child(input1).child("userType").getValue(String.class).equalsIgnoreCase("manager")) {
                                    preferences.setDataLogin(SignInActivity.this, true);
                                    preferences.setDataAs(SignInActivity.this, "manager");
                                    startActivity(new Intent(SignInActivity.this, UserActivity.class));
                                } else if (dataSnapshot.child(input1).child("userType").getValue(String.class).equalsIgnoreCase("user")){
                                    preferences.setDataLogin(SignInActivity.this, true);
                                    preferences.setDataAs(SignInActivity.this, "user");
                                    startActivity(new Intent(SignInActivity.this, UserActivity.class));
                                }else if (dataSnapshot.child(input1).child("userType").getValue(String.class).equalsIgnoreCase("artist")){
                                    preferences.setDataLogin(SignInActivity.this, true);
                                    preferences.setDataAs(SignInActivity.this, "artist");
                                    startActivity(new Intent(SignInActivity.this, UserActivity.class));
                                }
                            } else {
                                if (dataSnapshot.child(input1).child("userType").getValue(String.class).equalsIgnoreCase("manager")) {
                                    preferences.setDataLogin(SignInActivity.this, false);
                                    startActivity(new Intent(SignInActivity.this, UserActivity.class));
                                    preferences.setDataAs(SignInActivity.this, "manager");
                                } else if (dataSnapshot.child(input1).child("userType").getValue(String.class).equalsIgnoreCase("user")){
                                    preferences.setDataLogin(SignInActivity.this, true);
                                    preferences.setDataAs(SignInActivity.this, "user");
                                    startActivity(new Intent(SignInActivity.this, UserActivity.class));
                                }else if (dataSnapshot.child(input1).child("userType").getValue(String.class).equalsIgnoreCase("artist")){
                                    preferences.setDataLogin(SignInActivity.this, true);
                                    preferences.setDataAs(SignInActivity.this, "artist");
                                    startActivity(new Intent(SignInActivity.this, UserActivity.class));
                                }
                            }
                            preferences.setLoggedInUser(SignInActivity.this, input1);
                        } else {
                            Toast.makeText(SignInActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "No user found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        singup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nav();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (preferences.getDataLogin(this)) {
            if (preferences.getDataAs(this).equals("manager")) {
                startActivity(new Intent(this, UserActivity.class));
                finish();
            } else {
                navigateToUserActivity();
            }
        }
    }

    public void navigateToUserActivity() {
        startActivity(new Intent(this, UserActivity.class));
        finish();
    }

    public void navigateToHomeActivity() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    public void nav(){
        startActivity(new Intent(this, RegisterActivity.class));
    }
}