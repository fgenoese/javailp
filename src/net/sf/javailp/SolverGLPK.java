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
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

/**
 * The {@code SolverGLPK} is the {@code Solver} GLPK.
 * 
 * @author lukasiewycz @author fgenoese
 * 
 */
public class SolverGLPK extends AbstractSolver {
	
	private glp_prob model;
	private Problem problem;
	private glp_smcp simplexParameters;
	private glp_iocp integerParameters;
	
	/**
	 * Constructs a {@code SolverGLPK}.
	 * 
	 */
	public SolverGLPK() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#createProblem()
	 */
	public Problem createProblem() {
		if (this.problem != null) {
			this.deleteProblem();
		}
		this.model = GLPK.glp_create_prob();
		this.simplexParameters = new glp_smcp();
		this.integerParameters = new glp_iocp();
		GLPK.glp_init_smcp(this.simplexParameters);
		GLPK.glp_init_iocp(this.integerParameters);
		this.updateParameters();
		this.problem = new ProblemGLPK(this.model, this.simplexParameters, this.integerParameters);
		return this.problem;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#getProblem()
	 */
	public Problem getProblem() {
		if (this.problem == null) {
			return this.createProblem();
		}
		return this.problem;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#deleteProblem()
	 */
	public void deleteProblem() {
		this.problem = null;
		GLPK.glp_delete_prob(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem)
	 */
	public Result solve(Problem problem) {
		boolean postSolve = false;
		Number postsolve = this.parameters.get(Solver.POSTSOLVE);
		if (postsolve != null && postsolve.intValue() != 0 ) postSolve = true;
		
		Result result = this.problem.optimize(postSolve);
				
		return result;
	}
	
	protected void updateParameters() {
		Number timeout = this.parameters.get(Solver.TIMEOUT);
		Number verbose = this.parameters.get(Solver.VERBOSE);
		Number mipgap = this.parameters.get(Solver.MIPGAP);

		if (timeout != null) {
			int value = timeout.intValue() * 1000;
			this.integerParameters.setTm_lim(value);
			this.simplexParameters.setTm_lim(value);
		}

		if (verbose != null) {
			int value = verbose.intValue();
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
			default:
				msgLevel = GLPKConstants.GLP_MSG_ALL;
			}
			this.simplexParameters.setMsg_lev(msgLevel);
			this.integerParameters.setMsg_lev(msgLevel);
		}
		
		if (mipgap != null) {
			double value = mipgap.doubleValue();
			this.integerParameters.setMip_gap(value);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.javailp.Solver#getInternalValueForID(int)
	 */
	public int getInternalValueForID(int ID) {
		switch (ID) {
			default: return -999;
		}
	}
	
}