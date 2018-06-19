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


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login";
    private static final int TIMEOUT_VALUE = 5000;
    private boolean alreadyTriedAuthenticating = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ((EditText)findViewById(R.id.login)).setText("ntakovoy");
        ((EditText)findViewById(R.id.password)).setText("aoiwnu91su3");
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPref.getString("login", "login").equals("login"))
        {
            ((EditText)findViewById(R.id.login)).setText(sharedPref.getString("login", "login"));
            ((EditText)findViewById(R.id.password)).setText(sharedPref.getString("password", "password"));
            onClickLogIn(new View(this));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings)
        {
            Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickLogIn(View view)
    {
        final String login = ((EditText)findViewById(R.id.login)).getText().toString();
        final String password = ((EditText)findViewById(R.id.password)).getText().toString();

        ProgressBar pb = findViewById(R.id.login_progress);
        pb.setVisibility(View.VISIBLE);
        LinearLayout layout = findViewById(R.id.log_in_layout);
        layout.setVisibility(View.INVISIBLE);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> result = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return checkLogIn(login, password);
                //   return false;
            }
        });
        executor.shutdown();

        try
        {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            if (result.isDone())
            {
                boolean res = result.get();
                Log.d(TAG, "connecting result: " + String.valueOf(res));
                if (res)
                {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else
                {
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
            }
            else
                Toast.makeText(this, R.string.end_wait_time, Toast.LENGTH_LONG).show();

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            pb.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.VISIBLE);
        }
        catch (ExecutionException e)
        {
            Log.d(TAG, "incorrect data: " + e.getMessage());
            Toast.makeText(LoginActivity.this, R.string.incorrect_data, Toast.LENGTH_LONG).show();
            alreadyTriedAuthenticating = false;
            pb.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.VISIBLE);
        }
    }
    private boolean checkLogIn(final String userName, final String password) throws IncorrectDataException
    {
        try {
            URL url = new URL("https://extern.tomsksoft.com/user/note/set/");

            Authenticator.setDefault(new Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    if (!alreadyTriedAuthenticating)
                    {
                        alreadyTriedAuthenticating = true;
                        return new PasswordAuthentication(userName, password.toCharArray());
                    }
                    else
                    {
                        return null;
                    }
                }});
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_VALUE);
            connection.setReadTimeout(TIMEOUT_VALUE);
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
                editor.putString("password", password);
                editor.apply();
                return true;
            } else if(rc == 401) {
                throw new IncorrectDataException(userName + ":" + password);
            }
            connection.disconnect();
        }
        catch (SocketTimeoutException e) {
            System.out.println("More than " + TIMEOUT_VALUE + " elapsed.");
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

