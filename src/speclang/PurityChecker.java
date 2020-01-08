package speclang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import speclang.AST.*;
import speclang.Env.ExtendEnv;
import speclang.Type.ErrorT;
import speclang.Type.*;

/**
 * This main class implements the purity checker of the interpreter.
 * 
 * @author hridesh
 *
 */

public class PurityChecker implements Visitor<Boolean, Env<Type>> {
	Printer.Formatter ts = new Printer.Formatter();

	Boolean check(Program p) {
		return (Boolean) p.accept(this, null);
	}

	public Boolean visit(Program p, Env<Type> env) {
		boolean purity = true; 
		for (DefineDecl d : p.decls()) {
			purity = purity && (Boolean) d.accept(this, env);
		}
		return purity;
	}

	public Boolean visit(VarExp e, Env<Type> env) {
		return true;
	}

	public Boolean visit(LetExp e, Env<Type> env) {
		List<Exp> value_exps = e.value_exps();
		boolean purity = true;
		for (Exp exp : value_exps) {
			purity = purity && (boolean) exp.accept(this, env);
		}

		return purity && (Boolean) e.body().accept(this, env);
	}

	public Boolean visit(DefineDecl d, Env<Type> env) {
		return (Boolean) d._value_exp.accept(this, env);
	}

	public Boolean visit(LambdaExp e, Env<Type> env) {
		return (Boolean) e.body().accept(this, env);
	}

	public Boolean visit(CallExp e, Env<Type> env) {
		Exp operator = e.operator();
		List<Exp> operands = e.operands();

		boolean purity = (Boolean) operator.accept(this, env);
		for (Exp operand : operands) {
			purity = purity && (Boolean) operand.accept(this, env);
		}
		return purity;
	}

	public Boolean visit(LetrecExp e, Env<Type> env) {
		List<String> names = e.names();
		List<Type> types = e.types();
		List<Exp> fun_exps = e.fun_exps();

		// collect the environment
		Env<Type> new_env = env;
		for (int index = 0; index < names.size(); index++) {
			new_env = new ExtendEnv<Type>(new_env, names.get(index), types.get(index));
		}

		// verify the types of the variables
		for (int index = 0; index < names.size(); index++) {
			Type type = (Type) fun_exps.get(index).accept(this, new_env);

			if (type instanceof ErrorT) {
				return type;
			}

			if (!assignable(types.get(index), type)) {
				return new ErrorT("The expected type of the " + index + " variable is " + types.get(index).tostring()
						+ " found " + type.tostring() + " in " + ts.visit(e, null));
			}
		}

		return (Type) e.body().accept(this, new_env);
	}

	public Boolean visit(IfExp e, Env<Type> env) {
		Exp cond = e.conditional();
		boolean purity = (Boolean) cond.accept(this, env);
		purity = purity && (Boolean) e.then_exp().accept(this, env);
		purity = purity && (Boolean) e.else_exp().accept(this, env);
		return purity;
	}

	public Boolean visit(CarExp e, Env<Type> env) {
		Exp exp = e.arg();
		return (Boolean) exp.accept(this, env);
	}

	public Boolean visit(CdrExp e, Env<Type> env) {
		Exp exp = e.arg();
		return (Boolean) exp.accept(this, env);
	}

	public Boolean visit(ConsExp e, Env<Type> env) {
		Exp fst = e.fst();
		Exp snd = e.snd();
		return (Boolean) fst.accept(this, env) && (Boolean) snd.accept(this, env);
	}

	public Boolean visit(ListExp e, Env<Type> env) {
		List<Exp> elems = e.elems();

		boolean purity = true;
		for (Exp elem : elems) {
			 purity &= (Boolean) elem.accept(this, env);
		}
		return purity;
	}

	public Boolean visit(NullExp e, Env<Type> env) {
		Exp exp = e.arg();
		return (Boolean) exp.accept(this, env);
	}

	public Boolean visit(RefExp e, Env<Type> env) {
		Exp value = e.value_exp();
		return (Boolean) value.accept(this, env);
	}

	public Boolean visit(DerefExp e, Env<Type> env) {
		Exp exp = e.loc_exp();
		return (Boolean) exp.accept(this, env);
	}

