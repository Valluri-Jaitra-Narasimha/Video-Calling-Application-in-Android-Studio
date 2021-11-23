package com.example.vidio;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class SheduleMeetingActivity extends AppCompatActivity {

    TextView title,code,emails,description,Start,End;
    Button schedule;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shedule_meeting);

        title=findViewById(R.id.sheduledname);
        code=findViewById(R.id.sheduleCode);
        emails=findViewById(R.id.addemails);
        description=findViewById(R.id.description);
        Start =findViewById(R.id.start);
        Start.setInputType(InputType.TYPE_NULL);
        End=findViewById(R.id.end);
        End.setInputType(InputType.TYPE_NULL);


        schedule=findViewById(R.id.schedulebtn);

        auth= FirebaseAuth.getInstance();

        FirebaseUser userr= auth.getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(userr.getUid());


        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimeDialogstart(Start);
            }
        });

        End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimeDialogend(End);
            }
        });


        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConnected(SheduleMeetingActivity.this)){
                    showCustomDialog();
                }else{
                    if (!title.getText().toString().isEmpty() && !code.getText().toString().isEmpty() && !description
                            .getText().toString().isEmpty() && !emails.getText().toString().isEmpty()) {

                        String temp1=Start.getText().toString();
                        String temp2=End.getText().toString();

                        String[] string1=temp1.replaceAll("-"," ").replaceAll(":"," ").split(" ");
                        int[] arr1=new int[string1.length];
                        for (int i=0;i<string1.length;i++){
                            arr1[i]=Integer.valueOf(string1[i]);
                        }

                        String[] string2=temp2.replaceAll("-"," ").replaceAll(":"," ").split(" ");
                        int[] arr2=new int[string2.length];
                        for (int i=0;i<string2.length;i++){
                            arr2[i]=Integer.valueOf(string2[i]);
                        }

                        Calendar beginTime = Calendar.getInstance();
                        beginTime.set(2021,arr1[1]-1 , arr1[2], arr1[3], arr1[4]);
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(2021, arr2[1]-1, arr2[2], arr2[3], arr2[4]);

                        boolean b = false;
                        try {
                            if(beginTime.before(endTime))
                            {
                                b = true;//If start date is before end date
                                accesscalendar(beginTime,endTime);
                            }
                            else if (beginTime.after(endTime))
                            {
                                b = false; //If start date is after the end date
                                End.setError("Please verify Start and End Time");

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }else{
                        Toast.makeText(SheduleMeetingActivity.this, "Please fill all the fields",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        BottomNavigationView bottom = findViewById(R.id.bottomnavigation);

        bottom.setSelectedItemId(R.id.scheduleBtn);

        bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.homebtn:
                        startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.historybtn:
                        if(!isConnected(SheduleMeetingActivity.this)){
                            showCustomDialog();
                        }else{
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild("History")){
                                        startActivity(new Intent(SheduleMeetingActivity.this,Historylist.class));

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

    private void showDateTimeDialogstart(TextView Start) {
        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendar.set(Calendar.MINUTE,minute);

                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yy-MM-dd HH:mm");

                        Start.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                };

                new TimePickerDialog(SheduleMeetingActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false).show();
            }
        };

        new DatePickerDialog(SheduleMeetingActivity.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void showDateTimeDialogend(TextView End) {
        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendar.set(Calendar.MINUTE,minute);

                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yy-MM-dd HH:mm");

                        End.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                };

                new TimePickerDialog(SheduleMeetingActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false).show();
            }
        };

        new DatePickerDialog(SheduleMeetingActivity.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void accesscalendar(Calendar beginTime, Calendar endTime){

        TimeZone timeZone=TimeZone.getDefault();

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.CALENDAR_ID,0)
                .putExtra(CalendarContract.Events.TITLE, title.getText().toString())
                .putExtra(CalendarContract.Events.DESCRIPTION, description.getText().toString()
                        +"\n\nPlease enter this code in the VID.IO app to join the meet :\t"+code.getText().toString())
                .putExtra(CalendarContract.Events.ALLOWED_REMINDERS, true)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID())
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY , false)
                .putExtra(Intent.EXTRA_EMAIL, emails.getText().toString());
        try{
            startActivity(intent);
        }catch (Exception e){
            Toast.makeText(SheduleMeetingActivity.this, "There is no app that support this action", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCustomDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(SheduleMeetingActivity.this);
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
                        startActivity(new Intent(getApplicationContext(),SheduleMeetingActivity.class));
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isConnected(SheduleMeetingActivity sheduleMeetingActivity) {
        ConnectivityManager connectivityManager=(ConnectivityManager) sheduleMeetingActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn!=null && wifiConn.isConnected())|| (mobileConn!=null && mobileConn.isConnected())){
            return true;
        }else{
            return false;
        }

    }


}