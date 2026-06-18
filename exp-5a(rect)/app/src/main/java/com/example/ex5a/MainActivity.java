package com.example.ex5a;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.content.Context;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(new MyView(this));



    }
    class MyView extends View{
        public  MyView(Context context){
            super(context);
        }
        @Override
        protected void onDraw(Canvas canvas){
            Paint mypaint=new Paint();
            mypaint.setColor(Color.GREEN);
            mypaint.setStyle(Paint.Style.STROKE);
            mypaint.setStrokeWidth(5);
            canvas.drawRect(100,100,500,400,mypaint);
        }

    }

}