package speclang;

import java.io.IOException;
import java.util.List;

import speclang.AST.*;

/**
 * This main class implements the purity checker of the interpreter.
 * 
 * @author hridesh
 *
 */

public class PurityChecker implements Visitor<Boolean, Type> {
	Printer.Formatter ts = new Printer.Formatter();

	Boolean check(Program p) throws ProgramError {
		return (Boolean) p.accept(this, null);
	}

	public Boolean visit(Program p, Env<Type> env) throws ProgramError {
		boolean purity = true;
		for (DefineDecl d : p.decls()) {
			purity = purity && (Boolean) d.accept(this, env);
		}
		return purity && (Boolean) p._e.accept(this, env);
	}

	public Boolean visit(VarExp e, Env<Type> env) throws ProgramError {
		return true;
	}

	public Boolean visit(LetExp e, Env<Type> env) throws ProgramError {
		List<Exp> value_exps = e.value_exps();
		boolean purity = true;
		for (Exp exp : value_exps) {
			purity = purity && (boolean) exp.accept(this, env);
		}

		return purity && (Boolean) e.body().accept(this, env);
	}

	public Boolean visit(DefineDecl d, Env<Type> env) throws ProgramError {
		return (Boolean) d._value_exp.accept(this, env);
	}

	public Boolean visit(LambdaExp e, Env<Type> env) throws ProgramError {
		return (Boolean) e.body().accept(this, env);
	}

	public Boolean visit(CallExp e, Env<Type> env) throws ProgramError {
		Exp operator = e.operator();
		List<Exp> operands = e.operands();

		boolean purity = (Boolean) operator.accept(this, env);
		for (Exp operand : operands) {
			purity = purity && (Boolean) operand.accept(this, env);
		}
		return purity;
	}

	public Boolean visit(LetrecExp e, Env<Type> env) throws ProgramError {
		List<Exp> fun_exps = e.fun_exps();
		boolean purity = true;

		// verify the types of the variables
		for (int index = 0; index < fun_exps.size(); index++) {
			purity = purity && (Boolean) fun_exps.get(index).accept(this, env);
		}

		return purity && (Boolean) e.body().accept(this, env);
	}

	public Boolean visit(IfExp e, Env<Type> env) throws ProgramError {
		Exp cond = e.conditional();
		boolean purity = (Boolean) cond.accept(this, env);
		purity = purity && (Boolean) e.then_exp().accept(this, env);
		purity = purity && (Boolean) e.else_exp().accept(this, env);
		return purity;
	}

	public Boolean visit(CarExp e, Env<Type> env) throws ProgramError {
		Exp exp = e.arg();
		return (Boolean) exp.accept(this, env);
	}

	public Boolean visit(CdrExp e, Env<Type> env) throws ProgramError {
		Exp exp = e.arg();
		return (Boolean) exp.accept(this, env);
	}

	public Boolean visit(ConsExp e, Env<Type> env) throws ProgramError {
		Exp fst = e.fst();
		Exp snd = e.snd();
		return (Boolean) fst.accept(this, env) && (Boolean) snd.accept(this, env);
	}

	public Boolean visit(ListExp e, Env<Type> env) throws ProgramError {
		List<Exp> elems = e.elems();

		boolean purity = true;
		for (Exp elem : elems) {
			purity &= (Boolean) elem.accept(this, env);
		}
		return purity;
	}

	public Boolean visit(NullExp e, Env<Type> env) throws ProgramError {
		Exp exp = e.arg();
		return (Boolean) exp.accept(this, env);
	}

	public Boolean visit(RefExp e, Env<Type> env) throws ProgramError {
		Exp value = e.value_exp();
		return (Boolean) value.accept(this, env);
	}

	public Boolean visit(DerefExp e, Env<Type> env) throws ProgramError {
		Exp exp = e.loc_exp();
		return (Boolean) exp.accept(this, env);
	}

	public Boolean visit(AssignExp e, Env<Type> env) throws ProgramError {
		return false;
	}

