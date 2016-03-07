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


/**
 *  This non-public class is for use with TableFunctions.  It defines one segment
 *  of a TableFunction whose style is TableFunction.SMOOTH.  A cubic segment is
 *  a segment of a cubic polynomial.  It is defined by six numbers:  two x-coordinates,
 *  the y-value at each x-coordinate, and the derivative at each x-coordinate.
 */
class CubicSegment {

   private double x1,x2,  // x-ccords at endpoints with x1 < x2
                  y1,y2,  // y-coords at endpoints
                  d1,d2;  // derivatives at endpoints
          
   private double a,b,c,d;  // coefficients in a(x-x1)^3 + b(x-x1)^2(x-x2) + ...

   CubicSegment() {
   }

   CubicSegment(double x1, double x2, double y1, double y2, double d1, double d2) {
      setData(x1, x2, y1, y2, d1, d2);
   }
   
   void setData(double nx1, double nx2, double ny1, double ny2, double nd1, double nd2) {
      double temp;
      if (nx1 == nx2)
         throw new IllegalArgumentException("Attempt to make CubicSegment of length 0");
      if (nx1 > nx2) {
         temp = nx1; nx1 = nx2; nx2 = temp;
         temp = ny1; ny1 = ny2; ny2 = temp;
         temp = nd1; nd1 = nd2; nd2 = temp;
      }
      x1 = nx1; 
      x2 = nx2;
      y1 = ny1;
      y2 = ny2;
      d1 = nd1;
      d2 = nd2;
      temp = (x2 - x1);
      a = y2/(temp*temp*temp);
      b = d2/(temp*temp) - 3*a;
      temp = -temp;
      d = y1/(temp*temp*temp);
      c = d1/(temp*temp) - 3*d;
   }
   
   void setDerivativesFromNeighbors(double leftX, double leftY, double rightX, double rightY) {
      double nd1,nd2;
      if (!Double.isNaN(leftX) && leftX < x1)
         nd1 = (y2 - leftY) / (x2 - leftX);
      else
         nd1 = (y2 - y1) / (x2 - x1);
      if (!Double.isNaN(rightX) && rightX > x2)
         nd2 = (rightY - y1) / (rightX - x1);
      else
         nd2 = (y2 - y1) / (x2 - x1);
      setData(x1,x2,y1,y2,nd1,nd2);
   }
   
   double value(double x) {  // should have x1 <= x <= x2, but not required
      return derivativeValue(x,0);
   }
   
   double derivativeValue(double x, int derivativeOrder) { 
          // Assume derivativeOrder >= 0.
          // This function, unlike value() returns the value that represents the
          // one-sided limit at the endpoint, even if that endpoint is not
          // in the domain.
      switch (derivativeOrder) {
         case 0:
            double t1 = x - x1;
            double t2 = t1*t1;
            double t3 = t2*t1;
            double s1 = x - x2;
            double s2 = s1*s1;
            double s3 = s2*s1;
            return a*t3 + b*t2*s1 + c*t1*s2 + d*s3;
         case 1:
            return ((3*a+b)*(x-x1)*(x-x1) + 2*(b+c)*(x-x1)*(x-x2) + (3*d+c)*(x-x2)*(x-x2));
         case 2:
            return 2*( (3*a+2*b+c)*(x-x1) + (3*d+2*c+b)*(x-x2) );
         case 3:
            return 6*(2*a+b+c);
         default:
            return 0;
      }   
   }

}
