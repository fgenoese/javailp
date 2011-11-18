package net.sf.javailp;

import java.util.HashMap;
import java.util.Map;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;

/**
 * @author fgenoese
 *
 */
public class ProblemGurobi extends Problem {
	
	private GRBModel model;
	private boolean hasChanged 					= false;
	private Map<String, GRBVar> nameToVar 		= new HashMap<String, GRBVar>();
	private Map<String, GRBConstr> nameToCon 	= new HashMap<String, GRBConstr>();
	private Linear objectiveFunction;
	
	/**
	 * Constructs a {@code ProblemGurobi}.
	 * 
	 */
	protected ProblemGurobi(GRBEnv env, GRBModel model) {
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setObjective(net.sf.javailp.Linear, net.sf.javailp.OptType)
	 */
	public void setObjective(Linear objective, OptType optType) {
		try {
			if (hasChanged) {
				model.update();
				hasChanged = false;
			}
			for (Term term : objective.terms) {
				GRBVar var = nameToVar.get(term.getVariableName());
				if (var == null) {
					throw new IllegalArgumentException(
					"Variables in a linear expression must be added to the problem first. " +
					"(missing: "+term.getVariableName()+")");
				}
				if (optType == OptType.MIN) {
					var.set(GRB.DoubleAttr.Obj, +term.getCoefficient().doubleValue());
				} else {
					var.set(GRB.DoubleAttr.Obj, -term.getCoefficient().doubleValue());
				}
			}
			this.objectiveFunction = objective;
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}
	
	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setStartingSolution(java.util.Map)
	 */
	public void setStartingSolution(Map<String, Number> startingSolution) {
		try {
			for (String variableName : startingSolution.keySet()) {
				GRBVar var = nameToVar.get(variableName);
				if (var == null) {
					throw new IllegalArgumentException(
					"Variables in the starting solution must be added to the problem first. " +
					"(missing: "+variableName+")");
				}
				var.set(DoubleAttr.Start, startingSolution.get(variableName).doubleValue());
			}
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#getConstraintsCount()
	 */
	public int getConstraintsCount() {
		return nameToCon.size();
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
		if (nameToCon.containsKey(name)) {
			System.err.println("cannot add constraint '"+name+"': a constraint with this name already exists");
			return;
		}
		try {
			if (hasChanged) {
				model.update();
				hasChanged = false;
			}
			
			GRBLinExpr expr = new GRBLinExpr();
			for (Term term : lhs.terms) {
				GRBVar var = nameToVar.get(term.getVariableName());
				if (var == null) {
					throw new IllegalArgumentException(
					"Variables in a linear expression must be added to the problem first. " +
					"(missing: "+term.getVariableName()+")");
				}
				expr.addTerm(term.getCoefficient().doubleValue(), var);
			}

			final char op;
			switch (operator) {
				case GE:
					op = GRB.GREATER_EQUAL;
					break;
				case LE:
					op = GRB.LESS_EQUAL;
					break;
				default:
					op = GRB.EQUAL;
			}

			nameToCon.put(name, model.addConstr(expr, op, rhs.doubleValue(), name));
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
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
			hasChanged = true;
			
			double lowerBound = (lb != null ? lb.doubleValue() : Double.NEGATIVE_INFINITY);
			double upperBound = (ub != null ? ub.doubleValue() : Double.POSITIVE_INFINITY);
			
			final char varType;
			switch (type) {
				case BOOL:
					varType = GRB.BINARY;
					break;
				case INT:
					varType = GRB.INTEGER;
					break;
				default:
					varType = GRB.CONTINUOUS;
			}
			
			nameToVar.put(name, model.addVar(lowerBound, upperBound, 0, varType, name));
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setVariableLowerBound(java.lang.String, java.lang.Number)
	 */
	public void setVariableLowerBound(String name, Number lb) {
		try {
			if (hasChanged) {
				model.update();
				hasChanged = false;
			}
			GRBVar var = nameToVar.get(name);
			if (var == null) {
				throw new IllegalArgumentException(
				"Variables must be added to the problem before a bound can be set. " +
				"(missing: "+name+")");
			}
			double lowerBound = (lb != null ? lb.doubleValue() : Double.NEGATIVE_INFINITY);
			var.set(GRB.DoubleAttr.LB, lowerBound);
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setVariableUpperBound(java.lang.String, java.lang.Number)
	 */
	public void setVariableUpperBound(String name, Number ub) {
		try {
			if (hasChanged) {
				model.update();
				hasChanged = false;
			}
			GRBVar var = nameToVar.get(name);
			if (var == null) {
				throw new IllegalArgumentException(
				"Variables must be added to the problem before a bound can be set. " +
				"(missing: "+name+")");
			}
			double upperBound = (ub != null ? ub.doubleValue() : Double.POSITIVE_INFINITY);
			var.set(GRB.DoubleAttr.UB, upperBound);
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.javailp.AbstractProblem#optimize(boolean)
	 */
	protected Result optimize(boolean postSolve) {
		int i;
		try {
			model.optimize();
			if (model.get(GRB.IntAttr.Status) != GRB.OPTIMAL) {
				throw new OptimizationException("No optimal solution found [status: "+model.get(GRB.IntAttr.Status)+"].");
			}
			
			Result result = new ResultImpl(this.objectiveFunction);
			
			// post-solve: LP relaxation with fixed integers
			if (postSolve) {
				GRBModel fixed = model.fixedModel();
				fixed.getEnv().set(GRB.IntParam.Presolve, 0);
				fixed.optimize();
				if (fixed.get(GRB.IntAttr.Status) != GRB.OPTIMAL) {
					throw new OptimizationException("No optimal solution found [status: "+model.get(GRB.IntAttr.Status)+"].");
				}
				
				GRBVar[] variables		  	= fixed.getVars();
				double[] primalValues     	= fixed.get(GRB.DoubleAttr.X, variables);
				double[] dualValues			= fixed.get(GRB.DoubleAttr.RC, variables);
			    String[] variableNames		= fixed.get(GRB.StringAttr.VarName, variables);
				
			    for (i = 0; i < variables.length; i++) {
			    	if (variables[i].get(GRB.CharAttr.VType) != GRB.CONTINUOUS && variables[i].get(GRB.CharAttr.VType) != GRB.SEMICONT) {
			    		int v = (int) Math.round(primalValues[i]);
			    		result.putPrimalValue(variableNames[i], v);
			    	} else {
			    		result.putPrimalValue(variableNames[i], primalValues[i]);
			    	}
			    	result.putDualValue(variableNames[i], dualValues[i]);
			    }
			    
			    GRBConstr[] constraints		= fixed.getConstrs();
			    double[] shadowPrices		= fixed.get(GRB.DoubleAttr.Pi, constraints);
			    String[] constraintNames	= fixed.get(GRB.StringAttr.ConstrName, constraints);
			    
			    for (i = 0; i < constraints.length; i++) {
			    	result.putDualValue(constraintNames[i], shadowPrices[i]);
			    }
			    
			    fixed.dispose();
			    
			    return result;
			} // end post-solve
			
			GRBVar[] variables 		= model.getVars();
			double[] primalValues	= model.get(GRB.DoubleAttr.X, variables);
			String[] variableNames 	= model.get(GRB.StringAttr.VarName, variables);
			
			for (i = 0; i < variables.length; i++) {
				if (variables[i].get(GRB.CharAttr.VType) != GRB.CONTINUOUS && variables[i].get(GRB.CharAttr.VType) != GRB.SEMICONT) {
		    		int v = (int) Math.round(primalValues[i]);
		    		result.putPrimalValue(variableNames[i], v);
		    	} else {
		    		result.putPrimalValue(variableNames[i], primalValues[i]);
		    	}
			}
									
			return result;
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

}
