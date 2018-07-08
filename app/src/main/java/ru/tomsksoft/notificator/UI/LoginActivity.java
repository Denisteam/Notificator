package ru.tomsksoft.notificator.UI;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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

import ru.tomsksoft.notificator.NotificationRepeater;
import ru.tomsksoft.notificator.R;
import ru.tomsksoft.notificator.UserCreditans;
import ru.tomsksoft.notificator.UserDataStorage;
import ru.tomsksoft.notificator.exceptions.IncorrectDataException;
import ru.tomsksoft.notificator.message.Message;
import ru.tomsksoft.notificator.message.MessageSender;
import ru.tomsksoft.notificator.message.RPCMethod;


public class LoginActivity extends AppCompatActivity implements SettingsFragment.OnClickSettingsListener{
    private static final String TAG = "LOGIN_ACTIVITY";
    private FragmentManager fragmentManager;
    private MenuItem settingsMI;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        settingsMI = menu.findItem(R.id.settings);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("cancel", false)) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationRepeater.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            am.cancel(alarmIntent);
        }

        setContentView(R.layout.activity_login);
        ((EditText) findViewById(R.id.login)).setText("ntakovoy");
        ((EditText) findViewById(R.id.password)).setText("aoiwnu91su3");

        UserCreditans authData = new UserDataStorage(this).getUserAuthData();
        String login = authData.getLogin();
        String password = authData.getPassword();

        if (!login.equals("login")) {
            ((EditText) findViewById(R.id.login)).setText(login);
            ((EditText) findViewById(R.id.password)).setText(password);

            login();
        }
        fragmentManager = getFragmentManager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            //Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
            //startActivity(intent);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.popBackStack();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment settingsFragment = new SettingsFragment();
            fragmentTransaction.add(R.id.container, settingsFragment, "settings");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            findViewById(R.id.log_in_layout).setVisibility(View.INVISIBLE);
            settingsMI.setVisible(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickLogIn(View view)
    {
        login();
    }

    private void login()
    {
        final String login = ((EditText) findViewById(R.id.login)).getText().toString();
        final String password = ((EditText) findViewById(R.id.password)).getText().toString();

        ProgressBar pb = findViewById(R.id.login_progress);
        pb.setVisibility(View.VISIBLE);
        LinearLayout layout = findViewById(R.id.log_in_layout);
        layout.setVisibility(View.INVISIBLE);

        try {
            boolean res = MessageSender.checkLogIn(LoginActivity.this, login, password);
                if (res) {
                    UserDataStorage dataStorage = new UserDataStorage(this);
                    if (dataStorage.isNotificationsEnabled() && dataStorage.isTokenRefreshed()) {
                        Message msg = new Message(this, RPCMethod.TOKEN_ADD);
                        msg.addParam("token", dataStorage.getToken());
                        msg.addParam("model", Build.MANUFACTURER + " " + Build.MODEL);
                        msg.addParam("os", Build.VERSION.RELEASE);
                        MessageSender.send(this, msg);
                    }

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    @Override
    public void onClick()
    {
        backToMain();
    }

    private void backToMain()
    {
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction()
                .remove(fragmentManager.findFragmentByTag("settings"))
                .addToBackStack(null)
                .commit();
        findViewById(R.id.log_in_layout).setVisibility(View.VISIBLE);
        settingsMI.setVisible(true);
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.log_in_layout).getVisibility() == View.INVISIBLE)
            if (((SettingsFragment) fragmentManager.findFragmentByTag("settings")).isChanged())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(R.string.alert)
                        .setMessage(R.string.unsaved_settings)
                        .setCancelable(false)
                        .setNegativeButton(R.string.no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        backToMain();
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            else
                backToMain();
        else
            super.onBackPressed();
    }
}