package ru.tomsksoft.notificator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



    }

    public void onClickLogIn(View view)
    {
        final String login = ((EditText)findViewById(R.id.login)).getText().toString();
        final String password = ((EditText)findViewById(R.id.password)).getText().toString();

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> result = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return checkLogIn(login, password);
             //   return false;
            }
        });
        executor.shutdown();




        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            if (result.isDone()) {
                boolean res = result.get();
                Log.d(TAG, "connecting result: " + String.valueOf(res));
                if (res == true) {
                    ProgressBar pb = findViewById(R.id.login_progress);
                    pb.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Too long waiting");
                    //TODO(Nikich): if your are here, it means that something went wrong
                    //server not available or something like that

                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d(TAG, "incorrect data: " + e.getMessage());
            //TODO(Nikich): show message about incorrect data
        }
    }

    private boolean checkLogIn(final String userName, final String passvord) throws IncorrectDataException {
        URL url = null;
        try {
            url = new URL("https://extern.tomsksoft.com/user/note/set/");

            Authenticator.setDefault(new Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, passvord.toCharArray());
                }});

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            //connection.getOutputStream().write("{\"jsonrpc\": \"2.0\", \"method\": \"notification\", \"params\": {\"type\": 1, \"message\": \"will be in office at 13:00\", \"date\": \"2018-03-19\", \"remind_at\": \"2018-03-19 13:15:00\"}, \"id\": 1}".getBytes());
            connection.connect();
            int rc = connection.getResponseCode();
            if (rc == 200) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("login", userName);
                editor.putString("password", passvord);
                editor.apply();

                return true;
            } else if(rc == 401) {
                throw new IncorrectDataException(userName + ":" + passvord);
            }
            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    @Override
    public void onDestroy()
    {
        moveTaskToBack(true);

        super.onDestroy();

        System.runFinalizersOnExit(true);
        System.exit(0);
    }
}

