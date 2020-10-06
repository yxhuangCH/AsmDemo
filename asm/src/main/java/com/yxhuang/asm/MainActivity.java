package com.yxhuang.asm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.yxhuang.asm.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_";

    private TextView mTvHello;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvHello = findViewById(R.id.tv_hello);

        mTvHello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Hello word", Toast.LENGTH_LONG).show();
            }
        });
    }

}
