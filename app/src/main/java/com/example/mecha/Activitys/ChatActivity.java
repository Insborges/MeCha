package com.example.mecha.Activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mecha.Adapters.ChatAdapter;
import com.example.mecha.Models.ChatMessage;
import com.example.mecha.Models.User;
import com.example.mecha.Network.ApiCliente;
import com.example.mecha.Network.ApiServicos;
import com.example.mecha.Utilidades.Constants;
import com.example.mecha.Utilidades.PreferenceManager;
import com.example.mecha.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receberUtilizadores;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversaId = null;
    private Boolean estaDisponivelRecetor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        receberDetalhes();
        setListeners();
        init();
        ouvirMensagens();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                //getBitmapFromEncodeString(receberUtilizadores.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void enviarMensagem() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receberUtilizadores.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMensagem.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversaId != null) {
            atualizarConversa(binding.inputMensagem.getText().toString());
        } else {
            HashMap<String, Object> conversa = new HashMap<>();
            conversa.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversa.put(Constants.KEY_ENVIAR_NOME, preferenceManager.getString(Constants.KEY_NAME));
            conversa.put(Constants.KEY_ENVIAR_IMAGEM, preferenceManager.getString(Constants.KEY_IMAGE));
            conversa.put(Constants.KEY_RECEIVER_ID, receberUtilizadores.id);
            conversa.put(Constants.KEY_RECEBER_NOME, receberUtilizadores.name);
            //conversa.put(Constants.KEY_RECEBER_IMAGEM, receberUtilizadores.image);
            conversa.put(Constants.KEY_ULTIMA_MENSAGEM, binding.inputMensagem.getText().toString());
            conversa.put(Constants.KEY_TIMESTAMP, new Date());
            adicionarConversa(conversa);
        }
        if(!estaDisponivelRecetor){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receberUtilizadores.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMensagem.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());
            }catch(Exception exception){
                mostrarToast(exception.getMessage());
            }
        }
        binding.inputMensagem.setText(null);
    }


    private void mostrarToast(String mensagem) {
        Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification (String messageBody){
        ApiCliente.getClient().create(ApiServicos.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    try {
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1 ){
                                JSONObject error = (JSONObject) results.get(0);
                                Toast.makeText(ChatActivity.this, error.getString("error"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    //Toast.makeText(ChatActivity.this, "Notificação enviada com sucesso", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ChatActivity.this, "Error: "+ response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ouvirDisponabilidadeRecetor() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(receberUtilizadores.id)
                .addSnapshotListener(ChatActivity.this, ((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        if (value.getLong(Constants.KEY_ONLINE) != null) {
                            int disponabilidade = Objects.requireNonNull(value.getLong(Constants.KEY_ONLINE)).intValue();
                            estaDisponivelRecetor = disponabilidade == 1;
                        }
                        receberUtilizadores.token = value.getString(Constants.KEY_FCM_TOKEN);
                        /*if(receberUtilizadores.image == null){
                            receberUtilizadores.image = value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceberFotoPerfil(getBitmapFromEncodeString(receberUtilizadores.image));
                            chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                        }*/
                    }
                    if (estaDisponivelRecetor) {
                        binding.textDisponibilidade.setVisibility(View.VISIBLE);
                    } else {
                        binding.textDisponibilidade.setVisibility(View.GONE);
                    }
                }));
    }

    private void ouvirMensagens() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receberUtilizadores.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receberUtilizadores.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.enviarId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receberId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.mensagem = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dataTime = getDataTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dataObject.compareTo(obj2.dataObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.processBar.setVisibility(View.GONE);
        if (conversaId == null) {
            verificarConversa();
        }
    };

    private Bitmap getBitmapFromEncodeString(String encodeImage) {
        if(encodeImage != null) {
            byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else{
            return null;
        }
    }

    private void receberDetalhes() {
        receberUtilizadores = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receberUtilizadores.name);
    }

    private void setListeners() {
        binding.imagemTras.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
        binding.layoutSend.setOnClickListener(v -> enviarMensagem());
    }

    private String getDataTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void adicionarConversa(HashMap<String, Object> conversas) {
        database.collection(Constants.KEY_COLECAO_CONVERSAS)
                .add(conversas)
                .addOnSuccessListener(documentReference -> conversaId = documentReference.getId());
    }

    private void atualizarConversa(String mensagem) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLECAO_CONVERSAS).document(conversaId);
        documentReference.update(Constants.KEY_ULTIMA_MENSAGEM, mensagem, Constants.KEY_TIMESTAMP, new Date());
    }


    private void verificarConversa() {
        if (chatMessages.size() != 0) {
            verificarConversaRemotamente(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receberUtilizadores.id
            );
            verificarConversaRemotamente(
                    receberUtilizadores.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void verificarConversaRemotamente(String enviarID, String receberID) {
        database.collection(Constants.KEY_COLECAO_CONVERSAS)
                .whereEqualTo(Constants.KEY_SENDER_ID, enviarID)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receberID)
                .get()
                .addOnCompleteListener(conversaOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversaOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversaId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ouvirDisponabilidadeRecetor();
    }
}