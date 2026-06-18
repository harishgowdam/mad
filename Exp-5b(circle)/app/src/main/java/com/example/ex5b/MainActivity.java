package com.example.ex5b;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.view.View;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(new MyView(this));
    }
    class MyView extends View{
        public MyView(Context context){
            super(context);
        }
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            int x=getWidth();
            int y=getHeight();
            int  radius=100;
            Paint mypaint=new Paint();
            mypaint.setStyle(Paint.Style.FILL);
            mypaint.setColor(Color.WHITE);
            canvas.drawPaint(mypaint);
            mypaint.setColor(Color.GREEN);
            canvas.drawCircle(x/2,y/2,radius,mypaint);
        }
    }
}