package com.example.mecha.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mecha.Utilidades.Constants;
import com.example.mecha.Utilidades.PreferenceManager;
import com.example.mecha.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;
    FirebaseAuth auth;
    private DocumentReference documentReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();
        setListeners();
    }

    private void verLogin() {
        String email = binding.inputEmail.getText().toString();
        String pass = binding.inputPassword.getText().toString();

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (!user.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "Email não verificado", Toast.LENGTH_SHORT).show();
                    } else {
                        login();
                        //Toast.makeText(LoginActivity.this, "Login feito", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Falha no login: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setListeners() {
        binding.textCriarNovaConta.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistarActivity.class)));
        binding.textEsqueceuPalavraPasse.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), EsquecerPassActivity.class)));
        binding.buttonLogin.setOnClickListener(v -> {
            if (loginDetalhes()) {
                verLogin();
            }
        });
    }

    private void login() {
        fazerLogin(false);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));

                String tipoAcesso = documentSnapshot.getString(Constants.KEY_TIPO_ACESSO);
                if(tipoAcesso != null){
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                    preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                    preferenceManager.putString(Constants.KEY_TIPO_ACESSO, documentSnapshot.getString(Constants.KEY_TIPO_ACESSO));

                    Class<?> redirectClass = tipoAcesso.equals("admin") ? AdminActivity.class : UsersActivity.class;
                    showToast("Bem-Vindo " + (tipoAcesso.equals("admin") ? "Administrador" : "Usuário") + "!" );
                    startActivity(new Intent(getApplicationContext(), redirectClass));
                }


            } else {
                fazerLogin(false);
                showToast("Não deu para fazer login");
            }
        });
    }


    private void fazerLogin(Boolean isLoading) {
        if (isLoading) {
            binding.buttonLogin.setVisibility(View.INVISIBLE);
            binding.processBar.setVisibility(View.VISIBLE);
        } else {
            binding.processBar.setVisibility(View.INVISIBLE);
            binding.buttonLogin.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String mensagem) {
        Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
    }

    private Boolean loginDetalhes() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Mete o email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Mete o email correto");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Mete a palavra-passe");
            return false;
        } else {
            return true;
        }
    }
}