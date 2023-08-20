package com.example.havadismedya.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.havadismedya.R;
import com.example.havadismedya.adaptor.paylasimAdaptor;
import com.example.havadismedya.databinding.ActivityZamanTuneliBinding;
import com.example.havadismedya.model.Paylasim;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class zamanTuneli extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private ArrayList<Paylasim> paylasimlar;
    private paylasimAdaptor PaylasimAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityZamanTuneliBinding binding = ActivityZamanTuneliBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        paylasimlar = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();
        getData();

        if (paylasimlar!=null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setReverseLayout(false);
            binding.recyclerView.setLayoutManager(layoutManager);
            PaylasimAdaptor = new paylasimAdaptor(paylasimlar, this);
            binding.recyclerView.setAdapter(PaylasimAdaptor);

        }
    }
    public void getData(){
        firebaseFirestore.collection("Paylasimm").orderBy("tarih",Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {
            if (error!=null){
                Toast.makeText(zamanTuneli.this, error.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
            if (value!=null){
                paylasimlar.clear();
                for (DocumentSnapshot snapshot: value.getDocuments()){
                    Map<String,Object> veri =  snapshot.getData();
                    assert veri != null;
                    String id = snapshot.getId();
                    String email = (String) veri.get("email");
                    String aciklama = (String) veri.get("aciklama");
                    String url = (String) veri.get("url");
                    String begeniSayisi = String.valueOf(veri.get("begeniSayisi"));
                    String paylasimTuru = (String) veri.get("paylasimTuru");
                    String formattedDate;
                    if (veri.get("tarih")!=null) {
                        Timestamp tarih = (Timestamp) veri.get("tarih");
                        assert tarih != null;
                        Date date = tarih.toDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        formattedDate = sdf.format(date);
                    }
                    else{
                        formattedDate = "";
                    }
                    paylasimlar.add(new Paylasim(id,email,aciklama,url,begeniSayisi,formattedDate,paylasimTuru));
                }
                PaylasimAdaptor.notifyDataSetChanged();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.paylasim){
            Intent intent = new Intent(zamanTuneli.this, yuklemeEkrani.class);
            startActivity(intent);
            finish();
        }
        else if (item.getItemId()==R.id.cikis){
            auth.signOut();
            Intent anamenu = new Intent(zamanTuneli.this, anaEkran.class);
            anamenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(anamenu);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}