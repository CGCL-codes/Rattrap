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

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;

public class Graph3DView extends GLSurfaceView {

	private Graph3DRenderer renderer;
	Activity context;
	float startX=-1, startY=-1, startX2=-1, startY2=-1, pinchDist = 0;
	int width, height;
	
	public static Method _getX = null, _getY = null, getPointerCount = null;

	static {
		initMultiTouch();
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
	
	private void setDisplay(){
		Display display = context.getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		height = (int) (display.getHeight() * .96);
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
	
	public Graph3DView(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = (Activity)c;
		renderer = new Graph3DRenderer(context);
		setDisplay();
		setRenderer(renderer);
	}
	
	public Graph3DView(Context c){
		super(c);
		context = (Activity)c;
		renderer = new Graph3DRenderer(context);
		setDisplay();
		setRenderer(renderer);
	}
	
	public void zoom(float z){
		final float zoom = z;
		queueEvent(new Runnable(){
			@Override
			public void run() {
				renderer.scale *= (1+zoom);
				renderer.dirty = true;
			}			
		});
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			
				// Checks to see if api >=5
				if (_getX != null) {
					try {
						// Sets initial distance for multi-touch gesture
						if (((Integer) getPointerCount.invoke(event))
								.intValue() == 2) {
							pinchDist = spacing(event);
							// Otherwise sets initial distance for a simple move
						} else {
							renderer.stopRotate();
							startX = ((Float) _getX.invoke(event,
									new Integer(0))).floatValue();
							startY = ((Float) _getY.invoke(event,
									new Integer(0))).floatValue();
						}
					} catch (Throwable t) {

					}
					// If api < 5 sets initial distance for move
				} else {
					renderer.stopRotate();
					startX = event.getX();
					startY = event.getY();
				}
			return true;
			// If the user has moved their finger
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// Moves the trace or derivative
			
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
							renderer.move((event.getX()-startX)/width*360,
									      (event.getY()-startY)/height*360);
							startX=event.getX();
							startY=event.getY();
						}
					} catch (IllegalArgumentException e) {
					} catch (Throwable t) {
					}
					// api < 5
				} else {
					renderer.move((event.getX()-startX)/width*360,
						      (event.getY()-startY)/height*360);
					startX=event.getX();
					startY=event.getY();
				}
			return true;
		//Resets values when users finishes their gesture
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			pinchDist = -1;
			startX = -1; 
			startY=-1;
		}
		return true;
	}

}
