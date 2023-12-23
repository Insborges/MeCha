package com.example.mecha.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mecha.R;
import com.example.mecha.Utilidades.PreferenceManager;
import com.example.mecha.databinding.ActivityChatBinding;
import com.example.mecha.databinding.ActivityEsquecerPassBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class EsquecerPassActivity extends AppCompatActivity {

    private ActivityEsquecerPassBinding binding;
    private PreferenceManager preferenceManager;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEsquecerPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.imagemTras.setOnClickListener(v -> onBackPressed());

        binding.buttonMudar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mandarEmail();
            }
        });
    }

    public void mandarEmail() {
        String resetPassword = binding.inputEmail.getText().toString();


        auth.sendPasswordResetEmail(resetPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(EsquecerPassActivity.this, "Vai ao email para mudar a passe", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EsquecerPassActivity.this, LoginActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EsquecerPassActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}