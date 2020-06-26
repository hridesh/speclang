package speclang;

import static speclang.AST.*;
import static speclang.Heap.*;
import static speclang.Value.*;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import speclang.AST.Exp;
import speclang.AST.FuncSpec;
import speclang.Env.*;

public class Evaluator implements Visitor<Value, Env<Value>> {

	private final Printer.Formatter ts = new Printer.Formatter();

	private volatile Env<Value> initEnv = initialEnv();

	Heap heap = null;

	Value valueOf(Program p) {
		heap = new Heap16Bit();
		return (Value) p.accept(this, initEnv);
	}

	@Override
	public Value visit(AddExp e, Env<Value> env) {
		List<Exp> operands = e.all();
		int result = 0;
		for (Exp exp : operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env);
			result += intermediate.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(UnitExp e, Env<Value> env) {
		return new UnitVal();
	}

	@Override
	public Value visit(NumExp e, Env<Value> env) {
		return new NumVal(e.v());
	}

	@Override
	public Value visit(StrExp e, Env<Value> env) {
		return new StringVal(e.v());
	}

	@Override
	public Value visit(BoolExp e, Env<Value> env) {
		return new BoolVal(e.v());
	}

	@Override
	public Value visit(DivExp e, Env<Value> env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v();
		for (int i = 1; i < operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result / rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(ErrorExp e, Env<Value> env) {
		return new Value.DynamicError("Encountered an error expression");
	}

	@Override
	public Value visit(MultExp e, Env<Value> env) {
		List<Exp> operands = e.all();
		double result = 1;
		for (Exp exp : operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic
			// type-checking
			result *= intermediate.v(); // Semantics of MultExp.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(Program p, Env<Value> env) {
		for (DefineDecl d : p.decls())
			d.accept(this, initEnv);
		return (Value) p.e().accept(this, initEnv);
	}

	@Override
	public Value visit(SubExp e, Env<Value> env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v();
		for (int i = 1; i < operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result - rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(VarExp e, Env<Value> env) {
		// Previously, all variables had value 42. New semantics.
		return env.get(e.name());
	}

	@Override
	public Value visit(LetExp e, Env<Value> env) { // New for varlang.
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());

		for (Exp exp : value_exps)
			values.add((Value) exp.accept(this, env));

		Env<Value> new_env = env;
		for (int index = 0; index < names.size(); index++)
			new_env = new ExtendEnv<>(new_env, names.get(index), values.get(index));

		return (Value) e.body().accept(this, new_env);
	}

	@Override
	public Value visit(DefineDecl e, Env<Value> env) { // New for definelang.
		String name = e.name();
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env);
		initEnv = new ExtendEnv<>(initEnv, name, value);
		return new Value.UnitVal();
	}

	@Override
	public Value visit(LambdaExp e, Env<Value> env) { // New for funclang.
		return new Value.FunVal(env, e.formals(), e.spec(), e.body()); //Notice that function values also contain specifications now.
	}

	@Override
	public Value visit(CallExp e, Env<Value> env) { // New for funclang.
		Object result = e.operator().accept(this, env);
		if (!(result instanceof Value.FunVal))
			return new Value.DynamicError("Operator not a function in call " + ts.visit(e, null));
		Value.FunVal operator = (Value.FunVal) result; // Dynamic checking
		List<Exp> operands = e.operands();

		// Call-by-value semantics
		List<Value> actuals = new ArrayList<Value>(operands.size());
		for (Exp exp : operands)
			actuals.add((Value) exp.accept(this, env));

		List<String> formals = operator.formals();
		if (formals.size() != actuals.size())
			return new Value.DynamicError("Argument mismatch in call " + ts.visit(e, null));

		Env<Value> closure_env = operator.env();
		Env<Value> fun_env = appendEnv(closure_env, initEnv);
		for (int index = 0; index < formals.size(); index++)
			fun_env = new ExtendEnv<>(fun_env, formals.get(index), actuals.get(index));

		// Runtime verification of specifications.
		// First check the precondition
		Value.BoolVal precondition = (Value.BoolVal) operator.spec().accept(this, fun_env);
		if (precondition.v()) { //Precondition is true
			Value fresult = (Value) operator.body().accept(this, fun_env); 
			//Create a new environment to check postconditions that has the result of the function.
			Env<Value> post_env = new ExtendEnv<>(fun_env, "result", fresult);
			Value.BoolVal postcondition = (Value.BoolVal) operator.spec().accept(this, post_env);
			if (postcondition.v()) // Postcondition is true
				return fresult;
			return new Value.DynamicError("Postcondition violation in call " + ts.visit(e, null));
		}
		return new Value.DynamicError("Precondition violation in call " + ts.visit(e, null));
	}

	/* Helper for CallExp */
	/***
	 * Create an env that has bindings from fst appended to bindings from snd. The
	 * order of bindings is bindings from fst followed by that from snd.
	 * 
	 * @param fst
	 * @param snd
	 * @return
	 */
	private Env<Value> appendEnv(Env<Value> fst, Env<Value> snd) {
		if (fst.isEmpty())
			return snd;
		if (fst instanceof ExtendEnv) {
			ExtendEnv<Value> f = (ExtendEnv<Value>) fst;
			return new ExtendEnv<>(appendEnv(f.saved_env(), snd), f.var(), f.val());
		}
		ExtendEnvRec f = (ExtendEnvRec) fst;
		return new ExtendEnvRec(appendEnv(f.saved_env(), snd), f.names(), f.vals());
	}
	/* End: helper for CallExp */

	@Override
	public Value visit(IfExp e, Env<Value> env) { // New for funclang.
		Object result = e.conditional().accept(this, env);
		if (!(result instanceof Value.BoolVal))
			return new Value.DynamicError("Condition not a boolean in expression " + ts.visit(e, null));
		Value.BoolVal condition = (Value.BoolVal) result; // Dynamic checking

		if (condition.v())
			return (Value) e.then_exp().accept(this, env);
		else
			return (Value) e.else_exp().accept(this, env);
	}

	@Override
	public Value visit(LessExp e, Env<Value> env) { // New for funclang.
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() < second.v());
	}

	@Override
	public Value visit(EqualExp e, Env<Value> env) { // New for funclang.
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() == second.v());
	}

	@Override
	public Value visit(GreaterExp e, Env<Value> env) { // New for funclang.
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() > second.v());
	}

	@Override
	public Value visit(CarExp e, Env<Value> env) {
		Value.PairVal pair = (Value.PairVal) e.arg().accept(this, env);
		return pair.fst();
	}

	@Override
	public Value visit(CdrExp e, Env<Value> env) {
		Value.PairVal pair = (Value.PairVal) e.arg().accept(this, env);
		return pair.snd();
	}

	@Override
	public Value visit(ConsExp e, Env<Value> env) {
		Value first = (Value) e.fst().accept(this, env);
		Value second = (Value) e.snd().accept(this, env);
		return new Value.PairVal(first, second);
	}

	@Override
	public Value visit(ListExp e, Env<Value> env) { // New for funclang.
		List<Exp> elemExps = e.elems();
		int length = elemExps.size();
		if (length == 0)
			return new Value.Null();

		// Order of evaluation: left to right e.g. (list (+ 3 4) (+ 5 4))
		Value[] elems = new Value[length];
		for (int i = 0; i < length; i++)
			elems[i] = (Value) elemExps.get(i).accept(this, env);

		Value result = new Value.Null();
		for (int i = length - 1; i >= 0; i--)
			result = new PairVal(elems[i], result);
		return result;
	}

	@Override
	public Value visit(NullExp e, Env<Value> env) {
		Value val = (Value) e.arg().accept(this, env);
		return new BoolVal(val instanceof Value.Null);
	}

	public Value visit(EvalExp e, Env<Value> env) {
		StringVal programText = (StringVal) e.code().accept(this, env);
		Program p = _reader.parse(programText.v());
		return (Value) p.accept(this, env);
	}

	public Value visit(ReadExp e, Env<Value> env) {
		StringVal fileName = (StringVal) e.file().accept(this, env);
		try {
			String text = Reader.readFile("" + System.getProperty("user.dir") + File.separator + fileName.v());
			return new StringVal(text);
		} catch (IOException ex) {
			return new DynamicError(ex.getMessage());
		}
	}

	@Override
	public Value visit(LetrecExp e, Env<Value> env) { // New for reclang.
		List<String> names = e.names();
		List<Exp> fun_exps = e.fun_exps();
		List<Value.FunVal> funs = new ArrayList<Value.FunVal>(fun_exps.size());

		for (Exp exp : fun_exps)
			funs.add((Value.FunVal) exp.accept(this, env));

		Env<Value> new_env = new ExtendEnvRec(env, names, funs);
		return (Value) e.body().accept(this, new_env);
	}

	@Override
	public Value visit(IsListExp e, Env<Value> env) {
		Value val = (Value) e.exp().accept(this, env);
		return new BoolVal(val instanceof Value.PairVal && ((Value.PairVal) val).isList() || val instanceof Value.Null);
	}

	@Override
	public Value visit(IsPairExp e, Env<Value> env) {
		Value val = (Value) e.exp().accept(this, env);
		return new BoolVal(val instanceof Value.PairVal);
	}

	@Override
	public Value visit(IsUnitExp e, Env<Value> env) {
		Value val = (Value) e.exp().accept(this, env);
		return new BoolVal(val instanceof Value.UnitVal);
	}

	@Override
	public Value visit(IsProcedureExp e, Env<Value> env) {
		Value val = (Value) e.exp().accept(this, env);
		return new BoolVal(val instanceof Value.FunVal);
	}

	@Override
	public Value visit(IsStringExp e, Env<Value> env) {
		Value val = (Value) e.exp().accept(this, env);
		return new BoolVal(val instanceof Value.StringVal);
	}

	@Override
	public Value visit(IsNumberExp e, Env<Value> env) {
		Value val = (Value) e.exp().accept(this, env);
		return new BoolVal(val instanceof Value.NumVal);
	}

	@Override
	public Value visit(IsBooleanExp e, Env<Value> env) {
		Value val = (Value) e.exp().accept(this, env);
		return new BoolVal(val instanceof Value.BoolVal);
	}

	@Override
	public Value visit(IsNullExp e, Env<Value> env) {
		Value val = (Value) e.exp().accept(this, env);
		return new BoolVal(val instanceof Value.Null);
	}

	@Override
	public Value visit(RefExp e, Env<Value> env) { // New for typelang.
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env);
		return heap.ref(value);
	}

