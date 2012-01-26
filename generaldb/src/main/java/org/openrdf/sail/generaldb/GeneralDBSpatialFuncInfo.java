package org.openrdf.sail.generaldb;

/**
 * 
 * @author manolee
 * Class used to store all info needed about a spatial construct / metric / property located in select clause
 * Currently storing info about the name of the field that has to be retrieved from the Result Set, 
 * as well as the type of the spatial function 
 */
public class GeneralDBSpatialFuncInfo {
	
	public enum typeOfField 
	{
			Integer,
			Double,
			Boolean,
			String,
			WKB
			;
	}
	
	

	public GeneralDBSpatialFuncInfo(String fieldName, typeOfField type) {
		super();
		this.fieldName = fieldName;
		this.type = type;
	}
	private String fieldName;
	private typeOfField type;
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public typeOfField getType() {
		return type;
	}
	public void setType(typeOfField type) {
		this.type = type;
	}
	
	
}


