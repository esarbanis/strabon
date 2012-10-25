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


/**
 * This class implements the {@link Capabilities} interface and
 * shall be used only for versions of Strabon Endpoint newer than
 * version 3.2.4.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class EndpointCapabilities implements Capabilities {

	@Override
	public String getVersion() {
		return "3.2.4-SNAPSHOT";
	}
	
	@Override
	public boolean supportsLimit() {
		return true;
	}

	@Override
	public boolean supportsAuthentication() {
		return true;
	}

	@Override
	public boolean supportsConnectionModification() {
		return true;
	}
	
	@Override
	public boolean supportsQuerying() {
		return true;
	}

	@Override
	public boolean supportsUpdating() {
		return true;
	}

	@Override
	public boolean supportsStoring() {
		return true;
	}

	@Override
	public boolean supportsDescribing() {
		return true;
	}

	@Override
	public boolean supportsBrowsing() {
		return true;
	}

	@Override
	public RequestCapabilities getQueryCapabilities() {
		return QueryBeanCapabilities.getInstance();
	}

	@Override
	public RequestCapabilities getUpdateCapabilities() {
		return UpdateBeanCapabilities.getInstance();
	}

	@Override
	public RequestCapabilities getStoreCapabilities() {
		return StoreBeanCapabilities.getInstance();
	}

	@Override
	public RequestCapabilities getBrowseCapabilities() {
		return BrowseBeanCapabilities.getInstance();
	}

	@Override
	public RequestCapabilities getConnectionCapabilities() {
		return ConnectionBeanCapabilities.getInstance();
	}
}
