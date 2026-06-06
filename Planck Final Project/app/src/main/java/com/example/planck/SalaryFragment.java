package com.example.planck;

import com.example.planck.database.Event;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.planck.database.AppDatabase;
import com.example.planck.database.Job;
import com.example.planck.database.ShiftRecord;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalaryFragment extends Fragment {

    private LinearLayout layoutJobHours;
    private TextView tvMonthTitle, tvActualPay, tvActualHours;
    private TextView tvScheduledPay, tvDiffPay;
    private LinearLayout layoutShifts;
    private Calendar currentMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salary, container, false);

        view.findViewById(R.id.hero_card).setOnClickListener(v -> showSalaryDetail());
        tvMonthTitle = view.findViewById(R.id.tv_month_title);
        tvActualPay = view.findViewById(R.id.tv_actual_pay);
        tvActualHours = view.findViewById(R.id.tv_actual_hours);
        tvScheduledPay = view.findViewById(R.id.tv_scheduled_pay);
        tvDiffPay = view.findViewById(R.id.tv_diff_pay);

        layoutJobHours = view.findViewById(R.id.layout_job_hours);
        layoutShifts = view.findViewById(R.id.layout_shifts);

        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        currentMonth.set(Calendar.HOUR_OF_DAY, 0);
        currentMonth.set(Calendar.MINUTE, 0);
        currentMonth.set(Calendar.SECOND, 0);
        currentMonth.set(Calendar.MILLISECOND, 0);

        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadMonthData();
        });
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadMonthData();
        });

        view.findViewById(R.id.btn_add_shift).setOnClickListener(v -> showAddShiftDialog());
        view.findViewById(R.id.fab_add_job).setOnClickListener(v -> showAddJobDialog());

        loadMonthData();
        return view;
    }

    private void loadMonthData() {
        SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy年 M月", Locale.TAIWAN);
        tvMonthTitle.setText(monthFmt.format(currentMonth.getTime()));

        long monthStart = currentMonth.getTimeInMillis();
        Calendar endCal = (Calendar) currentMonth.clone();
        endCal.add(Calendar.MONTH, 1);
        long monthEnd = endCal.getTimeInMillis();

        AppDatabase db = AppDatabase.getInstance(requireContext());

        db.shiftDao().getShiftsByMonth(monthStart, monthEnd)
                .observe(getViewLifecycleOwner(), shifts -> {
                    layoutShifts.removeAllViews();


                    float totalActualHours = 0;
                    int totalActualPay = 0;
                    int totalScheduledPay = 0;

                    for (ShiftRecord shift : shifts) {
                        addShiftRow(getLayoutInflater(), shift);
                        if (shift.isConfirmed && shift.actualEnd > 0) {
                            totalActualHours += shift.actualHours;
                            totalActualPay += shift.actualPay;
                        }
                        long scheduledMillis = shift.scheduledEnd - shift.scheduledStart;
                        float scheduledHours = scheduledMillis / 3600000f;
                        totalScheduledPay += (int)(scheduledHours * shift.hourlyRate);
                    }

                    tvActualHours.setText(String.format(Locale.TAIWAN, "%.1f hr", totalActualHours));
                    tvActualPay.setText("$" + totalActualPay);
                    tvScheduledPay.setText("$" + totalScheduledPay);
                    int diff = totalActualPay - totalScheduledPay;
                    tvDiffPay.setText((diff >= 0 ? "+" : "") + "$" + diff);
                });

        db.shiftDao().getShiftsByMonth(monthStart, monthEnd)
                .observe(getViewLifecycleOwner(), allShifts -> {
                    // 計算每份工作的時數和班數
                    java.util.Map<String, float[]> jobStats = new java.util.LinkedHashMap<>();

                    for (ShiftRecord shift : allShifts) {
                        String key = shift.jobName;
                        if (!jobStats.containsKey(key)) {
                            jobStats.put(key, new float[]{0, 0}); // [時數, 班數]
                        }
                        float[] stats = jobStats.get(key);
                        if (shift.isConfirmed && shift.actualHours > 0) {
                            stats[0] += shift.actualHours;
                        } else {
                            float scheduledHours = (shift.scheduledEnd - shift.scheduledStart) / 3600000f;
                            stats[0] += scheduledHours;
                        }
                        stats[1]++;
                    }

                    // 更新 UI
                    layoutJobHours.removeAllViews();
                    if (jobStats.isEmpty()) {
                        // 沒有班次時顯示提示
                        TextView emptyView = new TextView(requireContext());
                        emptyView.setText("本月尚無班次");
                        emptyView.setTextSize(13);
                        emptyView.setPadding(14, 14, 14, 14);
                        emptyView.setTextColor(getResources().getColor(R.color.text_secondary, null));
                        layoutJobHours.addView(emptyView);
                    } else {
                        for (java.util.Map.Entry<String, float[]> entry : jobStats.entrySet()) {
                            View card = getLayoutInflater().inflate(
                                    R.layout.item_job_hours, layoutJobHours, false);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.weight = 1;
                            params.setMarginEnd((int)(8 * getResources().getDisplayMetrics().density));
                            card.setLayoutParams(params);

                            TextView tvJobName = card.findViewById(R.id.tv_job_name);
                            TextView tvJobHours = card.findViewById(R.id.tv_job_hours);
                            TextView tvJobShifts = card.findViewById(R.id.tv_job_shifts);

                            tvJobName.setText(entry.getKey());
                            tvJobHours.setText(String.format(Locale.TAIWAN,
                                    "%.1f hr", entry.getValue()[0]));
                            tvJobShifts.setText((int)entry.getValue()[1] + " 班");

                            layoutJobHours.addView(card);

                        }
                    }
                });
    }

    private void addShiftRow(LayoutInflater inflater, ShiftRecord shift) {
        View row = inflater.inflate(R.layout.item_shift_row, layoutShifts, false);

        TextView tvDate = row.findViewById(R.id.tv_shift_date);
        TextView tvScheduled = row.findViewById(R.id.tv_shift_scheduled);
        TextView tvActual = row.findViewById(R.id.tv_shift_actual);
        TextView tvPay = row.findViewById(R.id.tv_shift_pay);
        TextView tvDiff = row.findViewById(R.id.tv_shift_diff);
        View dot = row.findViewById(R.id.view_shift_dot);

        SimpleDateFormat dateFmt = new SimpleDateFormat("M月d日 EEEE", Locale.TAIWAN);
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.TAIWAN);

        tvDate.setText(shift.jobName + " · " + dateFmt.format(new Date(shift.scheduledStart)));

        float scheduledHours = (shift.scheduledEnd - shift.scheduledStart) / 3600000f;
        tvScheduled.setText(String.format("預計 %s–%s · %.0fhr",
                timeFmt.format(new Date(shift.scheduledStart)),
                timeFmt.format(new Date(shift.scheduledEnd)),
                scheduledHours));

        if (shift.actualEnd > 0) {
            tvActual.setText(String.format("實際 %s–%s · %.1fhr",
                    timeFmt.format(new Date(shift.actualStart)),
                    timeFmt.format(new Date(shift.actualEnd)),
                    shift.actualHours));
            tvActual.setTextColor(getResources().getColor(
                    shift.isConfirmed ? R.color.planck_green : R.color.planck_orange, null));
        } else {
            tvActual.setText("尚未打卡");
        }

        int scheduledPay = (int)(scheduledHours * shift.hourlyRate);
        tvPay.setText("$" + (shift.isConfirmed ? shift.actualPay : scheduledPay));

        if (shift.isConfirmed) {
            int diff = shift.actualPay - scheduledPay;
            if (diff > 0) {
                tvDiff.setText("+" + diff);
                tvDiff.setBackgroundResource(R.drawable.badge_green);
                tvDiff.setTextColor(getResources().getColor(R.color.planck_green, null));
            } else if (diff < 0) {
                tvDiff.setText(String.valueOf(diff));
                tvDiff.setBackgroundResource(R.drawable.badge_red);
                tvDiff.setTextColor(getResources().getColor(R.color.planck_red, null));
            } else {
                tvDiff.setText("$0");
            }
            dot.getBackground().setTint(getResources().getColor(R.color.planck_green, null));
        } else {
            tvDiff.setText("待確認");
            tvDiff.setBackgroundResource(R.drawable.badge_orange);
            tvDiff.setTextColor(getResources().getColor(R.color.planck_orange, null));
            dot.getBackground().setTint(getResources().getColor(R.color.planck_purple, null));
        }

        row.setOnClickListener(v -> showUpdateShiftDialog(shift));
        layoutShifts.addView(row);
    }

    private void showSalaryDetail() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            long monthStart = currentMonth.getTimeInMillis();
            Calendar endCal = (Calendar) currentMonth.clone();
            endCal.add(Calendar.MONTH, 1);
            long monthEnd = endCal.getTimeInMillis();

            // 取得當月所有班次
            List<ShiftRecord> shifts = db.shiftDao()
                    .getShiftsByMonthDirect(monthStart, monthEnd);

            if (shifts == null || shifts.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        android.widget.Toast.makeText(requireContext(),
                                "本月尚無班次紀錄",
                                android.widget.Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // 依工作分組
            java.util.Map<Integer, List<ShiftRecord>> jobShifts =
                    new java.util.LinkedHashMap<>();
            java.util.Map<Integer, Job> jobMap = new java.util.LinkedHashMap<>();

            for (ShiftRecord shift : shifts) {
                if (!jobShifts.containsKey(shift.jobId)) {
                    jobShifts.put(shift.jobId, new java.util.ArrayList<>());
                    Job job = db.jobDao().getJobById(shift.jobId);
                    if (job != null) jobMap.put(shift.jobId, job);
                }
                jobShifts.get(shift.jobId).add(shift);
            }

            SimpleDateFormat monthFmt =
                    new SimpleDateFormat("yyyy年 M月", Locale.TAIWAN);
            String monthStr = monthFmt.format(currentMonth.getTime());

            requireActivity().runOnUiThread(() -> {
                // 每份工作跳出一個 Dialog
                for (java.util.Map.Entry<Integer, List<ShiftRecord>> entry
                        : jobShifts.entrySet()) {

                    Job job = jobMap.get(entry.getKey());
                    if (job == null) continue;
                    List<ShiftRecord> jobShiftList = entry.getValue();

                    // 計算統計
                    float totalHours = 0;
                    int totalGross = 0;
                    int shiftCount = jobShiftList.size();

                    for (ShiftRecord shift : jobShiftList) {
                        if (shift.isConfirmed && shift.actualHours > 0) {
                            totalHours += shift.actualHours;
                            // 計算含假日加成的薪資
                            float rate = shift.hourlyRate;
                            if (shift.isHoliday) rate *= 1.33f;
                            totalGross += (int)(shift.actualHours * rate);
                        } else {
                            float scheduledHours =
                                    (shift.scheduledEnd - shift.scheduledStart) / 3600000f;
                            totalHours += scheduledHours;
                            totalGross += (int)(scheduledHours * shift.hourlyRate);
                        }
                    }

                    final float finalHours = totalHours;
                    final int finalGross = totalGross;

                    // 顯示 Dialog
                    Dialog dialog = new Dialog(requireContext());
                    dialog.setContentView(R.layout.dialog_salary_detail);
                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawableResource(
                            android.R.color.transparent);

                    TextView tvTitle = dialog.findViewById(R.id.tv_detail_title);
                    TextView tvMonth = dialog.findViewById(R.id.tv_detail_month);
                    TextView tvShifts = dialog.findViewById(R.id.tv_detail_shifts);
                    TextView tvHours = dialog.findViewById(R.id.tv_detail_hours);
                    TextView tvRate = dialog.findViewById(R.id.tv_detail_rate);
                    TextView tvGross = dialog.findViewById(R.id.tv_detail_gross);
                    TextView tvNet = dialog.findViewById(R.id.tv_detail_net);
                    com.google.android.material.textfield.TextInputEditText etLabor =
                            dialog.findViewById(R.id.et_labor);
                    com.google.android.material.textfield.TextInputEditText etHealth =
                            dialog.findViewById(R.id.et_health);

                    tvTitle.setText(job.name + " 薪資明細");
                    tvMonth.setText(monthStr);
                    tvShifts.setText(shiftCount + " 班");
                    tvHours.setText(String.format(Locale.TAIWAN, "%.1f hr", finalHours));
                    tvRate.setText("$" + job.hourlyRate);
                    tvGross.setText("$" + finalGross);
                    tvNet.setText("$" + finalGross);

                    // 即時計算實領薪資
                    android.text.TextWatcher watcher = new android.text.TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                        @Override
                        public void afterTextChanged(android.text.Editable s) {
                            int labor = 0, health = 0;
                            try {
                                String laborStr = etLabor.getText() != null ?
                                        etLabor.getText().toString() : "0";
                                String healthStr = etHealth.getText() != null ?
                                        etHealth.getText().toString() : "0";
                                labor = laborStr.isEmpty() ? 0 : Integer.parseInt(laborStr);
                                health = healthStr.isEmpty() ? 0 : Integer.parseInt(healthStr);
                            } catch (NumberFormatException ignored) {}
                            int net = finalGross - labor - health;
                            tvNet.setText("$" + net);
                        }
                    };

                    etLabor.addTextChangedListener(watcher);
                    etHealth.addTextChangedListener(watcher);

                    dialog.findViewById(R.id.btn_close)
                            .setOnClickListener(v -> dialog.dismiss());

                    dialog.show();
                }
            });
        }).start();
    }
    private void showAddJobDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_job);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText etName = dialog.findViewById(R.id.et_job_name);
        TextInputEditText etRate = dialog.findViewById(R.id.et_hourly_rate);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String rateStr = etRate.getText() != null ? etRate.getText().toString().trim() : "";
            if (name.isEmpty()) { etName.setError("請輸入工作名稱"); return; }
            if (rateStr.isEmpty()) { etRate.setError("請輸入時薪"); return; }
            new Thread(() -> {
                Job job = new Job();
                job.name = name;
                job.hourlyRate = Integer.parseInt(rateStr);
                job.holidayMultiplier = 1.33f;
                job.overtimeThreshold = 8.0f;
                job.overtimeMultiplier = 1.33f;
                job.color = "#534AB7";
                AppDatabase.getInstance(requireContext()).jobDao().insert(job);
            }).start();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showAddShiftDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_shift);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvDate = dialog.findViewById(R.id.et_shift_date);
        TextView tvStart = dialog.findViewById(R.id.et_shift_start);
        TextView tvEnd = dialog.findViewById(R.id.et_shift_end);
        TextView tvSelectedJob = dialog.findViewById(R.id.tv_selected_job);
        TextView tvDayType = dialog.findViewById(R.id.tv_day_type); // 新增顯示班別

        int[] selectedDate = {
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        };
        int[] selectedStart = {9, 0};
        int[] selectedEnd = {18, 0};
        Job[] selectedJob = {null};

        // 更新班別顯示
        Runnable updateDayType = () -> {
            Calendar cal = Calendar.getInstance();
            cal.set(selectedDate[0], selectedDate[1], selectedDate[2], 0, 0, 0);
            String label = SalaryCalculator.getDayTypeLabel(cal.getTimeInMillis());
            tvDayType.setText("班別：" + label);
        };

        tvDate.setText(String.format(Locale.TAIWAN, "%d/%02d/%02d",
                selectedDate[0], selectedDate[1] + 1, selectedDate[2]));
        tvStart.setText(String.format(Locale.TAIWAN, "%02d:%02d",
                selectedStart[0], selectedStart[1]));
        tvEnd.setText(String.format(Locale.TAIWAN, "%02d:%02d",
                selectedEnd[0], selectedEnd[1]));
        updateDayType.run();

        // 選擇工作
        tvSelectedJob.setOnClickListener(v -> {
            new Thread(() -> {
                List<Job> jobs = AppDatabase.getInstance(requireContext())
                        .jobDao().getAllJobsDirect();
                if (jobs == null || jobs.isEmpty()) {
                    requireActivity().runOnUiThread(() ->
                            android.widget.Toast.makeText(requireContext(),
                                    "請先新增工作", android.widget.Toast.LENGTH_SHORT).show());
                    return;
                }
                String[] jobNames = new String[jobs.size()];
                for (int i = 0; i < jobs.size(); i++) {
                    jobNames[i] = jobs.get(i).name +
                            "（時薪 $" + jobs.get(i).hourlyRate + "）";
                }
                requireActivity().runOnUiThread(() ->
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("選擇工作")
                                .setItems(jobNames, (d, which) -> {
                                    selectedJob[0] = jobs.get(which);
                                    tvSelectedJob.setText(jobNames[which]);
                                }).show()
                );
            }).start();
        });

        // 選擇日期
        tvDate.setOnClickListener(v ->
                new DatePickerDialog(requireContext(),
                        (dp, year, month, day) -> {
                            selectedDate[0] = year;
                            selectedDate[1] = month;
                            selectedDate[2] = day;
                            tvDate.setText(String.format(Locale.TAIWAN,
                                    "%d/%02d/%02d", year, month + 1, day));
                            updateDayType.run(); // 更新班別
                        }, selectedDate[0], selectedDate[1], selectedDate[2]).show()
        );

        tvStart.setOnClickListener(v ->
                showTimePickerDialog("上班時間", selectedStart[0], selectedStart[1],
                        (hour, minute) -> {
                            selectedStart[0] = hour; selectedStart[1] = minute;
                            tvStart.setText(String.format(Locale.TAIWAN, "%02d:%02d", hour, minute));
                        })
        );

        tvEnd.setOnClickListener(v ->
                showTimePickerDialog("下班時間", selectedEnd[0], selectedEnd[1],
                        (hour, minute) -> {
                            selectedEnd[0] = hour; selectedEnd[1] = minute;
                            tvEnd.setText(String.format(Locale.TAIWAN, "%02d:%02d", hour, minute));
                        })
        );

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            if (selectedJob[0] == null) {
                android.widget.Toast.makeText(requireContext(),
                        "請先選擇工作", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            Job job = selectedJob[0];
            new Thread(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(requireContext());
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(selectedDate[0], selectedDate[1], selectedDate[2],
                            selectedStart[0], selectedStart[1], 0);
                    startCal.set(Calendar.MILLISECOND, 0);

                    Calendar endCal = Calendar.getInstance();
                    endCal.set(selectedDate[0], selectedDate[1], selectedDate[2],
                            selectedEnd[0], selectedEnd[1], 0);
                    endCal.set(Calendar.MILLISECOND, 0);

                    // 自動判斷班別
                    SalaryCalculator.DayType dayType =
                            SalaryCalculator.getDayType(startCal.getTimeInMillis());
                    boolean isHoliday = (dayType != SalaryCalculator.DayType.WEEKDAY);

                    ShiftRecord shift = new ShiftRecord();
                    shift.jobId = job.id;
                    shift.jobName = job.name;
                    shift.hourlyRate = job.hourlyRate;
                    shift.scheduledStart = startCal.getTimeInMillis();
                    shift.scheduledEnd = endCal.getTimeInMillis();
                    shift.actualStart = 0;
                    shift.actualEnd = 0;
                    shift.actualHours = 0;
                    shift.actualPay = 0;
                    shift.isHoliday = isHoliday;
                    shift.isConfirmed = false;
                    db.shiftDao().insert(shift);

                    // 同步到行程
                    Event event = new Event();
                    event.title = job.name + "（打工）";
                    event.type = "SHIFT";
                    event.priority = 0;
                    event.startTime = startCal.getTimeInMillis();
                    event.endTime = endCal.getTimeInMillis();
                    event.bufferMinutes = 0;
                    event.deadlineTime = 0;
                    event.note = "時薪 $" + job.hourlyRate + "・" +
                            SalaryCalculator.getDayTypeLabel(startCal.getTimeInMillis());
                    event.jobId = job.id;
                    db.eventDao().insert(event);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showUpdateShiftDialog(ShiftRecord shift) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_update_shift);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvActualStart = dialog.findViewById(R.id.et_actual_start);
        TextView tvActualEnd = dialog.findViewById(R.id.et_actual_end);

        Calendar defCal = Calendar.getInstance();
        defCal.setTimeInMillis(shift.scheduledStart);
        int[] actualStart = {defCal.get(Calendar.HOUR_OF_DAY), defCal.get(Calendar.MINUTE)};

        Calendar defEndCal = Calendar.getInstance();
        defEndCal.setTimeInMillis(shift.scheduledEnd);
        int[] actualEnd = {defEndCal.get(Calendar.HOUR_OF_DAY), defEndCal.get(Calendar.MINUTE)};

        if (shift.actualStart > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(shift.actualStart);
            actualStart[0] = c.get(Calendar.HOUR_OF_DAY);
            actualStart[1] = c.get(Calendar.MINUTE);
        }
        if (shift.actualEnd > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(shift.actualEnd);
            actualEnd[0] = c.get(Calendar.HOUR_OF_DAY);
            actualEnd[1] = c.get(Calendar.MINUTE);
        }

        tvActualStart.setText(String.format(Locale.TAIWAN, "%02d:%02d",
                actualStart[0], actualStart[1]));
        tvActualEnd.setText(String.format(Locale.TAIWAN, "%02d:%02d",
                actualEnd[0], actualEnd[1]));

        tvActualStart.setOnClickListener(v ->
                showTimePickerDialog("實際上班時間", actualStart[0], actualStart[1],
                        (hour, minute) -> {
                            actualStart[0] = hour;
                            actualStart[1] = minute;
                            tvActualStart.setText(String.format(Locale.TAIWAN, "%02d:%02d", hour, minute));
                        })
        );

        tvActualEnd.setOnClickListener(v ->
                showTimePickerDialog("實際下班時間", actualEnd[0], actualEnd[1],
                        (hour, minute) -> {
                            actualEnd[0] = hour;
                            actualEnd[1] = minute;
                            tvActualEnd.setText(String.format(Locale.TAIWAN, "%02d:%02d", hour, minute));
                        })
        );

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTimeInMillis(shift.scheduledStart);
                    startCal.set(Calendar.HOUR_OF_DAY, actualStart[0]);
                    startCal.set(Calendar.MINUTE, actualStart[1]);
                    startCal.set(Calendar.SECOND, 0);

                    Calendar endCal = Calendar.getInstance();
                    endCal.setTimeInMillis(shift.scheduledStart);
                    endCal.set(Calendar.HOUR_OF_DAY, actualEnd[0]);
                    endCal.set(Calendar.MINUTE, actualEnd[1]);
                    endCal.set(Calendar.SECOND, 0);

                    shift.actualStart = startCal.getTimeInMillis();
                    shift.actualEnd = endCal.getTimeInMillis();
                    shift.actualHours = (shift.actualEnd - shift.actualStart) / 3600000f;

                    float rate = shift.hourlyRate;
                    if (shift.isHoliday) rate *= 1.33f;
                    int grossPay = (int)(shift.actualHours * rate);

                    int laborInsurance = (int)(grossPay * 0.02215f);
                    int healthInsurance = (int)(grossPay * 0.0235f);
                    // 原本的計算邏輯全部換成這一行
                    shift.actualPay = SalaryCalculator.calculate(
                            shift.hourlyRate, shift.actualStart, shift.actualEnd);
                    shift.isConfirmed = true;

// 薪資明細顯示也更新
//                    float rate = shift.hourlyRate;
//                    int grossPay = shift.actualPay;
                    String dayTypeLabel = SalaryCalculator.getDayTypeLabel(shift.actualStart);

                    requireActivity().runOnUiThread(() -> {
                        String msg = String.format(Locale.TAIWAN,
                                "班別：%s\n實際時數：%.1f hr\n實領薪資：$%d",
                                dayTypeLabel, shift.actualHours, grossPay);
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("薪資明細")
                                .setMessage(msg)
                                .setPositiveButton("確認", null)
                                .show();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTimePickerDialog(String title, int defaultHour, int defaultMinute,
                                      OnTimeSelectedListener listener) {
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
        pickerHour.setFormatter(value -> String.format(Locale.TAIWAN, "%02d", value));

        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(59);
        pickerMinute.setValue(defaultMinute);
        pickerMinute.setFormatter(value -> String.format(Locale.TAIWAN, "%02d", value));

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            listener.onTimeSelected(pickerHour.getValue(), pickerMinute.getValue());
            dialog.dismiss();
        });

        dialog.show();
    }


}