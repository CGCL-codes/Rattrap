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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class KeyboardView extends View {
	
	AndyActivity calc;
	int height, width, columns, rows;
	final static int LANDSCAPE=Configuration.ORIENTATION_LANDSCAPE,
	                  PORTRAIT=Configuration.ORIENTATION_PORTRAIT;
	float startX, startY, shift  = 0;
	int orientation;
	int buttonH, buttonW;
	boolean shiftable = false;
	
	public static final int CALCULATOR = 0;
	
	public static boolean vibrate = true;;
	
	static String[][] screen1 = {{"x","y","pi" ,"e","del"},
		                         {"7","8","9"  ,"*","^"},
		                         {"4","5","6",  "/","("},
		                         {"1","2","3",  "+",")"},
		                         {"0",".","ans","-","ENTER"}};
	static String[][] screen2 = {{"sin","cos","tan","sqrt","del"},
		                         {"csc","sec","cot","ln","log10"},
		                         {"arcsin","arccos","arctan","fint","abs"},
		                         {"sinh","cosh","tanh","det","inv"},
		                         {"csch","sech","coth","eval","evec"}};
	
	static String[][] screen3 = {{"=",">","<",">=","<=","&","|","~",":","?"},
		                         {"q","w","e","r","t","y","u","i","o","p"},
								 {"a","s","d","f","g","h","j","k","l"},
								 {"z","c","x","v","b","n","m",".",","},
								 {"(",")","space","del"}};
	
	static String[][] screen1L ={{"x","7","8","9","pi","e","^","del"},
		                         {".",  "4","5","6", "+","-","(","ans"},
		                         {"0",  "1","2","3", "*","/",")",  "ENTER"}};
	
	static String[][] screen2L ={{"sin","csc","arcsin","sinh","csch","sqrt","ln","del"},
                                 {"cos","sec","arccos","cosh","sech","fint","abs","det"},
                                 {"tan","cot","arctan","tanh","coth","inv","eval","evec"}};
	
	static String[][] screen3L ={{"=",">","<",">=","<=","&","|","~",":","?"},
                                 {"q","w","e","r","t","y","u","i","o","p"},
                                 {"a","s","d","f","g","h","j","k","l"},
                                 {",","z","x","v","b","n","m",".","del"}};
	
	static int[] buttonShade =  {Color.rgb(200,200,200),
		                         Color.rgb(80,80,80),
		                         Color.rgb(50,50,50),
		                         Color.rgb(80,80,80),
		                         Color.rgb(200,200,200)};
	
	static int[][] screen1Val = 
	   {{KeyEvent.KEYCODE_X,KeyEvent.KEYCODE_Y,-20,KeyEvent.KEYCODE_E,KeyEvent.KEYCODE_BACK},
		{KeyEvent.KEYCODE_7,KeyEvent.KEYCODE_8,KeyEvent.KEYCODE_9,KeyEvent.KEYCODE_STAR,KeyEvent.KEYCODE_POWER},
		{KeyEvent.KEYCODE_4,KeyEvent.KEYCODE_5,KeyEvent.KEYCODE_6,KeyEvent.KEYCODE_SLASH,KeyEvent.KEYCODE_LEFT_BRACKET},
		{KeyEvent.KEYCODE_1,KeyEvent.KEYCODE_2,KeyEvent.KEYCODE_3,KeyEvent.KEYCODE_PLUS,KeyEvent.KEYCODE_RIGHT_BRACKET},
		{KeyEvent.KEYCODE_0,KeyEvent.KEYCODE_PERIOD,-22,KeyEvent.KEYCODE_MINUS,KeyEvent.KEYCODE_EQUALS}};
	
	String[][] cScreen;
	int current;

	public KeyboardView(Context c) {
		super(c);
		calc = (AndyActivity)c;
		DisplayMetrics metrics = new DisplayMetrics();
		calc.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		orientation = calc.getResources().getConfiguration().orientation;
		width = metrics.widthPixels;
		height = metrics.heightPixels * 4/5;
		if(orientation==LANDSCAPE){
			cScreen = screen1L;
		}else{
			cScreen = screen1;
		}
		current = 1;
		rows = cScreen.length;
		vibrate = calc.getSharedPreferences("options", 
				Context.MODE_PRIVATE).getBoolean("haptic", true);
	}

	public KeyboardView(Context c, AttributeSet a) {
		super(c, a);
		calc = (AndyActivity)c;
		DisplayMetrics metrics = new DisplayMetrics();
		calc.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		orientation = calc.getResources().getConfiguration().orientation;
		width = metrics.widthPixels;
		height = metrics.heightPixels * 9 / 20;
		if(orientation==LANDSCAPE){
			cScreen = screen1L;
		}else{
			cScreen = screen1;
		}
		current = 1;
		rows = cScreen.length;
		vibrate = calc.getSharedPreferences("options", 
				Context.MODE_PRIVATE).getBoolean("haptic", true);
	}
	
	@Override
	public void onMeasure(int w, int h){
		if(getVisibility()==View.GONE){
			setMeasuredDimension(0,0);
		}else{
			setMeasuredDimension(width,height);
		}
	}
	
	
	@Override
	public void onDraw(Canvas canvas){
		if(getVisibility()==View.GONE){
		}else{
			buttonH = height/rows;
			Paint paint = new Paint();
			paint.setTextAlign(Paint.Align.CENTER);
			int textSize = (width+height)/33;
			paint.setTextSize(textSize);
			paint.setColor(Color.rgb(255, 255, 255));
			Bitmap[][] buttons = new Bitmap[rows][];
			int spacing = (width+height)/400;
			LinearGradient gradient = new LinearGradient(buttonW/2,0,buttonW/2,buttonH,
					buttonShade,null,Shader.TileMode.REPEAT);
			for(int i = 0; i < rows; i++){
				int thisColumns = cScreen[i].length;
				buttons[i] = new Bitmap[thisColumns];
				buttonW = width/thisColumns;
				for(int j = 0; j < thisColumns; j++){
					buttons[i][j] = Bitmap.createBitmap(buttonW,buttonH, Bitmap.Config.RGB_565);
					Canvas temp = new Canvas(buttons[i][j]);
					paint.setShader(gradient);
					RectF rect = new RectF(spacing,spacing,buttonW-spacing,buttonH-spacing);
					temp.drawRoundRect(rect, width/100,height/100,paint);
					paint.setShader(null);
					temp.drawText(cScreen[i][j],buttonW/2,(buttonH+textSize)/2,paint);
					canvas.drawBitmap(buttons[i][j], buttonW*j+shift,buttonH*i, paint);
				}					
			}
			if(shift != 0){
				String[][] newScreen;
				float newShift;
				if(shift > 0){
					newScreen = getRight(false);
					newShift = shift-width;
				}else{
					newScreen = getLeft(false);
					newShift = shift+width;
				}
				int newRows = newScreen.length;
				int newButtonH = height/newRows;
				buttons = new Bitmap[newRows][];
				spacing = (width+height)/400;
				for(int i = 0; i < newRows; i++){
					int thisColumns = newScreen[i].length;
					buttons[i] = new Bitmap[thisColumns];
					int newButtonW = width/thisColumns;
					for(int j = 0; j < thisColumns; j++){
						buttons[i][j] = Bitmap.createBitmap(newButtonW,newButtonH, Bitmap.Config.RGB_565);
						Canvas temp = new Canvas(buttons[i][j]);
						paint.setShader(gradient);
						RectF rect = new RectF(spacing,spacing,newButtonW-spacing,newButtonH-spacing);
						temp.drawRoundRect(rect, width/100,height/100,paint);
						paint.setShader(null);
						temp.drawText(newScreen[i][j],newButtonW/2,(newButtonH+textSize)/2,paint);
						canvas.drawBitmap(buttons[i][j], newButtonW*j+newShift,newButtonH*i, paint);
					}					
				}
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(getVisibility()==View.GONE){
			return true;
		}
		if(event.getAction()==MotionEvent.ACTION_UP){
			int[] posEnd = getPos(event.getX(),event.getY());
			int[] posStart = getPos(startX, startY);
			if(posEnd[0] == posStart[0] && posEnd[1] == posStart[1]){
				String s = cScreen[posEnd[1]][posEnd[0]];
				if(current == 2){
					s += "(";
				}
				calc.type(s);
				if(vibrate){
					((Vibrator) calc.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(20);
				}
			}else{
				if(Math.abs(startX-event.getX()) > Math.abs(startY-event.getY())){
					if(startX - width/3> event.getX()){
						cScreen=getLeft(true);
						rows = cScreen.length;
						columns = cScreen[0].length;
						buttonW = width/columns;
						buttonH = height/rows;
					}else if(startX + width/3< event.getX()){
						cScreen=getRight(true);
						rows = cScreen.length;
						columns = cScreen[0].length;
						buttonW = width/columns;
						buttonH = height/rows;
					}
				}else if(startY + height/5 < event.getY()){
					setVisibility(View.GONE);
				}
			}
			shift=0;
			shiftable = false;
			invalidate();
		}else if(event.getAction()==MotionEvent.ACTION_DOWN){
			startX = event.getX();
			startY = event.getY();
		}else if(event.getAction()==MotionEvent.ACTION_MOVE){
			if(Math.abs(startX-event.getX()) > width/5 || shiftable){
				shift = event.getX()-startX;
				shiftable = true;
				invalidate();
			}
		}
		return true;
	}
	
	public int[] getPos(float x, float y){
		int[] pos = new int[2];
		pos[1] = (int)(y/height*rows);
		pos[1] = Math.max(0, pos[1]);
		pos[1] = Math.min(pos[1],rows-1);
		pos[0] = (int)(x/width*cScreen[pos[1]].length);
		pos[0] = Math.max(0, pos[0]);
		pos[0] = Math.min(pos[0], cScreen[pos[1]].length);
		return pos;
	}
	
	public String[][] getLeft(boolean changed){
		if(current == 1){
		    if(changed)current = 2;
			if(orientation==LANDSCAPE){
				return screen2L;
			}
			return screen2;
		}else if(current == 2){
			if(changed)current = 3;
			if(orientation==LANDSCAPE){
				return screen3L;
			}
			return screen3;
		}else{
			if(changed)current = 1;
			if(orientation==LANDSCAPE){
				return screen1L;
			}
			return screen1;
		}
	}
	
	public String[][] getRight(boolean changed){
		if(current == 3){
			if(changed) current=2;
			if(orientation==LANDSCAPE){
				return screen2L;
			}
			return screen2;
		}else if(current == 1){
			if(changed) current=3;
			if(orientation==LANDSCAPE){
				return screen3L;
			}
			return screen3;
		}else{
			if(changed)current=1;
			if(orientation==LANDSCAPE){
				return screen1L;
			}
			return screen1;
		}
	}
}
