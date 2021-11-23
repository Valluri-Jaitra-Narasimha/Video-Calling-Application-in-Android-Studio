package com.example.vidio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class SignupActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore database;
    FirebaseUser firebaseUser;
    DatabaseReference usersRef;
    EditText emailBox, passwordBox,nameBox;
    Button loginBtn,signupBtn;
    TextView Knowmore;
    CheckBox termsandcond;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth=FirebaseAuth.getInstance();
        database=FirebaseFirestore.getInstance();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        emailBox=findViewById(R.id.emailBox);
        nameBox =findViewById(R.id.namebox);
        passwordBox=findViewById(R.id.passwordBox);

        signupBtn=findViewById(R.id.backtologinBtn);
        loginBtn=findViewById(R.id.resetbtn);

        Knowmore=findViewById(R.id.knowmore);
        termsandcond=findViewById(R.id.checkBox);

        termsandcond.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    signupBtn.setEnabled(true);
                    signupBtn.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(SignupActivity.this,"Please Accept Terms and Conditions",Toast.LENGTH_SHORT).show();
                    signupBtn.setEnabled(false);
                    signupBtn.setVisibility(View.INVISIBLE);
                }
            }
        });

        Knowmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConnected(SignupActivity.this)) {
                    showCustomDialog();
                }else{
                    startActivity(new Intent(SignupActivity.this,TermsandConditions.class));
                }
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this,LoginActivity.class));
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isConnected(SignupActivity.this)) {
                    showCustomDialog();
                }else{
                    String email, pass, name;
                    email = emailBox.getText().toString();
                    pass = passwordBox.getText().toString();
                    name = nameBox.getText().toString();

                    if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
                        Toast.makeText(SignupActivity.this, "Please Provide all Fields", Toast.LENGTH_SHORT).show();
                    }
                    else {

                        Pattern p=Pattern.compile("((?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@%#$]).{6,15})");
                        Matcher m = p.matcher(pass);

                        if(m.matches()==true){

                            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
//                                    database.collection("Users")
//                                            .document().set(user);
                                        createDatabaseValues();
//                                    FirebaseUser userr= auth.getCurrentUser();
                                        Toast.makeText(SignupActivity.this, "Account is created Successfully", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(SignupActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else{
                            passwordBox.setError("Should be of length 6-15 and must have atleast one lowercase,uppercase,number and special characters @%#$");
                        }
                    }
                }

            }
            });
    }

    public void createDatabaseValues(){
        firebaseUser = auth.getCurrentUser();
        String email, password, name;
        email = emailBox.getText().toString();
        password = passwordBox.getText().toString();
        name = nameBox.getText().toString();

        String pass = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        User user = new User(email,pass,name);

        usersRef.child(firebaseUser.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                sendverificationemail();
//                Toast.makeText(SignupActivity.this, "welcome "+name, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendverificationemail() {
        FirebaseUser currentuser = auth.getCurrentUser();

        if(currentuser!=null){

            currentuser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SignupActivity.this, "verification Email sent", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        finish();
                    }
                    else{
                        Toast.makeText(SignupActivity.this, "Can't send email verify the mail provided", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }


    }

    private void showCustomDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(SignupActivity.this);
        builder.setMessage("Please connect to the internet to proceed further")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isConnected(SignupActivity signupActivity) {
        ConnectivityManager connectivityManager=(ConnectivityManager) signupActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn!=null && wifiConn.isConnected())|| (mobileConn!=null && mobileConn.isConnected())){
            return true;
        }else{
            return false;
        }

    }


}