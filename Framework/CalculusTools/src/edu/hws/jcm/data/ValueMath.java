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

package edu.hws.jcm.data;

/**
 *  A ValueMath object is an easy way to create Value objects that are computed
 *  from other Value objects.  For example, "new ValueMath(a,b,'+')" is an
 *  object whose value is obtained by adding the values of a and b.
 */
public class ValueMath implements Value {

   private Function f;  // If non-null, this is a value of the form f(params);
                        // If null, it's of the form x + y, x - y, ...
   private double[] param;
   private Value x,y;
   private char op;
   
   /**
    * Create a ValueMath object whose value is computed by applying an arithmetic
    * operator the values of x and y.
    * @param op The arithmetic operator that is to be applied to x and y.  This should
    *           be one of the characters '+', '-', '*', '/', or '^'.  (No error is
    *           thrown if another character is provided.  It will be treated as a '/').
    */
   public ValueMath(Value x, Value y, char op) {
      this.x = x;
      this.y = y;
      this.op = op;
   }
   
   /**
    * Create a ValueMath object whose value is computed as f(x).
    */
   public ValueMath(Function f, Value x) {
       if (f.getArity() != 1)
          throw new IllegalArgumentException("Internal Error:  The function in a ValueMath object must have arity 1.");
       this.f = f;
       this.x = x;
       param = new double[1];
   }
   
   /**
    *  Get the value of this object.
    */
   public double getVal() {
      if (f != null) {
         param[0] = x.getVal();
         return f.getVal(param);
      }
      else {
         double a = x.getVal();
         double b = y.getVal();
         switch (op) {
            case '+': return a+b;
            case '-': return a-b;
            case '*': return a*b;
            case '/': return a/b;
            case '^': return Math.pow(a,b);
            default:  throw new IllegalArgumentException("Internal Error:  Unknown math operator.");
         }
      }
   }

} // end class ValueMath
