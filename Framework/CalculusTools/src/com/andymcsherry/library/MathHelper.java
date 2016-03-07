package com.andymcsherry.library;

public class MathHelper {
	public static OffMath math;
	private static String TAG = "MathHelper";
	
	public static double integrate(String fun, String v1, String v2, String[] bounds){
		math.setBounds(bounds);
		double result = math.integrate(fun, v1, v2);
		
		return result;
	}
}
