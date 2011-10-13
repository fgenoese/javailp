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
 * The class {@code Problem} represents a linear problem consisting of multiple
 * constraints and up to one objective function.
 * 
 * @author lukasiewycz @author fgenoese
 * 
 */
public interface ProblemInterface {

	/**
	 * Sets the objective function.
	 * 
	 * @param objective
	 *            the objective function
	 * @param optType
	 *            the optimization type
	 */
	public void setObjective(Linear objective, OptType optType);

	/**
	 * Sets the starting solution for a set of variables.
	 * 
	 * @param startingSolution
	 * 			  the starting solution for a set of variables
	 */
	public void setStartingSolution(Map<String, Number> startingSolution);
	
	/**
	 * Returns the number of objectives.
	 * 
	 * @return the number of objectives
	 */
	public int getConstraintsCount();

	/**
	 * Returns the number of variables.
	 * 
	 * @return the number of variables
	 */
	public int getVariablesCount();

	/**
	 * Adds a constraint to the map of constraints.
	 * 
	 * @param name
	 *            the name of the constraint
	 * @param lhs
	 *            the left-hand-side linear expression
	 * @param operator
	 *            the operator
	 * @param rhs
	 *            the right-hand-side number
	 */
	public void addConstraint(String name, Linear lhs, Operator operator, Number rhs);

	/**
	 * Adds a variable to the map of constraints.
	 * 
	 * @param name
	 *            the name of the constraint
	 * @param type
	 *            the variable type
	 * @param lb
	 *            the lower bound
	 * @param ub
	 *            the upper bound
	 */
	public void addVariable(String name, VarType type, Number lb, Number ub);
	
	/**
	 * Adds variable to the map of variables.
	 * 
	 * @param name
	 *            the name of the variable
	 * @param type
	 *            the variable type
	 */
	public void addVariable(String name, VarType type);
	
	/**
	 * Sets the lower bound of a variable.
	 * 
	 * @param name
	 *            the name of the variable
	 * @param lb
	 *            the lower bound
	 */
	public void setVariableLowerBound(String name, Number lb);
	
	/**
	 * Sets the upper bound of a variable.
	 * 
	 * @param name
	 *            the name of the variable
	 * @param ub
	 *            the upper bound
	 */
	public void setVariableUpperBound(String name, Number ub);
		
}
