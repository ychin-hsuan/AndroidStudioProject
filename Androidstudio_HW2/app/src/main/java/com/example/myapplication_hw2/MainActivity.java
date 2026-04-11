package com.example.myapplication_hw2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText Start,End;
    private Button btnClear;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }

    private void findViews() {
        Start = (EditText)findViewById(R.id.etStart);
        End = (EditText)findViewById(R.id.etEnd);
        tvMessage=(TextView) findViewById(R.id.tvMessage);
        btnClear=(Button) findViewById(R.id.btnClear);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Start.setText(null);
                End.setText(null);
                tvMessage.setText(null);
                Toast.makeText(MainActivity.this,R.string.msg_ClearAllField,Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void OnRunClick(View view) {
            String startStr = Start.getText().toString();
            String endStr = End.getText().toString();

            int startVal = Integer.parseInt(startStr);
            int endVal = Integer.parseInt(endStr);


            int min, max;
            if (startVal <= endVal) {
                min = startVal;
                max = endVal;
            } else {
                min = endVal;
                max = startVal;
            }

            int sum = 0;
            for (int i = min; i <= max; i++) {
                sum += i;
            }

            tvMessage.setText("初值=" + min + ", 終值=" + max + ", 合計值=" + sum);
        }
}