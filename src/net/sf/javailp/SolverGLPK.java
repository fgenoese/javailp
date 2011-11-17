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

import java.util.HashMap;
import java.util.Map;

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
	
	private Map<String, glp_prob> models = new HashMap<String, glp_prob>();
	private Map<String, Problem> problems = new HashMap<String, Problem>();
	
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
	 * @see net.sf.javailp.Solver#createProblem(String)
	 */
	public Problem createProblem(String identifier) {
		if (this.models.containsKey(identifier)) {
			throw new OptimizationException("A problem with this identifier already exists.");
		}
		glp_prob model = GLPK.glp_create_prob();
		this.models.put(identifier, model);
		glp_smcp simplexParameters = new glp_smcp();
		glp_iocp integerParameters = new glp_iocp();
		GLPK.glp_init_smcp(simplexParameters);
		GLPK.glp_init_iocp(integerParameters);
		this.updateParameters(simplexParameters, integerParameters);
		Problem problem = new ProblemGLPK(model, simplexParameters, integerParameters);
		this.problems.put(identifier, problem);
		return problem;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#getProblem(String)
	 */
	public Problem getProblem(String identifier) {
		if (!this.problems.containsKey(identifier)) {
			return this.createProblem(identifier);
		}
		return this.problems.get(identifier);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#deleteProblem(String)
	 */
	public void deleteProblem(String identifier) {
		if (this.problems.containsKey(identifier)) {
			this.models.remove(identifier);
			this.problems.remove(identifier);
		}
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
		
		Result result = problem.optimize(postSolve);
				
		return result;
	}
	
	protected void updateParameters(glp_smcp simplexParameters, glp_iocp integerParameters) {
		Number timeout = this.parameters.get(Solver.TIMEOUT);
		Number verbose = this.parameters.get(Solver.VERBOSE);
		Number mipgap = this.parameters.get(Solver.MIPGAP);

		if (timeout != null) {
			int value = timeout.intValue() * 1000;
			integerParameters.setTm_lim(value);
			simplexParameters.setTm_lim(value);
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
			simplexParameters.setMsg_lev(msgLevel);
			integerParameters.setMsg_lev(msgLevel);
		}
		
		if (mipgap != null) {
			double value = mipgap.doubleValue();
			integerParameters.setMip_gap(value);
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