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
 * The SummationParser class makes it possible to use summations such as  sum(i,1,5,x^i)  in a Parser.
 * The summation psedu-function has four parameters:  (1) The summation variable, which must be an identifier;
 * (2) The lower limit for the summation, given as an expression;  (3) The upper limit for the
 * summation, given as an expression; and (4) The expression that is summed.  The values of
 * the lower and upper limit expressions are rounded to the nearest integer.  The expression in
 * the fourth parameter can (and presumably will) use the summation variable (as well as other
 * identifiers known to the parser).
 * 
 * <p>To use summations with a Parser p, just say  p.add(new SummationParser()).  It's unlikely that
 * you will ever need to do anything else with SummationParsers.
 * If you want to use a name other than "sum", you can change the name after creating the
 * SummationParser object but before adding it to a parser.  (Note, by the way, that parsers by default do not do 
 * factorials.  If you want a parser that recognizes factorials, you could do something
 * like  p = new Parser(Parser.DEFAULT_OPTIONS | Parser.FACTORIAL).)
 *
 */
public class SummationParser implements ParserExtension {

   /* A name is required by the MathObject interface, which ParserExtension extends.  The name
      is what is used to indicate a summation in an expression.  The name should not be changed
      after the SummationParser is added to a Parser. */

   private String name = "sum";

   /**
    *  Set the name, which will be used in place of "sum" in expressions.  This should not
    *  be done after the SummationParser has been added to a Parser.  The default name is "sum".
    */
   public void setName(String name) {
      this.name = name;
   }
  
   /**
    *  Get the name, which will be used in place of "sum" in expressions. 
    */
   public String getName() {
      return name;
   }

   /**
    * When the name of this ParserExtension is encountered by a parser with which
    * the extension is registered, the parser calls this routine to parse the
    * summation subexpression.  The subexpression has the form 
    * (<variable>,<lower-limit>,<upper-limit>,<expression>).  This method is
    * not meant to be called directly
    */
   public void doParse(Parser parser, ParserContext context) {
      int tok = context.next();
      String open = context.tokenString;
      if (tok == ParserContext.OPCHARS &&
             ( open.equals("(")  || (open.equals("[") && (context.options & Parser.BRACKETS) != 0)
                                       || (open.equals("{") && (context.options & Parser.BRACES) != 0) )) {
          String close = open.equals("(") ? ")" : (open.equals("[") ? "]" : "}");
          tok = context.next();  // Must be an identifier.
          if (tok != ParserContext.IDENTIFIER)
             throw new ParseError("Expected the summation variable as the first argument of " + name + ".", context);
          String varName = context.tokenString;
          tok = context.next();
          if (tok != ParserContext.OPCHARS || ! context.tokenString.equals(","))
             throw new ParseError("Exprected a comma after the index variable, " + varName +".", context);
          parser.parseExpression(context);
          tok = context.next();
          if (tok != ParserContext.OPCHARS || ! context.tokenString.equals(","))
             throw new ParseError("Exprected a comma after the lower limit expression for " + name + ".", context);
          parser.parseExpression(context);
          tok = context.next();
          if (tok != ParserContext.OPCHARS || ! context.tokenString.equals(","))
             throw new ParseError("Exprected a comma after the upper limit expression for " + name + ".", context);
          Variable v = new Variable(varName);
          context.mark();  // Temporoarily add the summation variable to the symbol table.
          context.add(v);
          ExpressionProgram saveProg = context.prog;
          context.prog = new ExpressionProgram();  // Compile the expression into a new program.
          parser.parseExpression(context);
          tok = context.next();
          if (tok != ParserContext.OPCHARS || ! context.tokenString.equals(close))
             throw new ParseError("Expected a \"" + close + "\" at the end of the paramter list for " + name + ".", context);
          context.revert();  // Restore the state of the ParserContext.
          saveProg.addCommandObject(new Cmd(v,context.prog));
          context.prog = saveProg;
      }
      else
         throw new ParseError("Parentheses required around parameters of summation.", context);
   } // end doParse()
  

