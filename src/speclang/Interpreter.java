package speclang;

import java.io.IOException;

import speclang.AST.*;

/**
 * This main class implements the Read-Eval-Print-Loop of the interpreter with
 * the help of Reader, Evaluator, and Printer classes.
 * 
 * @author hridesh
 *
 */
public class Interpreter {
	public static void main(String[] args) {
		System.out.println("SpecLang: Type a program to evaluate and press the enter key,\n"
				+ "e.g. ((lambda (x: num | #t -> #t) (+ 3 (+ 4 x))) 2) \n" 
				+ "or try ((lambda (x: num | (> x 0) -> #t) (+ 3 (+ 4 x))) 2) \n"
				+ "or try ((lambda (x: num | (> x 3) -> #t) (+ 3 (+ 4 x))) 2) \n"
				+ "or try ((lambda (x: num | (> x 0) -> (= result 342)) (+ 3 (+ 4 x))) 2) \n" + "Press Ctrl + C to exit.");
		Reader reader = new Reader();
		Evaluator eval = new Evaluator(reader);
		Printer printer = new Printer();
		Checker checker = new Checker(); // Type checker
		REPL: while (true) { // Read-Eval-Print-Loop (also known as REPL)
			Program p = null;
			try {
				p = reader.read();
				if (p._e == null)
					continue REPL;
				Type t = checker.check(p); /*** Type checking the program ***/
				if (t instanceof Type.ErrorT)
					printer.print(t);
				else {
					Value val = eval.valueOf(p);
					printer.print(val);
				}
			} catch (Env.LookupException e) {
				printer.print(e);
			} catch (IOException e) {
				System.out.println("Error reading input:" + e.getMessage());
			} catch (NullPointerException e) {
				System.out.println("Error:" + e.getMessage());
			}
		}

	}
}
