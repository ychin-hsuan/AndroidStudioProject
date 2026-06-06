package com.example.planck;

import android.widget.ImageView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.planck.database.AppDatabase;
import com.example.planck.database.Event;
import com.example.planck.database.TodoItem;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private TextView tvGreeting, tvDate;
    private TextView tvFreeTime, tvFreeSlots;
    private TextView tvPendingCount, tvPendingUrgent;
    private TextView tvWarning;
    private View cardWarning;
    private LinearLayout layoutEvents, layoutReminders;
    private ProgressBar progressBar;
    private TextView tvProgressCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvDate = view.findViewById(R.id.tv_date);
        tvFreeTime = view.findViewById(R.id.tv_free_time);
        tvFreeSlots = view.findViewById(R.id.tv_free_slots);
        tvPendingCount = view.findViewById(R.id.tv_pending_count);
        tvPendingUrgent = view.findViewById(R.id.tv_pending_urgent);
        tvWarning = view.findViewById(R.id.tv_warning);
        cardWarning = view.findViewById(R.id.card_warning);
        layoutEvents = view.findViewById(R.id.layout_events);
        layoutReminders = view.findViewById(R.id.layout_reminders);

        // 設定日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日，EEEE", Locale.TAIWAN);
        tvDate.setText(sdf.format(new Date()));

        // 設定打招呼
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) tvGreeting.setText("早安！");
        else if (hour < 18) tvGreeting.setText("午安！");
        else tvGreeting.setText("晚安！");

        // 載入真實資料
        loadTodayEvents();
        loadTodayTodos();

        // 三點按鈕 PopupMenu
        ImageView btnMore = view.findViewById(R.id.btn_more);
        btnMore.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(requireContext(), v);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_refresh) {
                    // 重新整理
                    loadTodayEvents();
                    loadTodayTodos();
                    return true;
                } else if (id == R.id.menu_add_event) {
                    requireActivity().findViewById(R.id.bottom_nav)
                            .performClick();
                    ((com.google.android.material.bottomnavigation.BottomNavigationView)
                            requireActivity().findViewById(R.id.bottom_nav))
                            .setSelectedItemId(R.id.nav_schedule);
                    return true;
                } else if (id == R.id.menu_add_todo) {
                    ((com.google.android.material.bottomnavigation.BottomNavigationView)
                            requireActivity().findViewById(R.id.bottom_nav))
                            .setSelectedItemId(R.id.nav_todo);
                    return true;
                } else if (id == R.id.menu_settings) {
                    ((com.google.android.material.bottomnavigation.BottomNavigationView)
                            requireActivity().findViewById(R.id.bottom_nav))
                            .setSelectedItemId(R.id.nav_settings);
                    return true;
                } else if (id == R.id.menu_about) {
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("關於 Planck")
                            .setMessage("Planck\n版本 1.0.0\n\n精準掌握你的每一天\n\n開發者：kiki\n行動裝置應用程式設計 期末專題")
                            .setPositiveButton("確認", null)
                            .show();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        return view;
    }

    private void loadTodayEvents() {
        // 計算今天的開始和結束時間
        Calendar dayStart = Calendar.getInstance();
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);

        Calendar dayEnd = (Calendar) dayStart.clone();
        dayEnd.add(Calendar.DAY_OF_MONTH, 1);

        AppDatabase db = AppDatabase.getInstance(requireContext());

        // 觀察今日事件
        db.eventDao().getEventsByDay(
                dayStart.getTimeInMillis(),
                dayEnd.getTimeInMillis()
        ).observe(getViewLifecycleOwner(), events -> {
            layoutEvents.removeAllViews();
            layoutReminders.removeAllViews();

            if (events.isEmpty()) {
                // 沒有事件時顯示空白提示
                TextView emptyView = new TextView(requireContext());
                emptyView.setText("今天沒有行程，點行程頁新增！");
                emptyView.setTextSize(13);
                emptyView.setPadding(32, 24, 32, 24);
                emptyView.setTextColor(getResources().getColor(R.color.text_secondary, null));
                layoutEvents.addView(emptyView);

                // 隱藏警示
                cardWarning.setVisibility(View.GONE);
                tvFreeTime.setText("整天空閒");
                tvFreeSlots.setText("沒有行程");
                tvPendingCount.setText("0");
                tvPendingUrgent.setText("快去新增行程！");
                return;
            }

            // 分類事件：一般事件 vs 只需提醒
            int urgentCount = 0;
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.TAIWAN);

            for (Event event : events) {
                if ("REMIND".equals(event.type)) {
                    // 提醒類事件放到提醒區
                    addEventRow(getLayoutInflater(), layoutReminders,
                            event.title,
                            timeFmt.format(new Date(event.startTime)) + " · 提醒",
                            getPriorityLabel(event.priority),
                            getPriorityColor(event.priority));
                } else {
                    // 一般事件放到行程區
                    String meta = buildEventMeta(event);
                    addEventRow(getLayoutInflater(), layoutEvents,
                            event.title, meta,
                            getPriorityLabel(event.priority),
                            getPriorityColor(event.priority));

                    if (event.priority == 2) urgentCount++;
                }
            }

            // 更新待安排統計
            tvPendingCount.setText(String.valueOf(events.size()));
            tvPendingUrgent.setText(urgentCount > 0 ?
                    urgentCount + "個緊急事件" : "今天加油！");

            // 計算剩餘空檔
            calculateFreeTime(events);

            // 如果有提醒區是空的，顯示提示
            if (layoutReminders.getChildCount() == 0) {
                TextView emptyReminder = new TextView(requireContext());
                emptyReminder.setText("今天沒有提醒");
                emptyReminder.setTextSize(12);
                emptyReminder.setPadding(32, 20, 32, 20);
                emptyReminder.setTextColor(
                        getResources().getColor(R.color.text_secondary, null));
                layoutReminders.addView(emptyReminder);
            }
        });
    }

    private void loadTodayTodos() {
        // Todo 的今日待辦數量（顯示在摘要卡片）
        // 這裡可以之後擴充
    }

    private String buildEventMeta(Event event) {
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.TAIWAN);
        String startStr = timeFmt.format(new Date(event.startTime));
        String endStr = timeFmt.format(new Date(event.endTime));

        long durationMs = event.endTime - event.startTime;
        float hours = durationMs / 3600000f;

        if (hours < 1) {
            int minutes = (int)(durationMs / 60000);
            return String.format(Locale.TAIWAN,
                    "需要 %d 分鐘 · %s – %s", minutes, startStr, endStr);
        } else {
            return String.format(Locale.TAIWAN,
                    "需要 %.0f 小時 · %s – %s", hours, startStr, endStr);
        }
    }

    private void calculateFreeTime(List<Event> events) {
        // 讀取設定的活動時間範圍
        android.content.SharedPreferences prefs =
                requireContext().getSharedPreferences("planck_settings", 0);
        int dayStartHour = prefs.getInt("day_start_hour", 8);
        int dayEndHour = prefs.getInt("day_end_hour", 22);

        Calendar today = Calendar.getInstance();
        Calendar start = (Calendar) today.clone();
        start.set(Calendar.HOUR_OF_DAY, dayStartHour);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) today.clone();
        end.set(Calendar.HOUR_OF_DAY, dayEndHour);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        long totalMs = end.getTimeInMillis() - start.getTimeInMillis();
        long busyMs = 0;

        for (Event event : events) {
            if ("REMIND".equals(event.type)) continue;
            long evStart = Math.max(event.startTime, start.getTimeInMillis());
            long evEnd = Math.min(event.endTime, end.getTimeInMillis());
            if (evEnd > evStart) busyMs += (evEnd - evStart);
        }

        long freeMs = Math.max(0, totalMs - busyMs);
        int freeHours = (int)(freeMs / 3600000);
        int freeMinutes = (int)((freeMs % 3600000) / 60000);

        if (freeHours > 0) {
            tvFreeTime.setText(String.format(Locale.TAIWAN,
                    "%dh %dm", freeHours, freeMinutes));
        } else {
            tvFreeTime.setText(freeMinutes + "m");
        }
        tvFreeSlots.setText("今日活動 " + events.size() + " 個");

        // 警示：如果空閒時間少於2小時
        if (freeMs < 2 * 3600000) {
            cardWarning.setVisibility(View.VISIBLE);
            tvWarning.setText("今日空閒時間不足 2 小時，請注意行程安排！");
        } else {
            cardWarning.setVisibility(View.GONE);
        }
    }

    private String getPriorityLabel(int priority) {
        switch (priority) {
            case 2: return "緊急";
            case 1: return "重要";
            default: return "一般";
        }
    }

    private String getPriorityColor(int priority) {
        switch (priority) {
            case 2: return "red";
            case 1: return "orange";
            default: return "green";
        }
    }

    private void addEventRow(LayoutInflater inflater, LinearLayout parent,
                             String title, String meta,
                             String badge, String color) {
        View row = inflater.inflate(R.layout.item_event_row, parent, false);

        TextView tvTitle = row.findViewById(R.id.tv_event_title);
        TextView tvMeta = row.findViewById(R.id.tv_event_meta);
        TextView tvBadge = row.findViewById(R.id.tv_priority_badge);
        View dot = row.findViewById(R.id.view_dot);

        tvTitle.setText(title);
        tvMeta.setText(meta);
        tvBadge.setText(badge);

        switch (color) {
            case "red":
                dot.setBackgroundResource(R.drawable.circle_dot);
                dot.getBackground().setTint(
                        getResources().getColor(R.color.planck_red, null));
                tvBadge.setBackgroundResource(R.drawable.badge_red);
                tvBadge.setTextColor(
                        getResources().getColor(R.color.planck_red, null));
                break;
            case "orange":
                dot.setBackgroundResource(R.drawable.circle_dot);
                dot.getBackground().setTint(
                        getResources().getColor(R.color.planck_orange, null));
                tvBadge.setBackgroundResource(R.drawable.badge_orange);
                tvBadge.setTextColor(
                        getResources().getColor(R.color.planck_orange, null));
                break;
            case "green":
                dot.setBackgroundResource(R.drawable.circle_dot);
                dot.getBackground().setTint(
                        getResources().getColor(R.color.planck_green, null));
                tvBadge.setBackgroundResource(R.drawable.badge_green);
                tvBadge.setTextColor(
                        getResources().getColor(R.color.planck_green, null));
                break;
        }

        parent.addView(row);
    }
}