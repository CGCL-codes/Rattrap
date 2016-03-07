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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;

import edu.hws.jcm.data.Expression;
import edu.hws.jcm.data.ParseError;
import edu.hws.jcm.data.Parser;
import edu.hws.jcm.data.Variable;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GraphView extends View implements OnTouchListener {

	public static final int RECT = 0, POLAR = 1, PARAM = 2, THREED = 3;
	public static Parser constantParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
			| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
			| Parser.BRACES | Parser.BRACKETS);
	public Bitmap graphImage;
	public String[] functions, paramX, paramY;
	public boolean[] graphable = new boolean[] {false,false,false,false,false,false};
	public static final int[][] colors = new int[][] { { 204, 0, 0 }, { 102, 153, 255 },
			{ 255, 102, 0 }, { 0, 204, 0 }, { 255, 204, 0 }, { 0, 51, 153 } };
	public Activity context;
	public String rk,slope;
	public double minX = -3, maxX = 3, minY = -5, maxY = 5, scaleX = 1,
			scaleY = 1, initX = 0, initY = 0, startPolar = -1*Math.PI, endPolar = Math.PI,
			startT = -20, endT = 20;
	public GraphHelper helper;
	public float startX, startY, pinchDist;
	public int width, height, interA, interB, mode;
	private double tracexVal, traceyVal, traceDeriv;
	private int traceFun = -1;
	private boolean allowMove;
	private boolean slopeBool = false, rkBool = false, trace = false,
			deriv = false, choose = false, rect = true, polar = false, param = false, threeD = false;

	public static Method _getX = null, _getY = null, getPointerCount = null;

	static {
		initMultiTouch();
		AndyMath.setUpParser(constantParser);
	}
	
	/*
	 * Fetches methods used from API 5+ for multi-touch for use through
	 * reflection
	 */
	private static void initMultiTouch() {
		try {
			_getX = MotionEvent.class.getMethod("getX", Integer.TYPE);
			_getY = MotionEvent.class.getMethod("getY", Integer.TYPE);
			getPointerCount = MotionEvent.class.getMethod("getPointerCount");
		} catch (Throwable t) {
		}
	}

	public GraphView(int mode, Activity c) {
		super(c);
		context = c;
		setDisplay();
		setMode(mode);
		importFunctions();
		helper = new GraphHelper(this);
	}
	
	private void setDisplay(){
		Display display = context.getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		height = (int) (display.getHeight() * .96);
	}
	
	public void importFunctions(){
		SharedPreferences sp = context.getSharedPreferences("functions", 0);
		if(rect||polar){
			functions = new String[6];
			rk = sp.getString("rkFun", "");
			slope = sp.getString("slopeFun","");
			rkBool = AndyMath.isValid(rk, new String[]{"x","y"});
			slopeBool = AndyMath.isValid(slope,new String[]{"x","y"});
			try{
				initX = constantParser.parse(sp.getString("rkX", "")).getVal();
				initY = constantParser.parse(sp.getString("rkY", "")).getVal();
			}catch(ParseError e){
				initX = 0;
				initY = 0;
			}
		}else{
			paramX = new String[6];
			paramY = new String[6];					
		}
		for(int i = 0; i < 6; i++){
			if(rect){
				functions[i] = sp.getString("f"+(i+1),"");
				
			}else if(polar){
				functions[i] = sp.getString("r"+(i+1),"");
			}else{	
				paramX[i] = sp.getString("x"+(i+1),"");
				paramY[i] = sp.getString("y"+(i+1),"");
			}
		}
	}
    
	/*
	 * Draws a slope field to the graph
	 */
	private void drawDiffy(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.rgb(191, 191, 191));
		for (float i = 0; i < width; i += 24) {
			for (float j = 0; j < height; j += 24) {
				float slope = (float) helper.getSlopeVal(
						getX(i + 12), getY(j + 12));
				if (Math.abs(slope) <= 1) {
					canvas.drawLine(i + 2, j + 12 + slope * 10, i + 22, j + 12
							- slope * 10, paint);
				} else {
					canvas.drawLine(i + 12 + 10 / slope, j + 2, i + 12 - 10
							/ slope, j + 22, paint);
				}
			}
		}
	}

	/*
	 * Redraws the graph
	 */
	public void drawGraph() {
		invalidate();
	}

	private void drawParam(Canvas canvas, double start, double end){
		Paint paint = new Paint();
		for(int i = 0; i < 6; i++){
			if(graphable[i]){
				paint.setColor(Color.rgb(colors[i][0], colors[i][1],
						colors[i][2]));
				double x0 = helper.getXVal(i,start);
				double y0 = helper.getYVal(i,start);
				double x1 = x0, y1 = y0;
				for(double t = start + (end-start)/1000; t < end; t += (end-start)/1000){
					x0 = x1;
					y0 = y1;
					x1 = helper.getXVal(i,t);
					y1 = helper.getYVal(i,t);
					if(y1 != Double.POSITIVE_INFINITY && y0 != Double.POSITIVE_INFINITY && (y1 >= 0 || y1 < 0) && (y0 >= 0 || y0 < 0)){
						if(x1 != Double.POSITIVE_INFINITY && x0 != Double.POSITIVE_INFINITY && (x1 >= 0 || x1 < 0) && (x0 >= 0 || x0 < 0)){
							if(Math.abs(x1-x0) < (maxX-minX)/5){
								if(Math.abs(x1-x0) < (maxX-minX)/5){
										canvas.drawLine(getxPixel(x0),getyPixel(y0),getxPixel(x1)
												,getyPixel(y1), paint);
								}
							}
						}
					}
				}
			}
		}
	}

	private void drawPolar(Canvas canvas, double start, double end){
		Paint paint = new Paint();
		for(int i = 0; i < 6; i++){
			if(graphable[i]){
				paint.setColor(Color.rgb(colors[i][0], colors[i][1],
						colors[i][2]));
				for(double theta = start; theta < (end-(end-start)/200); theta += (end-start)/200){
					double r = helper.getVal(i,theta);
					double newTheta = theta + (end-start)/200;
					double newR = helper.getVal(i,newTheta);
					if(r != Double.POSITIVE_INFINITY && newR != Double.POSITIVE_INFINITY 
							&& (r >= 0 || r < 0) && (newR >= 0 || newR < 0)){
						canvas.drawLine(getPolarX(theta,r),getPolarY(theta,r),getPolarX(newTheta,newR)
							,getPolarY(newTheta,newR), paint);
					}
				}
			}
		}
	}
	
	private void drawRect(Canvas canvas){
		Paint paint = new Paint();
		for (int i = 0; i < 6; i++) {
			if (graphable[i]) {
				paint.setColor(Color.rgb(colors[i][0], colors[i][1],
						colors[i][2]));
				double y1 = helper.getVal(i,minX);
				double y2 = y1;
				for (int j = 0; j < width; j++) {
					double k = j;
					y1 = y2;
					y2 = helper.getVal(i,minX
							+ (k + 1) * (maxX - minX) / width);

					if(y1 != Double.POSITIVE_INFINITY && y2 != Double.POSITIVE_INFINITY && (y1 >= 0 || y1 < 0) && (y2 >= 0 || y2 < 0)){
						if(!((y1 > 20) && (y2 < -20)) && !((y1 < -20) && (y2 > 20))){
							canvas.drawLine(j, getyPixel(y1), j + 1, getyPixel(y2),paint);
						}
					}
				}
			}
		}
	}
	
	/*
	 * Draws a solution to a differential equation to the graph using the
	 * classical Runge-Kutta method
	 */
	private void drawRK(Canvas canvas) {
		// Set paint Color
		Paint paint = new Paint();
		paint.setARGB(255, 128, 0, 128);

		double[] k = new double[4];
		double y = initY;

		// Draws the portion of the graph between x=0 and the right edge
		for (int i = getxPixel(initX); i < width; i++) {
			k[0] = (maxX - minX) / width
					* helper.getRKVal(getX(i), y);
			k[1] = (maxX - minX)
					/ width
					* helper.getRKVal(getX((float) (i + .5)), y
							+ .5 * k[0]);
			k[2] = (maxX - minX)
					/ width
					* helper.getRKVal(getX((float) (i + .5)), y
							+ .5 * k[1]);
			k[3] = (maxX - minX) / width
					* helper.getRKVal(getX(i + 1), y + k[2]);
			double tempY = y + (k[0] + 2 * k[1] + 2 * k[2] + k[3]) / 6;
			canvas.drawLine(i, getyPixel(y), i + 1, getyPixel(tempY), paint);
			y = tempY;
		}
		y = initY;

		// Draws the portion of the graph between x=0 and the left edge
		for (int i = getxPixel(initX); i > 0; i--) {
			k[0] = (maxX - minX) / width
					* helper.getRKVal(getX(i), y);
			k[1] = (maxX - minX)
					/ width
					* helper.getRKVal(getX((float) (i - .5)), y
							+ .5 * k[0]);
			k[2] = (maxX - minX)
					/ width
					* helper.getRKVal(getX((float) (i - .5)), y
							+ .5 * k[1]);
			k[3] = (maxX - minX) / width
					* helper.getRKVal(getX(i - 1), y + k[2]);
			double tempY = y - (k[0] + 2 * k[1] + 2 * k[2] + k[3]) / 6;
			canvas.drawLine(i, getyPixel(y), i - 1, getyPixel(tempY), paint);
			y = tempY;
		}

	}

	/*
	 * Draws either the trace or derivative values to the graph
	 */
	private void drawTrace(Canvas canvas) {
		if (traceFun != -1) {
			// Set color to the same as the function
			Paint paint = new Paint();
			
			paint.setColor(Color.rgb(colors[traceFun][0], colors[traceFun][1],
					colors[traceFun][2]));
			paint.setTextSize((width+height)/50);

			// Rounds values for more concise display to the screen
			double roundX = Math.round(tracexVal * 1000) / 1000.0;
			double roundY = Math.round(traceyVal * 10000) / 10000.0;
			double roundD = Math.round(traceDeriv * 10000) / 10000.0;
			if (trace && !choose) {
				// Draws a small x at the point
				canvas.drawLine(getxPixel(tracexVal) - 8,
						getyPixel(traceyVal) - 8, getxPixel(tracexVal) + 8,
						getyPixel(traceyVal) + 8, paint);
				canvas.drawLine(getxPixel(tracexVal) + 8,
						getyPixel(traceyVal) - 8, getxPixel(tracexVal) - 8,
						getyPixel(traceyVal) + 8, paint);
				
				paint.setColor(Color.rgb(colors[traceFun][0], colors[traceFun][1],
						colors[traceFun][2]));
				// Draws text in the upper left for the function value
				canvas.drawText("f(" + roundX + ")=" + roundY, 20, 20, paint);
			} else if (deriv && !choose) {
				// Draws a line tangent to the curve at the proper point
				canvas.drawLine(getxPixel(tracexVal - (traceyVal - minY)
						/ traceDeriv), height, getxPixel(tracexVal
						+ (maxY - traceyVal) / traceDeriv), 0, paint);

				paint.setColor(Color.rgb(colors[traceFun][0], colors[traceFun][1],
						colors[traceFun][2]));
				// Draws text in the upper right for the function value
				canvas.drawText("f'(" + roundX + ")=" + roundD, 20, 20, paint);

			}
		}
	}

	public int getMode(){
		if(rect){
			return RECT;
		}else if(polar){
			return POLAR;
		}else{
			return PARAM;
		}
	}
	
	public double getParamEnd(){
		return endT;
	}
	
	public double getParamStart(){
		return startT;
	}

	public double getPolarEnd(){
		return endPolar;
	}

	public double getPolarStart(){
		return startPolar;
	}
	
	public float getPolarX(double theta, double r){
		while(theta > Math.PI){
			theta -= Math.PI;
		}
		while(theta < -1*Math.PI){
			theta += Math.PI;
		}
		double xCord = r*Math.cos(theta);
		return getxPixel(xCord);
	}
	
	public float getPolarY(double theta, double r){
		while(theta > Math.PI){
			theta -= Math.PI;
		}
		while(theta < -1*Math.PI){
			theta += Math.PI;
		}
		double yCord = r*Math.sin(theta);
		return getyPixel(yCord);
	}

	/*
	 * Returns the values of all window variables
	 */
	public double[] getWindow() {
		return new double[] { minX, maxX, minY, maxY, scaleX, scaleY };
	}

	/*
	 * Converts an android x-coordinate to its corresponding x-value
	 */
	private double getX(float x) {
		return (x / width) * (maxX - minX) + minX;
	}

	/*
	 * Converts an x-value to its corresponding android x-coordinate
	 */
	private int getxPixel(double x) {
		return (int) (width * (x - minX) / (maxX - minX));
	}

	/*
	 * Converts an android y-coordinate to its corresponding y-value
	 */
	private double getY(float y) {
		return (height - y) * (maxY - minY) / height + minY;
	}

	/*
	 * Converts a y-value to its corresponding android y-coordinate
	 */
	private int getyPixel(double y) {
		return (int) (height - height * (y - minY) / (maxY - minY));
	}

	public boolean isEmpty(){
		for(boolean b:graphable){
			if(b) return false;
		}
		return true;
	}

	/*
	 * Called when the user makes a multi-touch gesture in api 5+ Currently only
	 * does pinch to zoom
	 */
	private void multiTouchMove(MotionEvent event) {
		try {
			float x = startX - event.getX();
			float y = event.getY() - startY;
			double difX = (maxX - minX) * x / width;
			double difY = (maxY - minY) * y / height;
			minX += difX;
			maxX += difX;
			minY += difY;
			maxY += difY;
			startX = event.getX();
			startY = event.getY();
			invalidate();
		} catch (Throwable t) {

		}
	}

	/*
	 * Called when the invalidate is called
	 */
	@Override
	protected void onDraw(Canvas original) {
		helper = new GraphHelper(this);
		graphImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(graphImage);
		// Set color for axis and scale
		Paint paint = new Paint();
		paint.setARGB(255, 255, 255, 255);
		paint.setTextSize((width+height)/75);
		Paint paint2 = new Paint();
		paint2.setARGB(50, 255, 255, 255);

		int j = 0;
		if((maxY-minY) > 1){
			while(Math.log10(maxX-minX)/Math.log10(5)>j){
				j++;
			}
			j-=2;
		}else{
			while(Math.log10(maxX-minX)/Math.log10(5)<j){
				j--;
			}
			j-=1;
		}
		Log.e("graph",j+"");
		if(!threeD){
			// Draws notches and numbers for the horizontal x-scale
			for (int i = 1; i<20; i++) {
				double x1Real = i*Math.pow(5,j);
				double x2Real = -i*Math.pow(5,j);
				int x1 = getxPixel(x1Real);
				int x2 = getxPixel(x2Real);
				int y = getyPixel(0);
				int yText = 18;
				if (y < 0) {
					y = 0;
				} else if (y > height - 20) {
					y = height;
					yText = -18;
				}
				if(polar){
					x2 = x1;
				}
				Log.e(x1+"",x2+"");
				canvas.drawLine(x1, 0, x1, height, paint2);
				canvas.drawLine(x2, 0, x2, height, paint2);
				canvas.drawLine(x1, y - 10, x1, y + 10, paint);
				canvas.drawLine(x2, y - 10, x2, y + 10, paint);
				canvas.drawText("" + (new BigDecimal(x1Real)).round(new MathContext(4)).floatValue(), x1, y + yText, paint);
				canvas.drawText("" + (new BigDecimal(x2Real)).round(new MathContext(4)).floatValue(), x2, y + yText, paint);
			}
			
			
			// Draws notches and numbers for the vertical y-scale
			j = 0;
			if((maxY-minY) > 1){
				while(Math.log10(maxY-minY)/Math.log10(5)>j){
					j++;
				}
				j-=2;
			}else{
				while(Math.log10(maxY-minY)/Math.log10(5)<j){
					j--;
				}
				j-=1;
			}
			for (int i = 1; i<20; i++) {
				int x = getxPixel(0);
				double y1Real = i*Math.pow(5,j);
				double y2Real = -i*Math.pow(5,j);
				int y1 = getyPixel(y1Real);
				int y2 = getyPixel(y2Real);
				int xText = 10;
				if (x < 0) {
					x = 0;
				} else if (x > width - 20) {
					x = width;
					xText = -20;
				}
				canvas.drawLine(0, y1, width, y1, paint2);
				canvas.drawLine(0, y2, width, y2, paint2);
				canvas.drawLine(x - 10, y1, x + 10, y1, paint);
				canvas.drawLine(x - 10, y2, x + 10, y2, paint);
				canvas.drawText("" + (new BigDecimal(y1Real)).round(new MathContext(4)).floatValue()/*((int)Math.round(getY(y1)/Math.pow(5,j-3)))*Math.pow(5,j-3)*/, x + xText, y1, paint);
				if(polar){
					y2 = y1;
				}
				canvas.drawText("" + (new BigDecimal(y2Real)).round(new MathContext(4)).floatValue(), x + xText, y2, paint);
			}

			// Draws the axis
			int x0 = getxPixel(0);
			int y0 = getyPixel(0);
			canvas.drawLine(x0, 0, x0, height, paint);
			canvas.drawLine(0, y0, width, y0, paint);
		}
		// Draws all the functions to the graph
		if(rect){
			drawRect(canvas);
		}else if(polar){
			drawPolar(canvas, startPolar, endPolar);
		}else if(param){
			drawParam(canvas, startT, endT);
		}
		// Draws slope field
		if (slopeBool)
			drawDiffy(canvas);
		// Draws diffy-q solution
		if (rkBool)
			drawRK(canvas);
		// Draws trace and derivative values and graphics
		if(!choose){
			if (trace || deriv){
			drawTrace(canvas);
			}
		}
		original.drawBitmap(graphImage, 0, 0, paint);
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return this.onTouchEvent(event);
	}

	/*
	 * Called when the user touches the screen
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// On the initial press
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// If the trace or derivative is turned on
			if (trace || deriv) {
				if(!choose){
					tracexVal = getX(event.getX());
					traceyVal = helper.getVal(traceFun,tracexVal);
					traceDeriv = helper.getDerivative(traceFun,tracexVal);
					invalidate();
				}
			} else {
				// Checks to see if api >=5
				if (_getX != null) {
					try {
						// Sets initial distance for multi-touch gesture
						if (((Integer) getPointerCount.invoke(event))
								.intValue() == 2) {
							pinchDist = spacing(event);
							// Otherwise sets initial distance for a simple move
						} else {
							allowMove = true;
							startX = ((Float) _getX.invoke(event,
									new Integer(0))).floatValue();
							startY = ((Float) _getY.invoke(event,
									new Integer(0))).floatValue();
						}
					} catch (Throwable t) {

					}
					// If api < 5 sets initial distance for move
				} else {
					startX = event.getX();
					startY = event.getY();
					allowMove = true;
				}
			}
			return true;
			// If the user has moved their finger
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// Moves the trace or derivative
			if (trace || deriv) {
				if(!choose){
					if(rect){
					tracexVal = getX(event.getX());
					traceyVal = helper.getVal(traceFun,tracexVal);
					traceDeriv = helper.getDerivative(traceFun, tracexVal);
					}else if(polar){
						double x = getX(event.getX());
						double y = getY(event.getY());
						if(x > 0){
							tracexVal = Math.atan(y/x);
						}else{
							if(y > 0){
								tracexVal = Math.PI + Math.atan(y/x);
							}else{
								tracexVal = Math.PI + Math.atan(y/x);
							}
						}
					}
					invalidate();
				}
			} else {
				if (_getX != null) {// api >= 5
					try {
						// multi touch
						if (((Integer) getPointerCount.invoke(event))
								.intValue() == 2) {
							float tempDist = spacing(event);

							if (tempDist > (width+height)/4 && pinchDist > (width+height)/4) {
								zoom((pinchDist-tempDist) / pinchDist);
							}
							pinchDist = tempDist;
						} else {// not multi touch
							if (allowMove)
								multiTouchMove(event);
						}
					} catch (IllegalArgumentException e) {
					} catch (Throwable t) {
					}
					// api < 5
				} else {
					touchMove(event);
				}
			}
			return true;
		//Resets values when users finishes their gesture
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			pinchDist = -1;
			allowMove = false;
			if(choose){
				double x = getX(event.getX());
				double y = getY(event.getY());
				double[] distance = new double[6];
				distance[0] = Double.MAX_VALUE;
				for (int i = 0; i < 6; i++) {
					if (graphable[i]) {
						distance[i] = Math.abs(helper.getVal(i,x) - y);
					}
				}
				int smallest = 0;
				for (int i = 1; i < 6; i++) {
					if (graphable[i]) {
						if (distance[smallest] > distance[i]) {
							smallest = i;
						}
					}
				}
				traceFun = smallest;
			}
			choose = false;
		}

		return false;
	}
	
	/*
	 * Turns the trace derivative feature on/off
	 */
	public boolean setDeriv(boolean dr) {
		if (!isEmpty()) {
			deriv = dr;
			choose = true;
			invalidate();
			return true;
		}
		deriv = false;
		return false;
	}

	private void setMode(int m){
		if(m==RECT){
			rect = true;
			polar = false;
			param = false;
			threeD = false;
		}else if(m==POLAR){
			rect = false;
			polar = true;
			param = false;
			threeD = false;
		}else if(m==PARAM){
			rect = false;
			polar = false;
			param = true;
			threeD = false;
		}else{
			rect = false;
			polar = false;
			param = false;
			threeD = true;
		}
	}

	public void setParamBounds(double start, double end){
		startT = start;
		endT = end;
	}

	public void setPolarBounds(double start, double end) {
		startPolar = start;
		endPolar = end;		
	}

	/*
	 * Sets the trace feature on/off
	 */
	public boolean setTrace(boolean tr) {
		if (!isEmpty()) {
			trace = tr;
			choose = true;
			invalidate();
			return true;
		}
		trace = false;
		return false;
	}
	/*
	 * Changes the window size and scale
	 */
	public void setWindow(double minx, double miny, double maxx, double maxy,
			double scalex, double scaley) {
		minX = minx;
		minY = miny;
		maxX = maxx;
		maxY = maxy;
		scaleX = scalex;
		scaleY = scaley;
	}

	/*
	 * Returns the distance between the users fingers on a multi-touch
	 */
	public float spacing(MotionEvent event) {
		try {
			Integer arg0 = new Integer(0);
			Integer arg1 = new Integer(1);
			float x = ((Float) _getX.invoke(event, arg0)).floatValue()
					- ((Float) _getX.invoke(event, arg1)).floatValue();
			float y = ((Float) _getY.invoke(event, arg0)).floatValue()
					- ((Float) _getY.invoke(event, arg1)).floatValue();
			return (float) Math.sqrt(x * x + y * y);
		} catch (Throwable t) {
			return Float.NaN;
		}
	}
	
	/*
	 * Moves the graph
	 */
	private void touchMove(MotionEvent event) {
		float x = startX - event.getX();
		float y = event.getY() - startY;
		double difX = (maxX - minX) * x / width;
		double difY = (maxY - minY) * y / height;
		minX += difX;
		maxX += difX;
		minY += difY;
		maxY += difY;
		startX = event.getX();
		startY = event.getY();
		invalidate();
	}
	
	/*
	 * Zooms the graph in and out
	 */
	public void zoom(float perc) {
		double realWidth = maxX - minX;
		double realHeight = maxY - minY;
		maxX += realWidth * perc / 2;
		minX -= realWidth * perc / 2;
		minY -= realHeight * perc / 2;
		maxY += realHeight * perc / 2;
		scaleX += scaleX * perc;
		scaleY += scaleY * perc;
		invalidate();
	}
	
	public Bitmap getBitmap(){
		return graphImage;
	}
	
	private class GraphHelper{
		
		Parser simpParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS | Parser.BOOLEANS);
		Parser paramParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		Parser threeDParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		Expression[] rectExp = new Expression[6];
		Expression[] rectDeriv = new Expression[6];
		Expression[][] paramExp = new Expression[2][6];
		Expression[] polarExp = new Expression[6];
		Expression rk, slope;
		Variable simpVar, paramVar, threeDXVar, threeDYVar;
		GraphView graphView;
		
		public GraphHelper(GraphView g){
			threeDXVar = new Variable("x");
			threeDYVar = new Variable("y");
			simpVar = new Variable("x");
			paramVar = new Variable("t");
			graphView = g;
			simpParser.add(simpVar);
			paramParser.add(paramVar);
			AndyMath.setUpParser(simpParser);
			AndyMath.setUpParser(paramParser);
			AndyMath.setUpParser(threeDParser);
			threeDParser.add(threeDXVar);
			threeDParser.add(threeDYVar);
			for(int i = 0; i < 6; i++){
				if(g.rect){
					try{
						rectExp[i] = simpParser.parse(g.functions[i]);
						g.graphable[i] = true;
					}catch(ParseError e){
						g.graphable[i] = false;
					}
					try{
						rectDeriv[i] = rectExp[i].derivative(simpVar);
					}catch(NullPointerException e){}
				}else if(g.polar){
					try{
						polarExp[i] = simpParser.parse(g.functions[i]);
						g.graphable[i]=true;
					}catch(ParseError e){
						g.graphable[i]=false;
					}
				}else{
					try{
						paramExp[0][i] = paramParser.parse(g.paramX[i]);
						paramExp[1][i] = paramParser.parse(g.paramY[i]);
						g.graphable[i]=true;
					}catch(ParseError e){
						g.graphable[i]=false;
					}
				}				
			}
			if(g.rect){
				try{
					rk = threeDParser.parse(g.rk);
				}catch(ParseError e){}
				try{
					slope = threeDParser.parse(g.slope);
				}catch(ParseError e){
					slope = threeDParser.parse("1");
				}
			}
		}
		
		public double getVal(int i, double val){
			if(graphView.getMode() == GraphView.RECT){
				simpVar.setVal(val);
				return rectExp[i].getVal();
			}else if(graphView.getMode() == GraphView.POLAR){
				simpVar.setVal(val);
				return polarExp[i].getVal();
			}
			return 0;
		}
		
		public double getXVal(int i, double val){
			paramVar.setVal(val);
			return paramExp[0][i].getVal();
		}
		
		public double getYVal(int i, double val){
			paramVar.setVal(val);
			return paramExp[1][i].getVal();
		}
		
		public double getRKVal(double x, double y){
			threeDXVar.setVal(x);
			threeDYVar.setVal(y);
			return rk.getVal();
		}
		
		public double getSlopeVal(double x, double y){
			threeDXVar.setVal(x);
			threeDYVar.setVal(y);
			return slope.getVal();
		}
		
		public double getDerivative(int i, double val){
			simpVar.setVal(val);
			return rectDeriv[i].getVal();
		}
	}
}