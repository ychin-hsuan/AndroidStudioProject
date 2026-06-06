package com.example.planck;

import java.util.Calendar;

public class SalaryCalculator {

    /**
     * 計算單班薪資
     * @param hourlyRate 時薪
     * @param startTime  上班時間 (timestamp)
     * @param endTime    下班時間 (timestamp)
     * @return 應得薪資
     */
    public static int calculate(int hourlyRate, long startTime, long endTime) {
        float totalHours = (endTime - startTime) / 3600000f;

        // 判斷是哪種班別
        DayType dayType = getDayType(startTime);

        switch (dayType) {
            case NATIONAL_HOLIDAY:
                return calculateNationalHoliday(hourlyRate, totalHours);
            case REST_DAY:
                return calculateRestDay(hourlyRate, totalHours);
            default:
                return calculateWeekday(hourlyRate, totalHours);
        }
    }

    /**
     * 平日薪資（正常工時 8hr，超過才算加班）
     */
    private static int calculateWeekday(int hourlyRate, float totalHours) {
        if (totalHours <= 8) {
            return (int)(hourlyRate * totalHours);
        }

        float overtimeHours = totalHours - 8;
        int normalPay = hourlyRate * 8;
        int overtimePay = 0;

        // 前2小時加班 × 1.34
        float first = Math.min(overtimeHours, 2);
        overtimePay += (int)(hourlyRate * first * 1.34f);

        // 後2小時加班 × 1.67
        if (overtimeHours > 2) {
            float second = Math.min(overtimeHours - 2, 2);
            overtimePay += (int)(hourlyRate * second * 1.67f);
        }

        return normalPay + overtimePay;
    }

    /**
     * 休息日薪資
     * 前2小時 × 1.34，3-8小時 × 1.67，9-12小時 × 2.67
     */
    private static int calculateRestDay(int hourlyRate, float totalHours) {
        int pay = 0;

        // 前2小時
        float first = Math.min(totalHours, 2);
        pay += (int)(hourlyRate * first * 1.34f);

        // 3-8小時
        if (totalHours > 2) {
            float second = Math.min(totalHours - 2, 6);
            pay += (int)(hourlyRate * second * 1.67f);
        }

        // 9-12小時
        if (totalHours > 8) {
            float third = Math.min(totalHours - 8, 4);
            pay += (int)(hourlyRate * third * 2.67f);
        }

        return pay;
    }

    /**
     * 國定假日薪資（雙倍時薪）
     */
    private static int calculateNationalHoliday(int hourlyRate, float totalHours) {
        return (int)(hourlyRate * 2 * totalHours);
    }

    /**
     * 判斷日期類型
     */
    public static DayType getDayType(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        if (isNationalHoliday(cal)) return DayType.NATIONAL_HOLIDAY;
        if (isRestDay(cal)) return DayType.REST_DAY;
        return DayType.WEEKDAY;
    }

    /**
     * 判斷是否為休息日（週六）
     * 台灣一般以週日為例假日、週六為休息日
     */
    private static boolean isRestDay(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY;
    }

    /**
     * 判斷是否為國定假日（週日 + 2026年國定假日）
     */
    private static boolean isNationalHoliday(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY) return true;

        // 2026年國定假日
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return (month == 1 && day == 1)   // 元旦
                || (month == 1 && day == 26)  // 春節
                || (month == 1 && day == 27)
                || (month == 1 && day == 28)
                || (month == 1 && day == 29)
                || (month == 1 && day == 30)
                || (month == 2 && day == 28)  // 和平紀念日
                || (month == 4 && day == 4)   // 兒童節
                || (month == 4 && day == 5)   // 清明節
                || (month == 5 && day == 1)   // 勞動節
                || (month == 6 && day == 19)  // 端午節
                || (month == 9 && day == 25)  // 中秋節
                || (month == 10 && day == 9)  // 國慶日
                || (month == 10 && day == 10);
    }

    public enum DayType {
        WEEKDAY,         // 平日
        REST_DAY,        // 休息日（週六）
        NATIONAL_HOLIDAY // 國定假日（週日 + 假日）
    }

    /**
     * 取得日期類型的中文說明
     */
    public static String getDayTypeLabel(long timestamp) {
        switch (getDayType(timestamp)) {
            case NATIONAL_HOLIDAY: return "國定假日（×2）";
            case REST_DAY: return "休息日（加班費制）";
            default: return "平日";
        }
    }
}