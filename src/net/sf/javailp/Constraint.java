/**
 * Java ILP is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Opt4J is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Opt4J. If not, see http://www.gnu.org/licenses/.
 */
package net.sf.javailp;

/**
 * The class {@code Constraint} represent a linear constraint.
 * 
 * @author lukasiewycz
 * 
 */
public class Constraint {

	protected final Linear lhs;
	protected final Operator operator;
	protected final Number rhs;

	/**
	 * Constructs a {@code Constraint}.
	 * 
	 * @param lhs
	 * @param operator
	 * @param rhs
	 */
	public Constraint(Linear lhs, Operator operator, Number rhs) {
		this.lhs = lhs;
		this.operator = operator;
		this.rhs = rhs;
	}

	/**
	 * Returns the left-hand-side linear expression.
	 * 
	 * @return the left-hand-side linear expression
	 */
	public Linear getLhs() {
		return lhs;
	}

	/**
	 * Returns the Boolean operator.
	 * 
	 * @return the Boolean operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Returns the right-hand-side number.
	 * 
	 * @return the right-hand-side number
	 */
	public Number getRhs() {
		return rhs;
	}

	/**
	 * Returns the size of the linear expression.
	 * 
	 * @return the size
	 */
	public int size() {
		return lhs.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return lhs.toString() + " " + operator.toString() + " " + rhs;
	}

}
