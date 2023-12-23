package com.example.mecha.Adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mecha.Models.ChatMessage;
import com.example.mecha.databinding.ItemContainerReceivedMessageBinding;
import com.example.mecha.databinding.ItemContainerSentMessageBinding;

import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    //private  Bitmap receberFotoPerfil;
    private final String enviarId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    /*public void setReceberFotoPerfil(Bitmap bitmap){
        receberFotoPerfil = bitmap;
    }*/


    public ChatAdapter(List<ChatMessage> chatMessages/*, Bitmap receberFotoPerfil*/, String enviarId) {
        this.chatMessages = chatMessages;
        //this.receberFotoPerfil = receberFotoPerfil;
        this.enviarId = enviarId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==VIEW_TYPE_SENT){
            return new SendMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),parent,false
                    )
            );
        }else{
            return new ReceiveMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),parent,false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position)==VIEW_TYPE_SENT){
            ((SendMessageViewHolder) holder).setData(chatMessages.get(position));
        }else{
            ((ReceiveMessageViewHolder) holder).setData(chatMessages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).enviarId.equals(enviarId)){
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SendMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessagem.setText(chatMessage.mensagem);
            binding.textDataTime.setText(chatMessage.dataTime);
        }
    }

    static class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceiveMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage/*, Bitmap receberFotoPerfil*/) {
            binding.textMessagem.setText(chatMessage.mensagem);
            binding.textDataTime.setText(chatMessage.dataTime);
            /*if(receberFotoPerfil != null){
                binding.imagemperfil.setImageBitmap(receberFotoPerfil);
            }*/
        }
    }
}
