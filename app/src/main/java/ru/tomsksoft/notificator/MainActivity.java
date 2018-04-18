package ru.tomsksoft.notificator;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.READ_PHONE_STATE;
import static ru.tomsksoft.notificator.MessageType.MESSAGE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TimePicker tp;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        MessageSender.setMessageID(sharedPref.getInt("current_messsage_id", 0));

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        //I wouldn't remove it now
//        Button subscribeButton = findViewById(R.id.subscribeButton);
//        subscribeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseMessaging.getInstance().subscribeToTopic("news");
//
//                String msg = getString(R.string.msg_subscribed);
//                Log.d(TAG, msg);
//                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//            }
//        });
        tp = findViewById(R.id.timePicker);
        tp.setIs24HourView(true);
        tp.setCurrentHour(0);
        tp.setCurrentMinute(0);

        final LinearLayout layout2 = findViewById(R.id.layout2);
        final LinearLayout layout3 = findViewById(R.id.layout3);
        final TextView timeText = findViewById(R.id.time_text);

        final Spinner spinnerTemplate = findViewById(R.id.template_spinner);
        spinnerTemplate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                switch ((int) spinnerTemplate.getSelectedItemId())
                {
                    case 0:
                        //Ставим шаблон
                        layout2.setVisibility(View.INVISIBLE);
                        layout3.setVisibility(View.INVISIBLE);
                        timeText.setVisibility(View.INVISIBLE);
                        tp.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        //Ставим шаблон
                        layout2.setVisibility(View.VISIBLE);
                        layout3.setVisibility(View.VISIBLE);
                        timeText.setVisibility(View.VISIBLE);
                        tp.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.exit:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                final SharedPreferences.Editor editor = sharedPref.edit();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.alert)
                        .setMessage(R.string.are_you_sure_to_exit)
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
                                editor.putString("login", "login");
                                editor.putString("password", "password");
                                editor.apply();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickSettings (View view)
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onClickSendMessage(View view)
    {
        System.out.println(String.valueOf(Calendar.getInstance().getTime().getHours() + tp.getCurrentHour())
                + ":" + String.valueOf(Calendar.getInstance().getTime().getMinutes() + tp.getCurrentMinute()));
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> result = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return MessageSender.sendMessage(MainActivity.this, String.valueOf(Calendar.getInstance().getTime().getHours() + tp.getCurrentHour())
                        + ":" + String.valueOf(Calendar.getInstance().getTime().getMinutes() + tp.getCurrentMinute()));
            }
        });
        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            if (result.isDone())
            {
                try {
                    if (result.get())
                    {
                        Toast.makeText(MainActivity.this, "отправлено", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, R.string.sending, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("current_messsage_id", MessageSender.getMessageID());
        editor.apply();
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

