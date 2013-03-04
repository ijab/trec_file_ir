package edu.pitt.sis.infsci2140.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TextTokenizer can split a sequence of text into individual word tokens.
 */
public class TextTokenizer {
	private List<String> words = new ArrayList<String>();
	private int pos = 0;
	private int size = 0;
	private Pattern regex_ws = Pattern.compile("\\s+");
	
	// YOU MUST IMPLEMENT THIS METHOD
	public TextTokenizer( char[] texts ) {
		// this constructor will tokenize the input texts (usually it is a char array for a whole document)
		List<Character> a_word = new ArrayList<Character>();
		int ix = 0;
		int len = texts.length;
		
		for(; ix < len; ++ix)
		{
			if(Character.isAlphabetic(texts[ix]) || texts[ix] == '-')
			{
				a_word.add(texts[ix]);
			}
			else
			{
				if(a_word.size() > 0)
				{
					String w = "";
					for(int j = 0; j < a_word.size(); ++j)
						w += a_word.get(j);
					
					words.add(w);
					a_word.clear();
				}
			}
		}
		
		size = words.size();
	}
	
	// YOU MUST IMPLEMENT THIS METHOD
	public char[] nextWord() {
		// read and return the next word of the document; or return null if it is the end of the document
		char ret_v[] = null;
		
		if(pos < size)
		{
			ret_v = words.get(pos).toCharArray();
			pos++;
		}
		return ret_v;
	}
	
}
