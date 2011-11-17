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

import net.sf.javailp.Problem;
import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.OptType;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverCPLEX;
import net.sf.javailp.SolverGLPK;
import net.sf.javailp.SolverGurobi;
import net.sf.javailp.VarType;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test(new SolverGurobi());
		test(new SolverGLPK());
		test(new SolverCPLEX());
	}

	public static void test(Solver solver) {
		solver.setParameter(Solver.VERBOSE, 0);
		solver.setParameter(Solver.TIMEOUT, 100); // set timeout to 100 seconds

		/**
		 * Constructing a Problem: Maximize: 143x+60y Subject to: 120x+210y <=
		 * 15000 110x+30y <= 4000 x+y <= 75
		 * 
		 * With x,y being integers
		 * 
		 */
		// for one problem
		Problem problem = solver.getProblem("Test");
		
		problem.addVariable("x", VarType.INT, null, null);
		problem.addVariable("y", VarType.INT, null, null);

		Linear linear = new Linear();
		linear.add(143, "x");
		linear.add(60, "y");

		problem.setObjective(linear, OptType.MAX);

		linear = new Linear();
		linear.add(120, "x");
		linear.add(210, "y");

		problem.addConstraint("143x + 60y <= 15000", linear, Operator.LE, 15000);

		linear = new Linear();
		linear.add(110, "x");
		linear.add(30, "y");

		problem.addConstraint("110x + 30y <= 4000", linear, Operator.LE, 4000);

		linear = new Linear();
		linear.add(1, "x");
		linear.add(1, "y");

		problem.addConstraint("x + y <= 75", linear, Operator.LE, 75);

		Result result = solver.solve(problem);

		System.out.println(result);

		/**
		 * Extend the problem with x <= 16 and solve it again
		 */
		/*problem.setVarUpperBound("x", 16);

		solver = factory.get();
		result = solver.solve(problem);

		System.out.println(result);*/

	}

}