	public Boolean visit(AssignExp e, Env<Type> env) {
		return false;
	}

	public Boolean visit(FreeExp e, Env<Type> env) {
		return false;
	}

	public Boolean visit(UnitExp e, Env<Type> env) {
		return true;
	}

	public Boolean visit(NumExp e, Env<Type> env) {
		return true;
	}

	public Boolean visit(StrExp e, Env<Type> env) {
		return true;
	}

	public Boolean visit(BoolExp e, Env<Type> env) {
		return true;
	}

	public Boolean visit(LessExp e, Env<Type> env) {
		return visitBinaryComparator(e, env, ts.visit(e, null));
	}

	public Boolean visit(EqualExp e, Env<Type> env) {
		return visitBinaryComparator(e, env, ts.visit(e, null));
	}

	public Boolean visit(GreaterExp e, Env<Type> env) {
		return visitBinaryComparator(e, env, ts.visit(e, null));
	}

	private Boolean visitBinaryComparator(BinaryComparator e, Env<Type> env, String printNode) {
		Exp first_exp = e.first_exp();
		Exp second_exp = e.second_exp();

		return (Boolean) first_exp.accept(this, env) && (Boolean) second_exp.accept(this, env);
	}

	public Boolean visit(AddExp e, Env<Type> env) {
		return visitCompoundArithExp(e, env, ts.visit(e, null));
	}

	public Boolean visit(DivExp e, Env<Type> env) {
		return visitCompoundArithExp(e, env, ts.visit(e, null));
	}

	public Boolean visit(MultExp e, Env<Type> env) {
		return visitCompoundArithExp(e, env, ts.visit(e, null));
	}

	public Boolean visit(SubExp e, Env<Type> env) {
		return visitCompoundArithExp(e, env, ts.visit(e, null));
	}

	public Boolean visit(ErrorExp e, Env<Type> env) {
		return true;
	}

	private Boolean visitCompoundArithExp(CompoundArithExp e, Env<Type> env, String printNode) {
		List<Exp> operands = e.all();

		boolean purity = true;
		for (Exp exp : operands) {
			purity  &= (Boolean) exp.accept(this, env); 
		}

		return purity;
	}

	private static boolean assignable(Type t1, Type t2) {
		if (t2 instanceof UnitT) {
			return true;
		}

		return t1.typeEqual(t2);
	}

	public Boolean visit(ReadExp e, Env<Type> env) {
		return false;
	}

	public Boolean visit(EvalExp e, Env<Type> env) {
		return false;
	}

	@Override
	public Boolean visit(IsNullExp e, Env<Type> env) {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsProcedureExp e, Env<Type> env) {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsListExp e, Env<Type> env) {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsPairExp e, Env<Type> env) {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsUnitExp e, Env<Type> env) {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsNumberExp e, Env<Type> env) {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsStringExp e, Env<Type> env) {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsBooleanExp e, Env<Type> env) {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(FuncSpec e, Env<Type> env) {
		Boolean purity = true;
		for (Exp precondition : e.preconditions()) {
			purity = purity && (Boolean) precondition.accept(this, env);
		}
		for (Exp postcondition : e.postconditions()) {
			purity = purity && (Boolean) postcondition.accept(this, env);
		}
		return purity;
	}

	public static void main(String[] args) {
		System.out.println("TypeLang: Type a program to check its purity press the enter key,\n"
				+ "e.g. ((lambda (x: num y: num z : num) (+ x (+ y z))) 1 2 3) \n" + "or try (let ((x : num 2)) x) \n"
				+ "or try (car (list : num  1 2 8)) \n" + "or try (ref : num 2) \n"
				+ "or try  (let ((a : Ref num (ref : num 2))) (set! a (deref a))) \n" + "Press Ctrl + C to exit.");
		Reader reader = new Reader();
		Printer printer = new Printer();
		PurityChecker checker = new PurityChecker(); // Purity checker
		REPL: while (true) { // Read-Eval-Print-Loop (also known as REPL)
			Program p = null;
			try {
				p = reader.read();
				if (p._e == null)
					continue REPL;
				Boolean t = checker.check(p); /*** Checking the purity of program ***/
				System.out.println(t);
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
