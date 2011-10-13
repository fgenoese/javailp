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

/**
 * The {@code SolverCPLEX} is the {@code Solver} CPLEX.
 * 
 * @author lukasiewycz
 * 
 */
public class SolverCPLEX extends AbstractSolver {
	
	private IloCplex cplex;
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
			this.cplex = new IloCplex();
			updateParameters(this.cplex);
			this.problem = new ProblemCPLEX(this.cplex);
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
		this.cplex.end();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem)
	 */
	public Result solve(Problem problem) {
		boolean postSolve = false;
		Object postsolve = parameters.get(Solver.POSTSOLVE);
		if (postsolve != null && ((Number)postsolve).intValue() != 0 ) postSolve = true;
		
		Result result = problem.optimize(postSolve);
		
		return result;
	}

	protected void updateParameters(IloCplex cplex) throws IloException {
		Object timeout = parameters.get(Solver.TIMEOUT);
		Object verbose = parameters.get(Solver.VERBOSE);
		Object mipgap = parameters.get(Solver.MIPGAP);

		if (timeout != null && timeout instanceof Number) {
			Number number = (Number) timeout;
			double value = number.doubleValue();
			cplex.setParam(DoubleParam.TiLim, value);
		}
		
		if (verbose != null && verbose instanceof Number) {
			Number number = (Number) verbose;
			int value = number.intValue();

			if (value == 0) {
				cplex.setOut(null);
			}
		}

		if (mipgap != null && mipgap instanceof Number) {
			Number number = (Number) mipgap;
			double value = number.doubleValue();
			cplex.setParam(DoubleParam.EpGap, value);
		}
		
		/*System.out.println("number of threads: "+cplex.getParam(IntParam.Threads));
		System.out.println("parallel mode: "+cplex.getParam(IntParam.ParallelMode));
		cplex.setParam(IntParam.Threads, 8);
		System.out.println("number of threads: "+cplex.getParam(IntParam.Threads));*/
	}

}
