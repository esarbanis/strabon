package org.openrdf.query.resultio.sparqlxml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import info.aduna.xml.XMLWriter;

/**
 * @author charnik
 *
 */
public class stSPARQLXMLWriter extends XMLWriter {

	/**
	 * @param writer
	 */
	public stSPARQLXMLWriter(Writer writer) {
		super(writer);
	}

	/**
	 * @param outputStream
	 */
	public stSPARQLXMLWriter(OutputStream outputStream) {
		super(outputStream);
	}

	/**
	 * @param outputStream
	 * @param charEncoding
	 * @throws UnsupportedEncodingException
	 */
	public stSPARQLXMLWriter(OutputStream outputStream, String charEncoding) throws UnsupportedEncodingException {
		super(outputStream, charEncoding);
	}
	
	/**
	 * Like XMLWriter.text(String text) but without escaping the string.
	 */
	public void unescapedText(String text) throws IOException {
		_write(text);
	}

}
