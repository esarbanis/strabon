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
		
		StringBuffer whereClauses = new StringBuffer(2048);
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
			
			String updateREGEX= "(where([\\s])*\\{(.)*\\})";
	
			//check whether the query contains quadruples	
			String Word="((\\w)|(\\p{InGreek}))+";
			String URI="(<([\\S])*>)|("+Word+":"+Word+")";
			String Literal="\".*\"(\\^\\^"+URI+")?";
			String Variable="\\?"+Word;
			
			String SPOT="(("+URI+")|("+Literal+")|("+Variable+"))";
			REGEX="("+SPOT+"(\\s)+){3}"+SPOT+"(\\s)*[\\}\\.]";
			

			pattern = Pattern.compile(REGEX, Pattern.DOTALL);							
			matcher = pattern.matcher(oldQueryString);
			
			Pattern updatePattern = Pattern.compile(updateREGEX, Pattern.DOTALL);
			Matcher updateMatcher =  updatePattern.matcher(oldQueryString);
			
			System.out.println("oldQuerySting "+oldQueryString);
			while(updateMatcher.find())
			{
				String clause=oldQueryString.substring(updateMatcher.start(), updateMatcher.end()).trim();
				System.out.println("Clause is: "+ clause);
				whereClauses.append(clause);
			}
			
			
			while(matcher.find())		
			{
				String quadruple=oldQueryString.substring(matcher.start(), matcher.end()).trim();
				
				boolean inWhere;
				if( whereClauses.indexOf(quadruple) <0 )
				{
					inWhere=false;
				}
				else
				{
					inWhere = true;
				}
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
				
				int i=3;
				if(!isVar(token[3]) && inWhere==false) //the forth element is a literal representation of valid time
				{
					System.out.println("quad constant in where clause");
					String graph =  "<"+parser.createValidTimeURI(token[3]).toString()+">";
					System.out.println("The graph URI = "+ graph.toString());
					newQueryString+="\n GRAPH "+graph+" { " +token[0]+" "+token[1]+" "+token[2]+" .}\n";
					i=5;
	
				}
				else if(isVar(token[3]) && inWhere==false) //the fourth element is a temporal variable in an update clause
				{
					//String addedPattern = graphVariable+numOfQuadruples+ " <http://strdf.di.uoa.gr/ontology#hasValidTime>"+ token[3];
					newQueryString+="\n GRAPH "+graphVariable+numOfQuadruples+" { " +token[0]+" "+token[1]+" "+token[2]+" .}\n";
					i=5;
				}
				else if(isVar(token[3]) && inWhere==true) //temporal variable in where clause
				{
					newQueryString+="\n GRAPH "+graphVariable+numOfQuadruples+" { " +token[0]+" "+token[1]+" "+token[2]+" .}\n";
					newQueryString+=graphVariable+numOfQuadruples+" <http://strdf.di.uoa.gr/ontology#hasValidTime>";
					i=3;
				}
				
				
				//add the rest tokens
				while( i<token.length)
				{
					newQueryString+=" "+token[i];
					i++;
					
				}
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
	/*
	 * checks whether an element of a statement pattern is a variable or not
	 */
	public static boolean isVar (String element)
	{
		if(element.startsWith("?"))
			return true;
		else
			return false;
	}
}
