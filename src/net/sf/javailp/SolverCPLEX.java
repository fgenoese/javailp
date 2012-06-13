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

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.DoubleParam;
import ilog.cplex.IloCplex.IntParam;

/**
 * The {@code SolverCPLEX} is the {@code Solver} CPLEX.
 * 
 * @author lukasiewycz
 * 
 */
public class SolverCPLEX extends AbstractSolver {
	
	private Map<String, IloCplex> models = new HashMap<String, IloCplex>();
	private Map<String, Problem> problems = new HashMap<String, Problem>();
	
	/**
	 * Constructs a {@code SolverCPLEX}.
	 * 
	 */
	public SolverCPLEX() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#createProblem(String)
	 */
	public synchronized Problem createProblem(String identifier) {
		try {
			if (this.models.containsKey(identifier)) {
				throw new OptimizationException("A problem with this identifier already exists.");
			}
			IloCplex model = new IloCplex();
			updateParameters(model);
			this.models.put(identifier, model);
			Problem problem = new ProblemCPLEX(model);
			this.problems.put(identifier, problem);
			return problem;
		} catch (IloException e) {
			throw new OptimizationException(e.getMessage());
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
			IloCplex model = this.models.get(identifier);
			model.end();
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
		boolean postSolve = false;
		Number postsolve = this.parameters.get(Solver.POSTSOLVE);
		if (postsolve != null && postsolve.intValue() != 0 ) postSolve = true;
		
		Result result = problem.optimize(postSolve, activateLog);
		
		return result;
	}

	protected void updateParameters(IloCplex model) throws IloException {
		Number timeout = parameters.get(Solver.TIMEOUT);
		Number verbose = parameters.get(Solver.VERBOSE);
		Number mipgap = parameters.get(Solver.MIPGAP);
		Number method = parameters.get(Solver.METHOD);

		if (timeout != null) {
			double value = timeout.doubleValue();
			model.setParam(DoubleParam.TiLim, value);
		}
		
		if (verbose != null) {
			int value = verbose.intValue();
			if (value == 0) {
				model.setOut(null);
			}
		}

		if (mipgap != null) {
			double value = mipgap.doubleValue();
			model.setParam(DoubleParam.EpGap, value);
		}
		
		// 0=automatic, 1=primal simplex, 2=dual simplex, 4=barrier, 6=concurrent
		if (method != null) {
			int value = method.intValue();
			model.setParam(IntParam.RootAlg, value);
			//model.setParam(IntParam.NodeAlg, value);
		}

		/*System.out.println("number of threads: "+cplex.getParam(IntParam.Threads));
		System.out.println("parallel mode: "+cplex.getParam(IntParam.ParallelMode));
		cplex.setParam(IntParam.Threads, 8);
		System.out.println("number of threads: "+cplex.getParam(IntParam.Threads));*/
	}
	
	/* (non-Javadoc)
	 * @see net.sf.javailp.Solver#getInternalValueForID(int)
	 */
	public int getInternalValueForID(int ID) {
		switch (ID) {
			case Solver.METHOD_ID_AUTOMATIC: return IloCplex.Algorithm.Auto;
			case Solver.METHOD_ID_PRIMAL_SIMPLEX: return IloCplex.Algorithm.Primal;
			case Solver.METHOD_ID_DUAL_SIMPLEX: return IloCplex.Algorithm.Dual;
			case Solver.METHOD_ID_BARRIER: return IloCplex.Algorithm.Barrier;
			case Solver.METHOD_ID_CONCURRENT: return IloCplex.Algorithm.Concurrent;
			default: throw new IllegalArgumentException("invalid method ID");
		}
	}
	
}
