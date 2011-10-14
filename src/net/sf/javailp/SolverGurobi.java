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
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;

/**
 * The {@code SolverGurobi} is the {@code Solver} Gurobi.
 * 
 * @author fabiogenoese @author lukasiewycz
 * 
 */
public class SolverGurobi extends AbstractSolver {
	
	private GRBEnv env;
	private GRBModel model;
	private Problem problem;
	
	/**
	 * Constructs a {@code SolverGurobi}.
	 * 
	 */
	public SolverGurobi() {
		super();
		try {
			this.env = new GRBEnv("gurobi.log");
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#createProblem()
	 */
	public Problem createProblem() {
		try {
			if (this.problem != null) {
				this.deleteProblem();
			}
			updateParameters();
			this.model = new GRBModel(this.env);
			this.problem = new ProblemGurobi(this.env, this.model);
			return this.problem;
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
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
		this.model.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem)
	 */
	public Result solve(Problem problem) {
		
		if (this.env == null) {
			throw new OptimizationException("GRBEnv must be initialized before any problem can be solved.");
		}

		boolean postSolve = false;
		Number postsolve = this.parameters.get(Solver.POSTSOLVE);
		if (postsolve != null && postsolve.intValue() != 0 ) postSolve = true;
		
		Result result = this.problem.optimize(postSolve);
		
		return result;
	}

	protected void updateParameters() throws GRBException {
		Number timeout = this.parameters.get(Solver.TIMEOUT);
		Number verbose = this.parameters.get(Solver.VERBOSE);
		Number mipgap = this.parameters.get(Solver.MIPGAP);
		Number method = this.parameters.get(Solver.METHOD);

		if (timeout != null) {
			double value = timeout.doubleValue();
			this.env.set(GRB.DoubleParam.TimeLimit, value);
		}
		
		if (verbose != null) {
			int value = verbose.intValue();
			final int msgLevel;
			switch (value) {
			case 0:
				msgLevel = 0;
				break;
			default:
				msgLevel = 1;
			}
			this.env.set(GRB.IntParam.OutputFlag, msgLevel);
		}

		if (mipgap != null) {
			double value = mipgap.doubleValue();
			this.env.set(GRB.DoubleParam.MIPGap, value);
		}
		
		// standard for MIP root node relaxtion: dual simplex
		if (method != null) {
			int value = method.intValue();
			this.env.set(GRB.IntParam.Method, value);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.Solver#getInternalValueForID(int)
	 */
	public int getInternalValueForID(int ID) {
		switch (ID) {
			case Solver.METHOD_ID_AUTOMATIC: return -1;
			case Solver.METHOD_ID_PRIMAL_SIMPLEX: return 0;
			case Solver.METHOD_ID_DUAL_SIMPLEX: return 1;
			case Solver.METHOD_ID_BARRIER: return 2;
			case Solver.METHOD_ID_CONCURRENT: return 3;
			default: throw new IllegalArgumentException("invalid ID");
		}
	}
	
}