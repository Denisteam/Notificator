package ru.tomsksoft.notificator.message;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.Map;


import ru.tomsksoft.notificator.UserDataStorage;

public class Message {
    @SerializedName("jsonrpc")
    private String jsonRPC = "2.0";
    private String method;
    private Map<String, String> params = new LinkedHashMap<>();
    private int id;

    public Message(Context context, RPCMethod method) {
        this.method = method.name().toLowerCase();
        id = UserDataStorage.getCurrentMessageId(context);
    }

    public void addParam(String k, String v) {
        params.put(k, v);
    }
}



//{"jsonrpc": "2.0", "method": "token_delete", "params": {"token": "XXXXX"}, "id": 2}