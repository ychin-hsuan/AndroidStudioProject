package com.example.myapplication1313;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private EditText etUser,etPassword,etPhone,etAge;
    private Button btClear;
    private TextView tvMessage;
    // 簡單事件處理法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }

    private void findViews() {
        etUser = (EditText) findViewById(R.id.etUser);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etAge = (EditText) findViewById(R.id.etAge);
        btClear = (Button) findViewById(R.id.btClear);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etUser.setText(null);
                etPassword.setText(null);
                etPhone.setText(null);
                etAge.setText(null);
                tvMessage.setText(null);
                Toast.makeText(
                        MainActivity.this,
                        R.string.msg_ClearAllFields,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    public void onSubmitClick(View view) {
        String user = etUser.getText().toString().trim(); // trim 刪除多於空白
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String text = ""; // 準備放到 message 裡面的
        text += "user name = " + user + "\n";
        text += "password = " + password + "\n";
        text += "phone number = " + phone + "\n";
        text += "age = " + age + "\n";
        tvMessage.setText(text); // set 放東西進去
    }
}

// 傳統事件處理法