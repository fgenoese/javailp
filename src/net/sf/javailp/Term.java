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

/**
 * The {@code Term} is the basic element the {@link Linear}. It is a coefficient
 * and its variable.
 * 
 * @author lukasiewycz @author fgenoese
 * 
 */
public class Term {

	protected final String variableName;
	protected final Number coefficient;

	/**
	 * Constructs a {@code Term}.
	 * 
	 * @param variableName
	 *            the variable name
	 * @param coefficient
	 *            the coefficient
	 */
	public Term(String variableName, Number coefficient) {
		super();
		if (coefficient == null) {
			throw new IllegalArgumentException("The variable " + variableName
					+ " has no valid coefficient.");
		}
		this.variableName = variableName;
		this.coefficient = coefficient;
	}

	/**
	 * Returns the variable name.
	 * 
	 * @return the variable name
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * Returns the coefficient.
	 * 
	 * @return the coefficient
	 */
	public Number getCoefficient() {
		return coefficient;
	}

}
