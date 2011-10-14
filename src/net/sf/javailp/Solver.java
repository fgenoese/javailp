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

import java.util.Map;

/**
 * The {@code Solver}.
 * 
 * @author lukasiewycz @author fgenoese
 * 
 */
public interface Solver {

	/**
	 * Identifier for the timeout value.
	 */
	public static final int TIMEOUT = 0;

	/**
	 * Identifier for the verbose value.
	 */
	public static final int VERBOSE = 1;
	
	/**
	 * Identifier for the post-solve value.
	 */
	public static final int POSTSOLVE = 2;
	
	/**
	 * Identifier for the post-solve value.
	 */
	public static final int MIPGAP = 3;
	
	/**
	 * Identifier for the method value.
	 */
	public static final int METHOD = 4;
	
	public static final int METHOD_ID_AUTOMATIC 		= 1000;
	public static final int METHOD_ID_PRIMAL_SIMPLEX 	= 1001;
	public static final int METHOD_ID_DUAL_SIMPLEX		= 1002;
	public static final int METHOD_ID_BARRIER			= 1003;
	public static final int METHOD_ID_CONCURRENT		= 1004;

	/**
	 * Sets a parameter.
	 * 
	 * @param identifier
	 *            the identifier
	 * @param value
	 *            the value
	 */
	public void setParameter(Integer identifier, Number value);

	/**
	 * Returns all set parameters.
	 * 
	 * @return the map of the parameters
	 */
	public Map<Integer, Number> getParameters();
	
	/**
	 * Returns a new optimization problem for this solver. If existent, the old one is deleted.
	 * 
	 * @return the optimization problem
	 */
	public Problem createProblem();
	
	/**
	 * Returns the current opimization problem for this solver. If not existent, a new one is created.
	 * 
	 * @return the optimization problem
	 */
	public Problem getProblem();
	
	/**
	 * Deletes the opimization problem for this solver.
	 */
	public void deleteProblem();

	/**
	 * Solves the optimization problem. Returns {@code null} if there exists no
	 * feasible solution for the problem.
	 * 
	 * @param problem
	 *            the optimization problem
	 * @return the result
	 */
	public Result solve(Problem problem);
	
	/**
	 * Returns the solver-specific parameter value for an ID.
	 * 
	 * @return the parameter value
	 */
	public int getInternalValueForID(int ID);

}
