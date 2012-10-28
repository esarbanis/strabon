package org.openrdf.query.algebra.evaluation.function.link;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
		return GeoConstants.stRDF+name;
	}

    @SuppressWarnings("deprecation")
	public Value evaluate(ValueFactory valueFactory, Value... args)
            throws ValueExprEvaluationException {
        if (args.length != 2) {
            throw new ValueExprEvaluationException("strdf:" + name
                    + " requires exactly 2 arguments, got " + args.length);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DDThh:mm:ss"); //the format of xsd:Datetime
		XMLGregorianCalendar calendar=null;

    	try {
    	    int minutesToAdd = Integer.parseInt(args[1].toString());
    	    String date = args[0].toString();
    	    Calendar cal = sdf.getCalendar();
    		cal.setTime(sdf.parse(date));
    		System.out.println("OLD TIME:"+cal.getTime());
    		cal.add(Calendar.MINUTE, minutesToAdd);
    		System.out.println("NEW TIME:"+cal.getTime());
    		
    		calendar.setYear(cal.getTime().getYear());
    		calendar.setYear(cal.getTime().getMonth());
    		calendar.setYear(cal.getTime().getDay());
    		calendar.setYear(cal.getTime().getHours());
    		calendar.setYear(cal.getTime().getMinutes());
    		calendar.setYear(cal.getTime().getSeconds());
    		
    	} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
        return valueFactory.createLiteral(calendar);
    }



}
