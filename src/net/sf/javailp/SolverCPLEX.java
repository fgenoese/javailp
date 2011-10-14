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
	
	private IloCplex model;
	private Problem problem;
	
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
	 * @see net.sf.javailp.Solver#createProblem()
	 */
	public Problem createProblem() {
		try {
			if (this.problem != null) {
				this.deleteProblem();
			}
			this.model = new IloCplex();
			updateParameters();
			this.problem = new ProblemCPLEX(this.model);
			return this.problem;
		} catch (IloException e) {
			throw new OptimizationException(e.getMessage());
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
		this.model.end();
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

	protected void updateParameters() throws IloException {
		Number timeout = parameters.get(Solver.TIMEOUT);
		Number verbose = parameters.get(Solver.VERBOSE);
		Number mipgap = parameters.get(Solver.MIPGAP);
		Number method = parameters.get(Solver.METHOD);

		if (timeout != null) {
			double value = timeout.doubleValue();
			this.model.setParam(DoubleParam.TiLim, value);
		}
		
		if (verbose != null) {
			int value = verbose.intValue();
			if (value == 0) {
				this.model.setOut(null);
			}
		}

		if (mipgap != null) {
			double value = mipgap.doubleValue();
			this.model.setParam(DoubleParam.EpGap, value);
		}
		
		// 0=automatic, 1=primal simplex, 2=dual simplex, 4=barrier, 6=concurrent
		if (method != null) {
			int value = method.intValue();
			this.model.setParam(IntParam.RootAlg, value);
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
			case Solver.METHOD_ID_AUTOMATIC: return 0;
			case Solver.METHOD_ID_PRIMAL_SIMPLEX: return 1;
			case Solver.METHOD_ID_DUAL_SIMPLEX: return 2;
			case Solver.METHOD_ID_BARRIER: return 4;
			case Solver.METHOD_ID_CONCURRENT: return 6;
			default: throw new IllegalArgumentException("invalid ID");
		}
	}
	
}
