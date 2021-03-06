/**
 * 
 */
package net.sf.javailp;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

/**
 * @author fgenoese
 *
 */
public class ProblemGLPK extends Problem {
	
	private glp_prob model;
	private glp_smcp simplexParameters;
	private glp_iocp integerParameters;
	private Map<String, Integer> varNameToIndex = new LinkedHashMap<String, Integer>();
	private Map<String, Integer> conNameToIndex = new HashMap<String, Integer>();
	private Linear objectiveFunction;
	private int numberOfIntegerVariables 		= 0;
	private int numberOfVariables 				= 0;
	private int numberOfConstraints				= 0;
	
	/**
	 * Constructs a {@code ProblemGLPK}.
	 * 
	 */
	protected ProblemGLPK(glp_prob model, glp_smcp simplexParameters, glp_iocp integerParameters) {
		this.model = model;
		GLPK.glp_set_prob_name(model, "GLPK");
		this.simplexParameters = simplexParameters;
		this.integerParameters = integerParameters;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setObjective(net.sf.javailp.Linear, net.sf.javailp.OptType)
	 */
	public void setObjective(Linear objective, OptType optType) {
		if (optType == OptType.MAX) {
			GLPK.glp_set_obj_dir(model, GLPKConstants.GLP_MAX);
		} else {
			GLPK.glp_set_obj_dir(model, GLPKConstants.GLP_MIN);
		}

		GLPK.glp_set_obj_coef(model, 0, 0);

		final Map<String, Double> obj = new HashMap<String, Double>();
		for (Term term : objective) {
			String variableName = term.getVariableName();
			double coeff = term.getCoefficient().doubleValue();
			obj.put(variableName, coeff);
		}

		for (Entry<String, Integer> entry : varNameToIndex.entrySet()) {
			String variableName = entry.getKey();
			int variableIndex = entry.getValue();
			
			if (obj.containsKey(variableName)) {
				double coeff = obj.get(variableName);
				GLPK.glp_set_obj_coef(model, variableIndex, coeff);
			} else {
				GLPK.glp_set_obj_coef(model, variableIndex, 0);
			}
		}
		
		this.objectiveFunction = objective;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setStartingSolution(java.util.Map)
	 */
	public void setStartingSolution(Map<String, Number> startingSolution) {
		System.err.println("usage of a starting solution not yet implemented for GLPK");
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
		return numberOfVariables;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#addConstraint(java.lang.String, net.sf.javailp.Linear, net.sf.javailp.Operator, java.lang.Number)
	 */
	public void addConstraint(String name, Linear lhs, Operator operator, Number rhs) {
		if (conNameToIndex.containsKey(name)) {
			System.err.println("cannot add constraint '"+name+"': a constraint with this name already exists");
			return;
		}
		GLPK.glp_add_rows(model, 1);
		numberOfConstraints++;
		conNameToIndex.put(name, numberOfConstraints);
		
		int size = lhs.size();
		SWIGTYPE_p_int variableIndices = GLPK.new_intArray(size + 1);
		SWIGTYPE_p_double coefficients = GLPK.new_doubleArray(size + 1);

		int j = 1;
		for (Term term : lhs) {
			String variableName = term.getVariableName();
			int variableIndex = varNameToIndex.get(variableName);
			double coefficient = term.getCoefficient().doubleValue();
			GLPK.intArray_setitem(variableIndices, j, variableIndex);
			GLPK.doubleArray_setitem(coefficients, j, coefficient);
			j++;
		}

		final int op;
		switch (operator) {
			case LE:
				op = GLPKConstants.GLP_UP;
				break;
			case GE:
				op = GLPKConstants.GLP_LO;
				break;
			default:
				op = GLPKConstants.GLP_FX;
		}

		GLPK.glp_set_row_name(model, numberOfConstraints, name);
		GLPK.glp_set_mat_row(model, numberOfConstraints, size, variableIndices, coefficients);
		GLPK.glp_set_row_bnds(model, numberOfConstraints, op, rhs.doubleValue(), rhs.doubleValue());
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#addVariable(java.lang.String, net.sf.javailp.VarType, java.lang.Number, java.lang.Number)
	 */
	public void addVariable(String name, VarType type, Number lb, Number ub) {
		if (varNameToIndex.containsKey(name)) {
			System.err.println("cannot add variable '"+name+"': a variable with this name already exists");
			return;
		}
		GLPK.glp_add_cols(model, 1);
		numberOfVariables++;
		varNameToIndex.put(name, numberOfVariables);
		
		final int varType;
		switch (type) {
			case BOOL:
				varType = GLPKConstants.GLP_BV;
				numberOfIntegerVariables++;
				break;
			case INT:
				varType = GLPKConstants.GLP_IV;
				numberOfIntegerVariables++;
				break;
			default:
				varType = GLPKConstants.GLP_CV;
		}
		
		double lowerBound;
		double upperBound;
		final int boundType;

		if (type == VarType.BOOL) {
			lowerBound = 0.0;
			upperBound = 1.0;
			if (lb != null && lb.doubleValue() > 0.0) {
				lowerBound = 1.0;
				boundType = GLPKConstants.GLP_FX;
			} else if (ub != null && ub.doubleValue() < 1.0) {
				upperBound = 0.0;
				boundType = GLPKConstants.GLP_FX;
			} else {
				boundType = GLPKConstants.GLP_DB;
			}
		} else {
			if (lb != null) lowerBound = lb.doubleValue(); else lowerBound = 0.0;
			if (ub != null) upperBound = ub.doubleValue(); else upperBound = 0.0;
			if (lb != null && ub != null) {
				boundType = GLPKConstants.GLP_DB;
			} else if (lb != null) {
				boundType = GLPKConstants.GLP_LO;
			} else if (ub != null) {
				boundType = GLPKConstants.GLP_UP;
			} else {
				boundType = GLPKConstants.GLP_FR;
			}
		}

		GLPK.glp_set_col_name(model, numberOfVariables, name);
		GLPK.glp_set_col_kind(model, numberOfVariables, varType);
		GLPK.glp_set_col_bnds(model, numberOfVariables, boundType, lowerBound, upperBound);
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setVariableLowerBound(java.lang.String, java.lang.Number)
	 */
	public void setVariableLowerBound(String name, Number lb) {
		Integer variableIndex = varNameToIndex.get(name);
		if (variableIndex == null) {
			throw new IllegalArgumentException(
					"Variables must be added to the problem before a bound can be set. " +
					"(missing: "+name+")");
		}
		double lowerBound;
		double upperBound = GLPK.glp_get_col_ub(model, variableIndex);
		
		int boundType = GLPK.glp_get_col_type(model, variableIndex);
		if (lb != null) {
			if (boundType == GLPKConstants.GLP_UP) {
				boundType = GLPKConstants.GLP_FX;
			} else if (boundType == GLPKConstants.GLP_FR) {
				boundType = GLPKConstants.GLP_LO;
			}
			lowerBound = lb.doubleValue();
		} else {
			if (boundType == GLPKConstants.GLP_FX) {
				boundType = GLPKConstants.GLP_UP;
			} else if (boundType == GLPKConstants.GLP_LO) {
				boundType = GLPKConstants.GLP_FR;
			}
			lowerBound = 0.0;
		}
		
		GLPK.glp_set_col_bnds(model, variableIndex, boundType, lowerBound, upperBound);
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setVariableUpperBound(java.lang.String, java.lang.Number)
	 */
	public void setVariableUpperBound(String name, Number ub) {
		Integer variableIndex = varNameToIndex.get(name);
		if (variableIndex == null) {
			throw new IllegalArgumentException(
					"Variables must be added to the problem before a bound can be set. " +
					"(missing: "+name+")");
		}
		double lowerBound = GLPK.glp_get_col_lb(model, variableIndex);
		double upperBound;
		
		int boundType = GLPK.glp_get_col_type(model, variableIndex);
		if (ub != null) {
			if (boundType == GLPKConstants.GLP_LO) {
				boundType = GLPKConstants.GLP_FX;
			} else if (boundType == GLPKConstants.GLP_FR) {
				boundType = GLPKConstants.GLP_UP;
			}
			upperBound = ub.doubleValue();
		} else {
			if (boundType == GLPKConstants.GLP_FX) {
				boundType = GLPKConstants.GLP_LO;
			} else if (boundType == GLPKConstants.GLP_UP) {
				boundType = GLPKConstants.GLP_FR;
			}
			upperBound = 0.0;
		}
		
		GLPK.glp_set_col_bnds(model, variableIndex, boundType, lowerBound, upperBound);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.javailp.AbstractProblem#optimize(boolean, boolean)
	 */
	protected Result optimize(boolean postSolve, boolean activateLog) {
		int status;
		Result result = new ResultImpl(this.objectiveFunction);
		
		if (numberOfIntegerVariables == 0) {
			GLPK.glp_simplex(model, simplexParameters);
			status = GLPK.glp_get_status(model);
			if (status != GLPKConstants.GLP_OPT && status != GLPKConstants.GLP_FEAS) {
				throw new OptimizationException("No optimal or feasible solution found.");
			}
			
			for (Entry<String, Integer> entry : varNameToIndex.entrySet()) {
				String variableName = entry.getKey();
				int variableIndex = entry.getValue();
				
				double primalValue = GLPK.glp_get_col_prim(model, variableIndex);
				double dualValue = GLPK.glp_get_col_dual(model, variableIndex);

				if (GLPK.glp_get_col_kind(model, variableIndex) == GLPKConstants.GLP_IV) {
					int v = (int) Math.round(primalValue);
					result.putPrimalValue(variableName, v);
				} else {
					result.putPrimalValue(variableName, primalValue);
				}
				result.putDualValue(variableName, dualValue);
			}

			for (Entry<String, Integer> entry : conNameToIndex.entrySet()) {
				String constraintName = entry.getKey();
				int constraintIndex = entry.getValue();
				
				double primalValue = GLPK.glp_get_row_prim(model, constraintIndex);
				double dualValue = GLPK.glp_get_row_dual(model, constraintIndex);
				
				result.putPrimalValue(constraintName, primalValue);
				result.putDualValue(constraintName, dualValue);
			}
			
			return result;
		}
		
		integerParameters.setPresolve(GLPKConstants.GLP_ON);
		GLPK.glp_intopt(model, integerParameters);
		status = GLPK.glp_mip_status(model);
		if (status == GLPKConstants.GLP_OPT || status == GLPKConstants.GLP_FEAS) {
			// post-solve: LP relaxation with fixed integers
			if (postSolve) {
				for (int i = 1; i <= numberOfVariables; i++) {
					int kind = GLPK.glp_get_col_kind(model, i);
					if (kind == GLPKConstants.GLP_IV || kind == GLPKConstants.GLP_BV) {
						double x = GLPK.glp_mip_col_val(model, i);
						GLPK.glp_set_col_bnds(model, i, GLPKConstants.GLP_FX, x, x);
					}
				}
				GLPK.glp_simplex(model, simplexParameters);
				status = GLPK.glp_get_status(model);
				
				if (status != GLPKConstants.GLP_OPT && status != GLPKConstants.GLP_FEAS) {
					throw new OptimizationException("No optimal or feasible solution found.");
				}
				
				for (Entry<String, Integer> entry : varNameToIndex.entrySet()) {
					String variableName = entry.getKey();
					int variableIndex = entry.getValue();
					
					double primalValue = GLPK.glp_mip_col_val(model, variableIndex);
					double dualValue = GLPK.glp_get_col_dual(model, variableIndex);

					if (GLPK.glp_get_col_kind(model, variableIndex) == GLPKConstants.GLP_IV) {
						int v = (int) Math.round(primalValue);
						result.putPrimalValue(variableName, v);
					} else {
						result.putPrimalValue(variableName, primalValue);
					}
					result.putDualValue(variableName, dualValue);
				}

				for (Entry<String, Integer> entry : conNameToIndex.entrySet()) {
					String constraintName = entry.getKey();
					int constraintIndex = entry.getValue();
					
					double primalValue = GLPK.glp_mip_row_val(model, constraintIndex);
					double dualValue = GLPK.glp_get_row_dual(model, constraintIndex);
					
					result.putPrimalValue(constraintName, primalValue);
					result.putDualValue(constraintName, dualValue);
				}
				
				return result;
			} // end post-solve
		} else {
			throw new OptimizationException("No optimal or feasible solution found.");
		}
		
		for (Entry<String, Integer> entry : varNameToIndex.entrySet()) {
			String variableName = entry.getKey();
			int variableIndex = entry.getValue();
			
			double primalValue = GLPK.glp_mip_col_val(model, variableIndex);
			
			if (GLPK.glp_get_col_kind(model, variableIndex) == GLPKConstants.GLP_IV || GLPK.glp_get_col_kind(model, variableIndex) == GLPKConstants.GLP_BV) {
				int v = (int) Math.round(primalValue);
				result.putPrimalValue(variableName, v);
			} else {
				result.putPrimalValue(variableName, primalValue);
			}
		}

		for (Entry<String, Integer> entry : conNameToIndex.entrySet()) {
			String constraintName = entry.getKey();
			int constraintIndex = entry.getValue();
			
			double primalValue = GLPK.glp_mip_row_val(model, constraintIndex);
			
			result.putPrimalValue(constraintName, primalValue);
		}

		return result;
	}

}
