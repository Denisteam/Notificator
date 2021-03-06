package ru.tomsksoft.notificator.UI;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashSet;
import java.util.Set;

import ru.tomsksoft.notificator.MessagingService;
import ru.tomsksoft.notificator.R;
import ru.tomsksoft.notificator.UserDataStorage;
import ru.tomsksoft.notificator.alarm.AlarmBootReceiver;
import ru.tomsksoft.notificator.alarm.AlarmReceiver;
import ru.tomsksoft.notificator.alarm.AlarmTuner;
import ru.tomsksoft.notificator.alarm.DayOfWeek;
import ru.tomsksoft.notificator.exceptions.IncorrectDataException;
import ru.tomsksoft.notificator.message.Message;
import ru.tomsksoft.notificator.message.MessageSender;
import ru.tomsksoft.notificator.message.RPCMethod;

import static android.content.Context.ALARM_SERVICE;

public class SettingsFragment extends Fragment
{
    private static final String TAG = "SETTINGS_ACTIVITY";
    private AlarmManager am;
    private PendingIntent alarmIntent;
    private TimePicker tp;
    private ToggleButton setAlarmTB;
    private ToggleButton setNotifTB;
    private View view;
    private OnClickSettingsListener listener;
    private boolean isChanged;
    private boolean isNotifChnged;

    public interface OnClickSettingsListener {
        public void onClick();
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnClickSettingsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SettingsFragment.OnClickSettingsListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isChanged = false;
        isNotifChnged = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_settings, container, false);

        am = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        tp = view.findViewById(R.id.timePicker);
        tp.setIs24HourView(true);
//---------------------------------------------------------------------------------------------------
        final LinearLayout alarmSettingsLayout = view.findViewById(R.id.alarmSettingsLayout);
        final TextView acceptTextView = view.findViewById(R.id.textViewAccept);

        setAlarmTB = view.findViewById(R.id.toggleButtonSetAlarm);
        setNotifTB = view.findViewById(R.id.toggleButtonSetNotif);
        setNotifTB.setChecked(new UserDataStorage(getActivity()).isNotificationsEnabled());

        loadAlarmParam();
//---------------------------------------------------------------------------------------------------
        if (setAlarmTB.isChecked()) {
            setAlarmTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
            alarmSettingsLayout.setVisibility(View.VISIBLE);
        }
        else
            setAlarmTB.setBackgroundColor(Color.RED);

        setAlarmTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                acceptTextView.setVisibility(View.VISIBLE);
                isChanged = true;
                if (isChecked)
                {
                    setAlarmTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
                    alarmSettingsLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    setAlarmTB.setBackgroundColor(Color.RED);
                    alarmSettingsLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
//---------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------
        if (setNotifTB.isChecked())
            setNotifTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
        else
            setNotifTB.setBackgroundColor(Color.RED);

        setNotifTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isChanged = true;
                acceptTextView.setVisibility(View.VISIBLE);
                if (isChecked)
                {
                    setNotifTB.setBackgroundColor(Color.argb(255, 0, 153, 204));
                    isNotifChnged = true;
                }
                else
                {
                    setNotifTB.setBackgroundColor(Color.RED);
                    isNotifChnged = true;
                }
                new UserDataStorage(getActivity()).saveNotificationsCheck(isChecked);
            }
        });
//---------------------------------------------------------------------------------------------------
        CompoundButton.OnCheckedChangeListener checkerListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                acceptTextView.setVisibility(View.VISIBLE);
                isChanged = true;
            }
        };
        ((CheckBox)view.findViewById(R.id.checkBox1)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)view.findViewById(R.id.checkBox2)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)view.findViewById(R.id.checkBox3)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)view.findViewById(R.id.checkBox4)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)view.findViewById(R.id.checkBox5)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)view.findViewById(R.id.checkBox6)).setOnCheckedChangeListener(checkerListener);
        ((CheckBox)view.findViewById(R.id.checkBox7)).setOnCheckedChangeListener(checkerListener);
