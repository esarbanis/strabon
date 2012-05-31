/**
 * 
 */
package org.openrdf.query.algebra.evaluation.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBConstants;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * This class is a singleton and provides access to the readers/writers
 * of Java Topology Suite. 
 * 
 * @author charnik
 *
 */
public class JTSWrapper {

	// single instance of JTSReaders
	private static JTSWrapper instance;
	
	private WKTReader wktr;
	private WKTWriter wktw;
	private WKBReader wkbr;
	private WKBWriter wkbw;
	
	private JTSWrapper() {
		// use a private constructor to force call of getInstance method and forbid subclassing
		wktr = new WKTReader();
		wktw = new WKTWriter();
		wkbr = new WKBReader();
		wkbw = new WKBWriter(); // PostGIS
//		wkbw = new WKBWriter(2, WKBConstants.wkbXDR); // MonetDB
	}
	
	public static synchronized JTSWrapper getInstance() {
		if (instance == null) {
			instance = new JTSWrapper();
		}
		return instance;
	}
	
	public synchronized Geometry WKTread(String wkt) throws ParseException {		
		return wktr.read(wkt);
	}
	
	public synchronized String WKTwrite(Geometry geom) {
		return wktw.write(geom);
	}
	
	public synchronized Geometry WKBread(byte[] bytes) throws ParseException {
		return wkbr.read(bytes);
	}
	
	public synchronized byte[] WKBwrite(Geometry geom) {
		return wkbw.write(geom); // PostGIS
		// MonetDB
//		byte[] temp = wkbw.write(geom);
//		temp[0] = 1;
//		return temp;
		//
		
	}
	
}
