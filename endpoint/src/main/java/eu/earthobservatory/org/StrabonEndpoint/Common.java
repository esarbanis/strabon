/**
 * 
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.rio.RDFFormat;

/**
 * Keeps common variables shared by beans and .jsp pages.
 *
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class Common {
	  
	/**
	 * Parameter used in JSP files to denote the usage
	 * of the HTML interface
	 */
	public static final String VIEW 			= "view";
	public static final String VIEW_TYPE 		= "HTML";

	/**
	 * Parameters used in the store.jsp file
	 */
	public static final String PARAM_DATA 		= "data";
	public static final String PARAM_FORMAT 	= "format";
	public static final String PARAM_DATA_URL	= "url";
	
	/**
	 * Submit buttons in store.jsp
	 */
	public static final String SUBMIT_INPUT		= "dsubmit";
	public static final String SUBMIT_URL		= "fromurl";
	
	/**
	 * Keeps the registered and available RDF formats.
	 */
	public static final List<String> registeredFormats = new ArrayList<String>();
	
	// initialize registered and available formats
	static {
		for (RDFFormat format : RDFFormat.values()) {
			registeredFormats.add(format.getName());
		}
	}
	
	/**
	 * Keeps the registered and available stSPARQL Query Results Formats.
	 */
	public static final List<stSPARQLQueryResultFormat> registeredQueryResultsFormats = new ArrayList<stSPARQLQueryResultFormat>();
	
	/**
	 * Keeps the name of the registered and available stSPARQL Query Results Formats.
	 * (to be used in the drop-down menu in query.jsp)
	 */
	public static final List<String> registeredQueryResultsFormatNames = new ArrayList<String>();
	
	// initialize registered and available stSPARQL query results formats
	static {
		for (TupleQueryResultFormat format : stSPARQLQueryResultFormat.values()) {
			if (format instanceof stSPARQLQueryResultFormat) {
				registeredQueryResultsFormats.add((stSPARQLQueryResultFormat) format);
				registeredQueryResultsFormatNames.add(format.getName());
			}
		}
	}
	
    /**
     * Determines the RDF format to use. We check only for "accept"
     * parameter (present in the header). 
     * 
     * The use of "format" parameter is now deprecated for using any
     * Bean as a service. It is only used through the HTML
     * visual interface, provided with Strabon Endpoint.
     * 
     * @param request
     * @return
     */
    public static RDFFormat getRDFFormatFromAcceptHeader(String acceptHeader) {
        if (acceptHeader != null) {
            // check whether the "accept" parameter contains any 
            // of the mime types of any RDF format
            for (RDFFormat format : RDFFormat.values()) {
                for (String mimeType : format.getMIMETypes()) {
                    if (acceptHeader.contains(mimeType)) {
                            return format;
                    }
                }
            }
        }
                
        return null;
    }
    
    /**
     * Determines the stSPARQL query result format to use. We check only for "accept"
     * parameter (present in the header). 
     * 
     * The use of "format" parameter is now deprecated for using any
     * Bean as a service. It is only used through the HTML
     * visual interface, provided with Strabon Endpoint.
     * 
     * @param request
     * @return
     */
    public static stSPARQLQueryResultFormat getResultFormatFromAcceptHeader(String acceptHeader) {
    	if (acceptHeader != null) {
            // check whether the "accept" parameter contains any 
            // of the mime types of any stSPARQL query result format
    		for (stSPARQLQueryResultFormat format : registeredQueryResultsFormats) {
				for (String mimeType : format.getMIMETypes()) {
					if (acceptHeader.contains(mimeType)) {
						return format;
					}
				}
            }
        }
                
        return null;
    }
}
