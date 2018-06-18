package ru.tomsksoft.notificator;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ru.tomsksoft.notificator.message.Message;
import ru.tomsksoft.notificator.message.MessageSender;
import ru.tomsksoft.notificator.message.RPCMethod;

public class MainActivity extends AppCompatActivity {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final String TAG = "MainActivity";
    private static ArrayList<TextView> templates = new ArrayList<>();
    //TODO(Nikita): Не держи андроид классы в статических полях("this is a memory leak (and also breaks Instant Run)")
    private static LinearLayout itemTable;
    private static Calendar calendar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_LOW));
            }
        }

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        itemTable = findViewById(R.id.itemTable);
        calendar = Calendar.getInstance();
        //TODO(Nikita) Warning: Do not concatenate text displayed with `setText`. Use resource string with placeholders.
        ((TextView) findViewById(R.id.dateField)).setText(calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1));
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.alert)
                        .setMessage(R.string.are_you_sure_to_exit)
                        .setCancelable(false)
                        .setNegativeButton(R.string.no,
                                (dialog, id1) -> dialog.cancel())
                        .setPositiveButton(R.string.yes,
                                (dialog, which) -> {
                                    UserDataStorage.cleanUserData(MainActivity.this);
                                    Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intent1);
                                    dialog.cancel();
                                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.send_message:
                sendMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickChangeDate(View view) {
        String str;
        switch (view.getId()) {
            case R.id.button_plus:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                str = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH) + "." + (((calendar.get(Calendar.MONTH) + 1) > 9) ? "" : "0") + calendar.get(Calendar.MONTH) + 1);
                ((TextView) findViewById(R.id.dateField)).setText(str);
                break;
            case R.id.button_minus:
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                if (calendar.before(Calendar.getInstance()) && calendar.get(Calendar.DAY_OF_MONTH) != Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    calendar.add(Calendar.DAY_OF_MONTH, +1);
                str = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH) + "." + (((calendar.get(Calendar.MONTH) + 1) > 9) ? "" : "0") + calendar.get(Calendar.MONTH) + 1);
                ((TextView) findViewById(R.id.dateField)).setText(str);
                break;
            default:
                break;
        }
    }

    //WTF is this??
    public void sendMessage() {
        final String msg = ((TextView) findViewById(R.id.messageField)).getText().toString();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Message message = new Message(this, RPCMethod.NOTIFICATION);
        message.addParam("type", "1");
        message.addParam("message", msg);
        message.addParam("date", dateFormat.format(new Date()));
        //TODO(Nikita): add remind_at
        message.addParam("remind_at", "00-00-00");


        Future<Boolean> result = executor.submit(() -> MessageSender.send(MainActivity.this, message));
        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            if (result.isDone()) {
                try {
                    if (result.get()) {
                        Toast.makeText(MainActivity.this, R.string.sending, Toast.LENGTH_SHORT).show();
                        addTemplate(((EditText) findViewById(R.id.messageField)).getText().toString());
                    } else {
                        Toast.makeText(MainActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addTemplate(((EditText) findViewById(R.id.messageField)).getText().toString());
    }

    public void addTemplate(String msg) {
        if (!templates.isEmpty() && ((TextView) findViewById(itemTable.getChildAt(0).getId())).getText().equals(msg))
            return;

        LinearLayout.LayoutParams lpView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView text = new TextView(this);
        lpView.setMargins(0, 5, 0, 5);
        text.setLayoutParams(lpView);
        text.setId(View.generateViewId());
        text.setText(msg);
        text.setPadding(10, 10, 5, 10);
        text.setTextSize(15);
        text.setSelected(true);
        text.setTextColor(Color.WHITE);
        text.setBackgroundColor(getResources().getColor(R.color.blue_grey_500));
        text.setOnClickListener(v -> ((EditText) findViewById(R.id.messageField)).setText(((TextView) findViewById(v.getId())).getText()));
        TextView tv = new TextView(this);
        for (Iterator<TextView> it = templates.iterator(); it.hasNext(); tv = it.next())
            if (tv.getText().equals(msg)) {
                itemTable.removeView(tv);
                it.remove();
            }
        itemTable.addView(text, 0);
        templates.add(text);

        if (itemTable.getChildCount() > 30)
            itemTable.removeViewAt(itemTable.getChildCount() - 1);
    }

    private ArrayList<String> getStringArray() {
        String savedString = UserDataStorage.getUserTamplate(this);
        if (savedString != null && !TextUtils.isEmpty(savedString)) {
            StringTokenizer st = new StringTokenizer(savedString, ",");
            ArrayList<String> array = new ArrayList<>(st.countTokens());
            while (st.hasMoreTokens()) {
                array.add(st.nextToken());
            }
            return array;
        }

        return new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ((EditText) findViewById(R.id.messageField)).setText(UserDataStorage.getMessage(this));

        if (templates.isEmpty() && getStringArray().size() > 0) {
            if (!getStringArray().isEmpty())
                for (String msg : getStringArray()) {
                    addTemplate(msg);
                }
        }
    }

    @Override
    protected void onPause() {
        UserDataStorage.saveMessage(this, ((EditText) findViewById(R.id.messageField)).getText().toString());

        StringBuilder sb = new StringBuilder();
        for (int i = itemTable.getChildCount() - 1; i >= 0; i--) {
            sb.append(((TextView) itemTable.getChildAt(i)).getText().toString()).append(',');
        }
        UserDataStorage.saveUserTamplate(this, sb.toString());

        templates.clear();
        itemTable.removeAllViews();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        moveTaskToBack(true);
        super.onDestroy();
    }

}

