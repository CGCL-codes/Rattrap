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

import com.andymcsherry.library.R;

import Jama.Matrix;
import android.content.Context;
import edu.hws.jcm.data.Constant;
import edu.hws.jcm.data.Expression;
import edu.hws.jcm.data.ParseError;
import edu.hws.jcm.data.Parser;
import edu.hws.jcm.data.ParserContext;
import edu.hws.jcm.data.Variable;
import edu.hws.jcm.functions.ExpressionFunction;

public class AndyMath {
	
	public static Double[][] rom;
	
	public static double arcLength(Context context, String f, String v, String a, String b) throws ParseError {
		Expression fun;
		Variable var = new Variable(v);
		Parser parser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		parser.add(var);
		setUpParser(parser);
		try{
			fun = parser.parse(f);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(), e.context);
		}
		String integrand = "sqrt(1+(" + fun.derivative(var) + ")^2)";
		return integrate(context, integrand, v, a, b);
	}

	/*
	 *  Replaces parser generated derivative of inverse trig, hyperbolic
	 *  and inverse hyperbolic with their actual derivatives
	 *  
	 *  ****KNOWN BUG***** Does not work for multiple instances of the same
	 *  function within the equation
	 */
	private static String cleanUp(String d){
		int index = d.indexOf("arcsinh'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(1/sqrt((";
			int i = index+9;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+9,i-1);
			temp += inside + ")^2+1))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("arccosh'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(1/sqrt((";
			int i = index+9;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+9,i-1);
			temp += inside + ")^2-1))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("arccoth'");
		int index2 = d.indexOf("arctanh'");
		if(index >= 0 || index2 >=0){
			if(index2>=0){
				index = index2;
			}
				
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(1/((";
			int i = index+9;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+9,i-1);
			temp += inside + ")^2+1))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("arcsech'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(-1/((";
			int i = index+9;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+9,i-1);
			temp += inside + ")(" + inside + "+1)sqrt((1-" + inside + ")/(1+" + inside + "))))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("arccsch'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(-1/((";
			int i = index+9;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+9,i-1);
			temp += inside + ")^2*sqrt(1+1/(" + inside + ")^2)))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("sinh'");
		if (index >= 0) {
			String temp = "";
			if (index != 0)
				temp = d.substring(0, index - 1);
			temp += "cosh";
			temp += d.substring(index + 5);
			d = temp;
		}
		index = d.indexOf("cosh'");
		if (index >= 0 && !(index > 2 && d.substring(index-3,index).equals("arc"))) {
			String temp = "";
			if (index != 0)
				temp = d.substring(0, index - 1);
			temp += "sinh";
			temp += d.substring(index + 5);
			d = temp;
		}
		
		index = d.indexOf("*1");
		if (index == (d.length() - 2) && d.length() > 2) {
			d = d.substring(0, d.length() - 2);
		}
		index = d.indexOf("tanh'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "sech(";
			int i = index+6;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			temp += d.substring(index+6, i) + "^2";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("coth'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(-csch(";
			int i = index+6;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			temp += d.substring(index+6, i) + "^2)";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("sech'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(-tanh(";
			int i = index+6;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+6,i-1);
			temp += inside + ")*sech(" + inside + "))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("csch'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(-coth(";
			int i = index+6;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+6,i-1);
			temp += inside + ")*csch(" + inside + "))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("arccsc'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(-1/((";
			int i = index+8;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+8,i-1);
			temp += inside + ")*sqrt((" + inside + ")^2-1))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("arcsec'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(1/((";
			int i = index+8;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+8,i-1);
			temp += inside + ")*sqrt((" + inside + ")^2-1))";
			temp += d.substring(i);
			d = temp;
		}
		index = d.indexOf("arccot'");
		if(index >= 0){
			String temp = "";
			if(index != 0)
				temp = d.substring(0,index-1);
			temp += "(-1/((";
			int i = index+8;
			int count;
			for(count = 1; count > 0; i++){
				if(d.charAt(i)=='('){
					count++;
				}else if(d.charAt(i)==')'){
					count --;
				}				
			}
			String inside = d.substring(index+8,i-1);
			temp += inside + ")^2+1))";
			temp += d.substring(i);
			d = temp;
		}
		return d;
	}

	public static String getDerivative(Context context, String fun, String[] var, String resp) throws ParseError{
		Parser parParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		Variable respVar = null;
		for(int i = 0; i < var.length; i++){
			if(var[i].equals(resp)){
				respVar = new Variable(resp);
				parParser.add(respVar);
			}else{
				parParser.add(new Variable(var[i]));
			}
		}
		if(respVar != null){
			try{
				Expression f = parParser.parse(fun);
				return cleanUp(f.derivative(respVar).toString());
			}catch(ParseError e){
				throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(), e.context);
			}
		}
		throw new ParseError(context.getString(R.string.invalidVariable)
					, new ParserContext(resp, 0, null));
	}
	
	public static String getFunctionVal(Context context, String fun, String[] var, String resp, String[] vals) throws ParseError{
		Parser parParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		if(var.length != vals.length){
			throw new ParseError(context.getString(R.string.insufArgs) + " f'.", new ParserContext(resp, 0, null));
		}
		double values[] = new double[vals.length];
		for(int i = 0; i < vals.length; i++){
			values[i] = parParser.parse(vals[i]).getVal();
		}
		Variable respVar = null;		
		for(int i = 0; i < var.length; i++){
			if(var[i].equals(resp)){
				respVar = new Variable(resp);
				parParser.add(respVar);
				respVar.setVal(values[i]);
			}else{
				Variable temp = new Variable(var[i]);
				temp.setVal(values[i]);
				parParser.add(temp);
			}
		}
		if(respVar != null){
			try{
				Expression f = parParser.parse(fun);
				return f.getVal() + "";
			}catch(ParseError e){
				throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(), e.context);
			}
		}	
		throw new ParseError(context.getString(R.string.invalidVariable)
					, new ParserContext(resp, 0, null));
	}
	
	public static double getDerivativeVal(Context context, String f, String v, String s) throws ParseError{
		Parser tempP = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		Variable tempV = new Variable(v);
		tempP.add(tempV);
		setUpParser(tempP);
		tempV.setVal(tempP.parse(s).getVal());
		try{
			Expression tempFun = tempP.parse(f);
			return tempFun.derivative(tempV).getVal();
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(), e.context);
		}
	}

	public static double getFunctionVal(Context context, String f, double s) throws ParseError{
		Parser tempP = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		Variable tempV = new Variable("x");
		tempP.add(tempV);
		setUpParser(tempP);
		tempV.setVal(s);
		try{
			Expression tempFun = tempP.parse(f);
			return tempFun.getVal();
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(), e.context);
		}
	}

	public static String getPowerSeries(Context context, String fun, String v, String cen, int iter)
			throws ParseError {
		Variable var = new Variable(v);
		Parser parser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		parser.add(var);
		setUpParser(parser);
		Expression f;
		try{
			f = parser.parse(fun);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(),e.context);
		}
		Expression cenExp;
		try{
			cenExp = parser.parse(cen);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidCenter) + e.getMessage(),e.context);
		}
		
		String output = "";

		output += f.getVal();
		
		Expression fDer = f.derivative(var);
			for (int i = 1; i <= iter; i++) {
				output += " +\n" + fDer.getVal() + "*";
				if (cenExp.getVal() != 0)
					output += "(";
				output += var;
				if (cenExp.getVal() != 0)
					output += "-" + cen + ")";
				output += "^" + i + " / " + i + "!";
				fDer = fDer.derivative(var);
			}
				return output;
		
	}

	public static double integrate(Context context, String fun, String v, String aS, String bS) throws ParseError {
		Variable var = new Variable(v);
		Parser parser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		parser.add(var);
		setUpParser(parser);
		double a, b;
		try{
			a = parser.parse(aS).getVal();
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidStart) + e.getMessage(), e.context);
		}
		try{
			b = parser.parse(bS).getVal();
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidEnd) + e.getMessage(), e.context);
		}
		
		Expression function;
		try{
			function = parser.parse(fun);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(), e.context);
		}

		rom = new Double[13][13];
		for(int i = 0; i < 13; i++){
			double step = (b-a)/Math.pow(2, i);
			double aTemp = a;
			var.setVal(a);
			double value = function.getVal()/2;
			for(int j = 0; j < Math.pow(2, i) - 1; j++){
				aTemp += step;
				var.setVal(aTemp);
				value += function.getVal();
			}
			var.setVal(b);
			value += function.getVal()/2;
			value *= step;
			rom[i][0] = new Double(value);
		}
		return romberg(12,12);
	}
	
	public static double integrate(Context context, String fun, String v1, String v2, String[] bounds){
		double x1, x2;
		Expression y1, y2, integFun;
		Variable integVarX = new Variable(v1);
		Variable integVarY = new Variable(v2);
		Parser integParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		integParser.add(integVarX);
		integParser.add(integVarY);
		setUpParser(integParser);
		try{
			x1 = integParser.parse(bounds[0]).getVal();
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidStartX) + e.getMessage(), e.context);
		}
		try{
			x2 = integParser.parse(bounds[1]).getVal();
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidEndX) + e.getMessage(), e.context);
		}
		try{
			y1 = integParser.parse(bounds[2]);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidStartY) + e.getMessage(), e.context);
		}
		try{
			y2 = integParser.parse(bounds[3]);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidEndY) + e.getMessage(), e.context);
		}
		try{
			integFun = integParser.parse(fun);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(), e.context);
		}
		int romI = 9;
		Double[][] romX = new Double[romI][romI];
		for(int i = 0; i < romI; i++){
			double step = (x2-x1)/Math.pow(2, i);
			double aTemp = x1;
			rom = new Double[romI][romI];
			
			integVarX.setVal(aTemp);
			double a = y1.getVal();
			double b = y2.getVal();
			
			for(int j = 0; j < romI; j++){
				double stepY = (b-a)/Math.pow(2, j);
				double aTempY = a;
				integVarY.setVal(a);
				double valueY = integFun.getVal()/2;
				for(int k = 0; k < Math.pow(2, j) - 1; k++){
					aTempY += stepY;
					integVarY.setVal(aTempY);
					valueY += integFun.getVal();
				}
				integVarY.setVal(b);
				valueY += integFun.getVal()/2;
				valueY *= stepY;
				rom[j][0] = new Double(valueY);
			}
			double valueX = romberg(romI-1, romI-1)/2;
			
			for(int l = 0; l < Math.pow(2,i)-1; l++){
				rom = new Double[romI][romI];
				aTemp += step;
				integVarX.setVal(aTemp);
				a = y1.getVal();
				b = y2.getVal();
				
				for(int j = 0; j < romI; j++){
					double stepY = (b-a)/Math.pow(2, j);
					double aTempY = a;
					integVarY.setVal(a);
					double valueY = integFun.getVal()/2;
					for(int k = 0; k < Math.pow(2, j) - 1; k++){
						aTempY += stepY;
						integVarY.setVal(aTempY);
						valueY += integFun.getVal();
					}
					integVarY.setVal(b);
					valueY += integFun.getVal()/2;
					valueY *= stepY;
					rom[j][0] = new Double(valueY);
				}
				valueX += romberg(romI-1,romI-1);
			}
			
			integVarX.setVal(x2);
			a = y1.getVal();
			b = y2.getVal();
			rom = new Double[romI][romI];
			
			for(int j = 0; j < romI; j++){
				double stepY = (b-a)/Math.pow(2, j);
				double aTempY = a;
				integVarY.setVal(a);
				double valueY = integFun.getVal()/2;
				for(int k = 0; k < Math.pow(2, j) - 1; k++){
					aTempY += stepY;
					integVarY.setVal(aTempY);
					valueY += integFun.getVal();
				}
				integVarY.setVal(b);
				valueY += integFun.getVal()/2;
				valueY *= stepY;
				rom[j][0] = new Double(valueY);
			}
			valueX += romberg(romI-1, romI-1)/2;
			valueX *= step;
			romX[i][0] = new Double(valueX);
		}
		rom = romX;
		
		return romberg(romI-1,romI-1);
	}

	public static boolean isValid(String fun, String[] var){
		Parser parParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		for(String v: var){
			parParser.add(new Variable(v));
		}
		setUpParser(parParser);
		try{
			parParser.parse(fun);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	public static boolean[] isValid(String[] fun, String[] var){
		Parser parParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		for(String v: var){
			parParser.add(new Variable(v));
		}
		setUpParser(parParser);
		boolean[] b = new boolean[fun.length];
		for(int i = 0; i < fun.length; i++){
			try{
				parParser.parse(fun[i]);
				b[i] = true;
			}catch(Exception e){
				b[i] = false;
			}
		}
		return b;
	}
	
	private static double romberg(int i, int j){
		if(j==0){
			return rom[i][0];
		}else if(rom[i][j] != null){
			return rom[i][j].doubleValue();
		}else{
			double temp = (Math.pow(4,j)*romberg(i,j-1)-romberg(i-1,j-1))/(Math.pow(4, j)-1);
			rom[i][j] = new Double(temp);
			return temp;
		}
	}
	
	public static void setUpParser(Parser p) {
		p.add(new ExpressionFunction("sinh", "(e^x-e^(-x))/2"));
		p.add(new ExpressionFunction("cosh", "(e^x+e^(-x))/2"));
		p.add(new ExpressionFunction("tanh", "(e^x-e^(-x))/(e^x+e^(-x))"));
		p.add(new ExpressionFunction("coth", "(e^x+e^(-x))/(e^x-e^(-x))"));
		p.add(new ExpressionFunction("csch", "1/(e^x-e^(-x))"));
		p.add(new ExpressionFunction("sech", "1/(e^x+e^(-x))"));
		p.add(new ExpressionFunction("arcsinh", "ln(x+sqrt(x^2+1))"));
		p.add(new ExpressionFunction("arccosh", "ln(x+sqrt(x^2-1))"));
		p.add(new ExpressionFunction("arctanh", "ln((1+x)/(ln(1-x)))/2"));
		p.add(new ExpressionFunction("arccsch", "ln(sqrt(1+x^(-2))+1/x)"));
		p.add(new ExpressionFunction("arcsech", "ln(sqrt(x^(-2)-1)+1/x)"));
		p.add(new ExpressionFunction("arccoth", "ln((x+1)/(x-1))/2"));
		p.add(new ExpressionFunction("arccot", "pi/2-arctan(x)"));
		p.add(new ExpressionFunction("arcsec", "arccos(1/x)"));
		p.add(new ExpressionFunction("arccsc", "arcsin(1/x)"));
		p.add(new ExpressionFunction("sign", "x/abs(x)"));
		p.add(new Constant("gol",1.61803398874989484820));
		p.add(new Constant("cc", 299792458));
		p.add(new Constant("gr", 6.6742867 * Math.pow(10, -11)));
		p.add(new Constant("h", 6.6260689633*Math.pow(10,-34)));
		p.add(new Constant("g", 9.80665));
	}
	
	public static double surfaceArea(Context context, String fun, String v1, String v2, String[] bounds) throws ParseError{
		Variable integVarX = new Variable(v1);
		Variable integVarY = new Variable(v2);
		Parser integParser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		integParser.add(integVarX);
		integParser.add(integVarY);
		setUpParser(integParser);
		try{
			String funX = integParser.parse(fun).derivative(integVarX).toString();
			String funY = integParser.parse(fun).derivative(integVarY).toString();
			fun = "sqrt(1+(" + funX + ")^2+(" + funY + ")^2)";
			return integrate(context, fun, v1, v2, bounds);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction) + e.getMessage(), e.context);
		}
	}
	
	public static double getFunctionVal(Context context, String f, String v, String x){
		Variable var = new Variable(v);
		Expression fun,val;
		Parser parser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		parser.add(var);
		setUpParser(parser);
		try{
			fun = parser.parse(f);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction) + ": " + e.getMessage(),e.context);
		}
		try{
			val = parser.parse(x);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidNumber) + ": " + e.getMessage(),e.context);
		}
		var.setVal(val.getVal());
		return fun.getVal();
	}
	

	// (y-y0)=m(x-x0)
	public static String tangentLine(Context context, String f, String x, String v) throws ParseError {
		Variable var = new Variable(v);
		Parser parser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		parser.add(var);
		setUpParser(parser);
		Expression fun;
		try{
			fun = parser.parse(f);
		}catch(ParseError e){
			throw new ParseError(context.getString(R.string.invalidfunction)+ ": " + e.getMessage(), 
					e.context);
		}
		try {
			var.setVal(parser.parse(x).getVal());
			try {
				double m = fun.derivative(var).getVal();
				String out;
				if (m == 1) {
					out = var.getName();
				} else if (m == -1) {
					out = "-" + var.getName();
				} else if (m == 0) {
					out = "";
				} else {
					out = m + "*" + var.getName();
				}
				double b = fun.getVal() - m * var.getVal();
				if (b > 0) {
					if (!out.equals("")) {
						out += "+" + b;
					} else {
						out += b;
					}
				} else if (b < 0) {
					b *= -1;
					out += "-" + b;
				}
				if (out.equals("")) {
					out = "0";
				}
				return out;
			} catch (ParseError e) {
				throw new ParseError(context.getString(R.string.invalidfunction), e.context);
			}
		} catch (ParseError e) {
			throw new ParseError(context.getString(R.string.invalidNumber), e.context);
		}
	}
	
	public static String matrixToString(Matrix m){
		if(m == null){
			return null;
		}
		double[][] tempDouble = m.getArrayCopy();
		String output = "";
		for(int i = 0; i < tempDouble.length; i++){
			for(int j = 0; j < tempDouble[0].length; j++){
				output += tempDouble[i][j] + ",";
			}
			output = output.substring(0,output.length()-1);
			output += "\n";
		}
		output = output.substring(0,output.length()-1);
		return output;
	}
	
	public static Matrix stringToMatrix(String s) throws IllegalArgumentException, ParseError{
		if(s == null || s.equals("")){
			return null;
		}
		Parser parser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
				| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
				| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
		int i = 1;
		int j = 1;
		int index = s.indexOf("\n");
		int firstIndex = index;
		if(firstIndex < 0){
			firstIndex = s.length();
		}
		String temp = s;
		while(index >= 0){
			i++;
			temp = temp.substring(index+2);
			index = temp.indexOf("\n");
		}
		temp = s.substring(0,firstIndex-1);
		index = temp.indexOf(',');
		while(index >=0 && index < firstIndex){
			j++;
			temp = temp.substring(index + 1);
			index = temp.indexOf(',');
		}
		double[][] values = new double[j][i];
		temp = s;
		for(int k = 0; k < j; k++){
			String thisLine;
			if(k != j-1){
				thisLine = temp.substring(0, temp.indexOf("\n"));
			} else {
				thisLine = temp;
			}
			for(int l = 0; l < i; l++){
				if(l == i -1){
					if(thisLine.indexOf(',') >= 0){
						throw new IllegalArgumentException("There are too many elements in row " + (k+1));
					}
					try{
						values[k][l] = parser.parse(thisLine).getVal();
					}catch(ParseError e){
						throw new IllegalArgumentException("Invalid matrix: " + e.getMessage());
					}
				}else{
					if(thisLine.indexOf(',') < 0){
						throw new IllegalArgumentException("There are too few elements in row " + (k+1));
					} else if (thisLine.indexOf(',') == 0) {
						throw new IllegalArgumentException("Missing element in row " + (k+1));
					} else {
						try{
							String thisValue = thisLine.substring(0,thisLine.indexOf(','));
							values[k][l] = parser.parse(thisValue).getVal();
						}catch(ParseError e){
							throw new IllegalArgumentException("Invalid input: " + e.getMessage());
						}
						thisLine = thisLine.substring(thisLine.indexOf(',') + 1);
					}
				}
			}
			if(k != i-1){
				temp = temp.substring(temp.indexOf("\n")+1);
			}
		}
		return new Matrix(values);
	}
}