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
package net.sf.javailp.test;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryCPLEX;
import net.sf.javailp.SolverFactoryGLPK;
import net.sf.javailp.SolverFactoryGurobi;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test(new SolverFactoryCPLEX());
		test(new SolverFactoryGLPK());
		test(new SolverFactoryGurobi());
	}

	public static void test(SolverFactory factory) {
		factory.setParameter(Solver.VERBOSE, 0);
		factory.setParameter(Solver.TIMEOUT, 100); // set timeout to 100 seconds

		/**
		 * Constructing a Problem: Maximize: 143x+60y Subject to: 120x+210y <=
		 * 15000 110x+30y <= 4000 x+y <= 75
		 * 
		 * With x,y being integers
		 * 
		 */
		Problem problem = new Problem();

		Linear linear = new Linear();
		linear.add(143, "x");
		linear.add(60, "y");

		problem.setObjective(linear, OptType.MAX);

		linear = new Linear();
		linear.add(120, "x");
		linear.add(210, "y");

		problem.add("143x + 60y", linear, "<=", 15000);

		linear = new Linear();
		linear.add(110, "x");
		linear.add(30, "y");

		problem.add("110x + 30y", linear, "<=", 4000);

		linear = new Linear();
		linear.add(1, "x");
		linear.add(1, "y");

		problem.add("x + y", linear, "<=", 75);

		problem.setVarType("x", Integer.class);
		problem.setVarType("y", Integer.class);

		Solver solver = factory.get(); // you should use this solver only once
		// for one problem
		Result result = solver.solve(problem);

		System.out.println(result);

		/**
		 * Extend the problem with x <= 16 and solve it again
		 */
		problem.setVarUpperBound("x", 16);

		solver = factory.get();
		result = solver.solve(problem);

		System.out.println(result);

	}

}
