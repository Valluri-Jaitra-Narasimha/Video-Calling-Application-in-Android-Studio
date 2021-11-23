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
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HistoryActivity extends AppCompatActivity {
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        auth=FirebaseAuth.getInstance();

        BottomNavigationView bottom = findViewById(R.id.bottomnavigation);

        bottom.setSelectedItemId(R.id.historybtn);

        bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.homebtn:
                        startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.historybtn:
                        if(!isConnected(HistoryActivity.this)){
                            showCustomDialog();
                        }else{
                            startActivity(new Intent(getApplicationContext(),HistoryActivity.class));
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

    private void showCustomDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(HistoryActivity.this);
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
                        startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isConnected(HistoryActivity historyActivity) {
        ConnectivityManager connectivityManager=(ConnectivityManager) historyActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn!=null && wifiConn.isConnected())|| (mobileConn!=null && mobileConn.isConnected())){
            return true;
        }else{
            return false;
        }

    }
}