   private static class Cmd implements ExpressionCommand {
          // When a summation occurs in an expression, it is represented in the compiled ExpressionProgram
          // by an object belonging to this class.
  
      private Variable sumVar;  // The summation variable.
      private ExpressionProgram sumExpr;  // The expression that is summed
      
      Cmd(Variable v, ExpressionProgram e) {
            // Constructor.
         sumVar = v;
         sumExpr = e;
      }

      public void apply(StackOfDouble stack, Cases cases) {
             // This routine is called when an ExpressionCommand object is encountered during
             // the evaluation of an ExpressionProgram.  The stack may contain results of
             // previous commands in the program.  In this case, the command is a summation
             // and the stack contains the upper and lower limits summation limits.
          double upper = Math.round(stack.pop()) + 0.1;  // Get summation limits.
          double lower = Math.round(stack.pop());
          if (Double.isNaN(upper) && Double.isNaN(lower) || upper - lower > 1000000)
             stack.push(Double.NaN);
          double sum = 0;
          for (double x = lower; x <= upper; x++) {  // Compute the sum.
             sumVar.setVal(x);
             sum += sumExpr.getVal(); 
          }
          stack.push(sum);  // Leave the sum on the stack.
      }

      public void compileDerivative(ExpressionProgram prog, int myIndex, ExpressionProgram deriv, Variable wrt) {
             // The ExpressionCommand occurs in the program prog at the index indicated by myIndex.
             // Add commands to deriv that will evaluate the derivative of this command with respect to
             // the variable wrt.  Note that the "Cmd" object is preceded in the program by commands that
             // compute the lower and upper limits of the summation.
          if (!sumExpr.dependsOn(wrt))
             deriv.addConstant(0);
          else {
             int upper = prog.extent(myIndex - 1);  // Size of expression giving the upper limit.
             prog.copyExpression(myIndex - 1 - upper,deriv);  // Copy lower limit exression to deriv.
             prog.copyExpression(myIndex - 1,deriv);    // Copy upper limit expression to deriv.
             deriv.addCommandObject( new Cmd(sumVar, (ExpressionProgram)sumExpr.derivative(wrt)) );
          }
      }
      
      public int extent(ExpressionProgram prog, int myIndex) {
             // The ExpressionCommand occurs in the program prog at the index indicated by myIndex.
             // Return the total number of indices in prog occupied by this command and the commands
             // that generate data used by this command.  In this case, that means the commands that
             // compute the upper and lower limits of the summatio, plus this Cmd object.
          int upper = prog.extent(myIndex - 1);          // Extent of upper limit expression in prog.
          int lower = prog.extent(myIndex - 1 - upper);  // Extent of lower limit expression in prog.
          return upper + lower + 1; // Upper + lower limits + this object.
      }
      
      public boolean dependsOn(Variable x) {
             // Return true if this command depends on the value of x, false otherwise.
             // That is, when apply() is called, can the result depend on the value of x?
          return sumExpr.dependsOn(x);
      }
      
      public void appendOutputString(ExpressionProgram prog, int myIndex, StringBuffer buffer) {
             // The ExpressionCommand occurs in the program prog at the index indicated by myIndex.
             // Add a print string representation of the sub-expression represented by this command
             // (including any previous commands in the program that generate data used by this
             // command).
          int upper = prog.extent(myIndex - 1);
          buffer.append("sum(");
          buffer.append(sumVar.getName());
          buffer.append(", ");
          prog.appendOutputString(myIndex - 1 - upper,buffer);
          buffer.append(", ");
          prog.appendOutputString(myIndex - 1, buffer);
          buffer.append(", ");
          buffer.append(sumExpr.toString());
          buffer.append(")");
      }
      
   } // end nested class Cmd

} // end SumParserExtension
