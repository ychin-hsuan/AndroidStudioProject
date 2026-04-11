package idv.ron.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText etName, etPassword;
    private TextView tvResult;
    private Button btLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etName = (EditText) findViewById(R.id.etName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvResult = (TextView) findViewById(R.id.tvResult);
        btLogin = (Button) findViewById(R.id.btLogin);

        // Java傳統UI事件處理
//        btLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String name = etName.getText().toString();
//                String password = etPassword.getText().toString();
//                String text = "Name = " + name + "\nPassword = " + password;
//                tvResult.setText(text);
//            }
//        });

    }

    // Android簡易事件處理
    public void onLoginClick(View view) {
        String name = etName.getText().toString();
        String password = etPassword.getText().toString();
        String text = "Name = " + name + "\nPassword = " + password;
        tvResult.setText(text);
    }
}
