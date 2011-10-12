package net.sf.javailp;

public abstract class Problem implements ProblemInterface {

	/**
	 * Starts the optimization and returns its solution.
	 * @param postSolve
	 *            performs an LP relaxation with fixed integers if true
	 * @return the solution
	 */
	protected abstract Result optimize(boolean postSolve);

}
