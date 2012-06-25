package org.openrdf.query.resultio.text;

import java.io.OutputStream;

import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;

/**
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 *
 */
public class stSPARQLResultsTSVWriter extends SPARQLResultsTSVWriter {

	public stSPARQLResultsTSVWriter(OutputStream out) {
		super(out);
	}

}
