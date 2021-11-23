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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class DashboardActivity extends AppCompatActivity {
    EditText codeBox;
    Button joinBtn,shareBtn;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    List<String>history;
    List<String>dl;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        codeBox=findViewById(R.id.codeBox);

        joinBtn=findViewById(R.id.Joinbtn);
        shareBtn=findViewById(R.id.Sharebtn);

        auth=FirebaseAuth.getInstance();
        String st;
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");



        FirebaseUser userr= auth.getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(userr.getUid());

//        try{
//
//            st=getIntent().getExtras().getString("text");
//                        codeBox.setText(st);
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConnected(DashboardActivity.this)){
                    showCustomDialog();
                }else{
                    String text = codeBox.getText().toString();
                    start_meet(text);
                }
            }
            });


        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //GET Secret Code from codebox
                String text = codeBox.getText().toString();
                if (text.isEmpty()) {
                    codeBox.setError("Required");
                } else {
                    //sharing
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, "Please Enter this code to join into the Video meet:   " + text);
                    startActivity(Intent.createChooser(sharingIntent, "Share Secret Code Via.."));
                }
            }
        });

        BottomNavigationView bottom = findViewById(R.id.bottomnavigation);

        bottom.setSelectedItemId(R.id.homebtn);

        bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.homebtn:
                        startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.historybtn:
                        if(!isConnected(DashboardActivity.this)){
                            showCustomDialog();
                        }else{
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild("History")){
                                        startActivity(new Intent(DashboardActivity.this,Historylist.class));
                                    }
                                    else{
                                        startActivity(new Intent(getApplicationContext(),HistoryActivity.class));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.scheduleBtn:
                        startActivity(new Intent(getApplicationContext(),SheduleMeetingActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.logoutbtn:
                        auth.signOut();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                }
                return false;
            }
        });

    }
    public void addhistory(){

        history=new ArrayList<>();
        String text = codeBox.getText().toString();
        history.add(text);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("History")){
                    updatehistory(text);
                }
                else{
                    reference.child("History").setValue(history);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
    public void updatehistory(String text){
        dl=new ArrayList<>();
        reference.child("History").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    dl.clear();

                    for(DataSnapshot dss: snapshot.getChildren()){
                        String hstry=dss.getValue(String.class);
                        dl.add(hstry);
                    }
                    dl.add(text);

                    reference.child("History").setValue(dl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void start_meet(String text){
        if (text.isEmpty()) {
            codeBox.setError("Required");
        } else {
            dialog.show();
            addhistory();
            try {
                Toast.makeText(DashboardActivity.this, "Please Wait....", Toast.LENGTH_SHORT).show();
                JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                        .setServerURL(new URL("https://meet.jit.si"))
                        .setRoom(text)
                        .setAudioMuted(true)
                        .setVideoMuted(true)
                        .setWelcomePageEnabled(false)
                        .build();
                dialog.dismiss();
                JitsiMeetActivity.launch(DashboardActivity.this, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void showCustomDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(DashboardActivity.this);
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
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isConnected(DashboardActivity dashboardActivity) {
        ConnectivityManager connectivityManager=(ConnectivityManager) dashboardActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn!=null && wifiConn.isConnected())|| (mobileConn!=null && mobileConn.isConnected())){
            return true;
        }else{
            return false;
        }

    }
}