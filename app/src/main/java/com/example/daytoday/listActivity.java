package com.example.daytoday;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daytoday.Model.Item;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import static java.lang.Boolean.TRUE;

public class listActivity extends AppCompatActivity {

    private FloatingActionButton fab_btn;
    private DatabaseReference mDatabase;
    //private FirebaseAuth mAuth;

    private FirebaseRecyclerOptions<Item> options;
    private FirebaseRecyclerAdapter<Item,MyViewHolder> adapter;

    private RecyclerView recyclerView;
    private TextView total;

    private String type;
    private float amount;
    private  String note;
    private  String postKey;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //mAuth = FirebaseAuth.getInstance();

        //FirebaseUser mUser = mAuth.getCurrentUser();
        //String uid = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference("Shopping List");
        mDatabase.keepSynced(true);

        total = findViewById(R.id.totAmount);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                float totAmount = 0;

                for(DataSnapshot snap:snapshot.getChildren()){

                    Item item = snap.getValue(Item.class);
                    totAmount += item.getAmount();
                }

                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                String fTotAmount = decimalFormat.format(totAmount);

                total.setText(String.valueOf(fTotAmount));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab_btn = findViewById(R.id.fab);

        recyclerView = findViewById(R.id.recyclerHome);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        fab_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                customDialog();
            }
        });

        options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(FirebaseDatabase.getInstance().getReference()
                        .child("Shopping List"),Item.class).build();

        adapter = new FirebaseRecyclerAdapter<Item, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Item model) {

                holder.setType(model.getType());
                holder.setDate(model.getDate());
                holder.setAmount(model.getAmount());
                holder.setNote(model.getNote());

                holder.myView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        postKey = getRef(position).getKey();
                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();

                        updateData();
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);

    }

    private void customDialog(){

        AlertDialog.Builder myDialog = new AlertDialog.Builder(listActivity.this);
        LayoutInflater inflater = LayoutInflater.from(listActivity.this);
        View myView = inflater.inflate(R.layout.input_data,null);

        final AlertDialog dialog = myDialog.create();

        dialog.setView(myView);

        final EditText type = myView.findViewById(R.id.edit_type);
        final EditText amount = myView.findViewById(R.id.edit_amount);
        final EditText note = myView.findViewById(R.id.edit_note);
        Button save = myView.findViewById(R.id.btnSave);

        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                int amint = Integer.parseInt(mAmount);

                if(TextUtils.isEmpty(mType)){
                    type.setError("Enter the type");
                    return;
                }

                if(TextUtils.isEmpty(mAmount)){
                    amount.setError("Enter amount");
                    return;
                }

                String id = mDatabase.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                Item item = new Item(mType,amint,mNote,date,id);

                mDatabase.child(id).setValue(item);

                Toast.makeText(listActivity.this,"Item Added",Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateData(){

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        AlertDialog.Builder myDialog = new AlertDialog.Builder(listActivity.this);

        LayoutInflater inflater = LayoutInflater.from(listActivity.this);

        View mView = inflater.inflate(R.layout.update_data,null);

        final AlertDialog dialog = myDialog.create();

        dialog.setView(mView);

        final EditText edtType = mView.findViewById(R.id.edit_type_upd);
        final EditText edtAmount = mView.findViewById(R.id.edit_amount_upd);
        final EditText edtNote = mView.findViewById(R.id.edit_note_upd);

        edtType.setText(type);
        edtType.setSelection(type.length());

        String tempAmount = decimalFormat.format(amount);
        edtAmount.setText(tempAmount);
        edtAmount.setSelection(tempAmount.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        Button btnUpdate = mView.findViewById(R.id.btnSaveUpd);
        Button btnDelete = mView.findViewById(R.id.btnDelete);

        btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                type = edtType.getText().toString().trim();
                String mAmount = edtAmount.getText().toString().trim();
                note = edtNote.getText().toString().trim();

                float floatAmount = Float.parseFloat(mAmount);

                String date = DateFormat.getDateInstance().format(new Date());

                Item item = new Item(type,floatAmount,note,date,postKey);

                mDatabase.child(postKey).setValue(item);

                Toast.makeText(listActivity.this,"Item Updated",Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            }
        });

        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(postKey).removeValue();

                Toast.makeText(listActivity.this,"Item Deleted",Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        dialog.show();;

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    public static class MyViewHolder extends  RecyclerView.ViewHolder{

        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }

        public void setType(String type){
            TextView mType = myView.findViewById(R.id.type);
            mType.setText(type);
        }

        public void setNote(String note){
            TextView mNote = myView.findViewById(R.id.note);
            mNote.setText(note);
        }

        public void setDate(String date){
            TextView mDate = myView.findViewById(R.id.date);
            mDate.setText(date);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public  void setAmount(float amount){

            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            String nAmount = decimalFormat.format(amount);

            TextView mAmount = myView.findViewById(R.id.amount);
            mAmount.setText(String.valueOf(nAmount));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}