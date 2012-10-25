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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link Capabilities} interface and
 * shall be used only for versions of Strabon Endpoint prior to
 * version 3.2.5.
 * 
 * The purpose of this implementation is to attempt to find out the
 * capabilities of old Strabon Endpoints based on two simple heuristics:
 * 
 * 1) what has been changed in the code (addition of methods/classes adding
 *    specific functionality)
 * 2) response messages or HTTP codes that old Strabon Endpoints give on wrong
 *    parameters.    
 * 
 * The result may not be accurate in every case.
 * 
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class AutoDiscoveryCapabilities implements Capabilities {

	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.capabilities.AutoDiscoveryCapabilities.class);
	
	@Override
	public boolean supportsLimit() {
		@SuppressWarnings("rawtypes")
		Class strabonWrapper;
		try {
			strabonWrapper = Class.forName("eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapper");

			strabonWrapper.getDeclaredField("maxLimit");
			
			return true;
				
		} catch (ClassNotFoundException e1) {
			// No StrabonBeanWrapper? How come?
			logger.warn("[StrabonEndpoint.AutoDiscoveryCapabilities] Didn't find StrabonEndpoint class!!! How come?");
			
		} catch (SecurityException e) {
			logger.info("[StrabonEndpoint.AutoDiscoveryCapabilities] Could not determine limit support due to security exception. ", e);
			
		} catch (NoSuchFieldException e) {
			// this exception is OK. Strabon Endpoint does not support limit of results
		}
		
		return false;
	}

	@Override
	public boolean supportsAuthentication() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.Authenticate");
	}

	@Override
	public boolean supportsConnectionModification() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.ChangeConnectionBean");
	}

	@Override
	public String getVersion() {
		return "<= 3.2.4";
	}

	@Override
	public RequestCapabilities getQueryCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestCapabilities getUpdateCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestCapabilities getStoreCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestCapabilities getBrowseCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestCapabilities getConnectionCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsQuerying() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.QueryBean");
	}

	@Override
	public boolean supportsUpdating() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.UpdateBean");
	}

	@Override
	public boolean supportsStoring() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.StoreBean");
	}

	@Override
	public boolean supportsDescribing() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.DescribeBean");
	}

	@Override
	public boolean supportsBrowsing() {
		return canBeLoaded("eu.earthobservatory.org.StrabonEndpoint.BrowseBean");
	}
	
	private boolean canBeLoaded(String className) {
		try {
			Class.forName(className);
			return true;
			
		} catch (ClassNotFoundException e1) {
			return false;
		}
	}
}
