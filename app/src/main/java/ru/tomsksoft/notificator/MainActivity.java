package ru.tomsksoft.notificator;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ru.tomsksoft.notificator.exceptions.IncorrectDataException;
import ru.tomsksoft.notificator.message.Message;
import ru.tomsksoft.notificator.message.MessageSender;
import ru.tomsksoft.notificator.message.RPCMethod;

public class MainActivity extends AppCompatActivity {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final String TAG = "MainActivity";
    private List<String> templates = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
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

        ListView listView = findViewById(R.id.list_templates);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, templates);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id)
           {
               ((EditText)findViewById(R.id.messageField)).setText(((TextView) itemClicked).getText());
           }
        });
        calendar = Calendar.getInstance();

        ((TextView)findViewById(R.id.dateField)).setText(calendar.get(Calendar.DAY_OF_MONTH) + "." + (((calendar.get(Calendar.MONTH)+1) > 9) ? "" : "0") + (calendar.get(Calendar.MONTH)+1));
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
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        new UserDataStorage(MainActivity.this).cleanUserData();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
                //TODO(Nikita): "отправить" это не item, а button. Исправь, сделай кнопку неактивной,
                //пока в текстовом поле не появится текст(няльзя отправлять пустую строку)
                //для этого к текстовому полю следует добавить onChangeListener или типо того
                //и там, если кол-во символов больше 0 делаем кнопку активной
            case R.id.send_message:
                sendMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickChangeDate(View view) {
        String str;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.ENGLISH);
        switch (view.getId()) {
            case R.id.button_plus:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                str = dateFormat.format(calendar.getTime());
                ((TextView) findViewById(R.id.dateField)).setText(str);
                break;
            case R.id.button_minus:
                if (!calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    str = dateFormat.format(calendar.getTime());
                    ((TextView) findViewById(R.id.dateField)).setText(str);
                }
                break;
            default:
                break;
        }
    }

    public void sendMessage() {
        final String msg = ((TextView) findViewById(R.id.messageField)).getText().toString();

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        Message message = new Message(this, RPCMethod.NOTIFICATION);
        message.addParam("type", "1");
        message.addParam("message", msg);
        message.addParam("date", dateFormat.format(new Date()));
        //TODO(Nikita): add remind_at
        message.addParam("remind_at", "00-00-00");

        try {
            boolean result = MessageSender.send(MainActivity.this, message);

            if (result) {
                Toast.makeText(this, R.string.sending, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IncorrectDataException e) {
            e.printStackTrace();
        }
        addTemplate(((EditText)findViewById(R.id.messageField)).getText().toString());
        ((EditText)findViewById(R.id.messageField)).setText("");
    }

    //
    public void addTemplate(String msg) {
        if(templates.contains(msg)) {
            return;
        }

        String st;
        Iterator<String> it = templates.iterator();
        while (it.hasNext()) {
            st = it.next();
            if (st.equals(msg))
                it.remove();
        }

        templates.add(msg);

        if (templates.size() > 30)
            templates.remove(templates.size()-1);

       listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //сохранять данные из текстовых полей на паузе такое себе

     /*   ((EditText)findViewById(R.id.messageField)).setText(new UserDataStorage(this).getMessage());
        Set<String> strSet = new UserDataStorage(this).getUserTamplate();
       for(String str: strSet) {
           addTemplate(str);
       }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}

