package com.andymcsherry.library;

import java.lang.reflect.Method;

import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remoteable;

import dalvik.system.DexClassLoader;
import edu.hws.jcm.data.Constant;
import edu.hws.jcm.data.Expression;
import edu.hws.jcm.data.ParseError;
import edu.hws.jcm.data.Parser;
import edu.hws.jcm.data.Variable;
import edu.hws.jcm.functions.ExpressionFunction;

public class OffMath extends Remoteable{
    transient private static String TAG = "OffMath";
	transient private ExecutionController controller;
	
	transient public static ClassLoader mCurrent;
	transient public static DexClassLoader mCurrentDexLoader = null;
	
	public Double[][] rom;
	
	public String[] bounds;
	
	public OffMath(ExecutionController controller){
		this.controller = controller;
	}
	
	public double integrate(String fun, String v1, String v2){
		Method toExecute;
		Class<?>[] paramTypes = {String.class, String.class, String.class};
		Object[] paramValues = {fun, v1, v2};

		Double result = null;
		try {
			toExecute = this.getClass().getDeclaredMethod("localIntegrate", paramTypes);
			result = (Double) controller.execute(toExecute, paramValues, this);
		} catch (SecurityException e) {
			// Should never get here
			e.printStackTrace();
			throw e;
		} catch (NoSuchMethodException e) {
			// Should never get here
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public double localIntegrate(String fun, String v1, String v2){
		double result = this.calculate(fun, v1, v2);
		return result;
		
	}
	
	private double calculate(String fun, String v1, String v2){
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
			throw new ParseError("invalidStartX" + e.getMessage(), e.context);
		}
		try{
			x2 = integParser.parse(bounds[1]).getVal();
		}catch(ParseError e){
			throw new ParseError("invalidEndX" + e.getMessage(), e.context);
		}
		try{
			y1 = integParser.parse(bounds[2]);
		}catch(ParseError e){
			throw new ParseError("invalidStartY" + e.getMessage(), e.context);
		}
		try{
			y2 = integParser.parse(bounds[3]);
		}catch(ParseError e){
			throw new ParseError("invalidEndY" + e.getMessage(), e.context);
		}
		try{
			integFun = integParser.parse(fun);
		}catch(ParseError e){
			throw new ParseError("invalidfunction" + e.getMessage(), e.context);
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
	
	private double romberg(int i, int j){
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
	
	public void setBounds(String[] bounds){
		this.bounds = bounds;
	}
	
	@Override
	public void copyState(Remoteable arg0) {
		// TODO Auto-generated method stub
		
	}
}
