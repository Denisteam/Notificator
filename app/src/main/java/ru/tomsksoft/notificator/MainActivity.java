package ru.tomsksoft.notificator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPref;
    private ArrayList<String> templates = new ArrayList<>();
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

        sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
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
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        editor.putString("login", "login");
                                        editor.putString("password", "password");
                                        editor.putString("Message", "");
                                        editor.putString("Date", "");
                                        editor.putString("Template", "");
                                        editor.apply();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
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

    public void onClickChangeDate(View view)
    {
        String str = "";
        switch (view.getId())
        {
            case R.id.button_plus:
                calendar.add(Calendar.DAY_OF_MONTH,1);
                str = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH) + "." + (((calendar.get(Calendar.MONTH)+1) > 9) ? "" : "0") + (calendar.get(Calendar.MONTH)+1));
                ((TextView)findViewById(R.id.dateField)).setText(str);
                break;
            case R.id.button_minus:
                calendar.add(Calendar.DAY_OF_MONTH,-1);
                if (calendar.before(Calendar.getInstance()) && calendar.get(Calendar.DAY_OF_MONTH) != Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    calendar.add(Calendar.DAY_OF_MONTH,+1);
                str = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH) + "." + (((calendar.get(Calendar.MONTH)+1) > 9) ? "" : "0") + (calendar.get(Calendar.MONTH)+1));
                ((TextView)findViewById(R.id.dateField)).setText(str);
                break;
            default:
                break;
        }
    }

    public void sendMessage()
    {
       final String msg = ((TextView)findViewById(R.id.messageField)).getText().toString();
       int day = calendar.get(Calendar.DAY_OF_MONTH);
       int month = calendar.get(Calendar.MONTH);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> result = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return MessageSender.sendMessage(MainActivity.this,
                        calendar.get(Calendar.DAY_OF_MONTH) + "." + (((calendar.get(Calendar.MONTH)+1) > 9) ? "" : "0") + calendar.get(Calendar.MONTH)+1,
                        msg);
            }
        });
        executor.shutdown();

        try
        {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            if (result.isDone())
            {
                try
                {
                    if (result.get())
                    {
                        Toast.makeText(MainActivity.this, R.string.sending, Toast.LENGTH_SHORT).show();
                        //addTemplate(((EditText)findViewById(R.id.messageField)).getText().toString());
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        addTemplate(((EditText)findViewById(R.id.messageField)).getText().toString());
    }

    public void addTemplate(String msg)
    {
        if(!templates.isEmpty() && (templates.get(0).equals(msg)))
            return;

        String st = "";
        Iterator<String> it = templates.iterator();
        while (it.hasNext())
        {
            st = it.next();
            if (st.equals(msg))
                it.remove();
        }

        templates.add(0, msg);

        if (templates.size() > 30)
            templates.remove(templates.size()-1);

        listAdapter.notifyDataSetChanged();
    }

    private void setStringArray(ArrayList<String> array)
    {
        SharedPreferences.Editor editor = sharedPref.edit();
        StringBuilder str = new StringBuilder();
        if(array != null)
        {
            for (int i = array.size()-1; i >= 0; i--)
            {
                str.append(array.get(i)).append(",");
                System.out.println(array.get(i));
            }
        }
        editor.putString("Template", str.toString()).apply();
    }

    private ArrayList<String> getStringArray()
    {
        String savedString = sharedPref.getString("Template", null);
        if (savedString != null && !TextUtils.isEmpty(savedString))
        {
            StringTokenizer st = new StringTokenizer(savedString, ",");
            ArrayList<String> array = new ArrayList<String>(st.countTokens());
            while(st.hasMoreTokens())
            {
                array.add(st.nextToken());
            }
            for (String str: array)
            {
                System.out.println(str);
            }
            return array;
        }
        return new ArrayList<>();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        ((EditText)findViewById(R.id.messageField)).setText(sharedPref.getString("Message", ""));
        ArrayList<String> strList = getStringArray();
        if(templates.isEmpty() && !strList.isEmpty())
            for (int i = 0; i < strList.size() ; i++)
                addTemplate(strList.get(i));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Message", ((EditText)findViewById(R.id.messageField)).getText().toString());

        setStringArray(templates);

        editor.apply();

        templates.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("current_messsage_id", MessageSender.getMessageID());
        editor.apply();
    }

    @Override
    public void onDestroy() {
        moveTaskToBack(true);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.apply();

        super.onDestroy();

        System.runFinalizersOnExit(true);
        System.exit(0);
    }

}

