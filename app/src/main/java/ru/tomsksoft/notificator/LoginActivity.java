package ru.tomsksoft.notificator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import ru.tomsksoft.notificator.message.MessageSender;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LOGIN_ACTIVITY";
    private static final int TIMEOUT_VALUE = 5000;
    private boolean alreadyTriedAuthenticating = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ((EditText) findViewById(R.id.login)).setText("ntakovoy");
        ((EditText) findViewById(R.id.password)).setText("aoiwnu91su3");

        String login = UserDataStorage.getUserLogin(this);
        String password = UserDataStorage.getUserPassword(this);

        if (!login.equals("login")) {
            ((EditText) findViewById(R.id.login)).setText(login);
            ((EditText) findViewById(R.id.password)).setText(password);

            onClickLogIn(new View(this));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickLogIn(View view) {
        final String login = ((EditText) findViewById(R.id.login)).getText().toString();
        final String password = ((EditText) findViewById(R.id.password)).getText().toString();

        ProgressBar pb = findViewById(R.id.login_progress);
        pb.setVisibility(View.VISIBLE);
        LinearLayout layout = findViewById(R.id.log_in_layout);
        layout.setVisibility(View.INVISIBLE);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> result = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return MessageSender.checkLogIn(LoginActivity.this, login, password);
                //   return false;
            }
        });
        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            if (result.isDone()) {
                boolean res = result.get();
                Log.d(TAG, "connecting result: " + String.valueOf(res));
                if (res) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Too long waiting");
                    pb.setVisibility(View.INVISIBLE);
                    layout.setVisibility(View.VISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(R.string.connection_error)
                            .setMessage(R.string.connection_error_message)
                            .setCancelable(false)
                            .setNegativeButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            } else
                Toast.makeText(this, R.string.end_wait_time, Toast.LENGTH_LONG).show();

        } catch (InterruptedException e) {
            e.printStackTrace();
            pb.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.VISIBLE);
        } catch (ExecutionException e) {
            Log.d(TAG, "incorrect data: " + e.getMessage());
            Toast.makeText(LoginActivity.this, R.string.incorrect_data, Toast.LENGTH_LONG).show();
            alreadyTriedAuthenticating = false;
            pb.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        moveTaskToBack(true);

        super.onDestroy();

        System.runFinalizersOnExit(true);
        System.exit(0);
    }
}

