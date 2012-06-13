package net.sf.javailp;

public abstract class Problem implements ProblemInterface {

	/**
	 * Starts the optimization and returns its solution.
	 * @param postSolve
	 *            performs an LP relaxation with fixed integers if true
	 * @param activateLog
	 * 			  activates logging (.lp) if true
	 * @return the solution
	 */
	protected abstract Result optimize(boolean postSolve, boolean activateLog);
	
	/* (non-Javadoc)
	 * @see net.sf.javailp.ProblemInterface#addVariable(java.lang.String, net.sf.javailp.VarType)
	 */
	public void addVariable(String name, VarType type) {
		addVariable(name, type, null, null);
	}

}
