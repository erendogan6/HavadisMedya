package com.example.havadismedya.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.havadismedya.databinding.ActivityYuklemeEkraniBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class yuklemeEkrani extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> permissonLauncher;
    private ActivityYuklemeEkraniBinding binding;
    Uri imagedata;
    FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private AnimationDrawable animationDrawable;
    private StorageReference storageReference;
    private boolean islem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYuklemeEkraniBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Objects.requireNonNull(getSupportActionBar()).hide();

        animationDrawable = (AnimationDrawable) binding.upload.getBackground();
        animationDrawable.setEnterFadeDuration(3000);
        animationDrawable.setExitFadeDuration(3000);

        registerLauncher();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        islem=true;
    }
    public void onBackPressed(){
        intentToMain();
    }
    protected void onResume() {
        super.onResume();
        if (animationDrawable != null && !animationDrawable.isRunning()) {
            animationDrawable.start();
        }
    }
    protected void onPause() {
        super.onPause();
        if (animationDrawable != null && animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
    }
    public void yuklemeButonu(View view){
        if (islem) {
            islem = false;
            String aciklama = binding.aciklama.getText().toString();
            if (aciklama.isBlank()) {
                toastGoster("Açıklama Girmelisiniz");
                islem = true;
                return;
            }
            FirebaseUser kullanici = mAuth.getCurrentUser();
            if (kullanici != null) {
                HashMap<String, Object> paylasimVeri = new HashMap<>();
                paylasimVeri.put("email", kullanici.getEmail());
                paylasimVeri.put("aciklama", aciklama);
                paylasimVeri.put("tarih", FieldValue.serverTimestamp());
                paylasimVeri.put("begeniSayisi", 0);
                paylasimVeri.put("begenenler", Collections.singletonList("null"));
                paylasimVeri.put("paylasimTuru", "Metin");
                if (imagedata != null) {
                    String resimIsim = "resimler/" + UUID.randomUUID() + ".jpg";
                    paylasimVeri.put("paylasimTuru", "Gorsel");

                    storageReference.child(resimIsim).putFile(imagedata).addOnSuccessListener(taskSnapshot -> {
                        StorageReference yeniReferans = firebaseStorage.getReference(resimIsim);
                        yeniReferans.getDownloadUrl().addOnSuccessListener(uri -> {
                            paylasimVeri.put("url", uri.toString());

                            firebaseFirestore.collection("Paylasimm").add(paylasimVeri).addOnSuccessListener(documentReference ->
                                    intentToMain()).addOnFailureListener(e -> toastGoster(e.getLocalizedMessage()));

                        });
                    });
                }
                else {
                    firebaseFirestore.collection("Paylasimm").add(paylasimVeri).addOnSuccessListener(documentReference ->
                            intentToMain()).addOnFailureListener(e -> toastGoster(e.getLocalizedMessage()));
                }
            }
        }
    }
    private void intentToMain(){
        Intent intent = new Intent(yuklemeEkrani.this, zamanTuneli.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void toastGoster(String mesaj){
        Toast.makeText(yuklemeEkrani.this,mesaj,Toast.LENGTH_SHORT).show();
    }

    public void yukle(View view) {
        String izin;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            izin = "android.permission.READ_MEDIA_IMAGES";
        } else {
            izin = "android.permission.READ_EXTERNAL_STORAGE";
        }

        if (ContextCompat.checkSelfPermission(yuklemeEkrani.this, izin) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(yuklemeEkrani.this,izin)) {
                Snackbar.make(view,"Galeri İçin İzin Gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin Ver", v -> permissonLauncher.launch(izin)).show();
            } else {
                permissonLauncher.launch(izin);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }
    public void registerLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intentFromResult = result.getData();
                        if (intentFromResult != null) {
                            imagedata = intentFromResult.getData();
                            binding.imageView.setImageURI(imagedata);
                            }
                    }
                });
        permissonLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                    if(result) {
                        //permission granted
                        Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        activityResultLauncher.launch(intentToGallery);

                    } else {
                        //permission denied
                        toastGoster("İzin Gerekli");
                    }
                });
    }
}