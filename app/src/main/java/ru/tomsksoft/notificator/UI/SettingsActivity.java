package ru.tomsksoft.notificator.UI;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.Calendar;

import ru.tomsksoft.notificator.R;
import ru.tomsksoft.notificator.UserDataStorage;
import ru.tomsksoft.notificator.alarm.AlarmBootReceiver;
import ru.tomsksoft.notificator.alarm.AlarmReceiver;
import ru.tomsksoft.notificator.alarm.DayOfWeek;


public class SettingsActivity extends AppCompatActivity
{
    private AlarmManager am;
    private PendingIntent alarmIntent;
    private TimePicker tp;
    private ToggleButton setAlarmTB;
    private ToggleButton setNotifTB;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        boolean changed = false;
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        tp = findViewById(R.id.timePicker);
        tp.setIs24HourView(true);
//---------------------------------------------------------------------------------------------------
        final LinearLayout alarmSettingsLayout = findViewById(R.id.alarmSettingsLayout);
        final LinearLayout addAlarmLayout = findViewById(R.id.addAlarmLayout);

        setAlarmTB = findViewById(R.id.toggleButtonSetAlarm);
        setNotifTB = findViewById(R.id.toggleButtonSetNotif);
        setNotifTB.setChecked(new UserDataStorage(this).getNotificationsCheck());

        loadAlarmParam();
//---------------------------------------------------------------------------------------------------
        if (setAlarmTB.isChecked())
        {
            setAlarmTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
            alarmSettingsLayout.setVisibility(View.VISIBLE);
        }
        else
            setAlarmTB.setBackgroundColor(Color.RED);

        setAlarmTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveAlarmParam();
                if (isChecked)
                {
                    setAlarmTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
                    alarmSettingsLayout.setVisibility(View.VISIBLE);
                    addAlarmLayout.setVisibility(View.VISIBLE);
                    loadAlarmParam();
                }
                else
                {

                    setAlarmTB.setBackgroundColor(Color.RED);
                    alarmSettingsLayout.setVisibility(View.INVISIBLE);
                    addAlarmLayout.setVisibility(View.INVISIBLE);
                    disableAlarm();
                }
            }
        });
//---------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------
        if (setNotifTB.isChecked())
        {
            setNotifTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
        }
        else
            setNotifTB.setBackgroundColor(Color.RED);

        setNotifTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    setNotifTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
                else
                    setNotifTB.setBackgroundColor(Color.RED);
                new UserDataStorage(SettingsActivity.this).saveNotificationsCheck(isChecked);
            }
        });
//---------------------------------------------------------------------------------------------------
        CompoundButton.OnCheckedChangeListener checkerListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    addAlarmLayout.setVisibility(View.VISIBLE);
            }
        };
        ((CheckBox)findViewById(R.id.checkBox1)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)findViewById(R.id.checkBox2)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)findViewById(R.id.checkBox3)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)findViewById(R.id.checkBox4)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)findViewById(R.id.checkBox5)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)findViewById(R.id.checkBox6)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)findViewById(R.id.checkBox7)).setOnCheckedChangeListener(checkerListener);
