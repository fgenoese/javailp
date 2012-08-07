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
import java.util.Set;

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
	private Map<String, GRBModel> models = new HashMap<String, GRBModel>();
	private Map<String, Problem> problems = new HashMap<String, Problem>();
	
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
	 * @see net.sf.javailp.Solver#createProblem(String)
	 */
	public synchronized Problem createProblem(String identifier) {
		try {
			updateParameters();
			if (this.models.containsKey(identifier)) {
				throw new OptimizationException("A problem with this identifier already exists.");
			}
			GRBModel model = new GRBModel(this.env);
			this.models.put(identifier, model);
			Problem problem = new ProblemGurobi(this.env, model, identifier);
			this.problems.put(identifier, problem);
			return problem;
		} catch (GRBException e) {
			throw new OptimizationException("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#getProblem(String)
	 */
	public synchronized Problem getProblem(String identifier) {
		if (!this.problems.containsKey(identifier)) {
			return this.createProblem(identifier);
		}
		return this.problems.get(identifier);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#getProblemNames()
	 */
	public synchronized Set<String> getProblemIdentifiers() {
		return this.problems.keySet();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#deleteProblem(String)
	 */
	public synchronized void deleteProblem(String identifier) {
		if (this.problems.containsKey(identifier)) {
			GRBModel model = this.models.get(identifier);
			model.dispose();
			this.problems.remove(identifier);
			this.models.remove(identifier);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem)
	 */
	public Result solve(Problem problem) {
		return this.solve(problem, false);
	}
	
	public Result solve(Problem problem, boolean activateLog) {
		
		if (this.env == null) {
			throw new OptimizationException("GRBEnv must be initialized before any problem can be solved.");
		}

		boolean postSolve = false;
		Number postsolve = this.parameters.get(Solver.POSTSOLVE);
		if (postsolve != null && postsolve.intValue() != 0 ) postSolve = true;
		
		Result result = problem.optimize(postSolve, activateLog);
		
		return result;
	}

	protected void updateParameters() throws GRBException {
		Number timeout = this.parameters.get(Solver.TIMEOUT);
		Number verbose = this.parameters.get(Solver.VERBOSE);
		Number mipgap = this.parameters.get(Solver.MIPGAP);
		Number method = this.parameters.get(Solver.METHOD);
		Number threads = this.parameters.get(Solver.THREADS);

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
			//this.env.set(GRB.IntParam.NodeMethod, value);
		}
		
		if (threads != null) {
			int value = threads.intValue();
			value = Math.max(0, value);
			this.env.set(GRB.IntParam.Threads, value);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.javailp.Solver#getInternalValueForID(int)
	 */
	public int getInternalValueForID(int ID) {
		switch (ID) {
			case Solver.METHOD_ID_AUTOMATIC: return GRB.METHOD_AUTO;
			case Solver.METHOD_ID_PRIMAL_SIMPLEX: return GRB.METHOD_PRIMAL;
			case Solver.METHOD_ID_DUAL_SIMPLEX: return GRB.METHOD_DUAL;
			case Solver.METHOD_ID_BARRIER: return GRB.METHOD_BARRIER;
			case Solver.METHOD_ID_CONCURRENT: return GRB.METHOD_CONCURRENT;
			default: throw new IllegalArgumentException("invalid method ID");
		}
	}
	
}