package com.example.mecha.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mecha.Listeners.ConversaListener;
import com.example.mecha.Models.ChatMessage;
import com.example.mecha.Models.User;
import com.example.mecha.Utilidades.PreferenceManager;
import com.example.mecha.databinding.ItemContainerRecenteConversaBinding;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class MensagemRecenteAdapter extends RecyclerView.Adapter<MensagemRecenteAdapter.ConversaViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversaListener conversaListener;
    private PreferenceManager preferenceManager;

    public MensagemRecenteAdapter(List<ChatMessage> chatMessages, ConversaListener conversaListener, PreferenceManager preferenceManager) {
        this.chatMessages = chatMessages;
        this.conversaListener = conversaListener;
        this.preferenceManager = preferenceManager;
    }

    @NonNull
    @Override
    public ConversaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversaViewHolder(
                ItemContainerRecenteConversaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversaViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversaViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecenteConversaBinding binding;

        private DocumentReference documentReference;

        ConversaViewHolder(ItemContainerRecenteConversaBinding itemContainerRecenteConversaBinding) {
            super(itemContainerRecenteConversaBinding.getRoot());
            binding = itemContainerRecenteConversaBinding;
        }

        void setData(ChatMessage chatMessage) {
            //binding.imagemperfil.setImageBitmap(getConversaImagem(chatMessage.conversaImagem));
            binding.textName.setText(chatMessage.conversaNome);
            binding.textMensagemRecente.setText(chatMessage.mensagem);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversaID;
                user.name = chatMessage.conversaNome;
                //user.image = chatMessage.conversaImagem;
                conversaListener.onConversaClica(user);
            });

            //Parte que não está a dar. Tentar mais tarde
            /*binding.getRoot().setOnLongClickListener(v -> {
                FirebaseFirestore database = FirebaseFirestore.getInstance();

                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle("Eliminar");
                builder.setMessage("Tens a certeza que queres eliminar esta conversa? ");
                builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        database.collection(Constants.KEY_COLECAO_CONVERSAS).document(chatMessage.conversaID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(v.getContext(), "Conversa eliminada", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            });*/
        }
    }

    private Bitmap getConversaImagem(String encodedImagem) {
        byte[] bytes = Base64.decode(encodedImagem, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