//---------------------------------------------------------------------------------------------------
        tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                acceptTextView.setVisibility(View.VISIBLE);
                isChanged = true;
            }
        });

        view.findViewById(R.id.addAlarm).setOnClickListener(onClickSetAlarm);
        return view;
    }

    View.OnClickListener onClickSetAlarm = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            saveAlarmParam();
            Toast.makeText(getActivity(), R.string.saved_settings, Toast.LENGTH_SHORT).show();
            onSomeClick(v);
        }
    };

    public void onSomeClick(View v) {
        listener.onClick();
    }

    public boolean isChanged()
    {
        return isChanged;
    }

    private void enableNotifications() {
        new UserDataStorage(getActivity()).saveNotificationsCheck(true);
        ComponentName service = new ComponentName(getActivity(), MessagingService.class);
        PackageManager pm = getActivity().getPackageManager();

        pm.setComponentEnabledSetting(service,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Message message = new Message(getActivity(), RPCMethod.TOKEN_ADD);
        message.addParam("token", FirebaseInstanceId.getInstance().getToken());
        message.addParam("model", Build.MANUFACTURER + " " + Build.MODEL);
        message.addParam("os", Build.VERSION.RELEASE);
        try {
            MessageSender.send(getActivity(), message);
        } catch (IncorrectDataException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void disableNotifications() {
        new UserDataStorage(getActivity()).saveNotificationsCheck(false);
        ComponentName service = new ComponentName(getActivity(), MessagingService.class);
        PackageManager pm = getActivity().getPackageManager();

        pm.setComponentEnabledSetting(service,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Message message = new Message(getActivity(), RPCMethod.TOKEN_DELETE);
        message.addParam("token", FirebaseInstanceId.getInstance().getToken());
        try {
            MessageSender.send(getActivity(), message);
        } catch (IncorrectDataException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enableAlarm()
    {
        ComponentName receiver = new ComponentName(getActivity(), AlarmBootReceiver.class);
        PackageManager pm = getActivity().getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableAlarm()
    {
        new UserDataStorage(getActivity()).setAlarmEnable(false);

        am.cancel(alarmIntent);

        ComponentName receiver = new ComponentName(getActivity(), AlarmBootReceiver.class);
        PackageManager pm = getActivity().getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void saveAlarmParam()
    {
        new UserDataStorage(getActivity()).setAlarmEnable(true);

        if (isNotifChnged)
            if(setNotifTB.isChecked())
                enableNotifications();
            else
                disableNotifications();

        Set<DayOfWeek> dayOfWeeks = new HashSet<>();
        if (((CheckBox)view.findViewById(R.id.checkBox1)).isChecked()) {
            dayOfWeeks.add(DayOfWeek.MONDAY);
        }

        if (((CheckBox)view.findViewById(R.id.checkBox2)).isChecked()) {
            dayOfWeeks.add(DayOfWeek.TUESDAY);
        }

        if (((CheckBox)view.findViewById(R.id.checkBox3)).isChecked()) {
            dayOfWeeks.add(DayOfWeek.WEDNESDAY);
        }

        if (((CheckBox)view.findViewById(R.id.checkBox4)).isChecked()) {
            dayOfWeeks.add(DayOfWeek.THURSDAY);
        }

        if (((CheckBox)view.findViewById(R.id.checkBox5)).isChecked()) {
            dayOfWeeks.add(DayOfWeek.FRIDAY);
        }

        if (((CheckBox)view.findViewById(R.id.checkBox6)).isChecked()) {
            dayOfWeeks.add(DayOfWeek.SATURDAY);
        }

        if (((CheckBox)view.findViewById(R.id.checkBox7)).isChecked()) {
            dayOfWeeks.add(DayOfWeek.SUNDAY);
        }

        int hourOfDay = tp.getCurrentHour();
        int minute = tp.getCurrentMinute();
        if(setAlarmTB.isChecked())
        {
            enableAlarm();
            AlarmTuner.setAlarm(getActivity(), hourOfDay, minute, alarmIntent, dayOfWeeks);
        }
        else
        {
            disableAlarm();
        }
        UserDataStorage dataStorage = new UserDataStorage(getActivity());
        dataStorage.saveAlarmParam(dayOfWeeks, hourOfDay, minute);
    }

    private void loadAlarmParam()
    {
        UserDataStorage dataStorage = new UserDataStorage(getActivity());
        int mask = DayOfWeek.getMaskByDayOfWeekList(dataStorage.loadDaysOfWeekSet());

        setAlarmTB.setChecked(dataStorage.isAlarmEnable());
        ((CheckBox)view.findViewById(R.id.checkBox1)).setChecked(DayOfWeek.MONDAY.isDayOfWeekSet(mask));
        ((CheckBox)view.findViewById(R.id.checkBox2)).setChecked(DayOfWeek.TUESDAY.isDayOfWeekSet(mask));
        ((CheckBox)view.findViewById(R.id.checkBox3)).setChecked(DayOfWeek.WEDNESDAY.isDayOfWeekSet(mask));
        ((CheckBox)view.findViewById(R.id.checkBox4)).setChecked(DayOfWeek.THURSDAY.isDayOfWeekSet(mask));
        ((CheckBox)view.findViewById(R.id.checkBox5)).setChecked(DayOfWeek.FRIDAY.isDayOfWeekSet(mask));
        ((CheckBox)view.findViewById(R.id.checkBox6)).setChecked(DayOfWeek.SATURDAY.isDayOfWeekSet(mask));
        ((CheckBox)view.findViewById(R.id.checkBox7)).setChecked(DayOfWeek.SUNDAY.isDayOfWeekSet(mask));

        int[] tmp = dataStorage.getTime();
        tp.setCurrentHour(tmp[0]);
        tp.setCurrentMinute(tmp[1]);
    }
}