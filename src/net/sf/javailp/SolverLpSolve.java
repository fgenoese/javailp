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

import java.util.HashMap;
import java.util.Map;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

/**
 * The {@code SolverLpSolve} is the {@code Solver} lp_solve.
 * 
 * @author lukasiewycz
 * 
 */
public class SolverLpSolve extends AbstractSolver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Solver#solve(net.sf.javailp.Problem)
	 */
	public Result solve(Problem problem) {

		Map<Integer, Object> indexToVar = new HashMap<Integer, Object>();
		Map<Object, Integer> varToIndex = new HashMap<Object, Integer>();

		int i = 1;
		for (Object variable : problem.getVariables()) {
			indexToVar.put(i, variable);
			varToIndex.put(variable, i);
			i++;
		}

		try {
			LpSolve lp = LpSolve.makeLp(0, problem.getVariablesCount());

			initWithParameters(lp);

			lp.setAddRowmode(true);

			for (Constraint constraint : problem.getConstraints()) {
				int size = constraint.size();

				int[] var = new int[size];
				double[] coeffs = new double[size];
				Linear linear = constraint.getLhs();

				convert(linear, var, coeffs, varToIndex);

				int operator;
				switch (constraint.getOperator()) {
				case LE:
					operator = LpSolve.LE;
					break;
				case GE:
					operator = LpSolve.GE;
					break;
				default: // EQ
					operator = LpSolve.EQ;
				}

				double rhs = constraint.getRhs().doubleValue();

				lp.addConstraintex(size, coeffs, var, operator, rhs);
			}

			lp.setAddRowmode(false);

			for (Object variable : problem.getVariables()) {
				int index = varToIndex.get(variable);

				VarType varType = problem.getVarType(variable);
				Number lowerBound = problem.getVarLowerBound(variable);
				Number upperBound = problem.getVarUpperBound(variable);

				if (varType == VarType.BOOL || varType == VarType.INT) {
					lp.setInt(index, true);
				}

				if (varType == VarType.BOOL) {
					int lb = 0;
					int ub = 1;
					if (lowerBound != null && lowerBound.doubleValue() > 0) {
						lb = 1;
					}
					if (upperBound != null && upperBound.doubleValue() < 1) {
						ub = 0;
					}
					lp.setLowbo(index, lb);
					lp.setUpbo(index, ub);
				} else {
					if (lowerBound != null) {
						lp.setLowbo(index, lowerBound.doubleValue());
					}
					if (upperBound != null) {
						lp.setUpbo(index, upperBound.doubleValue());
					}
				}

			}

			if (problem.getObjective() != null) {

				Linear objective = problem.getObjective();
				int size = objective.size();
				int[] var = new int[size];
				double[] coeffs = new double[size];

				convert(objective, var, coeffs, varToIndex);

				lp.setObjFnex(size, coeffs, var);

				if (problem.getOptType() == OptType.MIN) {
					lp.setMinim();
				} else {
					lp.setMaxim();
				}
			}

			@SuppressWarnings("unused")
			int ret = lp.solve();

			final Result result;
			if (problem.getObjective() != null) {
				double obj = lp.getObjective();
				result = new Result(obj);
			} else {
				result = new Result();
			}

			double[] values = new double[problem.getVariablesCount()];
			lp.getVariables(values);

			for (Object variable : problem.getVariables()) {

				int index = varToIndex.get(variable);
				VarType varType = problem.getVarType(variable);

				double value = values[index - 1];

				if (varType == VarType.INT || varType == VarType.BOOL) {
					int v = (int) Math.round(value);
					result.put(variable, v);
				} else {
					result.put(variable, value);
				}
			}

			lp.deleteLp();

			return result;

		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void initWithParameters(LpSolve lp) {
		Object timeout = parameters.get(Solver.TIMEOUT);
		Object verbose = parameters.get(Solver.VERBOSE);

		if (timeout != null && timeout instanceof Number) {
			Number number = (Number) timeout;
			long value = number.longValue();
			lp.setTimeout(value);
		}
		if (verbose != null && verbose instanceof Number) {
			Number number = (Number) verbose;
			int value = number.intValue();
			if (value == 0) {
				lp.setVerbose(0);
			} else if (value == 1) {
				lp.setVerbose(4);
			} else {
				lp.setVerbose(10);
			}
		}

	}

	protected void convert(Linear linear, int[] var, double[] coeffs, Map<Object, Integer> varToIndex) {

		int i = 0;
		for (Object variable : linear.getVariables()) {
			var[i] = varToIndex.get(variable);
			i++;
		}
		i = 0;
		for (Number coefficient : linear.getCoefficients()) {
			coeffs[i] = coefficient.doubleValue();
			i++;
		}
	}
}