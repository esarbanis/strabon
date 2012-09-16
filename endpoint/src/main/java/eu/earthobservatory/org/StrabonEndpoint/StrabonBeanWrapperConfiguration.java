/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class StrabonBeanWrapperConfiguration {
	private String label;
	private String bean;
	private String statement;
	private String format;
	private String title;

	public StrabonBeanWrapperConfiguration(String label, String bean, String statement, String format, String title) {
		this.label = label;
		this.bean = bean;
		this.statement = statement;
		this.format = format;
		this.title=title;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getBean() {
		return bean;
	}
	
	public void setBean(String bean) {
		this.bean = bean;
	}
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatement() {
		return statement;
	}
	
	public void setStatement(String statement) {
		this.statement = statement;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
}

