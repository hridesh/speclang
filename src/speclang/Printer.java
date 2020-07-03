package speclang;

import java.util.List;

import speclang.AST.Exp;
import speclang.AST.ProgramError;

public class Printer {

	public void print(Value v) {
		if (v.tostring() != "")
			System.out.println(v.tostring());
	}

	public void print(Type t) {
		if (t.tostring() != "")
			System.out.println(t.tostring());
	}

	public void print(Exception e) {
		System.out.println(e.toString());
	}

	public static class Formatter implements AST.Visitor<String, Void> {
		
		public String format(AST.ASTNode n, Env<Void> env) throws ProgramError {
			return n.accept(this, env);
		}

		public String visit(AST.AddExp e, Env<Void> env) throws ProgramError {
			String result = "(+ ";
			for (AST.Exp exp : e.all())
				result += exp.accept(this, env) + " ";
			return result + ")";
		}

		public String visit(AST.UnitExp e, Env<Void> env) throws ProgramError {
			return "unit";
		}

		public String visit(AST.NumExp e, Env<Void> env) throws ProgramError {
			return "" + e.v();
		}

		public String visit(AST.StrExp e, Env<Void> env) throws ProgramError {
			return e.v();
		}

		public String visit(AST.BoolExp e, Env<Void> env) throws ProgramError {
			if (e.v())
				return "#t";
			return "#f";
		}

		public String visit(AST.DivExp e, Env<Void> env) throws ProgramError {
			String result = "(/ ";
			for (AST.Exp exp : e.all())
				result += exp.accept(this, env) + " ";
			return result + ")";
		}

		public String visit(AST.ErrorExp e, Env<Void> env) throws ProgramError {
			return e.toString();
		}

		public String visit(AST.ReadExp e, Env<Void> env) throws ProgramError {
			return "(read " + e.file().accept(this, env) + ")";
		}

		public String visit(AST.EvalExp e, Env<Void> env) throws ProgramError {
			return "(eval " + e.code().accept(this, env) + ")";
		}

		public String visit(AST.MultExp e, Env<Void> env) throws ProgramError {
			String result = "(* ";
			for (AST.Exp exp : e.all())
				result += exp.accept(this, env) + " ";
			return result + ")";
		}

		public String visit(AST.Program p, Env<Void> env) throws ProgramError {
			return "" + p.e().accept(this, env);
		}

		public String visit(AST.SubExp e, Env<Void> env) throws ProgramError {
			String result = "(- ";
			for (AST.Exp exp : e.all())
				result += exp.accept(this, env) + " ";
			return result + ")";
		}

		public String visit(AST.VarExp e, Env<Void> env) throws ProgramError {
			return "" + e.name();
		}

		public String visit(AST.LetExp e, Env<Void> env) throws ProgramError {
			String result = "(let (";
			List<String> names = e.names();
			List<Exp> value_exps = e.value_exps();
			int num_decls = names.size();
			for (int i = 0; i < num_decls; i++) {
				result += " (";
				result += names.get(i) + " ";
				result += value_exps.get(i).accept(this, env) + ")";
			}
			result += ") ";
			result += e.body().accept(this, env) + " ";
			return result + ")";
		}

		public String visit(AST.DefineDecl d, Env<Void> env) throws ProgramError {
			String result = "(define ";
			result += d.name() + " ";
			result += d.value_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.LambdaExp e, Env<Void> env) throws ProgramError {
			String result = "(lambda ( ";
			for (String formal : e.formals())
				result += formal + " ";
			result += ") ";
			result += e.body().accept(this, env);
			return result + ")";
		}

		public String visit(AST.CallExp e, Env<Void> env) throws ProgramError {
			String result = "(";
			result += e.operator().accept(this, env) + " ";
			for (AST.Exp exp : e.operands())
				result += exp.accept(this, env) + " ";
			return result + ")";
		}

		public String visit(AST.IfExp e, Env<Void> env) throws ProgramError {
			String result = "(if ";
			result += e.conditional().accept(this, env) + " ";
			result += e.then_exp().accept(this, env) + " ";
			result += e.else_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.LessExp e, Env<Void> env) throws ProgramError {
			String result = "(< ";
			result += e.first_exp().accept(this, env) + " ";
			result += e.second_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.LessEqExp e, Env<Void> env) throws ProgramError {
			String result = "(<= ";
			result += e.first_exp().accept(this, env) + " ";
			result += e.second_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.EqualExp e, Env<Void> env) throws ProgramError {
			String result = "(= ";
			result += e.first_exp().accept(this, env) + " ";
			result += e.second_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.GreaterExp e, Env<Void> env) throws ProgramError {
			String result = "(> ";
			result += e.first_exp().accept(this, env) + " ";
			result += e.second_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.GreaterEqExp e, Env<Void> env) throws ProgramError {
			String result = "(>= ";
			result += e.first_exp().accept(this, env) + " ";
			result += e.second_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.CarExp e, Env<Void> env) throws ProgramError {
			String result = "(car ";
			result += e.arg().accept(this, env);
			return result + ")";
		}

