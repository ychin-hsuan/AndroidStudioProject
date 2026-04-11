package com.example.myapp1414;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText etH,etW;
    private Button btnClear;
    private TextView tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }

    private void findViews() {
       etW = (EditText)findViewById(R.id.etW);
       etH = (EditText)findViewById(R.id.etH);
       tvMsg=(TextView) findViewById(R.id.tvmessage);
       btnClear=(Button) findViewById(R.id.btClear);

       btnClear.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               etW.setText(null);
               etH.setText(null);
               tvMsg.setText(null);
               Toast.makeText(MainActivity.this,R.string.msg_ClearAllField,Toast.LENGTH_SHORT).show();
           }
       });

    }

    public void onRunCilck(View view) {
        double w = Double.parseDouble(etW.getText().toString()); //抓下來後轉文字再轉double型態
        double h = Double.parseDouble(etH.getText().toString());
        h /= 100;

        double bmi=w/Math.pow(h,2); //指數
        String msg="";
        if(bmi<18.5){
            msg="體重過輕";
        }
        if(bmi>=18.5&&bmi<24){
           msg="正常範圍";
        }
        if(bmi>=24&&bmi<27) {
           msg = "過重";
        }
        if(bmi>=27&&bmi<30) {
            msg = "輕度肥胖";
        }
        if(bmi>=30&&bmi<35) {
            msg = "中度肥胖";
        }
        if(bmi>=35) {
            msg = "重度肥胖";
        }

        String text = "BMI="+bmi+"\n訊息="+msg;
        tvMsg.setText(text);
    }


}