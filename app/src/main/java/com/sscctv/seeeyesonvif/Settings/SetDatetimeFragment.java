package com.sscctv.seeeyesonvif.Settings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;


import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.databinding.FragmentSettingsDatetimeBinding;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class SetDatetimeFragment extends Fragment {

    private static final String TAG = SetDatetimeFragment.class.getSimpleName();
    private TinyDB tinyDB;
    private EditText id, pw, domain;
    private TextView txtSpeaker;
    String[] splitDate, splitTime;
    private FragmentSettingsDatetimeBinding layout;

    static SetDatetimeFragment newInstance() {
        return new SetDatetimeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        getDateAndTime();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_settings_datetime, container, false);

        tinyDB = new TinyDB(getContext());

        layout.swAuto.setChecked(Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1);
        layout.swAuto.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.AUTO_TIME, 1);
                layout.btnSetDate.setEnabled(false);
                layout.btnSetTime.setEnabled(false);
                layout.btnSetGmt.setEnabled(false);

//                ((ActivityMain)MainActivity.context).restartTimer();

            } else {
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.AUTO_TIME, 0);
                layout.btnSetDate.setEnabled(true);
                layout.btnSetTime.setEnabled(true);
                layout.btnSetGmt.setEnabled(true);
            }

        });

        if(layout.swAuto.isChecked()){
            layout.btnSetDate.setEnabled(false);
            layout.btnSetTime.setEnabled(false);
            layout.btnSetGmt.setEnabled(false);
        } else {
            layout.btnSetDate.setEnabled(true);
            layout.btnSetTime.setEnabled(true);
            layout.btnSetGmt.setEnabled(true);
        }

        layout.swHour.setChecked(Settings.System.getString(getActivity().getContentResolver(), Settings.System.TIME_12_24).equals("24"));
        layout.swHour.setOnCheckedChangeListener(((compoundButton, b) -> {
            if (b) {
                Settings.System.putString(getActivity().getContentResolver(), Settings.System.TIME_12_24, "24");
            } else {
                Settings.System.putString(getActivity().getContentResolver(), Settings.System.TIME_12_24, "12");
            }
        }));


        layout.btnSetDate.setOnClickListener(view -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy%M%d", Locale.getDefault());
            splitDate = dateFormat.format(refreshMillis()).split("%");
            DatePickerDialog dialog = new DatePickerDialog(Objects.requireNonNull(getContext()), listener, Integer.valueOf(splitDate[0]), Integer.valueOf(splitDate[1]) - 1, Integer.valueOf(splitDate[2]));
            dialog.show();


        });

        layout.btnSetTime.setOnClickListener(view -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH%mm", Locale.getDefault());
            splitTime = timeFormat.format(refreshMillis()).split("%");
            TimePickerDialog dialog = new TimePickerDialog(getContext(), timeListener, Integer.valueOf(splitTime[0]), Integer.valueOf(splitTime[1]), false);
            dialog.show();

        });
//
        layout.btnSetDate.setText(AppUtils.currentDate());
        layout.btnSetTime.setText(AppUtils.currentTime());
        return layout.getRoot();
    }


    private DatePickerDialog.OnDateSetListener listener = (datePicker, i, i1, i2) -> {
        Log.d(TAG, i + "년 " + i1 + "월 " + i2 + "일");
        Calendar c = Calendar.getInstance();
        c.set(i, i1, i2);
        android.os.SystemClock.setCurrentTimeMillis(c.getTimeInMillis());
//        ((MainActivity)MainActivity.context).restartTimer();
        layout.btnSetDate.setText(AppUtils.currentDate());

//        refreshMillis();

    };

    private TimePickerDialog.OnTimeSetListener timeListener = ((timePicker, i, i1) -> {
        Calendar c = Calendar.getInstance();
        c.set(Integer.valueOf(splitDate[0]), Integer.valueOf(splitDate[1]) - 1, Integer.valueOf(splitDate[2]), i, i1);
        android.os.SystemClock.setCurrentTimeMillis(c.getTimeInMillis());
//        ((MainActivity)MainActivity.context).restartTimer();
        layout.btnSetTime.setText(AppUtils.currentTime());

//        refreshMillis();
    });

    private Date refreshMillis() {
//        getDateAndTime();
        Log.d(TAG, "Refresh: " + splitDate[0] + " | " + splitDate[1] + " | " + splitDate[2] + ", " + splitTime[0] + " | " + splitTime[1]);
        return new Date(System.currentTimeMillis());
    }

    private void getDateAndTime() {

        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy%M%d", Locale.getDefault());
        splitDate = dateFormat.format(date).split("%");

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH%mm", Locale.getDefault());
        splitTime = timeFormat.format(date).split("%");


    }
}