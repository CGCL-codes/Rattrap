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

import Jama.Matrix;

public class AndyObject {

	public static int
	    FUNCTION = 0,
	    MATRIX = 1,
	    VALUE = 2,
	    UNKNOWN = 3;
	
	public int type;
	public String content;
	public Double value;
	public Matrix matrix;
	
	public AndyObject(String c, int t){
		content = c;
		type = t;
	}
	
	public AndyObject(Matrix m) throws IllegalArgumentException{
		if(m != null){
			matrix = m;
			content = AndyMath.matrixToString(m);
			type = MATRIX;
		}else{
			throw new NullPointerException("Matrix is null.");
		}
	}
	
	public AndyObject(double v, int t){
		type = t;
		value = v;
		if(Math.abs(value) < Math.pow(10,-12)){
			value = 0.0;
		}
		content = "" + value;
	}
	
	public AndyObject(String c){
		content = c;
		type = UNKNOWN;
	}
	
	public AndyObject(double v) {
		type = VALUE;
		value = v;
		content = "" + value;
		if(Math.abs(value) < Math.pow(10,-15)){
			value = 0.0;
		}
	}
	
	

	public Matrix getMatrix() throws IllegalArgumentException{
		if(type == MATRIX){
			return matrix;
		}
		throw new IllegalArgumentException(value + " is required to be a matrix for this calculation.");
	}
	
	public double getParsable(){
		return value.doubleValue();
	}
	
	@Override
	public String toString(){
		return content;
	}
}
