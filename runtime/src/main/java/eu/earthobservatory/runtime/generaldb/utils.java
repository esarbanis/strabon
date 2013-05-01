package eu.earthobservatory.runtime.generaldb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class utils {
	
	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.generaldb.Strabon.class);
	
	public static String updateRewriting(String queryString) 
	{
		String newQueryString="";
		int numOfQuadruples=0;
		int startIndex=0;
		
		NQuadsParser parser = new NQuadsParser();
		
		try
		{
			String graphVariable="?g"+(int)(Math.random()*10000);
			while (queryString.contains(graphVariable))
			{
				graphVariable+="1";
			}
			graphVariable+="_";
			
			//remove comments from query
			queryString=queryString.replaceAll("\\.#", ".\n#");
		    String REGEX = "((^(\\s)*#)|((\\s)+#)).*$";
			Pattern pattern = Pattern.compile(REGEX, Pattern.MULTILINE);							
			Matcher matcher = pattern.matcher(queryString);
			String oldQueryString=matcher.replaceAll("");
			
			//check whether the query contains quadruples	
			String Word="((\\w)|(\\p{InGreek}))+";
			String URI="(<([\\S])*>)|("+Word+":"+Word+")";
			String Literal="\".*\"(\\^\\^"+URI+")?";
			String Variable="\\?"+Word;
			
			String SPOT="(("+URI+")|("+Literal+")|("+Variable+"))";
			REGEX="("+SPOT+"(\\s)+){3}"+SPOT+"(\\s)*[\\}\\.]";
			

			pattern = Pattern.compile(REGEX, Pattern.DOTALL);							
			matcher = pattern.matcher(oldQueryString);
			
			while(matcher.find())		
			{
				String quadruple=oldQueryString.substring(matcher.start(), matcher.end()).trim();
				numOfQuadruples++;
				
				newQueryString+=oldQueryString.substring(startIndex, matcher.start());
				startIndex=matcher.end();
	
				//tokenize quadruples and convert them to triples:
				//s p o t  --becomes--> GRAPH ?g(numOfQuadruples) {s p o}
				//                      ?g(numOfQuadruples) strdf:hasValidTime t
				
				//About to use rewriting mechanism in updates too: 
				/*example: insert data { s p o t} is translated into: 
				 * 
				 * insert data {graph <g> {s p o} }
				 * 
				 */
				String[] token = quadruple.split("(\\s)+");
				System.out.println("subject:"+ token[0]);
				System.out.println("predicate"+ token[1]);
				System.out.println("object:"+ token[2]);
				System.out.println("graph:"+ token[3]);
				String graph =  "<"+parser.createValidTimeURI(token[3]).toString()+">";
				System.out.println("The graph URI = "+ graph.toString());
				newQueryString+="\n GRAPH "+graph+" { " +token[0]+" "+token[1]+" "+token[2]+" .}\n";
				//newQueryString+=graphVariable+numOfQuadruples+" <http://strdf.di.uoa.gr/ontology#hasValidTime>";
				
				//add the rest tokens
				for( int i=5; i<token.length; i++)
					newQueryString+=" "+token[i];
			}
			
			if(numOfQuadruples==0)
			{
				newQueryString=queryString;
				logger.info("\n\nQuadruple not found\n\n");
			}
			else
			{
				newQueryString+=oldQueryString.substring(startIndex);
				logger.info("\n\nNew QueryString:\n {}\n\n", newQueryString);
			}
		}
		catch(Exception e)
		{
			logger.error("[Strabon.queryRewriting]", e);
		}

		return newQueryString;

}
}
