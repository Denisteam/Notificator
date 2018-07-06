package ru.tomsksoft.notificator.UI;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;


import ru.tomsksoft.notificator.R;
import ru.tomsksoft.notificator.UserDataStorage;
import ru.tomsksoft.notificator.exceptions.IncorrectDataException;
import ru.tomsksoft.notificator.message.Message;
import ru.tomsksoft.notificator.message.MessageSender;
import ru.tomsksoft.notificator.message.RPCMethod;

public class MainActivity extends AppCompatActivity implements SettingsFragment.OnClickSettingsListener
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final String TAG = "MainActivity";
    private List<String> templates = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private static Calendar calendar;
    private FragmentManager fragmentManager;
    private MenuItem sendMI;
    private MenuItem settingsMI;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        sendMI = menu.findItem(R.id.send_message);
        settingsMI = menu.findItem(R.id.settings);
        super.onCreateOptionsMenu(menu);
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

        fragmentManager = getFragmentManager();

        calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM");
        ((TextView)findViewById(R.id.dateField)).setText(dateFormat.format(calendar.getTime()));
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
        findViewById(R.id.layoutMessage).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutDate).setVisibility(View.VISIBLE);
        sendMI.setVisible(true);
        settingsMI.setVisible(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                //Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                //startActivity(intent);
                fragmentManager.popBackStack();
                Fragment settingsFragment = new SettingsFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.container, settingsFragment, "settings")
                        .addToBackStack(null)
                        .commit();
                findViewById(R.id.layoutMessage).setVisibility(View.INVISIBLE);
                findViewById(R.id.layoutDate).setVisibility(View.INVISIBLE);
                sendMI.setVisible(false);
                settingsMI.setVisible(false);
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
                                        if (findViewById(R.id.layoutMessage).getVisibility() == View.INVISIBLE)
                                            backToMain();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.send_message:
                if (((EditText)findViewById(R.id.messageField)).getText().toString().isEmpty())
                    Toast.makeText(this, R.string.null_text, Toast.LENGTH_SHORT).show();
                else
                    sendMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickChangeDate(View view) {
        String str;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM");
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
        } catch (InterruptedException | IncorrectDataException e) {
            e.printStackTrace();
        }

        addTemplate(((EditText)findViewById(R.id.messageField)).getText().toString());
        ((EditText)findViewById(R.id.messageField)).setText("");
    }

    //
    public void addTemplate(String msg) {
        if(!templates.isEmpty() && (templates.get(0).equals(msg))) {
            return;
        }

        String st;
        Iterator<String> it = templates.iterator();
        while (it.hasNext()) {
            st = it.next();
            if (st.equals(msg))
                it.remove();
        }

        templates.add(0, msg);

        if (templates.size() > 30)
            templates.remove(templates.size()-1);

       listAdapter.notifyDataSetChanged();

        StringBuilder sb = new StringBuilder();
        for (int i = templates.size() - 1; i >= 0; i--)
            sb.append(templates.get(i)).append(",");
        new UserDataStorage(MainActivity.this).saveUserTemplate(sb.toString());
    }

    private ArrayList<String> getStringArray()
    {
        String savedString = new UserDataStorage(MainActivity.this).getUserTemplate();
        if (savedString != null && !TextUtils.isEmpty(savedString))
        {
            StringTokenizer st = new StringTokenizer(savedString, ",");
            ArrayList<String> array = new ArrayList<String>(st.countTokens());
            while(st.hasMoreTokens())
                array.add(st.nextToken());
            return array;
        }
        return new ArrayList<>();
    }

    @Override
    protected void onResume() {
        ((EditText)findViewById(R.id.messageField)).setText(new UserDataStorage(this).getMessage());
        ArrayList<String> strList = getStringArray();
        if(templates.isEmpty() && !strList.isEmpty())
            for (int i = 0; i < strList.size() ; i++)
                addTemplate(strList.get(i));
        super.onResume();
    }

    @Override
    protected void onPause() {
        new UserDataStorage(MainActivity.this).saveMessage(((EditText) findViewById(R.id.messageField)).getText().toString());
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.layoutMessage).getVisibility() == View.INVISIBLE)
            if (((SettingsFragment) fragmentManager.findFragmentByTag("settings")).isChanged())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

