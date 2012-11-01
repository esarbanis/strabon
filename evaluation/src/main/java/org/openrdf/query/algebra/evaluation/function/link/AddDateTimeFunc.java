package org.openrdf.query.algebra.evaluation.function.link;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;

public class AddDateTimeFunc implements Function {
	

	
	protected static String name = "addDatetime";
	
	@Override
	public String getURI() {
		return "http://example.org/custom-function/addDateTime";
		
	}

    @SuppressWarnings("deprecation")
	public Value evaluate(ValueFactory valueFactory, Value... args)
            throws ValueExprEvaluationException {
        if (args.length != 2) {
            throw new ValueExprEvaluationException("strdf:" + name
                    + " requires exactly 2 arguments, got " + args.length);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD'T'hh:mm:ss"); //the format of xsd:Datetime
		GregorianCalendar calendar = new GregorianCalendar();
		GregorianCalendar cal = new GregorianCalendar(); 
	    DatatypeFactory dafa = null;
	    XMLGregorianCalendar xmlcal =null;
	    XMLGregorianCalendar gxml1 =null;

    	try {
    		String toParse = args[1].toString().replace("^^<http://www.w3.org/2001/XMLSchema#integer>", "").replace("\"", "");
    		System.out.println("TO PARSE:"+ toParse);
    		int minutesToAdd = Integer.parseInt(toParse);
    	    String date = args[0].toString();
    	    date = args[0].toString().replace("^^<http://www.w3.org/2001/XMLSchema#dateTime>", "").replace("\"", "");
    	    //cal = sdf.getCalendar();
    		cal.setTime(sdf.parse(date));
    		System.out.println("OLD TIME:"+cal.getTime());
    		cal.add(Calendar.MINUTE, minutesToAdd);
    		System.out.println("NEW TIME:"+cal.getTime());
    	   
    		
          // xmlcal =  dafa.newXMLGregorianCalendar(cal.toString());

    		System.out.println(cal.get(Calendar.YEAR));
    		System.out.println(cal.get(Calendar.MONTH));
    		System.out.println(cal.get(Calendar.DAY_OF_MONTH));
    		System.out.println(cal.get(Calendar.HOUR));
    		System.out.println(cal.get(Calendar.MINUTE));
    		System.out.println(cal.get(Calendar.MILLISECOND));
    		
    	} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
        //valueFactory.createLiteral(xmlmcal);
    	XMLGregorianCalendar gxml=null;
		try {
			gxml = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//gxml = dafa.newXMLGregorianCalendar(cal);
		Value value =  valueFactory.createLiteral(gxml);
		System.out.println("value="+value.toString());
		return value;
    }



}
