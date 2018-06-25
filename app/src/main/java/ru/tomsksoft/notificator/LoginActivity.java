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

import ru.tomsksoft.notificator.exceptions.IncorrectDataException;
import ru.tomsksoft.notificator.message.MessageSender;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LOGIN_ACTIVITY";

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

        String[] authData = new UserDataStorage(this).getUserAuthData();
        String login = authData[0];
        String password = authData[1];

        if (!login.equals("login")) {
            ((EditText) findViewById(R.id.login)).setText(login);
            ((EditText) findViewById(R.id.password)).setText(password);

            //он у тебя просто вызывается, без какого-либо нажатия, при чем тут onClick?
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

        try {
            boolean res = MessageSender.checkLogIn(LoginActivity.this, login, password);
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
        } catch (InterruptedException e) {
            e.printStackTrace();
            pb.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.VISIBLE);
        } catch (IncorrectDataException e) {
            Log.d(TAG, "incorrect data: " + e.getMessage());
            Toast.makeText(LoginActivity.this, R.string.incorrect_data, Toast.LENGTH_LONG).show();
            pb.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.VISIBLE);
        }
    }
}

