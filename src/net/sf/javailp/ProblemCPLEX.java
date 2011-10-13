package net.sf.javailp;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ProblemCPLEX extends Problem {
	private IloCplex cplex;
	private Map<String, IloNumVar> nameToVar 	= new LinkedHashMap<String, IloNumVar>();
	private Linear objectiveFunction;
	private int numberOfConstraints 			= 0;
	
	/**
	 * Constructs a {@code ProblemCPLEX}.
	 * 
	 */
	protected ProblemCPLEX(IloCplex cplex) {
		this.cplex = cplex;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setObjective(net.sf.javailp.Linear, net.sf.javailp.OptType)
	 */
	public void setObjective(Linear objective, OptType optType) {
		try {
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for (Term term : objective.terms) {
				IloNumVar var = nameToVar.get(term.getVariableName());
				if (var == null) {
					throw new IllegalArgumentException(
					"Variables in a linear expression must be added to the problem first. " +
					"(missing: "+term.getVariableName()+")");
				}
				expr.addTerm(term.getCoefficient().doubleValue(), var);
			}
	
			if (optType == OptType.MIN) {
				cplex.addMinimize(expr);
			} else {
				cplex.addMaximize(expr);
			}
		} catch (IloException e) {
			e.printStackTrace();
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
		return numberOfConstraints;
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
		try {
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for (Term term : lhs.terms) {
				IloNumVar var = nameToVar.get(term.getVariableName());
				if (var == null) {
					throw new IllegalArgumentException(
					"Variables in a linear expression must be added to the problem first. " +
					"(missing: "+term.getVariableName()+")");
				}
				expr.addTerm(term.getCoefficient().doubleValue(), var);
			}

			switch (operator) {
			case LE:
				cplex.addLe(expr, rhs.doubleValue());
				break;
			case GE:
				cplex.addGe(expr, rhs.doubleValue());
				break;
			default:
				cplex.addEq(expr, rhs.doubleValue());
			}
			numberOfConstraints++;
		} catch (IloException e) {
			e.printStackTrace();
			throw new OptimizationException(e.getMessage());
		} 

	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#addVariable(java.lang.String, net.sf.javailp.VarType, java.lang.Number, java.lang.Number)
	 */
	public void addVariable(String name, VarType type, Number lb, Number ub) {
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
				break;
			}
	
			nameToVar.put(name, cplex.numVar(lowerBound, upperBound, varType));
		} catch (IloException e) {
			e.printStackTrace();
			throw new OptimizationException(e.getMessage());
		} 
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.AbstractProblem#optimize(boolean)
	 */
	protected Result optimize(boolean postSolve) {
		try {
			if (!cplex.solve()) {
				throw new OptimizationException("No optimal solution found.");
			}
	
			Result result = new ResultImpl(this.objectiveFunction);
			
			if (postSolve) {
				System.err.println("lp relaxation with fixed integers not yet implemented for CPLEX");
			}
			
			for (Entry<String, IloNumVar> entry : nameToVar.entrySet()) {
				String variableName = entry.getKey();
				IloNumVar var = entry.getValue();
	
				double value = cplex.getValue(var);
				if (var.getType() != IloNumVarType.Float) {
					int v = (int) Math.round(value);
					result.putPrimalValue(variableName, v);
				} else {
					result.putPrimalValue(variableName, value);
				}
			}
	
			return result;
		} catch (IloException e) {
			e.printStackTrace();
			throw new OptimizationException(e.getMessage());
		} 
	}

}
