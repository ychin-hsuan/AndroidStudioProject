package com.example.myapplication1313131131313;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText etName,etPassword;
    private TextView tvResult;
    private Button btLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etName=(EditText) findViewById(R.id.etName);
        etPassword=(EditText) findViewById(R.id.etPassword);
        tvResult=(TextView) findViewById(R.id.tvResult);
        btLogin=(Button) findViewById(R.id.btLogin);
    }

    public void onMyClick(View view) {
        String name=etName.getText().toString();
        String ps=etPassword.getText().toString();
        String text="姓名="+name+"\n密碼="+ps;
        tvResult.setText(text);
    }
}