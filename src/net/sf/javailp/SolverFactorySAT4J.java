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
 * The {@code SolverFactoryCPLEX} is a {@code SolverFactory} for SAT4J.
 * 
 * @author lukasiewycz
 * 
 */
public class SolverFactorySAT4J extends AbstractSolverFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.AbstractSolverFactory#getInternal()
	 */
	@Override
	protected Solver getInternal() {
		return new SolverSAT4J();
	}

}