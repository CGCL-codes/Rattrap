/*
 * Copyright (C) 2010 Andrew P McSherry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.andymcsherry.library;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

public class GridViewItem extends View{

	Context context;
	int width;
	Drawable picture;
	String text;
	
	public GridViewItem(Context c, int w, int image, String  t) {
		super(c);
		width = w;
		Resources resources = c.getResources();
		picture = resources.getDrawable(image);
		picture.setBounds(0 + width / 8,0,width - width / 8, width * 3 / 4);
		text = t;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		picture.draw(canvas);
		Paint paint = new Paint();
		paint.setColor(Color.rgb(0,221,238));
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize(width /9);
		int index = text.indexOf("\n");
		if(index != -1){
			String s1 = text.substring(0,index);
			String s2 = text.substring(index+1);
			canvas.drawText(s1,width/2,width*(float).8125,paint);
			canvas.drawText(s2,width/2,width*(float).9375,paint);
		}else{
			canvas.drawText(text, width/2, width * 7/8, paint);
		}
	}

	@Override
	public void onMeasure(int w, int h){
		setMeasuredDimension(width,width);
	}
}
