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
	
	/**
	 * Given the WKT representation of a geometry, a SRID, and a datatype, it
	 * creates a WKT literal conforming to the syntax implied by the given
	 * datatype. If the given datatype is NULL or different than one of the
	 * standard ones (e.g., {@link GeoConstants.WKT} or
	 * {@link GeoConstants.WKTLITERAL}), then the default literal is returned,
	 * which is determined by {@link GeoConstants.default_WKT_datatype}.  
	 * 
	 * @param plainWKT
	 * @param srid
	 * @param datatype
	 * @return
	 */
	public static String createWKT(String plainWKT, int srid, String datatype) {
		if (GeoConstants.WKTLITERAL.equals(datatype)) {
			return createWKTLiteral(plainWKT, srid);
			
		} else if (GeoConstants.WKT.equals(datatype)) {
			return createstRDFWKT(plainWKT, srid);
			
		} else { // no datatype, create default
			return createWKT(plainWKT, srid, GeoConstants.default_WKT_datatype);
		}
	}
	
	/**
	 * Given the well-known representation of a geometry and a SRID, it creates
	 * a stRDF WKT literal. If the given SRID is the default for that type, then
	 * it is ignored. 
	 * 
	 * @param plainWKT
	 * @param srid
	 * @return
	 */
	public static String createstRDFWKT(String plainWKT, int srid) {
		if (srid == GeoConstants.default_stRDF_SRID) {
			return plainWKT;
			
		} else {
			return plainWKT + ";" + getURI_forSRID(srid);
		}
	}
	
	/**
	 * Given the well-known representation of a geometry and a SRID, it creates
	 * a GeoSPARQL wktLiteral literal. If the given SRID is the default for that type, then
	 * it is ignored.
	 * 
	 * @param plainWKT
	 * @param srid
	 * @return
	 */
	public static String createWKTLiteral(String plainWKT, int srid) {
		if (srid == GeoConstants.default_GeoSPARQL_SRID) {
			return plainWKT;
			
		} else {
			return getURI_forSRID(srid) + " " + plainWKT; 
		}
	}
	
	/**
	 * Returns the URI corresponding to the given SRID.
	 * The given SRID might be an EPSG one or our custom 84000
	 * corresponding to CRS84. If the given SRID is less than
	 * or equal to 0, then an empty string is returned.
	 * 
	 * @param srid
	 * @return
	 */
	public static String getURI_forSRID(int srid) {
		String uri = "";
		if (srid == GeoConstants.WGS84_LONG_LAT_SRID) {
			uri = GeoConstants.WGS84_LONG_LAT;
			
		} else if (srid > 0) { // assuming EPSG now
			uri = GeoConstants.EPSG_URI_PREFIX + srid; 
		}
		
		if (uri.length() > 0) {
			uri = "<" + uri + ">";
		}
		
		return uri;
	}
}
