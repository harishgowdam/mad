package com.example.ex2;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button b1;
    EditText t1,t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
               t1=findViewById(R.id.editTextText1);
               t2=findViewById(R.id.editTextText2);
               b1=findViewById(R.id.button1);
               b1.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       String v1=t1.getText().toString();
                       String v2=t2.getText().toString();
                       int a=Integer.parseInt(v1);
                       int b=Integer.parseInt(v2);
                       int sum=a+b;
                       Toast.makeText(MainActivity.this,"Sum of Two Number"+sum,
                               Toast.LENGTH_LONG).show();

                   }
               });


    }
}