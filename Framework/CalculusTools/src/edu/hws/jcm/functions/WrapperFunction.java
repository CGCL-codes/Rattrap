/**************************************************************************
* Copyright (c) 2001, 2005 David J. Eck                                   *
*                                                                         *
* Permission is hereby granted, free of charge, to any person obtaining   *
* a copy of this software and associated documentation files (the         *
* "Software"), to deal in the Software without restriction, including     *
* without limitation the rights to use, copy, modify, merge, publish,     *
* distribute, sublicense, and/or sell copies of the Software, and to      *
* permit persons to whom the Software is furnished to do so, subject to   *
* the following conditions:                                               *
*                                                                         *
* The above copyright notice and this permission notice shall be included *
* in all copies or substantial portions of the Software.                  *
*                                                                         *
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,         *
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF      *
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  *
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY    *
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,    *
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE       *
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                  *
*                                                                         *
* ----                                                                    *
* (Released under new license, April 2012.)                               *
*                                                                         *
*             David J. Eck                                                *
*             Department of Mathematics and Computer Science              *
*             Hobart and William Smith Colleges                           *
*             300 Pulteney Street                                         *
*             Geneva, NY 14456                                            *
*             eck@hws.edu                                                 *
*             http://math.hws.edu/eck                                     *
**************************************************************************/

package edu.hws.jcm.functions;

import edu.hws.jcm.data.*;

/**
 * A WrapperFunction contains another function and delegates to it
 * all calls to methods from the Function interface, except for calls
 * to setName() and getName().  (It maintains its own name, which can
 * be different from the name of the wrapped Function.)  This has at least two 
 * uses:  A WrapperFunction is a FunctionParserExtension, so it can
 * be added to a parser and then used in expressions parsed in that
 * parser.  Second, the function that is wrapped inside the WrapperFunction
 * object can be changed by a call to the setFunction() method.
 * This makes it possible to have a single function that can be used,
 * for example, in ValueMath objects and Graph1D's, whose definition
 * can be changed at will.  Note that it is NOT legal to change the
 * arity of the function when calling the setFunction() method.
 */
public class WrapperFunction extends FunctionParserExtension {

   private Function func;   // The non-null function.
   private double[] params; // For use in evaluating the function in the apply() method;
                            //  has length equal to func.getArity();
   private int serialNumber;  // serial number goes up when function def is changed;
                              //   this is used in the check() method for synchronizing derivatives.
   private WrapperFunction derivativeOf;  // If non-null, then this function is a
                                          // derivative of the specified function.
   private Variable derivativeVar;   // If non-null, then the derivative was taken w.r.t. this variable
   private int derivativeIndex;  // If derivativeVar is null, this is the argument
                                 //   number with respect to which the derivative was taken.
   
   /**
    * Create a WrapperFunction object containing a specified function. 
    *
    * @param f The non-null function that will be contained in the WrapperFunction.
    */ 
   public WrapperFunction(Function f) {
      setFunction(f);
      serialNumber = 0;
   }
                               
   private void check() {
         // Called if this function is the derivative of another wrapper function to see
         // if the serial number of the parent function has changed.  If so, the derivative is
         // recomputed.
      if (derivativeOf == null || derivativeOf.serialNumber == serialNumber)
         return;
      serialNumber = derivativeOf.serialNumber;
      if (derivativeVar != null)
         func = derivativeOf.derivative(derivativeVar);
      else
         func = derivativeOf.derivative(derivativeIndex);
   }
   
   /**
    * Set the function that is contained in this WrapperFunction.
    *
    * @param f The non-null function to be used in this WrapperFunction object.
    *          It must have the same arity as the current function.
    */
   public void setFunction(Function f) {
      if (f == null)
         throw new IllegalArgumentException("Function supplied to WrapperFunction object can't be null.");
      if (func != null && f.getArity() != func.getArity())
          throw new IllegalArgumentException("Attempt to change the arity of a WrapperFunction.");
      if (derivativeOf != null)
         throw new IllegalArgumentException("Can't change the definition of a function that is a derivative of another function.");
      func = f;
      params = new double[f.getArity()];
      serialNumber++;
   }
   
   /**
    * Return the function that is currently wrapped in this WrapperFunction.
    */
   public Function getFunction() {
      return func;
   }

   /**
    * Return the number of arguments of this function.
    */
   public int getArity() {
       return  func.getArity();
   }
      
   /** 
    * Find the value of the function at the argument value
    * argument[0], .... The number of arguments should match
    * the arity of the function.
    */
   public double getVal( double[] arguments ) {
       check();
       return func.getValueWithCases(arguments,null);
   }
   
   /**    
    * Find the value of the function at the argument values
    * argument[0],....  Information about "cases" is stored in
    * the Cases parameter, if it is non-null.  See the Cases
    * class for more information.
    */
   public double getValueWithCases( double[] arguments, Cases cases ) {
      check();
      return func == null? 1 : func.getValueWithCases(arguments,cases);
   }

   /**
    * Return the derivative of the function with repect to
    * argument number wrt, where arguments are numbered starting from 1.
    */
   public Function derivative(int wrt) {
       check();
       WrapperFunction deriv = new WrapperFunction(func.derivative(wrt));
       deriv.derivativeOf = this;
       deriv.derivativeIndex = wrt;
       deriv.serialNumber = serialNumber;
       return deriv;
   }
      
   /** 
    * Return the derivative of the function with respect to the
    * variable x (where x is NOT one of the parameters of the function).
    */
   public Function derivative(Variable x) {
       check();
       WrapperFunction deriv = new WrapperFunction(func.derivative(x));
       deriv.derivativeOf = this;
       deriv.derivativeVar = x;
       deriv.serialNumber = serialNumber;
       return deriv;
   }
   
   /**
    * Return true if the definition of this function depends 
    * in some way on the variable x.  (Note that the function does
    * NOT depend on the variables that are being used as its parameters!)
    */
   public boolean dependsOn(Variable x) {
      check();
      return func.dependsOn(x);
   }

   /**
    * Evaluate the function applied to argument values popped from the stack,
    * and leave the result on the stack.  This is not meant to be called 
    * directly.
    */
   public void apply(StackOfDouble stack, Cases cases) {
      check();
      for (int i = params.length - 1; i >= 0; i--)
         params[i] = stack.pop();
      stack.push( getValueWithCases(params, cases) );
   }

}
