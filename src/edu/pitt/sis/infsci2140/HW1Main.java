package edu.pitt.sis.infsci2140;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.Map;

import edu.pitt.sis.infsci2140.index.TrecwebCollection;
import edu.pitt.sis.infsci2140.index.TrectextCollection;
import edu.pitt.sis.infsci2140.index.DocumentCollection;

import edu.pitt.sis.infsci2140.analysis.TextTokenizer;
import edu.pitt.sis.infsci2140.analysis.TextNormalizer;
import edu.pitt.sis.infsci2140.analysis.StopwordsRemover;

/**
 * !!! YOU CANNOT CHANGE ANYTHING IN THIS CLASS !!!
 * 
 * Main class for running your HW1.
 * 
 */
public class HW1Main {
	
	public static void main(String[] args) {
		
		if( args==null || args.length<4 ) {
			System.out.println("Usage:");
			System.out.println("  args[0]: format of the collection file, trectext or trecweb.");
			System.out.println("  args[1]: path of the collection file.");
			System.out.println("  args[2]: path of the stopwords list file.");
			System.out.println("  args[3]: path of the output file.");
			System.exit(0);
		}
		
		String format = args[0];
		String path_input = args[1];
		String path_output = args[3];
		String path_stopwords = args[2];
		
		FileInputStream instream_collection = null;
		DocumentCollection collection = null;
		
		try{
			// Loading the collection file and initiate the DocumentCollection class
			instream_collection = new FileInputStream(path_input);
			if( format.equalsIgnoreCase("trectext") ) {
				collection = new TrectextCollection(instream_collection);
			}else if( format.equalsIgnoreCase("trecweb") ) {
				collection = new TrecwebCollection(instream_collection);
			}
		}catch(IOException e){
			System.out.println("ERROR: cannot load collection file.");
			e.printStackTrace();
		}
		
		FileInputStream instream_stopwords = null;
		StopwordsRemover stoprmv = null;
		
		try{
			// Loading the stopword list file and initiate the StopwordsRemover class
			instream_stopwords = new FileInputStream(path_stopwords);
			stoprmv = new StopwordsRemover(instream_stopwords);
		}catch(IOException e){
			System.out.println("ERROR: cannot load stopwords file.");
			e.printStackTrace();
		}
		
		BufferedWriter output = null;
		
		try {
			// Initiate the output writer ...
			output = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(path_output), "UTF-8" ) );
		}catch(IOException e){
			System.out.println("ERROR: cannot initiate output file.");
			e.printStackTrace();
		}
		
		if( collection!=null && stoprmv!=null && output!=null ) {
			
			try{
				
				Map<String,Object> doc = null;
				while( ( doc=collection.nextDocument() ) != null ) { // iteratively reading each document from the collection
					
					String docno = (String) doc.get("DOCNO"); // load docno of the document and output
					char[] content = (char[]) doc.get("CONTENT"); // document content
					
					output.write(docno);
					output.write("\n");
					
					TextTokenizer tokenizer = new TextTokenizer(content);
					char[] word = null;
					while( ( word=tokenizer.nextWord() ) != null ) { // iteratively loading each word from the document
						word = TextNormalizer.normalize(word); // normalize each word
						if( stoprmv.isStopword(word) ) {
							// if the word is a stopword, output <OOV> (Out Of Vocabulary)
							output.write("<OOV>");
						}else{
							// if the word is not a stopword, output the normalized word
							output.write(word);
						}
						output.write(" "); // each normalized word will be separated by a whitespace
					}
					output.write("\n");
					
				}
				
			}catch(IOException e){
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			
		}
		
		if( instream_collection!=null ) {
			try{
				instream_collection.close();
			}catch(IOException e){ }
		}
		
		if( instream_stopwords!=null ) {
			try{
				instream_stopwords.close();
			}catch(IOException e){ }
		}
		
		if( output!=null ) {
			try{
				output.close();
			}catch(IOException e){ }
		}
		
	}
	
}
