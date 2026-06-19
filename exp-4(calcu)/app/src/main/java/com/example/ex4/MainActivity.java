package com.example.ex4;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    Button b0,b1,b2,b3,b4,b5,b6,b7,b8,b9;
    Button badd,bsub,bmul,bdiv,bdot,beq,bc;
    boolean add,sub,mul,div;
    int val1,val2;
    EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        et=findViewById(R.id.editTextText1);
        b1=findViewById(R.id.button1);
        b2=findViewById(R.id.button2);
        b3=findViewById(R.id.button3);
        b4=findViewById(R.id.button4);
        b5=findViewById(R.id.button5);
        b6=findViewById(R.id.button6);
        b7=findViewById(R.id.button7);
        b8=findViewById(R.id.button8);
        b9=findViewById(R.id.button9);
        b0=findViewById(R.id.button10);

        bdot=findViewById(R.id.button11);
        badd=findViewById(R.id.button12);
        bsub=findViewById(R.id.button13);
        bmul=findViewById(R.id.button14);
        bdiv=findViewById(R.id.button15);
        beq=findViewById(R.id.button16);
        bc=findViewById(R.id.button17);

        b1.setOnClickListener(v -> et.append("1"));
        b2.setOnClickListener(v -> et.append("2"));
        b3.setOnClickListener(v -> et.append("3"));
        b4.setOnClickListener(v -> et.append("4"));
        b5.setOnClickListener(v -> et.append("5"));
        b6.setOnClickListener(v -> et.append("6"));
        b7.setOnClickListener(v -> et.append("7"));
        b8.setOnClickListener(v -> et.append("8"));
        b9.setOnClickListener(v -> et.append("9"));
        b0.setOnClickListener(v -> et.append("0"));
        bdot.setOnClickListener(v -> et.append("."));

        badd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                val1=Integer.parseInt(et.getText().toString());
                add=true;
                et.setText("");
            }
        });
        bsub.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (et.getText().toString().isEmpty()) {
                    return;
                }
                val1=Integer.parseInt(et.getText().toString());
                sub=true;
                et.setText("");
            }
        });
        bmul.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (et.getText().toString().isEmpty()) {
                    return;
                }
                val1=Integer.parseInt(et.getText().toString());
                mul=true;
                et.setText("");
            }
        });
        bdiv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (et.getText().toString().isEmpty()) {
                    return;
                }
                val1=Integer.parseInt(et.getText().toString());
                div=true;
                et.setText("");
            }
        });
        beq.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (et.getText().toString().isEmpty()) {
                    return;
                }
                val2=Integer.parseInt(et.getText().toString());
                if(add){
                    et.setText(String.valueOf(val1+val2));
                    add=false;
                }
                if(sub){
                    et.setText(String.valueOf(val1-val2));
                    sub=false;
                }
                if(mul){
                    et.setText(String.valueOf(val1*val2));
                    mul=false;
                }
                if(div){
                    if(val2==0){
                        et.setText("Error");
                    }
                    else{
                        et.setText(String.valueOf(val1/val2));
                    }

                    div=false;
                }

            }
        });
        bc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et.setText("");
            }
        });
    }
}