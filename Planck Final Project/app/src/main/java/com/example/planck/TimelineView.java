package com.example.planck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.planck.database.Event;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimelineView extends View {

    // 每小時的高度（px）
    private static final int HOUR_HEIGHT_DP = 64;

    // 顯示時間範圍
    private static final int START_HOUR = 6;
    private static final int END_HOUR = 23;

    private float hourHeight;
    private float density;

    private Paint linePaint;       // 小時分隔線
    private Paint freePaint;       // 空檔虛線框
    private Paint nowLinePaint;    // 現在時間紅線
    private Paint nowDotPaint;     // 現在時間圓點

    private List<Event> events = new ArrayList<>();
    private List<long[]> freeSlots = new ArrayList<>(); // 空檔時段

    // 顏色
    private int colorRed, colorOrange, colorGreen, colorPurple;
    private int colorRedLight, colorOrangeLight, colorGreenLight, colorPurpleLight;

    public TimelineView(Context context) {
        super(context);
        init(context);
    }

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        hourHeight = HOUR_HEIGHT_DP * density;

        // 顏色
        colorRed = ContextCompat.getColor(context, R.color.planck_red);
        colorOrange = ContextCompat.getColor(context, R.color.planck_orange);
        colorGreen = ContextCompat.getColor(context, R.color.planck_green);
        colorPurple = ContextCompat.getColor(context, R.color.planck_purple);
        colorRedLight = ContextCompat.getColor(context, R.color.planck_red_light);
        colorOrangeLight = ContextCompat.getColor(context, R.color.planck_orange_light);
        colorGreenLight = ContextCompat.getColor(context, R.color.planck_green_light);
        colorPurpleLight = ContextCompat.getColor(context, R.color.planck_purple_light);

        // 分隔線
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFFEEEEEE);
        linePaint.setStrokeWidth(1 * density);

        // 空檔虛線框
        freePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        freePaint.setColor(0xFFCCCCCC);
        freePaint.setStyle(Paint.Style.STROKE);
        freePaint.setStrokeWidth(1 * density);
        freePaint.setPathEffect(new android.graphics.DashPathEffect(
                new float[]{8 * density, 4 * density}, 0));

        // 現在時間線
        nowLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nowLinePaint.setColor(colorRed);
        nowLinePaint.setStrokeWidth(2 * density);

        // 現在時間圓點
        nowDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nowDotPaint.setColor(colorRed);
        nowDotPaint.setStyle(Paint.Style.FILL);
    }

    // 設定事件資料
    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        calculateFreeSlots();
        invalidate();
        requestLayout();
    }

    // 計算空檔
    private void calculateFreeSlots() {
        freeSlots.clear();
        // 以後接上真實資料時再完整實作
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalHours = END_HOUR - START_HOUR;
        int height = (int)(totalHours * hourHeight) + (int)(16 * density);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float padding = 8 * density;

        // 畫小時分隔線
        int totalHours = END_HOUR - START_HOUR;
        for (int i = 0; i <= totalHours; i++) {
            float y = i * hourHeight + (8 * density);
            canvas.drawLine(0, y, width, y, linePaint);
        }

        // 畫事件
        Paint eventBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint eventBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eventBorderPaint.setStyle(Paint.Style.FILL);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(12 * density);
        textPaint.setColor(0xFF333333);

        Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setTextSize(10 * density);
        subTextPaint.setColor(0xFF666666);

        for (Event event : events) {
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(event.startTime);
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(event.endTime);

            float startHour = startCal.get(Calendar.HOUR_OF_DAY)
                    + startCal.get(Calendar.MINUTE) / 60f - START_HOUR;
            float endHour = endCal.get(Calendar.HOUR_OF_DAY)
                    + endCal.get(Calendar.MINUTE) / 60f - START_HOUR;

            if (endHour <= 0 || startHour >= (END_HOUR - START_HOUR)) continue;
            startHour = Math.max(startHour, 0);
            endHour = Math.min(endHour, END_HOUR - START_HOUR);

            float top = startHour * hourHeight + (8 * density) + padding;
            float bottom = endHour * hourHeight + (8 * density) - padding;
            float left = padding * 2;
            float right = width - padding;

            // 根據優先度決定顏色
            int bgColor, borderColor;
            switch (event.priority) {
                case 2: bgColor = colorRedLight; borderColor = colorRed; break;
                case 1: bgColor = colorOrangeLight; borderColor = colorOrange; break;
                default: bgColor = colorPurpleLight; borderColor = colorPurple; break;
            }

            // 畫背景
            eventBgPaint.setColor(bgColor);
            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, 8 * density, 8 * density, eventBgPaint);

            // 畫左側色條
            eventBorderPaint.setColor(borderColor);
            RectF borderRect = new RectF(left, top, left + 4 * density, bottom);
            canvas.drawRoundRect(borderRect, 4 * density, 4 * density, eventBorderPaint);

            // 畫事件標題
            if (bottom - top > 20 * density) {
                canvas.drawText(event.title, left + 10 * density, top + 16 * density, textPaint);
            }

            // 畫時間
            if (bottom - top > 36 * density) {
                String timeText = String.format("%02d:%02d – %02d:%02d",
                        startCal.get(Calendar.HOUR_OF_DAY), startCal.get(Calendar.MINUTE),
                        endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE));
                canvas.drawText(timeText, left + 10 * density, top + 30 * density, subTextPaint);
            }
        }

        // 畫現在時間線
        Calendar now = Calendar.getInstance();
        float nowHour = now.get(Calendar.HOUR_OF_DAY)
                + now.get(Calendar.MINUTE) / 60f - START_HOUR;
        if (nowHour >= 0 && nowHour <= (END_HOUR - START_HOUR)) {
            float nowY = nowHour * hourHeight + (8 * density);
            canvas.drawCircle(padding * 2, nowY, 5 * density, nowDotPaint);
            canvas.drawLine(padding * 2, nowY, width, nowY, nowLinePaint);
        }
    }
}