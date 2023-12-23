package com.example.mecha.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.mecha.Utilidades.Constants;
import com.example.mecha.Utilidades.PreferenceManager;
import com.example.mecha.databinding.ActivityRegistarBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RegistarActivity extends AppCompatActivity {

    private ActivityRegistarBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        auth = FirebaseAuth.getInstance();
        setListeners();
    }

    private void criarConta() {
        String email = binding.inputEmail.getText().toString();
        String pass = binding.inputPassword.getText().toString();

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    registar();
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(RegistarActivity.this, "Email para veficar foi enviado", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegistarActivity.this, "Falhou o envio do email", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Intent intent = new Intent(RegistarActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    Toast.makeText(RegistarActivity.this, "Conta Criada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegistarActivity.this, "Já existe uma conta com esse email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void setListeners() {
        binding.textLogin.setOnClickListener(v -> onBackPressed());
        binding.buttonRegistar.setOnClickListener(v -> {
            if (registarDetalhes()) {
                criarConta();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void registar() {
        login(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_OPCOES_ESCOLHA, " ");
        user.put(Constants.KEY_TIPO_ACESSO, getTipoAcessoFromEmail(binding.inputEmail.getText().toString()));

        database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(documentReference -> {
            login(false);
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
            preferenceManager.putString(Constants.KEY_OPCOES_ESCOLHA, " ");
            preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
            preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());

        }).addOnFailureListener(exception -> {
            login(false);
            showToast(exception.getMessage());
        });
    }

    private Boolean registarDetalhes() {
        if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Mete um nome");
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Mete um email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Mete um email válido");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Mete uma palavra-passe");
            return false;
        } else if (binding.inputPassword.length() < 6) {
            binding.inputPassword.setError("Tem que ter pelo menos 6 caracteres");
            binding.inputPassword.requestFocus();
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirma a tua palavra-passe");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("A palavra-passe e confirmar a palavra-passe têm que ser os mesmos");
            return false;
        } else {
            return true;
        }
        return false;
    }

    private String getTipoAcessoFromEmail(String email){
        List<String> adminDomains = Arrays.asList("inesborges24680@gmail.com","stu.ipbeja.pt");

        for (String domain : adminDomains){
            if (email.endsWith(domain)){
                return "admin";
            }
        }

        return "user";
    }

    private void login(Boolean isloading) {
        if (isloading) {
            binding.buttonRegistar.setVisibility(View.INVISIBLE);
            binding.processBar.setVisibility(View.VISIBLE);
        } else {
            binding.processBar.setVisibility(View.INVISIBLE);
            binding.buttonRegistar.setVisibility(View.VISIBLE);
        }
    }
}