package com.knziha.plod.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.Shape;

public class SpeedTagShape extends Shape {
    int shape;
    float heightRatio;
    Path path = new Path();

    public SpeedTagShape(int shape_, float heightRatio_){
        shape = shape_;
		heightRatio = heightRatio_;
    }

    @Override
    protected void onResize(float width, float height) {
        float deltaY=0;
        float pendingH = heightRatio==0?width*1.f/12*7:(heightRatio*height);
        if(height>pendingH){
            deltaY=height/2-pendingH/2;
            height=pendingH;
        }
		if (shape == 0) {
			path.reset();
			path.moveTo(0, deltaY);
			path.lineTo(width * 1.f / 4 * 3, deltaY);
		
			path.lineTo(width, height / 2 + deltaY);
		
			path.lineTo(width * 1.f / 4 * 3, height + deltaY);
			path.lineTo(0, height + deltaY);
			path.close();
		} else if (shape == 1) {
			path.reset();
			path.moveTo(width, deltaY);
			path.lineTo(width * 1.f / 4 * 1, deltaY);
		
			path.lineTo(0, height / 2 + deltaY);
		
			path.lineTo(width * 1.f / 4 * 1, height + deltaY);
			path.lineTo(width, height + deltaY);
			path.close();
		}
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(path, paint);
    }
}
