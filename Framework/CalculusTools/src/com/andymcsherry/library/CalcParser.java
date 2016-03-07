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

import android.content.Context;
import edu.hws.jcm.data.ParseError;
import edu.hws.jcm.data.Parser;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class CalcParser {
	
	public static AndyObject answer;
	
	public static Parser parser = new Parser(Parser.STANDARD_FUNCTIONS | Parser.OPTIONAL_PARENS
			| Parser.OPTIONAL_STARS | Parser.OPTIONAL_SPACES
			| Parser.BRACES | Parser.BRACKETS| Parser.BOOLEANS);
	
	public static AndyObject calculate(Context context, AndyObject o) throws IllegalArgumentException{
		String exp = o.content.toLowerCase();
		try{
			double value = parser.parse(o.content).getVal();
			return new AndyObject(value,AndyObject.VALUE);
		}catch(ParseError e){	
		}catch(NullPointerException e){	
		}
		
		/*
		 * Check for addition / subtraction
		 */
		int thisLength = exp.length();
		int parans = 0;
		if(exp.charAt(0) == '+'){
			throw new IllegalArgumentException(context.getString(R.string.unfinished) + " +.");
		}
		for(int i = 0; i < exp.length(); i++){
			if(parans == 0){
				if(exp.charAt(i) == '+' || exp.charAt(i) == '-'){
					thisLength = i;
					break;
				}
			} 
			if (exp.charAt(i) == '('){
				parans++;
			} else if(exp.charAt(i) == ')'){
				parans--;
			}
		}
		if(parans > 0){
			throw new IllegalArgumentException(exp+ "   " +context.getString(R.string.extraclose));
		}else if (parans < 0){
			throw new IllegalArgumentException(context.getString(R.string.extraopen));
		}
		if(thisLength != exp.length()){
			if(thisLength != exp.length() - 1){
				AndyObject firstTerm = new AndyObject(exp.substring(0,thisLength));
				AndyObject remainder = new AndyObject(exp.substring(thisLength+1));
				if(exp.charAt(thisLength) == '+'){
					return add(context, firstTerm, remainder);
				}	
				return subtract(context, firstTerm, remainder);
			}
			throw new IllegalArgumentException(context.getString(R.string.unfinished) + "+/-.");
		}
		/*
		 * Check for multiplication *
		 */
		thisLength = exp.length();
		parans = 0;
		if(exp.charAt(0) == '*'){
			throw new IllegalArgumentException(context.getString(R.string.unfinished) + " *.");
		}
		for(int i = 1; i < exp.length(); i++){
			if(parans == 0){
				if(exp.charAt(i) == '*'){
					if(exp.charAt(i+1) != '*'){
						thisLength = i;
						break;
					}
					i++;
				}
			} 
			if (exp.charAt(i) == '('){
				parans++;
			} else if(exp.charAt(i) == ')'){
				parans--;
			}
		}
		if(thisLength != exp.length()){
			if(thisLength != exp.length() - 1){
				AndyObject firstTerm = new AndyObject(exp.substring(0,thisLength));
				AndyObject remainder = new AndyObject(exp.substring(thisLength+1));
				return multiply(context, firstTerm, remainder);
			}
			throw new IllegalArgumentException(context.getString(R.string.unfinished) + " *.");
		}
		
		/*
		 * Check for division /
		 */
		thisLength = exp.length();
		parans = 0;
		if(exp.charAt(0) == '/'){
			throw new IllegalArgumentException(context.getString(R.string.unfinished) + " *.");
		}
		for(int i = 1; i < exp.length(); i++){
			if(parans == 0){
				if(exp.charAt(i) == '/'){
					if(exp.charAt(i+1) != '/'){
						thisLength = i;
						break;
					}
					i++;
				}
			} 
			if (exp.charAt(i) == '('){
				parans++;
			} else if(exp.charAt(i) == ')'){
				parans--;
			}
		}
		if(thisLength != exp.length()){
			if(thisLength != exp.length() - 1){
				AndyObject firstTerm = new AndyObject(exp.substring(0,thisLength));
				AndyObject remainder = new AndyObject(exp.substring(thisLength+1));
				return divide(context, firstTerm, remainder);
			}
			throw new IllegalArgumentException(context.getString(R.string.unfinished) + " /.");
		}
		
		/*
		 * Check for multiplication by ()()
		 */
		
		if(exp.charAt(0) == '('){
			thisLength = exp.length();
			parans = 1;
			for(int i = 1; i < exp.length(); i++){
				if (exp.charAt(i) == '('){
					parans++;
				} else if(exp.charAt(i) == ')'){
					parans--;
				}
				if(parans == 0){
					thisLength = i;
					break;
				}
			}
			if(thisLength == 1){
				throw new IllegalArgumentException("");
			}
			if(thisLength < exp.length() - 1){
				AndyObject firstTerm = new AndyObject(exp.substring(1,thisLength-1));
				AndyObject remainder = new AndyObject(exp.substring(thisLength));
				return multiply(context, firstTerm, remainder);
			}else if(thisLength == exp.length()-1){
				return calculate(context, new AndyObject(exp.substring(1,thisLength)));
			}else{
				throw new IllegalArgumentException((exp.length()-thisLength) + context.getString(R.string.extraopen));
			}
		}
		
		/*
		 * Check for exponents ^
		 */
		thisLength = exp.length();
		parans = 0;
		if(exp.charAt(0) == '^'){
			throw new IllegalArgumentException(context.getString(R.string.unfinished) + " ^.");
		}
		for(int i = 1; i < exp.length(); i++){
			if(parans == 0){
				if(exp.charAt(i) == '^'){
					thisLength = i;
					break;
				}
			} 
			if (exp.charAt(i) == '('){
				parans++;
			} else if(exp.charAt(i) == ')'){
				parans--;
			}
		}
		if(thisLength != exp.length()){
			if(thisLength != exp.length() - 1){
				AndyObject firstTerm = new AndyObject(exp.substring(0,thisLength));
				AndyObject remainder = new AndyObject(exp.substring(thisLength+1));
				return exponent(context, firstTerm, remainder);
			}
			throw new IllegalArgumentException(context.getString(R.string.unfinished) + " ^.");
		}
		
		/*
		 * Check for functions with more than 3 letters
		 */
		if(exp.length() >= 4){
			int lastParan = lastParan(exp);
			if(exp.indexOf("det(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " det().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject(inside.getMatrix().det(), AndyObject.VALUE),outside);
				}
				return new AndyObject(inside.getMatrix().det(),AndyObject.VALUE);
			} else if(exp.indexOf("inv(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " inv().");
				}
				try{
					if(lastParan != exp.length()-1){
						AndyObject outside = new AndyObject(exp.substring(lastParan+1));
						return multiply(context, new AndyObject(inside.getMatrix().inverse()),outside);
					}
					return new AndyObject(inside.getMatrix().inverse());
				}catch(RuntimeException e){
					throw new IllegalArgumentException(e.getMessage());
				}
			} else if(exp.indexOf("sin(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " sin().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("sin(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.sin(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " sin()");
			}else if(exp.indexOf("cos(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " cos().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("cos(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.cos(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " cos()");
			}else if(exp.indexOf("tan(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " tan().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("tan(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.tan(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " tan()");
			}else if(exp.indexOf("cot(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " cot().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("cot(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(1/Math.tan(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " cot()");
			}else if(exp.indexOf("csc(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " csc().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("csc(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(1/Math.sin(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " csc()");
			}else if(exp.indexOf("sec(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " sec().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("sec(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(1/Math.cos(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " sec()");
			}else if(exp.indexOf("abs(") == 0){
				AndyObject inside = new AndyObject(exp.substring(4,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " abs().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("abs(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.abs(inside.value), AndyObject.VALUE);
				}else if(inside.type==AndyObject.MATRIX){
					return new AndyObject(inside.getMatrix().normF(), AndyObject.VALUE);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " abs()");
				}
			}
		}
		
		/*
		 * Check for functions with 4 letters
		 */
		if(exp.length() >= 5){
			int lastParan = lastParan(exp);
			if(exp.indexOf("eval(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " eval().");
				}
				EigenvalueDecomposition evd = inside.getMatrix().eig();
				double[] values = evd.getRealEigenvalues();
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject(new Matrix(values, values.length)),outside);
				}
				return new AndyObject(new Matrix(values, values.length));
			} else if(exp.indexOf("evec(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " evec().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject(inside.getMatrix().eig().getV()),outside);
				}
				return new AndyObject(inside.getMatrix().eig().getV());
			} else if(exp.indexOf("sinh(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " sinh().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("sinh(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.sinh(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " sinh()");
			}else if(exp.indexOf("cosh(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " cosh().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("cosh(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.tanh(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " tanh()");
			}else if(exp.indexOf("tanh(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " tanh().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("tanh(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.tanh(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " tanh()");
			}else if(exp.indexOf("csch(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " csch().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("csch(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(1/Math.sinh(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " csch()");
			}else if(exp.indexOf("sech(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " sech().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("sech(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(1/Math.cosh(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " sech()");
			}else if(exp.indexOf("coth(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " coth().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("coth(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(1/Math.tanh(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " tanh()");
			}else if(exp.indexOf("sqrt(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " sqrt().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("sqrt(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.sqrt(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " sqrt()");
			}else if(exp.indexOf("sign(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " sign().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("sign(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					if(inside.value > 0){
						return new AndyObject(1, AndyObject.VALUE);
					}else if(inside.value < 0){
						return new AndyObject(-1, AndyObject.VALUE);
					}else{
						return new AndyObject(0,AndyObject.VALUE);
					}
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " sign()");
			}else if(exp.indexOf("log2(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " log2().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("log2(" + inside.content + ")"),outside);
				}
				return calculate(context, new AndyObject("log2(" + inside.content + ")"));
			}else if(exp.indexOf("unit(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " unit().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject(inside.getMatrix().times(1/inside.getMatrix().normF())),outside);
				}
				return calculate(context, new AndyObject(inside.getMatrix().times(1/inside.getMatrix().normF())));
			}else if(exp.indexOf("norm(") == 0){
				AndyObject inside = new AndyObject(exp.substring(5,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " norm().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject(inside.getMatrix().normF(), AndyObject.VALUE),outside);
				}
				return new AndyObject(inside.getMatrix().normF(), AndyObject.VALUE);
			}else if(exp.indexOf("fint(") == 0){
				int comma1 = firstComma(exp);
				if(comma1 == -1){
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " fint().");
				}
				String fun = exp.substring(5,comma1);
				String remain = exp.substring(comma1+1);
				int comma2 = firstComma(remain);
				if(comma2 == -1){
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " fint().");
				}
				String var = remain.substring(0, comma2);
				remain = remain.substring(comma2+1);
				int comma3 = firstComma(remain);
				if(comma3 == -1){
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " fint().");
				}
				String bounds1 = remain.substring(0,comma3);
				remain = remain.substring(comma3+1);
				int comma4 = firstComma(remain);
				if(comma4 == -1){
					lastParan -= comma1;
					lastParan -= comma2;
					lastParan -= comma3;
					lastParan -= 3;
					String bounds2 = remain.substring(0,lastParan);
					AndyObject result;
					try{
						result = new AndyObject(AndyMath.integrate(context, fun, var, bounds1, bounds2));
					}catch(ParseError e){
						throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " fint(): " + e.getMessage());
					}
					if(lastParan < remain.length() - 1){
						AndyObject outside = new AndyObject(remain.substring(lastParan+1));
						return multiply(context, result, outside);
					}
					return result;
				}
				String bounds2 = remain.substring(0,comma4);
				String var2 = bounds1;
				bounds1 = bounds2;
				remain = remain.substring(comma4+1);
				int comma5 = firstComma(remain);
				if(comma5 == -1){
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " fint().");
				}
				bounds2 = remain.substring(0,comma5);
				remain = remain.substring(comma5+1);
				int comma6 = firstComma(remain);
				if(comma6 == -1){
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " fint().");
				}
				String bounds3 = remain.substring(0,comma6);
				remain = remain.substring(comma6+1);
				lastParan -= comma1;
				lastParan -= comma2;
				lastParan -= comma3;
				lastParan -= comma4;
				lastParan -= comma5;
				lastParan -= comma6;
				lastParan -= 6;
				String bounds4 = remain.substring(0,lastParan);
				AndyObject result;
				try{
					result = new AndyObject(AndyMath.integrate(context, fun, var, var2, new String[] {bounds1, bounds2, bounds3, bounds4}));
				}catch(ParseError e){
					throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " fint(): " + e.getMessage());
				}
				if(lastParan < remain.length() - 1){
					AndyObject outside = new AndyObject(remain.substring(lastParan+1));
					return multiply(context, result, outside);
				}
				return result;
			}
		}
		
		
		/*
		 * Check for functions with 5 letters
		 */
		if(exp.length() >= 6){
			int lastParan = lastParan(exp);
			if(exp.indexOf("log10(") == 0){
				AndyObject inside = new AndyObject(exp.substring(6,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " log10().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("log10(" + inside.content + ")"),outside);
				}
				return calculate(context, new AndyObject("log10(" + inside.content + ")"));
			} else if(exp.indexOf("solve(") == 0){
				AndyObject inside1 = calculate(context, new AndyObject(exp.substring(6,firstComma(exp))));
				AndyObject inside2 = calculate(context, new AndyObject(exp.substring(firstComma(exp)+1, lastParan)));
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, multiply(context, new AndyObject(inside1.getMatrix().inverse()), inside2),outside);
				}
				return multiply(context, new AndyObject(inside1.getMatrix().inverse()), inside2);
			}
		}
		
		/*
		 * Check for functions with 6 letters
		 */
		if(exp.length() >= 7){
			int lastParan = lastParan(exp);
			if(exp.indexOf("arcsin(") == 0){
				AndyObject inside = new AndyObject(exp.substring(7,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arcsin().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arcsin(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.asin(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " arcsin()");
			}else if(exp.indexOf("arccos(") == 0){
				AndyObject inside = new AndyObject(exp.substring(7,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arccos().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arccos(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.acos(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " arccos()");
			}else if(exp.indexOf("arctan(") == 0){
				AndyObject inside = new AndyObject(exp.substring(7,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arctan().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arctan(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.tan(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " arctan()");
			}else if(exp.indexOf("arcsec(") == 0){
				AndyObject inside = new AndyObject(exp.substring(7,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arcsec().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arcsec(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.acos(1/inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " arcsec()");
			}else if(exp.indexOf("arccsc(") == 0){
				AndyObject inside = new AndyObject(exp.substring(7,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arccsc().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arccsc(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.asin(1/inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " arccsc()");
			}else if(exp.indexOf("arccot(") == 0){
				AndyObject inside = new AndyObject(exp.substring(7,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arccot().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arccot(" + inside.content + ")"),outside);
				}
				if(inside.type == AndyObject.VALUE){
					return new AndyObject(Math.PI/2-Math.atan(inside.value), AndyObject.VALUE);
				}
				throw new IllegalArgumentException(context.getString(R.string.invalidfor) + " arcsec()");
			}
		}
		
		/*
		 * Check for functions with 7 letters
		 */
		if(exp.length() >= 8){
			int lastParan = lastParan(exp);
			if(exp.indexOf("arcsinh(") == 0){
				AndyObject inside = new AndyObject(exp.substring(8,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arcsinh().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arcsinh(" + inside.content + ")"),outside);
				}
				return calculate(context, new AndyObject("arcsinh(" + inside.content + ")"));
			}else if(exp.indexOf("arccosh(") == 0){
				AndyObject inside = new AndyObject(exp.substring(8,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arccosh().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arccosh(" + inside.content + ")"),outside);
				}
				return calculate(context, new AndyObject("arccosh(" + inside.content + ")"));
			}else if(exp.indexOf("arctanh(") == 0){
				AndyObject inside = new AndyObject(exp.substring(8,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arctanh().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arctanh(" + inside.content + ")"),outside);
				}
				return calculate(context, new AndyObject("arctanh(" + inside.content + ")"));
			}else if(exp.indexOf("arccsch(") == 0){
				AndyObject inside = new AndyObject(exp.substring(8,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arccsch().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arccsch(" + inside.content + ")"),outside);
				}
				return calculate(context, new AndyObject("arccsch(" + inside.content + ")"));
			}else if(exp.indexOf("arcsech(") == 0){
				AndyObject inside = new AndyObject(exp.substring(8,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arcsech().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arcsech(" + inside.content + ")"),outside);
				}
				return calculate(context, new AndyObject("arcsech(" + inside.content + ")"));
			}else if(exp.indexOf("arccoth(") == 0){
				AndyObject inside = new AndyObject(exp.substring(8,lastParan));
				if(inside.content!=null){
					inside = calculate(context, inside);
				}else{
					throw new IllegalArgumentException(context.getString(R.string.insufArgs) + " arccoth().");
				}
				if(lastParan != exp.length()-1){
					AndyObject outside = new AndyObject(exp.substring(lastParan+1));
					return multiply(context, new AndyObject("arccoth(" + inside.content + ")"),outside);
				}
				return calculate(context, new AndyObject("arccoth(" + inside.content + ")"));
			}
		}

		if(exp.indexOf("ans")==0){
			if(answer != null){
				if(exp.length()==3){
					return answer;
				}
				return multiply(context, answer,new AndyObject(exp.substring(3)));
			}
			throw new IllegalArgumentException("ans " + context.getString(R.string.notDefined));
		}
		
		if(exp.length() == 1){
			if(exp.equals("a")){
				try{
					return new AndyObject(AndyMath.stringToMatrix(Calculator.matrices[0]));
				}catch(NullPointerException e){
					throw new IllegalArgumentException("A " + context.getString(R.string.notDefined));
				}
			} else if(exp.charAt(0) == 'b'){
				try{
					return new AndyObject(AndyMath.stringToMatrix(Calculator.matrices[1]));
				}catch(NullPointerException e){
					throw new IllegalArgumentException("B" + context.getString(R.string.notDefined));
				}
			} else if(exp.charAt(0) == 'c'){
				try{
					return new AndyObject(AndyMath.stringToMatrix(Calculator.matrices[2]));
				}catch(NullPointerException e){
					throw new IllegalArgumentException("C" + context.getString(R.string.notDefined));
				}
			} else if(exp.charAt(0) == 'd'){
				try{
					return new AndyObject(AndyMath.stringToMatrix(Calculator.matrices[3]));
				}catch(NullPointerException e){
					throw new IllegalArgumentException("D" + context.getString(R.string.notDefined));
				}
			}
		}
		if(isNumber(exp.charAt(0))){
			int i;
			for(i = 1; i < exp.length(); i++){
				if(!isNumber(exp.charAt(i))){
					i--;
					break;
				}
			}
			double thisValue;
			try{
				thisValue = parser.parse(exp.substring(0,i+1)).getVal();
			}catch(ParseError e){
				throw new IllegalArgumentException(e.getMessage() + ":" + exp.substring(0,i));
			}
			if(i == exp.length()-1){
				return new AndyObject(thisValue);
			}
			AndyObject inside = calculate(context, new AndyObject(exp.substring(i+1)));
			if(inside.type == AndyObject.VALUE){
				return new AndyObject(thisValue * inside.value, AndyObject.VALUE);
			} else if (inside.type == AndyObject.MATRIX){
				return new AndyObject(inside.getMatrix().times(thisValue));
			}
		}
		if(isMatrix(exp.charAt(0))){
			return multiply(context, new AndyObject("" + exp.charAt(0)),new AndyObject(exp.substring(1)));			
		}
		
		
		
		throw new IllegalArgumentException(context.getString(R.string.limited) + " " + exp);
	}
	
	public static AndyObject calculate(Context context, String s) throws IllegalArgumentException{
		if(s == null || s.equals("")){
			throw new IllegalArgumentException(context.getString(R.string.noInput));
		}
		AndyObject expression = new AndyObject(s);
		AndyMath.setUpParser(parser);
		answer = calculate(context, expression);
		if(answer.type==AndyObject.VALUE){
			answer.content = "" + Math.round(answer.value.doubleValue()*1000000000000.)/1000000000000.;
		}
		return answer;
	}
	
	private static boolean isNumber(char c){
		return c == '0' || c == '1' || c == '2' || 	c == '3' || c == '4' || 
				c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || c == '.';
	}
	
	private static boolean isMatrix(char c){
		return c == 'a' || c == 'b' || c == 'c' || c == 'd';
	}
	
	public static int lastParan(String s){
		int paran = 1;
		for(int i = s.indexOf('(') + 1; i < s.length(); i++){
			if(s.charAt(i) == '('){
				paran ++;
			}else if(s.charAt(i) == ')'){
				paran --;
			}
			if(paran == 0){
				return i;
			}
		}
		return -1;
	}
	
	public static int firstComma(String s){
		for(int i = 0; i < s.length(); i++){
			if (s.charAt(i) == ','){
				return i;
			}
		}
		return -1;
	}
	
	public static AndyObject multiply(Context context, AndyObject o1, AndyObject o2) throws IllegalArgumentException{
		if(o1.content  == "" || o1.content == null){
			if(o2.type==AndyObject.UNKNOWN){
				o2 = calculate(context, o2);
			}
			return o2;
		}else if (o2.content == "" || o2.content == null){
			if(o1.type==AndyObject.UNKNOWN){
				o1 = calculate(context, o1);
			}
			return o1;
		} else {
			if(o1.type==AndyObject.UNKNOWN){
				o1 = calculate(context, o1);
			}
			if(o2.type==AndyObject.UNKNOWN){
				o2 = calculate(context, o2);
			}
			if(o1.type == AndyObject.VALUE && o2.type == AndyObject.VALUE){
				return new AndyObject(o1.value * o2.value, AndyObject.VALUE);
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.VALUE){
				return new AndyObject(o1.getMatrix().times(o2.value));
			} else if (o1.type == AndyObject.VALUE && o2.type == AndyObject.MATRIX){
				return new AndyObject(o2.getMatrix().times(o1.value));
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.MATRIX){
				return new AndyObject(o1.getMatrix().times(o2.getMatrix()));
			} else {
				throw new IllegalArgumentException("Cannot multiply unknown types.");
			}
		}
	}
	
	public static AndyObject add(Context context, AndyObject o1, AndyObject o2) throws IllegalArgumentException{
		if(o1.content  == ""){
			return o2;
		}else if (o2.content == ""){
			return o1;
		} else {
			if(o1.type==AndyObject.UNKNOWN){
				o1 = calculate(context, o1);
			}
			if(o2.type==AndyObject.UNKNOWN){
				o2 = calculate(context, o2);
			}
			if(o1.type == AndyObject.VALUE && o2.type == AndyObject.VALUE){
				return new AndyObject(o1.value + o2.value, AndyObject.VALUE);
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.VALUE){
				throw new IllegalArgumentException(context.getString(R.string.addError));
			} else if (o1.type == AndyObject.VALUE && o2.type == AndyObject.MATRIX){
				throw new IllegalArgumentException(context.getString(R.string.addError));
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.MATRIX){
				return new AndyObject(o1.getMatrix().plus(o2.getMatrix()));
			} else {
				throw new IllegalArgumentException("Cannot add unknown types.");
			}
		}
	}
	
	public static AndyObject subtract(Context context, AndyObject o1, AndyObject o2) throws IllegalArgumentException{
		if(o1.content  == ""){
			return new AndyObject(o2.value * -1, AndyObject.VALUE);
		}else if (o2.content == ""){
			return o1;
		} else {
			if(o1.type==AndyObject.UNKNOWN){
				o1 = calculate(context, o1);
			}
			if(o2.type==AndyObject.UNKNOWN){
				o2 = calculate(context, o2);
			}
			if(o1.type == AndyObject.VALUE && o2.type == AndyObject.VALUE){
				return new AndyObject(o1.value - o2.value, AndyObject.VALUE);
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.VALUE){
				throw new IllegalArgumentException(context.getString(R.string.addError));
			} else if (o1.type == AndyObject.VALUE && o2.type == AndyObject.MATRIX){
				throw new IllegalArgumentException(context.getString(R.string.addError));
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.MATRIX){
				return new AndyObject(o1.getMatrix().plus(o2.getMatrix()));
			} else {
				throw new IllegalArgumentException("Cannot subtract unknown types.");
			}
		}
	}
	
	public static AndyObject divide(Context context, AndyObject o1, AndyObject o2) throws IllegalArgumentException{
		if(o1.content  == ""){
			throw new IllegalArgumentException("Missing argument.");
		}else if (o2.content == ""){
			throw new IllegalArgumentException("Missing argument.");
		} else {
			if(o1.type==AndyObject.UNKNOWN){
				o1 = calculate(context, o1);
			}
			if(o2.type==AndyObject.UNKNOWN){
				o2 = calculate(context, o2);
			}
			if(o1.type == AndyObject.VALUE && o2.type == AndyObject.VALUE){
				return new AndyObject(o1.value / o2.value, AndyObject.VALUE);
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.VALUE){
				return new AndyObject(o1.getMatrix().times(1/o2.value));
			} else if (o1.type == AndyObject.VALUE && o2.type == AndyObject.MATRIX){
				return new AndyObject(o2.getMatrix().times(1/o1.value));
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.MATRIX){
				return new AndyObject(o1.getMatrix().times(o2.getMatrix().inverse()));
			} else {
				throw new IllegalArgumentException("Cannot divide unknown types.");
			}
		}
	}
	
	public static AndyObject exponent(Context context, AndyObject o1, AndyObject o2) throws IllegalArgumentException{
		if(o1.content  == ""){
			throw new IllegalArgumentException("Empty string.");
		}else if (o2.content == ""){
			throw new IllegalArgumentException("Empty string.");
		} else {
			if(o1.type==AndyObject.UNKNOWN){
				o1 = calculate(context, o1);
			}
			if(o2.type==AndyObject.UNKNOWN){
				o2 = calculate(context, o2);
			}
			if(o1.type == AndyObject.VALUE && o2.type == AndyObject.VALUE){
				return new AndyObject(Math.pow(o1.value, o2.value), AndyObject.VALUE);
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.VALUE){
				
				if(o2.value % ((int)o2.value.doubleValue()) != 0){
					throw new IllegalArgumentException("Cannot raise a matrix to a non-integer value.");
				} else if(o2.value < 1){
					throw new IllegalArgumentException("Cannot raise a matrix to a negative value.");
				}
				Matrix m = o1.getMatrix();
				Matrix result = m.copy();
				for(int i = 1; i < o2.value.doubleValue(); i++){
					result = result.times(m);					
				}
				return new AndyObject(result);
			} else if (o1.type == AndyObject.VALUE && o2.type == AndyObject.MATRIX){
				throw new IllegalArgumentException("Cannot raise value to a matrix.");
			} else if (o1.type == AndyObject.MATRIX && o2.type == AndyObject.MATRIX){
				throw new IllegalArgumentException("Cannot raise value to a matrix.");
			} else {
				throw new IllegalArgumentException("Cannot multiply unknown types.");
			}
		}
	}
}
