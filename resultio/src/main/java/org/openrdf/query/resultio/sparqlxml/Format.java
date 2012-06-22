package org.openrdf.query.resultio.sparqlxml;

import java.util.HashMap;
import java.util.Map;

/**
 * This enumeration type represents the available formats
 * for the results of the evaluation of a SPARQL query.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public enum Format {

	/**
	 * Default format
	 */
	DEFAULT(""),
	
	/**
	 * XML format
	 */
	XML("XML"),
	
	/**
	 * KML format
	 */
	KML("KML"),
	
	/**
	 * KMZ format (compressed KML)
	 */
	KMZ("KMZ"),
	
	/**
	 * GeoJSON format
	 */
	GEOJSON("GeoJSON"),
	
	/**
	 * Format for experiments
	 */
	EXP("EXP"),
	
	/**
	 * HTML format
	 */
	HTML("HTML"),
	
	/**
	 * Invalid format.
	 */
	INVALID("INVALID");
	
	/**
	 * The string representation of this format
	 */
	private String name;
	
	/**
	 * Map a string constant to a Format
	 */
	private static final Map<String, Format> stringToEnum = new HashMap<String, Format>();
	
	
	static { // initialize map from constant name to enum constant
		for (Format format : values()) {
			// add both upper- and lower-case versions of the format 
			stringToEnum.put(format.toString(), format);
			stringToEnum.put(format.toString().toLowerCase(), format);
		}
	}
	
	/**
	 * Format constructor.
	 * 
	 * @param name
	 */
	Format(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Returns a Format enum given a format string.
	 * 
	 * @param lang
	 * @return
	 */
	public static Format fromString(String format) {
		return (stringToEnum.get(format) == null) ? INVALID:stringToEnum.get(format);
	}
}
