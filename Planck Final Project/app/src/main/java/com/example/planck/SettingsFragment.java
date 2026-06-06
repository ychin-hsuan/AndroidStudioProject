package com.example.planck;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "planck_settings";
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);

        // 緩衝時間 Spinner
        Spinner spinnerBuffer = view.findViewById(R.id.spinner_buffer);
        String[] bufferOptions = {"0 分鐘", "10 分鐘", "15 分鐘", "30 分鐘"};
        int[] bufferValues = {0, 10, 15, 30};
        ArrayAdapter<String> bufferAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                bufferOptions
        );
        bufferAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuffer.setAdapter(bufferAdapter);

        // 讀取已儲存的設定
        int savedBuffer = prefs.getInt("default_buffer", 15);
        for (int i = 0; i < bufferValues.length; i++) {
            if (bufferValues[i] == savedBuffer) {
                spinnerBuffer.setSelection(i);
                break;
            }
        }

        // 儲存緩衝時間
        spinnerBuffer.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                prefs.edit().putInt("default_buffer", bufferValues[pos]).apply();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 活動開始時間
        TextView tvDayStart = view.findViewById(R.id.tv_day_start);
        int savedStartHour = prefs.getInt("day_start_hour", 8);
        tvDayStart.setText(String.format(Locale.TAIWAN, "%02d:00", savedStartHour));
        tvDayStart.setOnClickListener(v -> showHourPicker("活動開始時間", savedStartHour, hour -> {
            prefs.edit().putInt("day_start_hour", hour).apply();
            tvDayStart.setText(String.format(Locale.TAIWAN, "%02d:00", hour));
        }));

        // 活動結束時間
        TextView tvDayEnd = view.findViewById(R.id.tv_day_end);
        int savedEndHour = prefs.getInt("day_end_hour", 22);
        tvDayEnd.setText(String.format(Locale.TAIWAN, "%02d:00", savedEndHour));
        tvDayEnd.setOnClickListener(v -> showHourPicker("活動結束時間", savedEndHour, hour -> {
            prefs.edit().putInt("day_end_hour", hour).apply();
            tvDayEnd.setText(String.format(Locale.TAIWAN, "%02d:00", hour));
        }));

        // 上班提醒開關
        SwitchMaterial switchNotify = view.findViewById(R.id.switch_shift_notify);
        switchNotify.setChecked(prefs.getBoolean("notify_before_shift", true));
        switchNotify.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean("notify_before_shift", isChecked).apply()
        );

        // 提前通知時間 Spinner
        Spinner spinnerNotify = view.findViewById(R.id.spinner_notify_minutes);
        String[] notifyOptions = {"15 分鐘前", "30 分鐘前", "60 分鐘前"};
        int[] notifyValues = {15, 30, 60};
        ArrayAdapter<String> notifyAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                notifyOptions
        );
        notifyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNotify.setAdapter(notifyAdapter);

        int savedNotify = prefs.getInt("notify_minutes_before", 30);
        for (int i = 0; i < notifyValues.length; i++) {
            if (notifyValues[i] == savedNotify) {
                spinnerNotify.setSelection(i);
                break;
            }
        }

        spinnerNotify.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                prefs.edit().putInt("notify_minutes_before", notifyValues[pos]).apply();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        return view;
    }

    // 只選小時的 Picker
    private void showHourPicker(String title, int defaultHour,
                                OnHourSelectedListener listener) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_time_picker);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = dialog.findViewById(R.id.tv_time_picker_title);
        NumberPicker pickerHour = dialog.findViewById(R.id.picker_hour);
        NumberPicker pickerMinute = dialog.findViewById(R.id.picker_minute);

        tvTitle.setText(title);

        pickerHour.setMinValue(0);
        pickerHour.setMaxValue(23);
        pickerHour.setValue(defaultHour);
        pickerHour.setFormatter(v -> String.format(Locale.TAIWAN, "%02d", v));

        // 設定頁只選小時，分鐘固定為 00
        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(0);
        pickerMinute.setValue(0);
        pickerMinute.setDisplayedValues(new String[]{"00"});

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            listener.onHourSelected(pickerHour.getValue());
            dialog.dismiss();
        });

        dialog.show();
    }

    interface OnHourSelectedListener {
        void onHourSelected(int hour);
    }
}