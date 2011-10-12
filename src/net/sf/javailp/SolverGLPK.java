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

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_smcp;

/**
 * The {@code SolverGLPK} is the {@code Solver} GLPK.
 * 
 * @author lukasiewycz @author fgenoese
 * 
 */
public class SolverGLPK extends AbstractSolver {
	
	private Problem problem;
	private glp_smcp simplexParameters;
	private glp_iocp integerParameters;
	
	/**
	 * Constructs a {@code SolverGLPK}.
	 * 
	 */
	public SolverGLPK() {
		super();
		simplexParameters = new glp_smcp();
		integerParameters = new glp_iocp();
		GLPK.glp_init_smcp(simplexParameters);
		GLPK.glp_init_iocp(integerParameters);
		this.problem = new ProblemGLPK(simplexParameters, integerParameters);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#getProblem()
	 */
	public Problem getProblem() {
		if (this.problem == null) {
			this.problem = new ProblemGLPK(simplexParameters, integerParameters);
		}
		return this.problem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem)
	 */
	public Result solve(Problem problem) {
		updateParameters();
		
		boolean postSolve = false;
		Object postsolve = this.parameters.get(Solver.POSTSOLVE);
		if (postsolve != null && ((Number)postsolve).intValue() != 0 ) postSolve = true;
		
		Result result = this.problem.optimize(postSolve);
		
		this.problem = null;
		
		return result;
	}
	
	protected void updateParameters() {
		Object timeout = this.parameters.get(Solver.TIMEOUT);
		Object verbose = this.parameters.get(Solver.VERBOSE);
		Object mipgap = this.parameters.get(Solver.MIPGAP);

		if (timeout != null) {
			int v = ((Number) timeout).intValue() * 1000;
			this.integerParameters.setTm_lim(v);
			this.simplexParameters.setTm_lim(v);
		}

		if (verbose != null && verbose instanceof Number) {
			Number number = (Number) verbose;
			int value = number.intValue();
			final int msgLevel;

			switch (value) {
			case 0:
				msgLevel = GLPKConstants.GLP_MSG_OFF;
				break;
			case 1:
				msgLevel = GLPKConstants.GLP_MSG_ERR;
				break;
			case 2:
				msgLevel = GLPKConstants.GLP_MSG_ON;
				break;
			default: // >= 2
				msgLevel = GLPKConstants.GLP_MSG_ALL;
			}
			this.simplexParameters.setMsg_lev(msgLevel);
			this.integerParameters.setMsg_lev(msgLevel);
		}
		
		if (mipgap != null && mipgap instanceof Number) {
			Number number = (Number) mipgap;
			double value = number.doubleValue();
			this.integerParameters.setMip_gap(value);
		}
	}
}