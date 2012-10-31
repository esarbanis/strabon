/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.capabilities;

import java.util.List;
import java.util.Vector;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class Parameter {

	private String name;
	private String value;
	
	private List<String> acceptedValues;
	
	public Parameter(String name, String value) {
		this.name = name;
		this.value = value;
		
		this.acceptedValues = new Vector<String>();
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void addAcceptedValue(String value) {
		acceptedValues.add(value);
	}
	
	public List<String> getAcceptedValues() {
		return acceptedValues;
	}
}
