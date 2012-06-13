package net.sf.javailp;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author fgenoese
 *
 */
public class ProblemCPLEX extends Problem {
	private IloCplex model;
	private Map<String, IloNumVar> nameToVar 	= new LinkedHashMap<String, IloNumVar>();
	private List<String> conNames				= new ArrayList<String>();
	private Linear objectiveFunction;
	
	/**
	 * Constructs a {@code ProblemCPLEX}.
	 * 
	 */
	protected ProblemCPLEX(IloCplex model) {
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setObjective(net.sf.javailp.Linear, net.sf.javailp.OptType)
	 */
	public void setObjective(Linear objective, OptType optType) {
		try {
			List<IloNumExpr> expressions = new ArrayList<IloNumExpr>();
			IloNumExpr expr = model.linearNumExpr();
			for (Term term : objective.terms) {
				IloNumVar var = nameToVar.get(term.getVariableName());
				if (var == null) {
					throw new IllegalArgumentException(
					"Variables in a linear expression must be added to the problem first. " +
					"(missing: "+term.getVariableName()+")");
				}
				expressions.add(model.prod(term.getCoefficient().doubleValue(), var));
			}
			expr = model.sum(expressions.toArray(new IloNumExpr[0]));
	
			if (optType == OptType.MIN) {
				model.addMinimize(expr);
			} else {
				model.addMaximize(expr);
			}
		} catch (IloException e) {
			throw new OptimizationException(e.getMessage());
		} 

		this.objectiveFunction = objective;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setStartingSolution(java.util.Map)
	 */
	public void setStartingSolution(Map<String, Number> startingSolution) {
		System.err.println("usage of a starting solution not yet implemented for CPLEX");
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#getConstraintsCount()
	 */
	public int getConstraintsCount() {
		return conNames.size();
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#getVariablesCount()
	 */
	public int getVariablesCount() {
		return nameToVar.size();
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#addConstraint(java.lang.String, net.sf.javailp.Linear, net.sf.javailp.Operator, java.lang.Number)
	 */
	public void addConstraint(String name, Linear lhs, Operator operator, Number rhs) {
		if (conNames.contains(name)) {
			System.err.println("cannot add constraint '"+name+"': a constraint with this name already exists");
			return;
		}
		try {
			List<IloNumExpr> expressions = new ArrayList<IloNumExpr>();
			IloNumExpr expr = model.linearNumExpr();
			for (Term term : lhs.terms) {
				IloNumVar var = nameToVar.get(term.getVariableName());
				if (var == null) {
					throw new IllegalArgumentException(
					"Variables in a linear expression must be added to the problem first. " +
					"(missing: "+term.getVariableName()+")");
				}
				expressions.add(model.prod(term.getCoefficient().doubleValue(), var));
			}
			expr = model.sum(expressions.toArray(new IloNumExpr[0]));

			switch (operator) {
				case LE:
					model.addLe(expr, rhs.doubleValue());
					break;
				case GE:
					model.addGe(expr, rhs.doubleValue());
					break;
				default:
					model.addEq(expr, rhs.doubleValue());
			}
			conNames.add(name);
		} catch (IloException e) {
			throw new OptimizationException(e.getMessage());
		} 
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#addVariable(java.lang.String, net.sf.javailp.VarType, java.lang.Number, java.lang.Number)
	 */
	public void addVariable(String name, VarType type, Number lb, Number ub) {
		if (nameToVar.containsKey(name)) {
			System.err.println("cannot add variable '"+name+"': a variable with this name already exists");
			return;
		}
		try {
			double lowerBound = (lb != null ? lb.doubleValue() : Double.NEGATIVE_INFINITY);
			double upperBound = (ub != null ? ub.doubleValue() : Double.POSITIVE_INFINITY);
			
			final IloNumVarType varType;
			switch (type) {
				case BOOL:
					varType = IloNumVarType.Bool;
					break;
				case INT:
					varType = IloNumVarType.Int;
					break;
				default:
					varType = IloNumVarType.Float;
			}
	
			nameToVar.put(name, model.numVar(lowerBound, upperBound, varType));
		} catch (IloException e) {
			throw new OptimizationException(e.getMessage());
		} 
	}
	
	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setVariableLowerBound(java.lang.String, java.lang.Number)
	 */
	public void setVariableLowerBound(String name, Number lb) {
		try {
			IloNumVar var = nameToVar.get(name);
			if (var == null) {
				throw new IllegalArgumentException(
				"Variables must be added to the problem before a bound can be set. " +
				"(missing: "+name+")");
			}
			double lowerBound = (lb != null ? lb.doubleValue() : Double.NEGATIVE_INFINITY);
			var.setLB(lowerBound);
		} catch (IloException e) {
			throw new OptimizationException(e.getMessage());
		} 
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setVariableUpperBound(java.lang.String, java.lang.Number)
	 */
	public void setVariableUpperBound(String name, Number ub) {
		try {
			IloNumVar var = nameToVar.get(name);
			if (var == null) {
				throw new IllegalArgumentException(
				"Variables must be added to the problem before a bound can be set. " +
				"(missing: "+name+")");
			}
			double upperBound = (ub != null ? ub.doubleValue() : Double.POSITIVE_INFINITY);
			var.setUB(upperBound);
		} catch (IloException e) {
			throw new OptimizationException(e.getMessage());
		} 
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.AbstractProblem#optimize(boolean, boolean)
	 */
	protected Result optimize(boolean postSolve, boolean activateLog) {
		try {
			if (!model.solve()) {
				throw new OptimizationException("No optimal solution found.");
			}
	
			Result result = new ResultImpl(this.objectiveFunction);
			
			if (postSolve) {
				System.err.println("lp relaxation with fixed integers not yet implemented for CPLEX");
			}
			
			for (Entry<String, IloNumVar> entry : nameToVar.entrySet()) {
				String variableName = entry.getKey();
				IloNumVar var = entry.getValue();
	
				double value = model.getValue(var);
				if (var.getType() != IloNumVarType.Float) {
					int v = (int) Math.round(value);
					result.putPrimalValue(variableName, v);
				} else {
					result.putPrimalValue(variableName, value);
				}
			}
	
			return result;
		} catch (IloException e) {
			throw new OptimizationException(e.getMessage());
		} 
	}

}
