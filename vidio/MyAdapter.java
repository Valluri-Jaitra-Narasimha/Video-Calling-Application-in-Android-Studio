package com.example.vidio;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    List<String> dl;
    DatabaseReference reference;



    public MyAdapter(Context context) {
        this.context = context;
    }

    Context context;

    ArrayList<Userhelp> list;



    public MyAdapter(Context context, ArrayList<Userhelp> list) {
        this.context = context;
        this.list = list;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item,parent,false);
        return  new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Userhelp user = list.get(position);

        holder.code.setText(user.getCode());


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView code;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            code = itemView.findViewById(R.id.code);

            itemView.findViewById(R.id.rejoinBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        updatehistoryonrejoin(code.getText().toString());
                        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                                .setServerURL(new URL("https://meet.jit.si"))
                                .setRoom(code.getText().toString())
                                .setAudioMuted(true)
                                .setVideoMuted(true)
                                .setWelcomePageEnabled(false)
                                .build();

                        JitsiMeetActivity.launch(context, options);


                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

        }
    }

    private void updatehistoryonrejoin(String text) {

        FirebaseUser userr=FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(userr.getUid());
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

                    reference.child("History").setValue(dl).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "history is created Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}