		public String visit(AST.CdrExp e, Env<Void> env) throws ProgramError {
			String result = "(cdr ";
			result += e.arg().accept(this, env);
			return result + ")";
		}

		public String visit(AST.ConsExp e, Env<Void> env) throws ProgramError {
			String result = "(cons ";
			result += e.fst().accept(this, env) + " ";
			result += e.snd().accept(this, env);
			return result + ")";
		}

		public String visit(AST.ListExp e, Env<Void> env) throws ProgramError {
			String result = "(list ";
			for (AST.Exp exp : e.elems())
				result += exp.accept(this, env) + " ";
			return result + ")";
		}

		public String visit(AST.NullExp e, Env<Void> env) throws ProgramError {
			String result = "(null? ";
			result += e.arg().accept(this, env);
			return result + ")";
		}

		public String visit(AST.LetrecExp e, Env<Void> env) throws ProgramError {
			String result = "(letrec (";
			List<String> names = e.names();
			List<Exp> fun_exps = e.fun_exps();
			int num_decls = names.size();
			for (int i = 0; i < num_decls; i++) {
				result += " (";
				result += names.get(i) + " ";
				result += fun_exps.get(i).accept(this, env) + ")";
			}
			result += ") ";
			result += e.body().accept(this, env) + " ";
			return result + ")";
		}

		@Override
		public String visit(AST.IsListExp e, Env<Void> env) throws ProgramError {
			String result = "(list? ";
			result += e.exp().accept(this, env);
			return result + ")";
		}

		@Override
		public String visit(AST.IsPairExp e, Env<Void> env) throws ProgramError {
			String result = "(pair? ";
			result += e.exp().accept(this, env);
			return result + ")";
		}

		@Override
		public String visit(AST.IsUnitExp e, Env<Void> env) throws ProgramError {
			String result = "(unit? ";
			result += e.exp().accept(this, env);
			return result + ")";
		}

		@Override
		public String visit(AST.IsProcedureExp e, Env<Void> env) throws ProgramError {
			String result = "(procedure? ";
			result += e.exp().accept(this, env);
			return result + ")";
		}

		@Override
		public String visit(AST.IsStringExp e, Env<Void> env) throws ProgramError {
			String result = "(string? ";
			result += e.exp().accept(this, env);
			return result + ")";
		}

		@Override
		public String visit(AST.IsNumberExp e, Env<Void> env) throws ProgramError {
			String result = "(number? ";
			result += e.exp().accept(this, env);
			return result + ")";
		}

		@Override
		public String visit(AST.IsBooleanExp e, Env<Void> env) throws ProgramError {
			String result = "(boolean? ";
			result += e.exp().accept(this, env);
			return result + ")";
		}

		@Override
		public String visit(AST.IsNullExp e, Env<Void> env) throws ProgramError {
			String result = "(null? ";
			result += e.exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.RefExp e, Env<Void> env) throws ProgramError {
			String result = "(ref ";
			result += e.value_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.DerefExp e, Env<Void> env) throws ProgramError {
			String result = "(deref ";
			result += e.loc_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.AssignExp e, Env<Void> env) throws ProgramError {
			String result = "(set! ";
			result += e.lhs_exp().accept(this, env) + " ";
			result += e.rhs_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.FreeExp e, Env<Void> env) throws ProgramError {
			String result = "(free ";
			result += e.value_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.FuncSpec e, Env<Void> env) throws ProgramError {
			String result = "";
			List<AST.SpecCase> speccases = e.speccases();
			for(int i=0; i < speccases.size(); i++)
				if(i<speccases.size()-1)
					result += speccases.get(i).accept(this, env) + "||";
				else result += speccases.get(i).accept(this, env) + " ";
			return result;
		}
	
		public String visit(AST.SpecCase e, Env<Void> env) throws ProgramError {
			String result = "";
			for (Exp precondition : e.preconditions())
				result += precondition.accept(this, env) + " ";
			result += "->";
			for (Exp postcondition : e.postconditions())
				result += postcondition.accept(this, env) + " ";
			return result;
		}
	}
}