	public Boolean visit(FreeExp e, Env<Type> env) throws ProgramError {
		return false;
	}

	public Boolean visit(UnitExp e, Env<Type> env) throws ProgramError {
		return true;
	}

	public Boolean visit(NumExp e, Env<Type> env) throws ProgramError {
		return true;
	}

	public Boolean visit(StrExp e, Env<Type> env) throws ProgramError {
		return true;
	}

	public Boolean visit(BoolExp e, Env<Type> env) throws ProgramError {
		return true;
	}

	public Boolean visit(LessExp e, Env<Type> env) throws ProgramError {
		return visitBinaryComparator(e, env);
	}

	public Boolean visit(LessEqExp e, Env<Type> env) throws ProgramError {
		return visitBinaryComparator(e, env);
	}

	public Boolean visit(EqualExp e, Env<Type> env) throws ProgramError {
		return visitBinaryComparator(e, env);
	}

	public Boolean visit(GreaterExp e, Env<Type> env) throws ProgramError {
		return visitBinaryComparator(e, env);
	}

	public Boolean visit(GreaterEqExp e, Env<Type> env) throws ProgramError {
		return visitBinaryComparator(e, env);
	}

	private Boolean visitBinaryComparator(BinaryComparator e, Env<Type> env) throws ProgramError {
		Exp first_exp = e.first_exp();
		Exp second_exp = e.second_exp();

		return (Boolean) first_exp.accept(this, env) && (Boolean) second_exp.accept(this, env);
	}

	public Boolean visit(AddExp e, Env<Type> env) throws ProgramError {
		return visitCompoundArithExp(e, env);
	}

	public Boolean visit(DivExp e, Env<Type> env) throws ProgramError {
		return visitCompoundArithExp(e, env);
	}

	public Boolean visit(MultExp e, Env<Type> env) throws ProgramError {
		return visitCompoundArithExp(e, env);
	}

	public Boolean visit(SubExp e, Env<Type> env) throws ProgramError {
		return visitCompoundArithExp(e, env);
	}

	public Boolean visit(ErrorExp e, Env<Type> env) throws ProgramError {
		return true;
	}

	private Boolean visitCompoundArithExp(CompoundArithExp e, Env<Type> env) throws ProgramError {
		List<Exp> operands = e.all();

		boolean purity = true;
		for (Exp exp : operands) {
			purity &= (Boolean) exp.accept(this, env);
		}

		return purity;
	}

	public Boolean visit(ReadExp e, Env<Type> env) throws ProgramError {
		return false;
	}

	public Boolean visit(EvalExp e, Env<Type> env) throws ProgramError {
		return false;
	}

	@Override
	public Boolean visit(IsNullExp e, Env<Type> env) throws ProgramError {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsProcedureExp e, Env<Type> env) throws ProgramError {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsListExp e, Env<Type> env) throws ProgramError {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsPairExp e, Env<Type> env) throws ProgramError {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsUnitExp e, Env<Type> env) throws ProgramError {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsNumberExp e, Env<Type> env) throws ProgramError {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsStringExp e, Env<Type> env) throws ProgramError {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(IsBooleanExp e, Env<Type> env) throws ProgramError {
		Boolean exp_free = (Boolean) e.exp().accept(this, env);
		return exp_free;
	}

	@Override
	public Boolean visit(FuncSpec e, Env<Type> env) throws ProgramError {
		Boolean purity = true;
		for (AST.SpecCase speccase : e.speccases()) {
			purity = purity && (Boolean) speccase.accept(this, env);
		}
		return purity;
	}

	@Override
	public Boolean visit(SpecCase e, Env<Type> env) throws ProgramError {
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
		System.out.println("SpecLang: Type a program to check its purity press the enter key,\n"
				+ "e.g. (> (deref x) (set! x 0)) \n" + "or try  (> result 7) \n"
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
			} catch (ProgramError e) {
				System.out.println("Error:" + e.getMessage());
			}
		}

	}

}
