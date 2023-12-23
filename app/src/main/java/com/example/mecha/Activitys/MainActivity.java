package com.example.mecha.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mecha.Adapters.MensagemRecenteAdapter;
import com.example.mecha.Listeners.ConversaListener;
import com.example.mecha.Models.ChatMessage;
import com.example.mecha.Models.User;
import com.example.mecha.Utilidades.Constants;
import com.example.mecha.Utilidades.PreferenceManager;
import com.example.mecha.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversaListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversas;
    private MensagemRecenteAdapter mensagemRecenteAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        userDetalhes();
        getToken();
        setListeners();
        listenConversas();
    }

    private void init() {
        conversas = new ArrayList<>();
        mensagemRecenteAdapter = new MensagemRecenteAdapter(conversas, this, preferenceManager);
        binding.conversasRecycleView.setAdapter(mensagemRecenteAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.imageSair.setOnClickListener(v -> sair());
        binding.novaConversa.setOnClickListener(v -> {
            if (preferenceManager.getString(Constants.KEY_TIPO_ACESSO).equals("admin")){
                startActivity(new Intent(getApplicationContext(), AdminActivity.class));
            } else if (preferenceManager.getString(Constants.KEY_TIPO_ACESSO).equals("user")) {
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
    }

    private void userDetalhes() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversas() {
        database.collection(Constants.KEY_COLECAO_CONVERSAS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLECAO_CONVERSAS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String enviarID = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receberID = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.enviarId = enviarID;
                    chatMessage.receberId = receberID;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(enviarID)) {
                        chatMessage.conversaNome = documentChange.getDocument().getString(Constants.KEY_RECEBER_NOME);
                        chatMessage.conversaID = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversaNome = documentChange.getDocument().getString(Constants.KEY_ENVIAR_NOME);
                        chatMessage.conversaID = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.mensagem = documentChange.getDocument().getString(Constants.KEY_ULTIMA_MENSAGEM);
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversas.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversas.size(); i++) {
                        String enviarID = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receberID = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversas.get(i).enviarId.equals(enviarID) && conversas.get(i).receberId.equals(receberID)) {
                            conversas.get(i).mensagem = documentChange.getDocument().getString(Constants.KEY_ULTIMA_MENSAGEM);
                            conversas.get(i).dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversas, (obj1, obj2) -> obj2.dataObject.compareTo(obj1.dataObject));
            mensagemRecenteAdapter.notifyDataSetChanged();
            binding.conversasRecycleView.smoothScrollToPosition(0);
            binding.conversasRecycleView.setVisibility(View.VISIBLE);
            binding.processBar.setVisibility(View.GONE);
        }
    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::atualizarToken);
    }

    private void atualizarToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Não fêz a atualização"));
    }

    private void sair() {
        showToast("A desconectar...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> atualizacoes = new HashMap<>();
        atualizacoes.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(atualizacoes).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            FirebaseAuth.getInstance().signOut();
            finish();
        }).addOnFailureListener(e -> showToast("Não dá para sair "));

    }

    @Override
    public void onConversaClica(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

}