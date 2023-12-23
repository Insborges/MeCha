package com.example.mecha.Utilidades;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "utilizadores";

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_REFERENCE_NAME = "MeChaPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_TIPO_ACESSO = "acesso";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "sendId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLECAO_CONVERSAS = "conversas";
    public static final String KEY_ENVIAR_NOME = "enviarNome";
    public static final String KEY_RECEBER_NOME = "receberNome";
    public static final String KEY_ENVIAR_IMAGEM = "enviarImagem";
    public static final String KEY_RECEBER_IMAGEM = "receberImagem";
    public static final String KEY_ULTIMA_MENSAGEM = "ultimaMensagem";
    public static final String KEY_OPCOES_ESCOLHA = "escolha";
    public static final String KEY_ONLINE = "online";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAA4v7yS_E:APA91bFuhMpqadbf3ICB5hMV4ZPHNFIYMD0FI7kXV7YMKwYRDUwKuWPbOUubRi2zg95eD4LgURbe2gEZGkqUHkMaZetzqlF6twf4RZ5Oi5s2M46xVPNEBnQLKheKVzLiuAuukEEVeBO0"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
