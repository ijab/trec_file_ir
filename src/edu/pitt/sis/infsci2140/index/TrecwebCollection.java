package edu.pitt.sis.infsci2140.index;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import com.sun.jmx.snmp.Timestamp;

public class TrecwebCollection implements DocumentCollection {
	
	private FileInputStream trec_is = null;
	private TrecParser trec_parser = null;
	
	// YOU SHOULD IMPLEMENT THIS METHOD
	public TrecwebCollection( FileInputStream instream ) throws IOException {
		// This constructor should take an inputstream of the collection file as the input parameter.
		this.trec_is = instream;
		this.trec_parser = new TrecParser(instream);
	}
	
	// YOU SHOULD IMPLEMENT THIS METHOD
	public Map<String, Object> nextDocument() throws IOException {
		// Read the definition of this method from edu.pitt.sis.infsci2140.index.DocumentCollection interface 
		// and follow the assignment instructions to implement this method.
		
		return this.trec_parser.next_doc();
	}
}