//---------------------------------------------------------------------------------------------------
        tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                addAlarmLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.exit)
        {
            if (!new UserDataStorage(this).getUserAuthData()[0].equals("login"))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
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
                                        new UserDataStorage(SettingsActivity.this).cleanUserData();
                                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            else
            {
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickSetAlarm(View view)
    {
        boolean[] chek = new boolean[7];
        chek[0] = ((CheckBox)findViewById(R.id.checkBox1)).isChecked();
        chek[1] = ((CheckBox)findViewById(R.id.checkBox2)).isChecked();
        chek[2] = ((CheckBox)findViewById(R.id.checkBox3)).isChecked();
        chek[3] = ((CheckBox)findViewById(R.id.checkBox4)).isChecked();
        chek[4] = ((CheckBox)findViewById(R.id.checkBox5)).isChecked();
        chek[5] = ((CheckBox)findViewById(R.id.checkBox6)).isChecked();
        chek[6] = ((CheckBox)findViewById(R.id.checkBox7)).isChecked();
        System.out.println(chek[0] + " " + chek[1] + " " + chek[2] + " " + chek[3] + " " + chek[4] + " " + chek[5] + " " + chek[6]);
        enableAlarm(tp.getCurrentHour(), tp.getCurrentMinute(), chek);
        (findViewById(R.id.addAlarmLayout)).setVisibility(View.INVISIBLE);
        Toast.makeText(SettingsActivity.this, "Напоминание установлено", Toast.LENGTH_SHORT).show();
    }

    public void enableAlarm(int hourOfDay, int minute, boolean[] chek)
    {
        saveAlarmParam();

        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.getTimeInMillis() + "  " + hourOfDay + "  " + minute);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.MILLISECOND, 0);
        System.out.println(calendar.getTimeInMillis());

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 60 * 24, alarmIntent);

        ComponentName receiver = new ComponentName(this, AlarmBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableAlarm()
    {
        saveAlarmParam();

        am.cancel(alarmIntent);

        ComponentName receiver = new ComponentName(this, AlarmBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void saveAlarmParam()
    {
        int mask = 0;
        if (((CheckBox)findViewById(R.id.checkBox1)).isChecked()) {
            mask |= DayOfWeek.MONDAY.getValue();
        }

        if (((CheckBox)findViewById(R.id.checkBox2)).isChecked()) {
            mask |= DayOfWeek.TUESDAY.getValue();
        }

        if (((CheckBox)findViewById(R.id.checkBox3)).isChecked()) {
            mask |= DayOfWeek.WEDNESDAY.getValue();
        }

        if (((CheckBox)findViewById(R.id.checkBox4)).isChecked()) {
            mask |= DayOfWeek.THURSDAY.getValue();
        }

        if (((CheckBox)findViewById(R.id.checkBox5)).isChecked()) {
            mask |= DayOfWeek.FRIDAY.getValue();
        }

        if (((CheckBox)findViewById(R.id.checkBox6)).isChecked()) {
            mask |= DayOfWeek.SATURDAY.getValue();
        }

        if (((CheckBox)findViewById(R.id.checkBox7)).isChecked()) {
            mask |= DayOfWeek.SUNDAY.getValue();
        }

        UserDataStorage dataStorage = new UserDataStorage(this);
        dataStorage.saveAlarmParam(setAlarmTB.isChecked(), mask, tp.getCurrentHour(), tp.getCurrentMinute());
    }

    private void loadAlarmParam()
    {
        UserDataStorage dataStorage = new UserDataStorage(this);
        int mask = dataStorage.loadDaysOfWeekSet();

        setAlarmTB.setChecked(dataStorage.getAlarmCheck());
        ((CheckBox)findViewById(R.id.checkBox1)).setChecked(DayOfWeek.MONDAY.isDayOfWeekSet(mask));
        ((CheckBox)findViewById(R.id.checkBox2)).setChecked(DayOfWeek.TUESDAY.isDayOfWeekSet(mask));
        ((CheckBox)findViewById(R.id.checkBox3)).setChecked(DayOfWeek.WEDNESDAY.isDayOfWeekSet(mask));
        ((CheckBox)findViewById(R.id.checkBox4)).setChecked(DayOfWeek.THURSDAY.isDayOfWeekSet(mask));
        ((CheckBox)findViewById(R.id.checkBox5)).setChecked(DayOfWeek.FRIDAY.isDayOfWeekSet(mask));
        ((CheckBox)findViewById(R.id.checkBox6)).setChecked(DayOfWeek.SATURDAY.isDayOfWeekSet(mask));
        ((CheckBox)findViewById(R.id.checkBox7)).setChecked(DayOfWeek.SUNDAY.isDayOfWeekSet(mask));
        tp.setCurrentHour(dataStorage.getHour());
        tp.setCurrentMinute(dataStorage.getMinute());
    }

    @Override
    public void onBackPressed()
    {
        if ((findViewById(R.id.addAlarmLayout)).getVisibility() == View.VISIBLE)
            Toast.makeText(SettingsActivity.this, "отменено", Toast.LENGTH_SHORT).show();

        saveAlarmParam();

        Intent intent;

        if (new UserDataStorage(this).getUserAuthData()[0].equals("login"))
        {
            intent = new Intent(SettingsActivity.this, LoginActivity.class);
        }
        else
        {
            intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);

        //super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        saveAlarmParam();
        super.onDestroy();
    }


}
