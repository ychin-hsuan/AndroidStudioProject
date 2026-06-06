package com.example.planck;

import com.example.planck.database.Event;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FreeSlotFinder {

    // 代表一個空閒時段
    public static class FreeSlot {
        public long startTime;
        public long endTime;
        public float durationHours;

        public FreeSlot(long start, long end) {
            this.startTime = start;
            this.endTime = end;
            this.durationHours = (end - start) / 3600000f;
        }
    }

    /**
     * 找出指定日期的空閒時段
     * @param events      當天的事件列表
     * @param dayStartHour 活動開始時間（小時）
     * @param dayEndHour   活動結束時間（小時）
     * @param bufferMinutes 每個事件的緩衝時間（分鐘）
     * @param minSlotMinutes 最短有效空閒時間（分鐘）
     * @return 空閒時段列表
     */
    public static List<FreeSlot> findFreeSlots(
            List<Event> events,
            int dayStartHour,
            int dayEndHour,
            int bufferMinutes,
            int minSlotMinutes) {

        List<FreeSlot> result = new ArrayList<>();

        // 取得今天的開始和結束時間
        Calendar dayCal = Calendar.getInstance();
        Calendar dayStart = (Calendar) dayCal.clone();
        dayStart.set(Calendar.HOUR_OF_DAY, dayStartHour);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);

        Calendar dayEnd = (Calendar) dayCal.clone();
        dayEnd.set(Calendar.HOUR_OF_DAY, dayEndHour);
        dayEnd.set(Calendar.MINUTE, 0);
        dayEnd.set(Calendar.SECOND, 0);
        dayEnd.set(Calendar.MILLISECOND, 0);

        long bufferMs = bufferMinutes * 60000L;
        long minSlotMs = minSlotMinutes * 60000L;

        // 只考慮需要時間的事件（不含只提醒的）
        List<Event> timedEvents = new ArrayList<>();
        for (Event e : events) {
            if (!"REMIND".equals(e.type)) {
                timedEvents.add(e);
            }
        }

        // 依開始時間排序
        Collections.sort(timedEvents, (a, b) ->
                Long.compare(a.startTime, b.startTime));

        // 從活動開始掃描到結束，找空白區間
        long cursor = dayStart.getTimeInMillis();

        for (Event event : timedEvents) {
            // 加入緩衝時間
            long evStart = event.startTime - bufferMs;
            long evEnd = event.endTime + bufferMs;

            // 確保不超出今天範圍
            evStart = Math.max(evStart, dayStart.getTimeInMillis());
            evEnd = Math.min(evEnd, dayEnd.getTimeInMillis());

            // cursor 到 evStart 之間是空閒
            if (evStart > cursor) {
                long slotDuration = evStart - cursor;
                if (slotDuration >= minSlotMs) {
                    result.add(new FreeSlot(cursor, evStart));
                }
            }

            // 移動 cursor 到事件結束後
            if (evEnd > cursor) {
                cursor = evEnd;
            }
        }

        // 最後一個事件結束到活動結束之間的空閒
        if (cursor < dayEnd.getTimeInMillis()) {
            long slotDuration = dayEnd.getTimeInMillis() - cursor;
            if (slotDuration >= minSlotMs) {
                result.add(new FreeSlot(cursor, dayEnd.getTimeInMillis()));
            }
        }

        return result;
    }

    /**
     * 從空閒時段中找出符合所需時數的推薦時段
     * @param freeSlots    所有空閒時段
     * @param neededHours  需要的時數
     * @param priority     優先度（影響推薦順序）
     * @return 推薦時段列表（最多3個）
     */
    public static List<FreeSlot> recommend(
            List<FreeSlot> freeSlots,
            float neededHours,
            int priority) {

        List<FreeSlot> suitable = new ArrayList<>();

        for (FreeSlot slot : freeSlots) {
            if (slot.durationHours >= neededHours) {
                // 建立一個剛好符合所需時數的時段
                long endTime = slot.startTime + (long)(neededHours * 3600000);
                suitable.add(new FreeSlot(slot.startTime, endTime));
            }
        }

        // 根據優先度排序
        if (priority == 2) {
            // 緊急：推薦最近的空檔
            Collections.sort(suitable, (a, b) ->
                    Long.compare(a.startTime, b.startTime));
        } else if (priority == 1) {
            // 重要：推薦近期但空間較大的
            Collections.sort(suitable, (a, b) ->
                    Long.compare(a.startTime, b.startTime));
        } else {
            // 一般：推薦最寬裕的空檔（避免擠壓其他事件）
            Collections.sort(suitable, (a, b) ->
                    Float.compare(b.durationHours, a.durationHours));
        }

        // 最多回傳3個推薦
        return suitable.subList(0, Math.min(3, suitable.size()));
    }
}