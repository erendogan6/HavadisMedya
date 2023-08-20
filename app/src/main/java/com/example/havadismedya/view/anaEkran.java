package com.example.havadismedya.view;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.havadismedya.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;
/*
Yapılabilecekler:
filtreleme sistemini getir.
 */
public class anaEkran extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;
    private AnimationDrawable animationDrawable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Objects.requireNonNull(getSupportActionBar()).hide();

        animationDrawable = (AnimationDrawable) binding.linear.getBackground();
        animationDrawable.setEnterFadeDuration(3000);
        animationDrawable.setExitFadeDuration(3000);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null){
            toastGoster("Hesaba Giriş Yapılıyor");
            Intent intent = new Intent(anaEkran.this,zamanTuneli.class);
            startActivity(intent);
            finish();
        }
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
    private void toastGoster(String mesaj){
        Toast.makeText(anaEkran.this,mesaj,Toast.LENGTH_SHORT).show();
    }
    public void sifirla(View view){
        String email = Objects.requireNonNull(binding.mailInput.getEditText()).toString();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Şifre sıfırlama e-postası başarıyla gönderildi
                        toastGoster("Şifre Sıfırlama Mail'i Gönderildi");
                    } else {
                        // Şifre sıfırlama e-postası gönderilirken bir hata oluştu
                        toastGoster(Objects.requireNonNull(task.getException()).toString());
                    }
                });
    }
    public void girisYap(View view){
        String email = Objects.requireNonNull(binding.mailInputt.getText()).toString();
        String password = Objects.requireNonNull(binding.passwordInputt.getText()).toString();
        if (email.isBlank()||password.isBlank()){
            toastGoster("E Posta veya Şifre Girmediniz");
        }
        else {
            mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(this, authResult -> {
                Intent intent = new Intent(anaEkran.this, zamanTuneli.class);
                startActivity(intent);
                finish();
            }).addOnFailureListener(this, e -> Toast.makeText(anaEkran.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show());
        }
    }

    public void kayitOl(View view){
        String email = Objects.requireNonNull(binding.mailInputt.getText()).toString();
        String password = Objects.requireNonNull(binding.passwordInputt.getText()).toString();
        if (email.isBlank()|| password.isBlank()){
            toastGoster("E-posta veya şifreyi boş girdiniz");
        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(this, authResult -> {
                        Intent intent = new Intent(anaEkran.this,zamanTuneli.class);
                        startActivity(intent);
                        finish();
                    }).addOnFailureListener(e -> Toast.makeText(anaEkran.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show());
        }
    }
}