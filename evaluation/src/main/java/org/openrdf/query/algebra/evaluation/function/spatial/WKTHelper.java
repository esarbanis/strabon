package org.openrdf.query.algebra.evaluation.function.spatial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class WKTHelper {

	private static Logger logger = LoggerFactory.getLogger(org.openrdf.query.algebra.evaluation.function.spatial.WKTHelper.class);
	
	private static String SRID_DELIM 	= ";";
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
		
		int pos = wkt.indexOf(SRID_DELIM);
		if (pos != -1) {
			return wkt.substring(0, pos);
			
		} else {
			return wkt;
		}
	}
	
	/**
	 * Returns the SRID of the given WKT (if any). If the WKT
	 * does not contain any, then the default is returned (specified in
	 * org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron.defaultSRID).
	 * 
	 * @param wkt
	 * @return
	 */
	public static Integer getSRID(String wkt) {
		int srid = GeoConstants.defaultSRID;
		
		if (wkt == null) return srid;
		
		int pos = wkt.indexOf(SRID_DELIM);
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
