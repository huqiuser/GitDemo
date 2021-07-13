package com.example.surfacetexture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

public class RectView extends View{
	private int width;
	private int height;
	private int top,bottom,left,right;
	private Paint pen;
	public RectView(Context context,int width,int height) {
		super(context);
		this.width=248;
		this.height=64;
		this.bottom=(height+this.height)/2;
		this.top=(height-this.height)/2;
		this.left=(width-this.width)/2;
		this.right=(width+this.width)/2;
		pen = new Paint();
		pen.setColor(Color.RED);
		pen.setStrokeWidth(3);
		pen.setStyle(Style.STROKE);
	}
	public Paint getPen()
	{
		return this.pen;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(left, top, right, bottom,pen);
	}
}