	@Override
	public Value visit(DerefExp e, Env<Value> env) { // New for typelang.
		Exp loc_exp = e.loc_exp();
		Value.RefVal loc = (Value.RefVal) loc_exp.accept(this, env);
		return heap.deref(loc);
	}

	@Override
	public Value visit(AssignExp e, Env<Value> env) { // New for typelang.
		Exp rhs = e.rhs_exp();
		Exp lhs = e.lhs_exp();
		// Note the order of evaluation below.
		Value rhs_val = (Value) rhs.accept(this, env);
		Value.RefVal loc = (Value.RefVal) lhs.accept(this, env);
		Value assign_val = heap.setref(loc, rhs_val);
		return assign_val;
	}

	@Override
	public Value visit(FreeExp e, Env<Value> env) { // New for typelang.
		Exp value_exp = e.value_exp();
		Value.RefVal loc = (Value.RefVal) value_exp.accept(this, env);
		heap.free(loc);
		return new Value.UnitVal();
	}

	@Override
	public Value visit(FuncSpec s, Env<Value> env) {
		// Are we checking preconditions or postconditions?
		try {
			env.get("result"); // result of function
			// Check postconditions
			for (Exp postcondition : s.postconditions()) {
				Value postcond_value = postcondition.accept(this, env);
				if (!(postcond_value instanceof Value.BoolVal))
					return new Value.DynamicError("Condition not a boolean in expression " + ts.visit(s, null));
				Value.BoolVal condition = (Value.BoolVal) postcond_value;
				if (!condition.v())
					return condition;

			}
		} catch (LookupException e) {
			// Check preconditions
			for (Exp precondition : s.preconditions()) {
				Value precond_value = precondition.accept(this, env);
				if (!(precond_value instanceof Value.BoolVal))
					return new Value.DynamicError("Condition not a boolean in expression " + ts.visit(s, null));
				Value.BoolVal condition = (Value.BoolVal) precond_value;
				if (!condition.v())
					return condition;

			}
		}
		return new Value.BoolVal(true);
	}

	private Env<Value> initialEnv() {
		Env<Value> initEnv = new EmptyEnv<>();

		/*
		 * Procedure: (read <filename>). Following is same as (define read (lambda
		 * (file) (read file)))
		 */
		List<String> formals = new ArrayList<>();
		formals.add("file");
		Exp body = new AST.ReadExp(new VarExp("file"));
		Value.FunVal readFun = new Value.FunVal(initEnv, formals, null, body); //Exercise: What would the specification here?
		initEnv = new Env.ExtendEnv<>(initEnv, "read", readFun);

		/*
		 * Procedure: (require <filename>). Following is same as (define require (lambda
		 * (file) (eval (read file))))
		 */
		formals = new ArrayList<>();
		formals.add("file");
		body = new EvalExp(new AST.ReadExp(new VarExp("file")));
		Value.FunVal requireFun = new Value.FunVal(initEnv, formals, null, body); //Exercise: What would the specification here?
		initEnv = new Env.ExtendEnv<>(initEnv, "require", requireFun);

		/* Add new built-in procedures here */

		return initEnv;
	}

	Reader _reader;

	public Evaluator(Reader reader) {
		_reader = reader;
	}

}
