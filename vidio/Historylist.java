package com.example.vidio;

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Historylist extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference database;
    MyAdapter myAdapter;
    ArrayList<Userhelp> list;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historylist);


        recyclerView = findViewById(R.id.historylist2);

        auth= FirebaseAuth.getInstance();
        FirebaseUser userr= auth.getCurrentUser();

        database = FirebaseDatabase.getInstance().getReference("Users").child(userr.getUid()).child("History");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        myAdapter = new MyAdapter(this,list);
        recyclerView.setAdapter(myAdapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");

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
                        if(!isConnected(Historylist.this)){
                            showCustomDialog();
                        }else{
                            startActivity(new Intent(getApplicationContext(),Historylist.class));
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

        if(!isConnected(Historylist.this)){
            showCustomDialog();
        }else{
            dialog.show();
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        list.clear();

                        for(DataSnapshot dss: snapshot.getChildren()){
                            String code=dss.getValue(String.class);
                            Userhelp user=new Userhelp();
                            user.setCode(code);

                            list.add(user);

                        }

                        Collections.reverse(list);

                    }
                    dialog.dismiss();
                    myAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void showCustomDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(Historylist.this);
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
                        startActivity(new Intent(getApplicationContext(), Historylist.class));
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isConnected(Historylist historylist) {
        ConnectivityManager connectivityManager=(ConnectivityManager) historylist.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn!=null && wifiConn.isConnected())|| (mobileConn!=null && mobileConn.isConnected())){
            return true;
        }else{
            return false;
        }

    }
}