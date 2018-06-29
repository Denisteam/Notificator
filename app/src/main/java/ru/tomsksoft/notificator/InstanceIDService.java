package ru.tomsksoft.notificator;

import android.os.Build;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ru.tomsksoft.notificator.exceptions.IncorrectDataException;
import ru.tomsksoft.notificator.message.Message;
import ru.tomsksoft.notificator.message.MessageSender;
import ru.tomsksoft.notificator.message.RPCMethod;

public class InstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        UserDataStorage dataStorage = new UserDataStorage(this);
        dataStorage.refreshToken(refreshedToken);
        Message message = new Message(this, RPCMethod.TOKEN_ADD);
        message.addParam("token", dataStorage.getToken());
        message.addParam("model", Build.MANUFACTURER + " " + Build.MODEL);
        message.addParam("os", Build.VERSION.RELEASE);
        try {
            MessageSender.send(this, message);
        } catch (IncorrectDataException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
