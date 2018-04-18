package ru.tomsksoft.notificator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
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

import static android.Manifest.permission.READ_PHONE_STATE;
import static ru.tomsksoft.notificator.MessageType.MESSAGE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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


        Button logTokenButton = findViewById(R.id.send_message);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MessageSender.sendMessage(MainActivity.this, "12:55");
                } catch (IncorrectDataException e) {
                    //TODO(Nikich): tell user about entered incorrect data
                }
                Toast.makeText(MainActivity.this, "отправлено", Toast.LENGTH_SHORT).show();
            }
        });

        final TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
        tp.setIs24HourView(true);
        tp.setCurrentHour(0);
        tp.setCurrentMinute(0);

        final LinearLayout layout2 = (LinearLayout)findViewById(R.id.layout2);
        final TextView timeText = (TextView)findViewById(R.id.time_text);

        final Spinner spinnerTemplate = (Spinner)findViewById(R.id.template_spinner);
        spinnerTemplate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                switch ((int) spinnerTemplate.getSelectedItemId())
                {
                    case 0:
                        //Ставим шаблон
                        layout2.setVisibility(View.INVISIBLE);
                        timeText.setVisibility(View.INVISIBLE);
                        tp.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        //Ставим шаблон
                        layout2.setVisibility(View.VISIBLE);
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

    public void onClickSettings (View view)
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
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

