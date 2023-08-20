package com.example.havadismedya.adaptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.havadismedya.R;
import com.example.havadismedya.databinding.RecyclerRowBinding;
import com.example.havadismedya.model.Paylasim;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class paylasimAdaptor extends RecyclerView.Adapter<paylasimAdaptor.PaylasimTutucu> {
    private final ArrayList<Paylasim> paylasimlar;
    private final FirebaseFirestore ff = FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String user = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
    private final Context context;
    private boolean tiklanabilirMi = false;
    private final int tiklamaGecikme = 1000; // Bekletilecek süre milisaniye cinsinden

    public paylasimAdaptor(ArrayList<Paylasim> paylasimlar, Context context) {
        this.paylasimlar=paylasimlar;
        this.context=context;
    }
    class PaylasimTutucu extends RecyclerView.ViewHolder {
        RecyclerRowBinding recyclerRowBinding;
        public PaylasimTutucu(@NonNull RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }
    @NonNull
    @Override
    public PaylasimTutucu onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PaylasimTutucu(recyclerRowBinding);
    }

    @Override
    public int getItemCount() {
        return paylasimlar.size();
    }

    public void toastGoster(String mesaj){
        Toast.makeText(context,mesaj,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBindViewHolder(@NonNull PaylasimTutucu holder, @SuppressLint("RecyclerView") int position) {

        Paylasim mevcutPaylasim = paylasimlar.get(position);

        holder.recyclerRowBinding.imageView.setVisibility(mevcutPaylasim.paylasimTuru.equals("Metin") ? View.GONE : View.VISIBLE);
        holder.recyclerRowBinding.email.setText(mevcutPaylasim.email);
        holder.recyclerRowBinding.aciklama.setText(mevcutPaylasim.aciklama);
        Picasso.get().load(mevcutPaylasim.url).into(holder.recyclerRowBinding.imageView);
        holder.recyclerRowBinding.begeniSayisii.setText(mevcutPaylasim.begeniSayisi);
        holder.recyclerRowBinding.tarih.setText(mevcutPaylasim.tarih);

        String documentID = mevcutPaylasim.id;
        DocumentReference ref = ff.collection("Paylasimm").document(documentID);

        AtomicBoolean begeniDurum = new AtomicBoolean(false);

        ref.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> begenenler = (List<String>) documentSnapshot.get("begenenler");
                assert begenenler != null;
                ImageView img = holder.recyclerRowBinding.imageView3;
                begeniDurum.set(begenenler.contains(user));
                img.setImageResource(begeniDurum.get() ? R.drawable.redheart : R.drawable.blackheart);
            }
        });


        holder.recyclerRowBinding.imageView3.setOnClickListener(view -> {
            holder.recyclerRowBinding.imageView3.setClickable(false);
            if (!tiklanabilirMi) {
                tiklanabilirMi = true;
                ref.update("begenenler", begeniDurum.get() ? FieldValue.arrayRemove(user) : FieldValue.arrayUnion(user))
                        .addOnSuccessListener(unused -> {
                            int begeniDegisim = begeniDurum.get() ? -1 : 1;
                            int yeniBegeniSayisi = Integer.parseInt(mevcutPaylasim.begeniSayisi) + begeniDegisim;
                            ref.update("begeniSayisi",FieldValue.increment(begeniDegisim));
                            holder.recyclerRowBinding.begeniSayisii.setText(String.valueOf(yeniBegeniSayisi));
                            toastGoster(begeniDurum.get()? "Beğeni Kaldırıldı" : "Beğenildi");
                            holder.recyclerRowBinding.imageView3.setImageResource(begeniDurum.get() ? R.drawable.blackheart : R.drawable.redheart);
                            begeniDurum.set(!begeniDurum.get());
                            mevcutPaylasim.begeniSayisi = String.valueOf(yeniBegeniSayisi);
                        })
                        .addOnFailureListener(e ->
                                toastGoster(e.getLocalizedMessage()));

                new Handler().postDelayed(() -> tiklanabilirMi = false, tiklamaGecikme);
            }
            holder.recyclerRowBinding.imageView3.setClickable(true);
        });
    }
}