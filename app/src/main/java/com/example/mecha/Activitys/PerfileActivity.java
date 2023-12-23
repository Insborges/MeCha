package com.example.mecha.Activitys;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mecha.R;
import com.example.mecha.Utilidades.Constants;
import com.example.mecha.Utilidades.PreferenceManager;
import com.example.mecha.databinding.ActivityPerfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfileActivity extends BaseActivity {

    private ActivityPerfileBinding binding;
    private PreferenceManager preferenceManager;
    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfileBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        binding.imagemTras.setOnClickListener(v -> onBackPressed());

        detalhesUtilizadores();
    }

    private void detalhesUtilizadores() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        binding.txtUsername.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.txtUsername.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Mudar o nome");
            View view = LayoutInflater.from(this).inflate(R.layout.username_dialog_layout, null, false);
            alert.setView(view);
            TextInputEditText edtUsername = view.findViewById(R.id.edtDialogUsername);
            alert.setPositiveButton("Alterar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).update(Constants.KEY_NAME, edtUsername.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(PerfileActivity.this, "Nome atualizado", Toast.LENGTH_SHORT).show();
                            binding.txtUsername.setText(edtUsername.getText().toString().trim());
                            preferenceManager.putString(Constants.KEY_NAME, edtUsername.getText().toString().trim());
                            documentReference.update(Constants.KEY_NAME, edtUsername.getText().toString().trim());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PerfileActivity.this, "O nome não foi atualizado", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog alertDialog = alert.create();
            alertDialog.show();

        });
        binding.textViewEmail.setText(user.getEmail());
        /*byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imagemperfil.setImageBitmap(bitmap);*/
    }


}
