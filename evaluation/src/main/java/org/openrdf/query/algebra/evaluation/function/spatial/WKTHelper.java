/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.spatial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.constants.GeoConstants;


/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Panayiotis Smeros <psmeros@di.uoa.gr>
 *
 */
public class WKTHelper {

	private static Logger logger = LoggerFactory.getLogger(org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper.class);
	
	public static String  STRDF_SRID_DELIM 	= ";";
	private static String CUT_DELIM 	= "/";
	private static String URI_ENDING	= ">";
	
	/**
	 * Returns the given WKT without the SRID (if any).
	 * 
	 * @param wkt
	 * @return
	 */
	public static String getWithoutSRID(String wkt) {
		if (wkt == null) return wkt;
		
		int pos = wkt.lastIndexOf(STRDF_SRID_DELIM);
		if (pos != -1) {
			return wkt.substring(0, pos);
			
		} else {
			return wkt;
		}
	}
	
	/**
	 * Returns the SRID of the given WKT (if any). If the WKT
	 * does not contain any, then the default is returned.
	 * 
	 * @param wkt
	 * @return
	 */
	public static Integer getSRID(String wkt) {
		int srid = GeoConstants.default_stRDF_SRID;
		
		if (wkt == null) return srid;
		
		int pos = wkt.lastIndexOf(STRDF_SRID_DELIM);
		if (pos != -1) {
			try {
				srid = Integer.parseInt(wkt.substring(wkt.lastIndexOf(CUT_DELIM) + 1).replace(URI_ENDING, ""));
				
			} catch (NumberFormatException e) {
				logger.warn("[Strabon.WKTHelper] Was expecting an integer. The URL of the SRID was {}. Continuing with the default SRID, {}", wkt.substring(pos + 1), srid);
				
			}
		}
		
		return srid;
	}
}
