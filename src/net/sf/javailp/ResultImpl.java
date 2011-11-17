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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The class {@code ResultImpl} is a {@code Map} based implementation of the
 * {@link Result}.
 * 
 * @author lukasiewycz
 * 
 */
public class ResultImpl implements Result {

	protected Map<String, Number> primalValues;
	protected Map<String, Number> dualValues;
	protected Number objectiveValue = null;
	protected Linear objectiveFunction = null;

	/**
	 * Constructs a {@code ResultImpl} for a {@code Problem} with an objective
	 * function.
	 */
	public ResultImpl(Linear objectiveFunction) {
		super();
		this.primalValues = new LinkedHashMap<String, Number>();
		this.dualValues = new HashMap<String, Number>();
		this.objectiveFunction = objectiveFunction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Result#getObjective()
	 */
	public Number getObjective() {
		if (objectiveValue != null) {
			return objectiveValue;
		} else if (objectiveFunction != null) {
			objectiveValue = objectiveFunction.evaluate(this.primalValues);
			return objectiveValue;
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Result#getPrimalValue(java.lang.String)
	 */
	public Number getPrimalValue(String variableName) {
		return primalValues.get(variableName);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Result#putPrimalValue(java.lang.String, java.lang.Number)
	 */
	public void putPrimalValue(String variableName, Number value) {
		primalValues.put(variableName, value);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Result#getDualValue(java.lang.String)
	 */
	public Number getDualValue(String variableName) {
		return dualValues.get(variableName);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Result#putDualValue(java.lang.String, java.lang.Number)
	 */
	public void putDualValue(String variableName, Number value) {
		dualValues.put(variableName, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.javailp.Result#containsVar(java.lang.String)
	 */
	public Boolean containsVar(String variableName) {
		return primalValues.containsKey(variableName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractMap#toString()
	 */
	@Override
	public String toString() {
		return "Objective: " + getObjective() + " " + primalValues.toString();
	}

}