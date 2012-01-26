package org.openrdf.query.algebra.evaluation.function.spatial;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

public class ProjectionsFilter implements CoordinateFilter {

	int[] dims = new int[3];
	
	
	
	public ProjectionsFilter(int[] dims) {
		super();
		this.dims = dims;
	}



	public int[] getDims() {
		return dims;
	}



	public void setDims(int[] dims) {
		this.dims = dims;
	}

	public void filter(Coordinate coord) {
		
		if(dims[0]==0)
		{
			coord.x=0;
		}
		
		if(dims[1]==0)
		{
			coord.y=0;
		}
		
		if(dims[2]==0)
		{
			coord.z=0;
		}

	}

}
