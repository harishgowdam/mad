package com.example.app2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity {
    Button b1,b2;


    TextView t1;
    float font=20;
    int i=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        b1=findViewById(R.id.button1);
        b2=findViewById(R.id.button2);
        t1=findViewById(R.id.textView);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t1.setTextSize(font);
                font=font+4;
                if(font==40){
                    font=20;
                }

            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(i){
                    case 1:
                        t1.setTextColor(Color.BLUE);
                        break;
                    case 2:
                        t1.setTextColor(Color.RED);
                        break;
                    case 3:
                        t1.setTextColor(Color.GREEN);
                        break;
                    default:
                        t1.setTextColor(Color.rgb(128,20,30));
                        break;
                }
                i=i+1;

                if(i==5){
                    i=1;
                }

            }
        });

    }
}
