package com.example.planck;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.planck.database.AppDatabase;
import com.example.planck.database.Event;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private TextView tvCurrentDate;
    private TimelineView timelineView;
    private Calendar currentDay;
    private float density;
    private View scrollDay;
    private View scrollWeek;
    private LinearLayout weekContainer;
    private LinearLayout monthContainer;
    private GridLayout gridCalendar;
    private Calendar currentMonth;
    private TextView tvMonthLabel;
    private TextView tvSelectedDate;
    private View cardSelectedEvents;
    private LinearLayout layoutSelectedEvents;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        density = getResources().getDisplayMetrics().density;
        tvCurrentDate = view.findViewById(R.id.tv_current_date);
        timelineView = view.findViewById(R.id.timeline_view);
        currentDay = Calendar.getInstance();
        scrollDay = view.findViewById(R.id.scroll_day);
        scrollWeek = view.findViewById(R.id.scroll_week);
        weekContainer = view.findViewById(R.id.week_container);
        monthContainer = view.findViewById(R.id.month_container);
        gridCalendar = view.findViewById(R.id.grid_calendar);
        tvMonthLabel = view.findViewById(R.id.tv_month_label);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        cardSelectedEvents = view.findViewById(R.id.card_selected_events);
        layoutSelectedEvents = view.findViewById(R.id.layout_selected_events);
        currentMonth = Calendar.getInstance();

        // 建立時間標籤
        LinearLayout layoutTimeLabels = view.findViewById(R.id.layout_time_labels);
        for (int hour = 6; hour <= 23; hour++) {
            TextView tvHour = new TextView(requireContext());
            tvHour.setText(String.format(Locale.TAIWAN, "%02d:00", hour));
            tvHour.setTextSize(10);
            tvHour.setTextColor(getResources().getColor(R.color.text_secondary, null));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int)(64 * density));
            params.topMargin = (int)(8 * density);
            tvHour.setLayoutParams(params);
            layoutTimeLabels.addView(tvHour);
        }

        // Tab 切換
        TextView tabDay = view.findViewById(R.id.tab_day);
        TextView tabWeek = view.findViewById(R.id.tab_week);
        TextView tabMonth = view.findViewById(R.id.tab_month);

        tabDay.setOnClickListener(v -> {
            tabDay.setBackgroundResource(R.drawable.tab_selected);
            tabDay.setTextColor(getResources().getColor(android.R.color.white, null));
            tabWeek.setBackgroundResource(R.drawable.tab_unselected);
            tabWeek.setTextColor(getResources().getColor(R.color.text_secondary, null));
            tabMonth.setBackgroundResource(R.drawable.tab_unselected);
            tabMonth.setTextColor(getResources().getColor(R.color.text_secondary, null));
            scrollDay.setVisibility(View.VISIBLE);
            scrollWeek.setVisibility(View.GONE);
            monthContainer.setVisibility(View.GONE);
        });

        tabWeek.setOnClickListener(v -> {
            tabWeek.setBackgroundResource(R.drawable.tab_selected);
            tabWeek.setTextColor(getResources().getColor(android.R.color.white, null));
            tabDay.setBackgroundResource(R.drawable.tab_unselected);
            tabDay.setTextColor(getResources().getColor(R.color.text_secondary, null));
            tabMonth.setBackgroundResource(R.drawable.tab_unselected);
            tabMonth.setTextColor(getResources().getColor(R.color.text_secondary, null));
            scrollDay.setVisibility(View.GONE);
            scrollWeek.setVisibility(View.VISIBLE);
            monthContainer.setVisibility(View.GONE);
            loadWeekEvents();
        });

        tabMonth.setOnClickListener(v -> {
            tabMonth.setBackgroundResource(R.drawable.tab_selected);
            tabMonth.setTextColor(getResources().getColor(android.R.color.white, null));
            tabDay.setBackgroundResource(R.drawable.tab_unselected);
            tabDay.setTextColor(getResources().getColor(R.color.text_secondary, null));
            tabWeek.setBackgroundResource(R.drawable.tab_unselected);
            tabWeek.setTextColor(getResources().getColor(R.color.text_secondary, null));
            scrollDay.setVisibility(View.GONE);
            scrollWeek.setVisibility(View.GONE);
            monthContainer.setVisibility(View.VISIBLE);
            loadMonthCalendar();
        });

        // 月份導覽
        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadMonthCalendar();
        });
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadMonthCalendar();
        });

        // 上下日切換
        view.findViewById(R.id.btn_prev_day).setOnClickListener(v -> {
            currentDay.add(Calendar.DAY_OF_MONTH, -1);
            loadDayEvents();
        });
        view.findViewById(R.id.btn_next_day).setOnClickListener(v -> {
            currentDay.add(Calendar.DAY_OF_MONTH, 1);
            loadDayEvents();
        });

        // 新增事件
        view.findViewById(R.id.fab_add_event).setOnClickListener(v ->
                showAddEventDialog()
        );

        loadDayEvents();
        return view;
    }

    private void loadDayEvents() {
        SimpleDateFormat dateFmt = new SimpleDateFormat("M月d日，EEEE", Locale.TAIWAN);
        tvCurrentDate.setText(dateFmt.format(currentDay.getTime()));

        Calendar dayStart = (Calendar) currentDay.clone();
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);

        Calendar dayEnd = (Calendar) dayStart.clone();
        dayEnd.add(Calendar.DAY_OF_MONTH, 1);

        AppDatabase.getInstance(requireContext())
                .eventDao()
                .getEventsByDay(dayStart.getTimeInMillis(), dayEnd.getTimeInMillis())
                .observe(getViewLifecycleOwner(), events ->
                        timelineView.setEvents(events)
                );
    }

    private void loadWeekEvents() {
        weekContainer.removeAllViews();

        SharedPreferences prefs =
                requireContext().getSharedPreferences("planck_settings", 0);
        int dayStartHour = prefs.getInt("day_start_hour", 8);
        int dayEndHour = prefs.getInt("day_end_hour", 22);
        long totalDayMs = (dayEndHour - dayStartHour) * 3600000L;

        Calendar weekStart = Calendar.getInstance();
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat dayNameFmt = new SimpleDateFormat("EEE", Locale.TAIWAN);
        SimpleDateFormat dayNumFmt = new SimpleDateFormat("d", Locale.TAIWAN);

        // 先加入 7 個佔位 View
        for (int i = 0; i < 7; i++) {
            View placeholder = new View(requireContext());
            placeholder.setTag("day_" + i);
            weekContainer.addView(placeholder);
        }

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) weekStart.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            Calendar dayEnd = (Calendar) day.clone();
            dayEnd.add(Calendar.DAY_OF_MONTH, 1);

            final int dayIndex = i;
            final Calendar finalDay = (Calendar) day.clone();

            AppDatabase.getInstance(requireContext())
                    .eventDao()
                    .getEventsByDay(day.getTimeInMillis(), dayEnd.getTimeInMillis())
                    .observe(getViewLifecycleOwner(), events -> {

                        View row = getLayoutInflater().inflate(
                                R.layout.item_week_day, weekContainer, false);

                        TextView tvDayName = row.findViewById(R.id.tv_week_day_name);
                        TextView tvDayNum = row.findViewById(R.id.tv_week_day_num);
                        ProgressBar progressFree = row.findViewById(R.id.progress_free);
                        LinearLayout layoutPills = row.findViewById(R.id.layout_event_pills);
                        TextView tvFreeHours = row.findViewById(R.id.tv_free_hours);

                        tvDayName.setText(dayNameFmt.format(finalDay.getTime()));
                        tvDayNum.setText(dayNumFmt.format(finalDay.getTime()));

                        // 今天標示
                        Calendar today = Calendar.getInstance();
                        if (finalDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                                && finalDay.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                            tvDayNum.setBackgroundResource(R.drawable.circle_today);
                            tvDayNum.setTextColor(
                                    getResources().getColor(android.R.color.white, null));
                        }

                        // 計算空閒時間
                        long busyMs = 0;
                        for (Event event : events) {
                            if (!"REMIND".equals(event.type)) {
                                busyMs += (event.endTime - event.startTime);
                            }
                        }
                        long freeMs = Math.max(0, totalDayMs - busyMs);
                        float freeHours = freeMs / 3600000f;
                        int progressVal = totalDayMs > 0 ?
                                (int)(freeMs * 100 / totalDayMs) : 100;

                        progressFree.setProgress(progressVal);
                        tvFreeHours.setText(String.format(Locale.TAIWAN, "%.0fh", freeHours));

                        if (freeHours < 2) {
                            progressFree.setProgressTintList(
                                    android.content.res.ColorStateList.valueOf(
                                            getResources().getColor(R.color.planck_red, null)));
                            tvFreeHours.setTextColor(
                                    getResources().getColor(R.color.planck_red, null));
                        } else if (freeHours < 4) {
                            progressFree.setProgressTintList(
                                    android.content.res.ColorStateList.valueOf(
                                            getResources().getColor(R.color.planck_orange, null)));
                            tvFreeHours.setTextColor(
                                    getResources().getColor(R.color.planck_orange, null));
                        }

                        // 事件 pills
                        int pillCount = 0;
                        for (Event event : events) {
                            if (pillCount >= 3) break;
                            TextView pill = new TextView(requireContext());
                            pill.setText(event.title.length() > 4 ?
                                    event.title.substring(0, 4) + "…" : event.title);
                            pill.setTextSize(9);
                            pill.setPadding(8, 2, 8, 2);
                            switch (event.priority) {
                                case 2:
                                    pill.setBackgroundResource(R.drawable.badge_red);
                                    pill.setTextColor(getResources().getColor(R.color.planck_red, null));
                                    break;
                                case 1:
                                    pill.setBackgroundResource(R.drawable.badge_orange);
                                    pill.setTextColor(getResources().getColor(R.color.planck_orange, null));
                                    break;
                                default:
                                    pill.setBackgroundResource(R.drawable.badge_green);
                                    pill.setTextColor(getResources().getColor(R.color.planck_green, null));
                            }
                            LinearLayout.LayoutParams pillParams =
                                    new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);
                            pillParams.setMarginEnd(4);
                            pill.setLayoutParams(pillParams);
                            layoutPills.addView(pill);
                            pillCount++;
                        }

                        row.setOnClickListener(v -> {
                            currentDay = (Calendar) finalDay.clone();
                            requireView().findViewById(R.id.tab_day).performClick();
                            loadDayEvents();
                        });

                        View placeholder = weekContainer.findViewWithTag("day_" + dayIndex);
                        if (placeholder != null) {
                            int idx = weekContainer.indexOfChild(placeholder);
                            weekContainer.removeView(placeholder);
                            weekContainer.addView(row, idx);
                        }
                    });
        }
    }

    private void loadMonthCalendar() {
        SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy年 M月", Locale.TAIWAN);
        tvMonthLabel.setText(monthFmt.format(currentMonth.getTime()));

        gridCalendar.removeAllViews();
        tvSelectedDate.setVisibility(View.GONE);
        cardSelectedEvents.setVisibility(View.GONE);

        Calendar firstDay = (Calendar) currentMonth.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        int startDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellWidth = (screenWidth - (int)(24 * density)) / 7;
        int cellHeight = (int)(44 * density);

        Calendar today = Calendar.getInstance();

        // 空白格
        for (int i = 0; i < startDayOfWeek; i++) {
            View empty = new View(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cellWidth;
            params.height = cellHeight;
            empty.setLayoutParams(params);
            gridCalendar.addView(empty);
        }

        // 每一天
        for (int day = 1; day <= daysInMonth; day++) {
            Calendar dayCal = (Calendar) currentMonth.clone();
            dayCal.set(Calendar.DAY_OF_MONTH, day);

            Calendar dayStart = (Calendar) dayCal.clone();
            dayStart.set(Calendar.HOUR_OF_DAY, 0);
            dayStart.set(Calendar.MINUTE, 0);
            dayStart.set(Calendar.SECOND, 0);
            dayStart.set(Calendar.MILLISECOND, 0);

            Calendar dayEnd = (Calendar) dayStart.clone();
            dayEnd.add(Calendar.DAY_OF_MONTH, 1);

            LinearLayout cell = new LinearLayout(requireContext());
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

            GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams();
            cellParams.width = cellWidth;
            cellParams.height = cellHeight;
            cell.setLayoutParams(cellParams);

            TextView tvDay = new TextView(requireContext());
            tvDay.setText(String.valueOf(day));
            tvDay.setTextSize(13);
            tvDay.setGravity(android.view.Gravity.CENTER);
            tvDay.setLayoutParams(new LinearLayout.LayoutParams(
                    (int)(32 * density), (int)(32 * density)));

            boolean isToday = (dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    && dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR));
            if (isToday) {
                tvDay.setBackgroundResource(R.drawable.circle_today);
                tvDay.setTextColor(getResources().getColor(android.R.color.white, null));
                tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvDay.setTextColor(getResources().getColor(R.color.text_primary, null));
            }
            cell.addView(tvDay);

            // 圓點容器
            LinearLayout dotsLayout = new LinearLayout(requireContext());
            dotsLayout.setOrientation(LinearLayout.HORIZONTAL);
            dotsLayout.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams dotsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            dotsParams.topMargin = (int)(2 * density);
            dotsLayout.setLayoutParams(dotsParams);
            cell.addView(dotsLayout);

            final Calendar finalDayCal = (Calendar) dayCal.clone();

            AppDatabase.getInstance(requireContext())
                    .eventDao()
                    .getEventsByDay(dayStart.getTimeInMillis(), dayEnd.getTimeInMillis())
                    .observe(getViewLifecycleOwner(), events -> {

                        dotsLayout.removeAllViews();

                        int dotCount = 0;
                        for (Event event : events) {
                            if (dotCount >= 3) break;
                            View dot = new View(requireContext());
                            int dotColor;
                            switch (event.priority) {
                                case 2: dotColor = R.color.planck_red; break;
                                case 1: dotColor = R.color.planck_orange; break;
                                default: dotColor = R.color.planck_purple; break;
                            }
                            dot.setBackgroundResource(R.drawable.circle_dot);
                            dot.getBackground().setTint(
                                    getResources().getColor(dotColor, null));
                            LinearLayout.LayoutParams dotParams =
                                    new LinearLayout.LayoutParams(
                                            (int)(5 * density), (int)(5 * density));
                            dotParams.setMarginEnd((int)(2 * density));
                            dot.setLayoutParams(dotParams);
                            dotsLayout.addView(dot);
                            dotCount++;
                        }

                        cell.setOnClickListener(v -> {
                            SimpleDateFormat dateFmt =
                                    new SimpleDateFormat("M月d日 EEEE", Locale.TAIWAN);
                            tvSelectedDate.setText(dateFmt.format(finalDayCal.getTime()));
                            tvSelectedDate.setVisibility(View.VISIBLE);

                            layoutSelectedEvents.removeAllViews();

                            if (events.isEmpty()) {
                                TextView empty = new TextView(requireContext());
                                empty.setText("這天沒有行程");
                                empty.setTextSize(12);
                                empty.setPadding(24, 20, 24, 20);
                                empty.setTextColor(
                                        getResources().getColor(R.color.text_secondary, null));
                                layoutSelectedEvents.addView(empty);
                            } else {
                                SimpleDateFormat timeFmt =
                                        new SimpleDateFormat("HH:mm", Locale.TAIWAN);
                                for (Event event : events) {
                                    View row = getLayoutInflater().inflate(
                                            R.layout.item_event_row, layoutSelectedEvents, false);
                                    TextView tvTitle = row.findViewById(R.id.tv_event_title);
                                    TextView tvMeta = row.findViewById(R.id.tv_event_meta);
                                    TextView tvBadge = row.findViewById(R.id.tv_priority_badge);
                                    View dot = row.findViewById(R.id.view_dot);

                                    tvTitle.setText(event.title);
                                    tvMeta.setText(
                                            timeFmt.format(new Date(event.startTime))
                                                    + " – " + timeFmt.format(new Date(event.endTime)));

                                    switch (event.priority) {
                                        case 2:
                                            tvBadge.setText("緊急");
                                            tvBadge.setBackgroundResource(R.drawable.badge_red);
                                            tvBadge.setTextColor(getResources().getColor(R.color.planck_red, null));
                                            dot.setBackgroundResource(R.drawable.circle_dot);
                                            dot.getBackground().setTint(getResources().getColor(R.color.planck_red, null));
                                            break;
                                        case 1:
                                            tvBadge.setText("重要");
                                            tvBadge.setBackgroundResource(R.drawable.badge_orange);
                                            tvBadge.setTextColor(getResources().getColor(R.color.planck_orange, null));
                                            dot.setBackgroundResource(R.drawable.circle_dot);
                                            dot.getBackground().setTint(getResources().getColor(R.color.planck_orange, null));
                                            break;
                                        default:
                                            tvBadge.setText("一般");
                                            tvBadge.setBackgroundResource(R.drawable.badge_green);
                                            tvBadge.setTextColor(getResources().getColor(R.color.planck_green, null));
                                            dot.setBackgroundResource(R.drawable.circle_dot);
                                            dot.getBackground().setTint(getResources().getColor(R.color.planck_purple, null));
                                    }
                                    layoutSelectedEvents.addView(row);
                                }
                            }
                            cardSelectedEvents.setVisibility(View.VISIBLE);

                            tvSelectedDate.setOnClickListener(vv -> {
                                currentDay = (Calendar) finalDayCal.clone();
                                requireView().findViewById(R.id.tab_day).performClick();
                                loadDayEvents();
                            });
                        });
                    });

            gridCalendar.addView(cell);
        }
    }

    private void loadRecommendedSlots(LinearLayout layoutSlots,
                                      View tvSlotLabel,
                                      View cardSlots,
                                      float neededHours,
                                      int priority) {
        SharedPreferences prefs =
                requireContext().getSharedPreferences("planck_settings", 0);
        int dayStartHour = prefs.getInt("day_start_hour", 8);
        int dayEndHour = prefs.getInt("day_end_hour", 22);
        int bufferMinutes = prefs.getInt("default_buffer", 15);

        Calendar dayStart = Calendar.getInstance();
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);

        Calendar dayEnd = (Calendar) dayStart.clone();
        dayEnd.add(Calendar.DAY_OF_MONTH, 1);

        AppDatabase.getInstance(requireContext())
                .eventDao()
                .getEventsByDay(dayStart.getTimeInMillis(), dayEnd.getTimeInMillis())
                .observe(getViewLifecycleOwner(), events -> {

                    List<FreeSlotFinder.FreeSlot> freeSlots = FreeSlotFinder.findFreeSlots(
                            events, dayStartHour, dayEndHour, bufferMinutes, 30);
                    List<FreeSlotFinder.FreeSlot> recommended =
                            FreeSlotFinder.recommend(freeSlots, neededHours, priority);

                    layoutSlots.removeAllViews();

                    if (recommended.isEmpty()) {
                        tvSlotLabel.setVisibility(View.VISIBLE);
                        cardSlots.setVisibility(View.VISIBLE);
                        TextView emptyView = new TextView(requireContext());
                        emptyView.setText("今天沒有足夠的空閒時段");
                        emptyView.setTextSize(12);
                        emptyView.setPadding(24, 20, 24, 20);
                        emptyView.setTextColor(
                                getResources().getColor(R.color.text_secondary, null));
                        layoutSlots.addView(emptyView);
                        return;
                    }

                    tvSlotLabel.setVisibility(View.VISIBLE);
                    cardSlots.setVisibility(View.VISIBLE);

                    SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.TAIWAN);
                    boolean isFirst = true;

                    for (FreeSlotFinder.FreeSlot slot : recommended) {
                        View row = getLayoutInflater().inflate(
                                R.layout.item_slot_row, layoutSlots, false);
                        TextView tvTime = row.findViewById(R.id.tv_slot_time);
                        TextView tvDuration = row.findViewById(R.id.tv_slot_duration);
                        TextView tvBadge = row.findViewById(R.id.tv_slot_badge);

                        tvTime.setText(timeFmt.format(new Date(slot.startTime))
                                + " – " + timeFmt.format(new Date(slot.endTime)));

                        int slotMinutes = (int)(slot.durationHours * 60);
                        tvDuration.setText(slotMinutes >= 60 ?
                                String.format(Locale.TAIWAN, "空檔 %.0f 小時", slot.durationHours) :
                                "空檔 " + slotMinutes + " 分鐘");

                        if (isFirst) {
                            tvBadge.setText("最佳");
                            tvBadge.setBackgroundResource(R.drawable.badge_green);
                            tvBadge.setTextColor(getResources().getColor(R.color.planck_green, null));
                            isFirst = false;
                        } else {
                            tvBadge.setText("推薦");
                            tvBadge.setBackgroundResource(R.drawable.badge_orange);
                            tvBadge.setTextColor(getResources().getColor(R.color.planck_orange, null));
                        }
                        layoutSlots.addView(row);
                    }
                });
    }

    private void showAddEventDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_event);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText etTitle = dialog.findViewById(R.id.et_event_title);
        TextInputEditText etNote = dialog.findViewById(R.id.et_event_note);
        RadioGroup rgType = dialog.findViewById(R.id.rg_event_type);
        RadioGroup rgPriority = dialog.findViewById(R.id.rg_priority);
        TextView tvStartDate = dialog.findViewById(R.id.tv_start_date);
        TextView tvStartTime = dialog.findViewById(R.id.tv_start_time);
        TextView tvEndDate = dialog.findViewById(R.id.tv_end_date);
        TextView tvEndTime = dialog.findViewById(R.id.tv_end_time);
        View tvSlotLabel = dialog.findViewById(R.id.tv_slot_label);
        View cardSlots = dialog.findViewById(R.id.card_slots);
        LinearLayout layoutSlots = dialog.findViewById(R.id.layout_slots);

        int[] startDate = {
                currentDay.get(Calendar.YEAR),
                currentDay.get(Calendar.MONTH),
                currentDay.get(Calendar.DAY_OF_MONTH)
        };
        int[] endDate = {
                currentDay.get(Calendar.YEAR),
                currentDay.get(Calendar.MONTH),
                currentDay.get(Calendar.DAY_OF_MONTH)
        };
        int[] startTime = {9, 0};
        int[] endTime = {10, 0};

        tvStartDate.setText(String.format(Locale.TAIWAN, "%d/%02d/%02d",
                startDate[0], startDate[1] + 1, startDate[2]));
        tvEndDate.setText(String.format(Locale.TAIWAN, "%d/%02d/%02d",
                endDate[0], endDate[1] + 1, endDate[2]));

        tvStartDate.setOnClickListener(v ->
                new DatePickerDialog(requireContext(),
                        (dp, y, m, d) -> {
                            startDate[0] = y; startDate[1] = m; startDate[2] = d;
                            tvStartDate.setText(String.format(Locale.TAIWAN,
                                    "%d/%02d/%02d", y, m + 1, d));
                        }, startDate[0], startDate[1], startDate[2]).show()
        );

        tvEndDate.setOnClickListener(v ->
                new DatePickerDialog(requireContext(),
                        (dp, y, m, d) -> {
                            endDate[0] = y; endDate[1] = m; endDate[2] = d;
                            tvEndDate.setText(String.format(Locale.TAIWAN,
                                    "%d/%02d/%02d", y, m + 1, d));
                        }, endDate[0], endDate[1], endDate[2]).show()
        );

        tvStartTime.setOnClickListener(v ->
                showTimePicker("開始時間", startTime[0], startTime[1], (h, m) -> {
                    startTime[0] = h; startTime[1] = m;
                    tvStartTime.setText(String.format(Locale.TAIWAN, "%02d:%02d", h, m));
                })
        );

        tvEndTime.setOnClickListener(v ->
                showTimePicker("結束時間", endTime[0], endTime[1], (h, m) -> {
                    endTime[0] = h; endTime[1] = m;
                    tvEndTime.setText(String.format(Locale.TAIWAN, "%02d:%02d", h, m));
                })
        );

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_type_time) {
                int priority = 0;
                int priorityId = rgPriority.getCheckedRadioButtonId();
                if (priorityId == R.id.rb_priority_urgent) priority = 2;
                else if (priorityId == R.id.rb_priority_important) priority = 1;
                loadRecommendedSlots(layoutSlots, tvSlotLabel, cardSlots, 1.0f, priority);
            } else {
                tvSlotLabel.setVisibility(View.GONE);
                cardSlots.setVisibility(View.GONE);
            }
        });

        loadRecommendedSlots(layoutSlots, tvSlotLabel, cardSlots, 1.0f, 0);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String title = etTitle.getText() != null ?
                    etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                etTitle.setError("請輸入事件名稱");
                return;
            }

            int priority = 0;
            int priorityId = rgPriority.getCheckedRadioButtonId();
            if (priorityId == R.id.rb_priority_urgent) priority = 2;
            else if (priorityId == R.id.rb_priority_important) priority = 1;

            String type = "TIME";
            if (rgType.getCheckedRadioButtonId() == R.id.rb_type_remind) type = "REMIND";

            String note = etNote.getText() != null ?
                    etNote.getText().toString().trim() : "";

            int finalPriority = priority;
            String finalType = type;

            new Thread(() -> {
                Calendar sCal = Calendar.getInstance();
                sCal.set(startDate[0], startDate[1], startDate[2],
                        startTime[0], startTime[1], 0);
                sCal.set(Calendar.MILLISECOND, 0);

                Calendar eCal = Calendar.getInstance();
                eCal.set(endDate[0], endDate[1], endDate[2],
                        endTime[0], endTime[1], 0);
                eCal.set(Calendar.MILLISECOND, 0);

                Event event = new Event();
                event.title = title;
                event.type = finalType;
                event.priority = finalPriority;
                event.startTime = sCal.getTimeInMillis();
                event.endTime = eCal.getTimeInMillis();
                event.bufferMinutes = 0;
                event.deadlineTime = 0;
                event.note = note;
                event.jobId = -1;

                AppDatabase.getInstance(requireContext()).eventDao().insert(event);
            }).start();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTimePicker(String title, int defHour, int defMinute,
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
        pickerHour.setValue(defHour);
        pickerHour.setFormatter(v -> String.format(Locale.TAIWAN, "%02d", v));

        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(59);
        pickerMinute.setValue(defMinute);
        pickerMinute.setFormatter(v -> String.format(Locale.TAIWAN, "%02d", v));

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            listener.onTimeSelected(pickerHour.getValue(), pickerMinute.getValue());
            dialog.dismiss();
        });

        dialog.show();
    }
}