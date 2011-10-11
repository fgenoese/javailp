/**
 * Java ILP is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Java ILP is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Java ILP. If not, see http://www.gnu.org/licenses/.
 */
package net.sf.javailp;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@code SolverGurobi} is the {@code Solver} Gurobi.
 * 
 * @author fabiogenoese, lukasiewycz
 * 
 */
public class SolverGurobi extends AbstractSolver {
	
	private GRBEnv env;
	
	/**
	 * Constructs a {@code SolverGurobi}.
	 * 
	 */
	public SolverGurobi() {
		try {
			env = new GRBEnv("gurobi.log");
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			throw new OptimizationException();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem)
	 */
	public Result solve(Problem problem) {
		return solve(problem, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem,java.util.Map)
	 */
	public Result solve(Problem problem, Map<String,Double> startingSolution) {

		Map<Object, GRBVar> objToVar = new HashMap<Object, GRBVar>();
		Map<GRBVar, Object> varToObj = new HashMap<GRBVar, Object>();
		Map<String, Object> nameToObj = new HashMap<String, Object>();
		Map<String, Constraint> nameToCon = new HashMap<String, Constraint>();
		
		if (env == null) {
			throw new OptimizationException();
		}

		try {
			initWithParameters(env);

			GRBModel model = new GRBModel(env);

			OptType optType = problem.getOptType();
			Map<Object, Double> optimizationCoefficients = new HashMap<Object, Double>();
			Linear objective = problem.getObjective();
			if (objective != null) {
				for (Term term : objective) {
					Object variable = term.getVariable();
					double coeff = term.getCoefficient().doubleValue();
					if (optType == OptType.MAX) {
						coeff *= -1;
					}
					optimizationCoefficients.put(variable, coeff);
				}
			}

			int i = 1;
			for (Object variable : problem.getVariables()) {
				VarType varType = problem.getVarType(variable);
				Number lowerBound = problem.getVarLowerBound(variable);
				Number upperBound = problem.getVarUpperBound(variable);

				double lb = (lowerBound != null ? lowerBound.doubleValue()
						: -Double.MAX_VALUE);
				double ub = (upperBound != null ? upperBound.doubleValue()
						: Double.MAX_VALUE);

				final String name = variable.toString();
				final char type;
				switch (varType) {
				case BOOL:
					type = GRB.BINARY;
					break;
				case INT:
					type = GRB.INTEGER;
					break;
				default: // REAL
					type = GRB.CONTINUOUS;
					break;
				}

				Double coeff = optimizationCoefficients.get(variable);
				if (coeff == null) {
					coeff = 0.0;
				}

				GRBVar var = model.addVar(lb, ub, coeff, type, name);
								
				objToVar.put(variable, var);
				varToObj.put(var, variable);
				nameToObj.put(name, variable);
				i++;
			}
			model.update();
			
			if (startingSolution != null) {
				for (Object variable : problem.getVariables()) {
					final String name = variable.toString();
					if (startingSolution.get(name) != null) {
						objToVar.get(variable).set(DoubleAttr.Start, startingSolution.get(name));
					}
				}
			}

			for (Constraint constraint : problem.getConstraints()) {
				GRBLinExpr expr = new GRBLinExpr();

				for (Term term : constraint.getLhs()) {
					GRBVar var = objToVar.get(term.getVariable());
					expr.addTerm(term.getCoefficient().doubleValue(), var);
				}

				final char operator;
				if (constraint.getOperator() == Operator.GE)
					operator = GRB.GREATER_EQUAL;
				else if (constraint.getOperator() == Operator.LE)
					operator = GRB.LESS_EQUAL;
				else
					operator = GRB.EQUAL;

				model.addConstr(expr, operator, constraint.getRhs()
						.doubleValue(), constraint.getName());
				nameToCon.put(constraint.getName(), constraint);
			}

			for(Hook hook: hooks){
				hook.call(env, model, objToVar, varToObj, problem);
			}
			
			model.optimize();
			if (model.get(GRB.IntAttr.Status) != GRB.OPTIMAL) {
				throw new OptimizationException();
			}

			Result result;
			if (problem.getObjective() != null) {
				result = new ResultImpl(problem.getObjective());
			} else {
				result = new ResultImpl();
			}
			
			// post-solve: LP relaxation with fixed integers
			Object postsolve = parameters.get(Solver.POSTSOLVE);
			if (postsolve != null && ((Number)postsolve).intValue() != 0 ) {
				GRBModel fixed = model.fixedModel();
				fixed.getEnv().set(GRB.IntParam.Presolve, 0);
				fixed.optimize();
				if (fixed.get(GRB.IntAttr.Status) != GRB.OPTIMAL) {
					throw new OptimizationException();
				}
				
				GRBVar[] variables		  	= fixed.getVars();
				double[] primalValues     	= fixed.get(GRB.DoubleAttr.X, variables);
				double[] dualValues			= fixed.get(GRB.DoubleAttr.RC, variables);
			    String[] variableNames		= fixed.get(GRB.StringAttr.VarName, variables);
				
			    for (i = 0; i < variables.length; i++) {
			    	Object variable = nameToObj.get(variableNames[i]);
			    	
			    	if (problem.getVarType(variable).isInt()) {
			    		int v = (int) Math.round(primalValues[i]);
			    		result.putPrimalValue(variable, v);
			    	} else {
			    		result.putPrimalValue(variable, primalValues[i]);
			    	}
			    	result.putDualValue(variable, dualValues[i]);
			    }
			    
			    GRBConstr[] constraints		= fixed.getConstrs();
			    double[] shadowPrices		= fixed.get(GRB.DoubleAttr.Pi, constraints);
			    String[] constraintNames	= fixed.get(GRB.StringAttr.ConstrName, constraints);
			    
			    for (i = 0; i < variables.length; i++) {
			    	Constraint con = nameToCon.get(constraintNames[i]);
			    	result.putDualValue(con.getName(), shadowPrices[i]);
			    }
			    
			    fixed.dispose();
			    
			    return result;
			} // end post-solve

			GRBVar[] variables 		= model.getVars();
			double[] primalValues	= model.get(GRB.DoubleAttr.X, variables);
			String[] variableNames 	= model.get(GRB.StringAttr.VarName, variables);
			
			for (i = 0; i < variables.length; i++) {
				Object variable = nameToObj.get(variableNames[i]);
				
				if (problem.getVarType(variable).isInt()) {
		    		int v = (int) Math.round(primalValues[i]);
		    		result.putPrimalValue(variable, v);
		    	} else {
		    		result.putPrimalValue(variable, primalValues[i]);
		    	}
			}
			
			model.dispose();

			return result;

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			throw new OptimizationException();
		}

	}

	protected void initWithParameters(GRBEnv env) throws GRBException {
		Object verbose = parameters.get(Solver.VERBOSE);
		Object timeout = parameters.get(Solver.TIMEOUT);
		Object mipgap = parameters.get(Solver.MIPGAP);

		if (verbose != null && verbose instanceof Number) {
			Number number = (Number) verbose;
			final int value = number.intValue();
			final int msgLevel;
			switch (value) {
			case 0:
				msgLevel = 0;
				break;
			default: // > 0
				msgLevel = 1;
			}
			env.set(GRB.IntParam.OutputFlag, msgLevel);
		}

		if (timeout != null && timeout instanceof Number) {
			Number number = (Number) timeout;
			double value = number.doubleValue();
			env.set(GRB.DoubleParam.TimeLimit, value);
		}
		
		if (mipgap != null && mipgap instanceof Number) {
			Number number = (Number) mipgap;
			double value = number.doubleValue();
			env.set(GRB.DoubleParam.MIPGap, value);
		}
		
		// -1=automatic, 0=primal simplex, 1=dual simplex, 2=barrier, 3=concurrent, 4=deterministic concurrent
		// standard MIP: dual simplex
		//env.set(GRB.IntParam.Method, 2);
	}

	/**
	 * The {@code Hook} for the {@code SolverGurobi}.
	 * 
	 * @author lukasiewycz
	 * 
	 */
	public interface Hook {

		/**
		 * This method is called once before the optimization and allows to
		 * change some internal settings.
		 * 
		 * @param env
		 *            the environment
		 * @param model
		 *            the model
		 * @param objToVar
		 *            the map from objects to gurobi variables
		 * @param varToObj
		 *            the map from gurobi variables to objects
		 * @param problem
		 *            the problem
		 */
		public void call(GRBEnv env, GRBModel model,
				Map<Object, GRBVar> objToVar, Map<GRBVar, Object> varToObj,
				Problem problem);
	}

	protected final Set<Hook> hooks = new HashSet<Hook>();

	/**
	 * Adds a hook.
	 * 
	 * @param hook
	 *            the hook to be added
	 */
	public void addHook(Hook hook) {
		hooks.add(hook);
	}

	/**
	 * Removes a hook
	 * 
	 * @param hook
	 *            the hook to be removed
	 */
	public void removeHook(Hook hook) {
		hooks.remove(hook);
	}

}