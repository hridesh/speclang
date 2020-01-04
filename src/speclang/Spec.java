package speclang;

import java.util.List;
import static speclang.AST.*;
import static speclang.Value.*;

public interface Spec {
	public String tostring();

	public boolean specEqual(Spec other);
	
	public boolean checkPre(Evaluator evalutor, Env<Value> env);
	
	public boolean checkPost(Evaluator evalutor, Env<Value> env);	

	static class SpecTrue implements Spec {	
		private static final SpecTrue _instance = new SpecTrue();

		public static SpecTrue getInstance() {
			return _instance;
		}

		public String tostring() {
			return "#t";
		}

		public boolean specEqual(Spec other) {
			return other == this;
		}

		@Override
		public boolean checkPre(Evaluator evalutor, Env<Value> env) {
			return true;
		}

		@Override
		public boolean checkPost(Evaluator evalutor, Env<Value> env) {
			return true;
		}
	}

	static class FuncSpec implements Spec {
		protected List<Exp> _preconditions;
		protected List<Exp> _postconditions;

		public FuncSpec(List<Exp> preconditions, List<Exp> postconditions) {
			_preconditions = preconditions;
			_postconditions = postconditions;
		}

		public List<Exp> preconditions() {
			return _preconditions;
		}

		public List<Exp> postconditions() {
			return _postconditions;
		}

		public java.lang.String tostring() {
			StringBuffer sb = new StringBuffer();
			int size = _preconditions.size();
			int i = 0;
			sb.append("(");
			Printer.Formatter ts = new Printer.Formatter();
			for (Exp precondition : _preconditions) {
				sb.append(precondition.accept(ts, null));
				if (i != size - 1) {
					sb.append(" ^ ");
				}

				i++;
			}
			sb.append(" -> ");
			for (Exp postcondition : _postconditions) {
				sb.append(postcondition.accept(ts, null));
				if (i != size - 1) {
					sb.append(" ^ ");
				}

				i++;
			}
			sb.append(")");
			return sb.toString();
		}

		public boolean specEqual(Spec other) {
			if (other instanceof FuncSpec) {
				FuncSpec fs = (FuncSpec) other;

				List<Exp> preconditions = fs._preconditions;
				int size = _preconditions.size();
				if (preconditions.size() == size) {
					for (int i = 0; i < size; i++) {
						if (!(preconditions.get(i)==_preconditions.get(i))) {
							return false;
						}
					}

					List<Exp> postconditions = fs._postconditions;
					size = _postconditions.size();
					if (postconditions.size() == size) {
						for (int i = 0; i < size; i++) {
							if (!(postconditions.get(i)==_postconditions.get(i))) {
								return false;
							}
						}
						
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean checkPre(Evaluator evaluator, Env<Value> env) {
			for(Exp precondition : _preconditions) {
				Value result = precondition.accept(evaluator, env);
				if (!(result instanceof Value.BoolVal))
					return new Value.DynamicError("Condition not a boolean in expression " + ts.visit(this, null));
				Value.BoolVal condition = (Value.BoolVal) result; // Dynamic checking

				if (!condition.v())
					return false;

			}
			return false;
		}

		@Override
		public boolean checkPost(Evaluator evalutor, Env<Value> env) {
			// TODO Auto-generated method stub
			return false;
		}
	}

}
