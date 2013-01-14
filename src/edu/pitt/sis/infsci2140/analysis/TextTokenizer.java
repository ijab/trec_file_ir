package edu.pitt.sis.infsci2140.analysis;

import java.util.regex.Pattern;

/**
 * TextTokenizer can split a sequence of text into individual word tokens.
 */
public class TextTokenizer {
	private String words[];
	private int pos = 0;
	private int size = 0;
	private Pattern regex_ws = Pattern.compile("\\s+");
	
	// YOU MUST IMPLEMENT THIS METHOD
	public TextTokenizer( char[] texts ) {
		// this constructor will tokenize the input texts (usually it is a char array for a whole document)
		String content = new String(texts);
		//words = content.split("\\s+");
		words = regex_ws.split(content);
		size = words.length;
	}
	
	// YOU MUST IMPLEMENT THIS METHOD
	public char[] nextWord() {
		// read and return the next word of the document; or return null if it is the end of the document
		char ret_v[] = null;
		
		if(pos < size)
		{
			ret_v = words[pos].toCharArray();
			pos++;
		}
		return ret_v;
	}
	
}
