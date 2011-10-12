/**
 * 
 */
package net.sf.javailp;

import java.util.HashMap;
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
 * @author fg
 *
 */
public class ProblemGLPK extends Problem {
	
	private glp_prob lp;
	private glp_smcp simplexParameters;
	private glp_iocp integerParameters;
	private Map<String, Integer> nameToIndex 	= new HashMap<String, Integer>();
	private Map<String, Integer> conNameToIndex = new HashMap<String, Integer>();
	private Linear objectiveFunction;
	private int numberOfIntegerVariables 		= 0;
	private int numberOfVariables 				= 0;
	private int numberOfConstraints				= 0;
	
	protected ProblemGLPK(glp_smcp simplexParameters, glp_iocp integerParameters) {
		lp = GLPK.glp_create_prob();
		GLPK.glp_set_prob_name(lp, "GLPK");
		this.simplexParameters = simplexParameters;
		this.integerParameters = integerParameters;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#setObjective(net.sf.javailp.Linear, net.sf.javailp.OptType)
	 */
	public void setObjective(Linear objective, OptType optType) {
		if (optType == OptType.MAX) {
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
		} else {
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
		}

		GLPK.glp_set_obj_coef(lp, 0, 0);

		final Map<String, Double> obj = new HashMap<String, Double>();
		for (Term term : objective) {
			String variableName = term.getVariableName();
			double coeff = term.getCoefficient().doubleValue();
			obj.put(variableName, coeff);
		}

		for (Entry<String, Integer> entry : nameToIndex.entrySet()) {
			String variableName = entry.getKey();
			int variableIndex = entry.getValue();
			
			if (obj.containsKey(variableName)) {
				double coeff = obj.get(variableName);
				GLPK.glp_set_obj_coef(lp, variableIndex, coeff);
			} else {
				GLPK.glp_set_obj_coef(lp, variableIndex, 0);
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
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#getVariablesCount()
	 */
	public int getVariablesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#addConstraint(java.lang.String, net.sf.javailp.Linear, net.sf.javailp.Operator, java.lang.Number)
	 */
	public void addConstraint(String name, Linear lhs, Operator operator, Number rhs) {
		GLPK.glp_add_rows(lp, 1);
		numberOfConstraints++;
		conNameToIndex.put(name, numberOfConstraints);
		
		int size = lhs.size();
		SWIGTYPE_p_int variableIndices = GLPK.new_intArray(size + 1);
		SWIGTYPE_p_double coefficients = GLPK.new_doubleArray(size + 1);

		int j = 1;
		for (Term term : lhs) {
			String variableName = term.getVariableName();
			int variableIndex = nameToIndex.get(variableName);
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

		GLPK.glp_set_row_name(lp, numberOfConstraints, name);
		GLPK.glp_set_mat_row(lp, numberOfConstraints, size, variableIndices, coefficients);
		GLPK.glp_set_row_bnds(lp, numberOfConstraints, op, rhs.doubleValue(), rhs.doubleValue());
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#addVariable(java.lang.String, net.sf.javailp.VarType, java.lang.Number, java.lang.Number)
	 */
	public void addVariable(String name, VarType type, Number lb, Number ub) {
		GLPK.glp_add_cols(lp, 1);
		numberOfVariables++;
		nameToIndex.put(name, numberOfVariables);
		
		final int varType;
		switch (type) {
		case BOOL:
		case INT:
			varType = GLPKConstants.GLP_IV;
			numberOfIntegerVariables++;
			break;
		default:
			varType = GLPKConstants.GLP_CV;
		}
		
		double lowerBound;
		double upperBound;

		if (type == VarType.BOOL) {
			lowerBound = 0.0;
			upperBound = 1.0;
			if (lb != null && lb.doubleValue() > 0) lowerBound = 1.0;
			if (ub != null && ub.doubleValue() < 1) upperBound = 0.0;
		} else {
			if (lb != null) lowerBound = lb.doubleValue(); else lowerBound = 0.0;
			if (ub != null) upperBound = ub.doubleValue(); else upperBound = 0.0;
		}

		final int boundType;
		if (lb != null && ub != null) {
			boundType = GLPKConstants.GLP_DB;
		} else if (lb != null) {
			boundType = GLPKConstants.GLP_LO;
		} else if (ub != null) {
			boundType = GLPKConstants.GLP_UP;
		} else {
			boundType = GLPKConstants.GLP_FR;
		}
		
		GLPK.glp_set_col_name(lp, numberOfVariables, name);
		GLPK.glp_set_col_kind(lp, numberOfVariables, varType);
		GLPK.glp_set_col_bnds(lp, numberOfVariables, boundType, lowerBound, upperBound);
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.AbstractProblem#optimize(boolean)
	 */
	protected Result optimize(boolean postSolve) {
		int status;
		if (numberOfIntegerVariables == 0) {
			status = GLPK.glp_simplex(lp, simplexParameters);
		} else {
			integerParameters.setPresolve(GLPKConstants.GLP_ON);
			status = GLPK.glp_intopt(lp, integerParameters);
			if (status == GLPKConstants.GLP_OPT || status == GLPKConstants.GLP_FEAS) {
				// post-solve: LP relaxation with fixed integers
				if (postSolve) {
					boolean mip = false;
					for (int i = 1; i<= GLPK.glp_get_num_cols(lp); i++) {
						int kind = GLPK.glp_get_col_kind(lp, i);
						if (kind == GLPKConstants.GLP_IV || kind == GLPKConstants.GLP_BV) {
							double x = GLPK.glp_mip_col_val(lp, i);
							GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_FX, x, x);
							mip = true;
						}
					}
					if (mip) status = GLPK.glp_simplex(lp, simplexParameters);
				} // end post-solve
			}
		}
		
		if (status == GLPKConstants.GLP_OPT || status == GLPKConstants.GLP_FEAS) {
			throw new OptimizationException("No optimal or feasible solution found.");
		}
		
		Result result = new ResultImpl(this.objectiveFunction);
		
		for (Entry<String, Integer> entry : nameToIndex.entrySet()) {
			String variableName = entry.getKey();
			int variableIndex = entry.getValue();
			
			double primalValue = GLPK.glp_get_col_prim(lp, variableIndex);
			double dualValue = GLPK.glp_get_col_dual(lp, variableIndex);

			if (GLPK.glp_get_col_kind(lp, variableIndex) == GLPKConstants.GLP_IV) {
				int v = (int) Math.round(primalValue);
				result.putPrimalValue(variableName, v);
			} else {
				result.putPrimalValue(variableName, primalValue);
			}
			result.putDualValue(variableName, dualValue);
		}

		for (Entry<String, Integer> entry : conNameToIndex.entrySet()) {
			String variableName = entry.getKey();
			int variableIndex = entry.getValue();
			
			double primalValue = GLPK.glp_get_row_prim(lp, variableIndex);
			double dualValue = GLPK.glp_get_row_dual(lp, variableIndex);
			
			result.putPrimalValue(variableName, primalValue);
			result.putDualValue(variableName, dualValue);
		}
		
		GLPK.glp_delete_prob(lp);
		return result;
	}

}
