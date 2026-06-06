package com.example.planck;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.util.Log.d("SPLASH", "SplashActivity onCreate called");
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_splash);

        // 啟動貓咪 Frame Animation
        ImageView ivCat = findViewById(R.id.iv_cat);
        AnimationDrawable catAnimation = (AnimationDrawable) ivCat.getDrawable();

        // 讓貓咪從左跑到右
        ivCat.post(() -> {
            catAnimation.start();
            startCatRunning(ivCat);
        });

        // App 名稱淡入動畫
        TextView tvName = findViewById(R.id.tv_app_name);
        TextView tvTagline = findViewById(R.id.tv_tagline);
        tvName.setAlpha(0f);
        tvTagline.setAlpha(0f);
        tvName.animate().alpha(1f).setDuration(600).setStartDelay(200).start();
        tvTagline.animate().alpha(1f).setDuration(600).setStartDelay(400).start();

        // 2 秒後跳到 MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // 加入淡出轉場
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 3500);
    }

    private void startCatRunning(ImageView ivCat) {
        int stageWidth = ((android.widget.FrameLayout) ivCat.getParent()).getWidth();
        int catWidth = ivCat.getWidth();

        // 讓貓咪從左邊開始
        ivCat.setTranslationX(-catWidth);

        // 貓咪跑過整個舞台
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                ivCat, "translationX", -catWidth, stageWidth);
        animator.setDuration(1800);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.start();
    }
}