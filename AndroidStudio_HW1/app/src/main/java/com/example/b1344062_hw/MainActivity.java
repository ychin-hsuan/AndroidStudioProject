package com.example.b1344062_hw;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText etHeight, etWeight;
    private Button btRun;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etHeight=(EditText) findViewById(R.id.etHeight);
        etWeight=(EditText) findViewById(R.id.etWeight);
        btRun=(Button) findViewById(R.id.btRun);
        tvResult=(TextView) findViewById(R.id.tvResult);
        // 傳統事件處理法
        btRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double height=Double.parseDouble(etHeight.getText().toString());
                double weight=Double.parseDouble(etWeight.getText().toString());
                height=height/100;
                String text="your bmi is "+weight/(height*height);
                tvResult.setText(text);
            }
        });
    }
//簡單事件處理法
//    public void onRunClick(View view) {
//        double height=Double.parseDouble(etHeight.getText().toString());
//        double weight=Double.parseDouble(etWeight.getText().toString());
//        height=height/100;
//        String text="your bmi is "+weight/(height*height);
//        tvResult.setText(text);
//    }
}
