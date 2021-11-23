package com.example.vidio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {
    EditText emailBox, passwordBox;
    TextView forgotpass;
    Button loginBtn,signupBtn;

    FirebaseAuth auth;

    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        auth=FirebaseAuth.getInstance();

        emailBox=findViewById(R.id.emailBox);
        passwordBox=findViewById(R.id.passwordBox);

        forgotpass=findViewById(R.id.forgotpassword);

        loginBtn=findViewById(R.id.loginBtn);
        signupBtn=findViewById(R.id.backtologinBtn);

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });



//        direct login to dashboard
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isConnected(LoginActivity.this)){
                    showCustomDialog();
                }else{

                    String email, password;
                    email=emailBox.getText().toString();
                    password=passwordBox.getText().toString();

                    if(email.isEmpty() || password.isEmpty()){
                        Toast.makeText(LoginActivity.this,"Please Provide all Fields",Toast.LENGTH_SHORT).show();
                    }else{
                        dialog.show();
                        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                dialog.dismiss();
                                if(task.isSuccessful()){
                                    checkemailverified();
                                }
                                else{
                                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                }
            }
        });
    }

    private void showCustomDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
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

    private boolean isConnected(LoginActivity loginActivity) {
        ConnectivityManager connectivityManager=(ConnectivityManager) loginActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn!=null && wifiConn.isConnected())|| (mobileConn!=null && mobileConn.isConnected())){
            return true;
        }else{
            return false;
        }

    }

    private void checkemailverified() {
        DatabaseReference usersRef;
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid());
        FirebaseUser currentuser = auth.getCurrentUser();
        boolean result= currentuser.isEmailVerified();
        String password=passwordBox.getText().toString();
        String pass = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        if(result){
            finish();
            Toast.makeText(LoginActivity.this, "LogIn Succesful", Toast.LENGTH_SHORT).show();
            usersRef.child("status").setValue("True");
            usersRef.child("pass").setValue(pass);
            startActivity(new Intent(LoginActivity.this,DashboardActivity.class));
        }
        else{
            Toast.makeText(LoginActivity.this, "Please verify your Email / Register again with correct email", Toast.LENGTH_SHORT).show();
            usersRef.child("status").setValue("False");
            auth.signOut();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser userr= auth.getCurrentUser();
        if(userr!=null){
            startActivity(new Intent(LoginActivity.this,DashboardActivity.class));

            finish();
        }
    }
}
