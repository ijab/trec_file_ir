/**
 * Parse Trec Text or Web file. In order to handle a very large file, 
 * we use BufferedInputStream to read data from file instead of reading all data of the file 
 * into memory
 * File: TrecParser.java
 * Author: ijab(zhancaibao#gmail.com)
 * Date: 2013/01/14
 */
package edu.pitt.sis.infsci2140.index;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @class TrecParser
 * @@ A mini parser to parse TrecText and TrecWeb file
 *
 */
public class TrecParser {
	protected FileInputStream trec_fis = null;
	protected BufferedReader buffer_rd = null;
	
	protected Set<String> concerned_tags = new HashSet<String>();
	
	private static final String START_DOC_TAG = "<DOC>";
	private static final String END_DOC_TAG = "</DOC>";
	private static final String START_DOCHDR_TAG = "<DOCHDR>";
	private static final String END_DOCHDR_TAG = "</DOCHDR>";
	private static final String START_DOCNO_TAG = "<DOCNO>";
	private static final String END_DOCNO_TAG = "</DOCNO>";
	private static final String DOC_TAG_NAME = "DOC";
	private static final String DOC_NO_TAG_NAME = "DOCNO";
	private static final String DOC_CONTENT_TAG_NAME = "TEXT";
	private static final String HDR_TAG_NAME = "DOCHDR";
	private static final int BUFFER_SIZE = 8*1024;
	
	private enum STATE {OUTDOC, STARTDOC, STARTEL, INDOC, ENDDOC, ENDEL, STARTHDR, ENDHDR};
	private STATE state = STATE.OUTDOC;
	private STATE doc_state = STATE.OUTDOC;
	private long docs_count = 0;
	private String tag = "";
	private Map<String, Object> doc = null;

	// Debug related params
	private boolean DEBUG = false;
	private long start_time;
	private long end_time;
	
	/**
	 * @constructor: TrecParser
	 * @param : FileInputStream is
	 */
	public TrecParser(FileInputStream is)
	{
		this.trec_fis = is;
		this.buffer_rd = new BufferedReader(new InputStreamReader(this.trec_fis), BUFFER_SIZE);
		
		// concerned tags
		this.concerned_tags.add(DOC_NO_TAG_NAME);
		this.concerned_tags.add(DOC_CONTENT_TAG_NAME);
		
		start_time = System.currentTimeMillis();
		if(DEBUG)
		{
			System.out.println("Start handling trec file at " + current_datetime() );
		}
	}
	
	/**
	 * next_doc: Iterate the trec file to get doc
	 * @param void
	 * @return Map<String, Object> Doc content or null reaching the end of trec file
	 */
	public Map<String, Object> next_doc()
	{
		doc = null;
		
		parse();
		
		if(doc == null)
		{
			end_time = System.currentTimeMillis();
			if(DEBUG)
			{
				System.out.println("Finished handling " + docs_count + " docs in " 
								+ (end_time-start_time)/1000 + "s at " + current_datetime());
			}			
		}
		return doc;
	}
	
	/**
	 * parse: parse Trec text or web file
	 * @param void
	 * @return void
	 */
	private void parse()
	{
		int i_ch;
		ArrayList<Character> l_char = new ArrayList<Character>();
		
		try
		{
			while((i_ch=buffer_rd.read()) != -1)
			{
				char ch = (char)i_ch;
				if(ch == '<')
				{
					parse_tag();
					if(doc_state != STATE.INDOC)
					{
						// If we don't handle the tag in a doc, it should be malformted
						state = STATE.OUTDOC;
						l_char.clear();
						continue;
					}
					else
					{
						if(state == STATE.ENDDOC)
						{						
							// If after parsing tag, reaching </DOC>
							// just break and return the doc
							state = STATE.OUTDOC;
							break;
						}
						else if(state == STATE.ENDHDR)
						{
							// parse web content
							parse_web_value();
							break;
						}
						else if(state == STATE.ENDEL)
						{
							if(concerned_tags.contains(tag))
							{
								if(tag.compareToIgnoreCase(DOC_CONTENT_TAG_NAME) == 0)
									set_value("CONTENT", list_to_char_array(l_char));
								else
								{
									String _v_of_tag = new String(list_to_char_array(l_char));
									if(tag.compareToIgnoreCase(DOC_NO_TAG_NAME) == 0)
									{
										// Trim the value
										_v_of_tag = _v_of_tag.trim();
										_v_of_tag = _v_of_tag.replaceAll("\r\n|\n|\r", "");
									}
									set_value(tag, _v_of_tag);
								}
							}
							l_char.clear();
						}
					}
				}				
				else
				{
					if(state == STATE.OUTDOC || state == STATE.ENDDOC)
					{
						// If current state is OUTODC and not encounter a < char,
						// just skip it
						continue;
					}
					else
					{
						l_char.add(ch);
					}
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * parse_tag(): While encountering a < and try to figure out what tag it is
	 * @param void
	 * @return void
	 */
	private void parse_tag() throws IOException
	{
		int i_ch = 0;
		String _tag = "";
		
		if(state != STATE.ENDHDR)
		{
			// If last state is not ENDHDR - </DOCHDR>
			// set default state to STARTEL while encountering character <
			state = STATE.STARTEL;
		}
		
		while((i_ch = buffer_rd.read()) != -1)
		{
			char ch = (char)i_ch;

			if(ch == '/')
			{
				state = STATE.ENDEL;
			}
			else if(ch == '>')
			{
				tag = _tag;
				
				if(_tag.compareToIgnoreCase(DOC_TAG_NAME) == 0)
				{
					if(state == STATE.ENDEL)
					{
						state = STATE.ENDDOC;
					}
					else
					{
						state = STATE.STARTDOC;
						doc_state = STATE.INDOC;
						
						docs_count++;
						if(DEBUG == true)
						{
							System.out.println(current_datetime() + " Handle document " + docs_count);							
						}						
					}
				}
				else if(_tag.compareToIgnoreCase(HDR_TAG_NAME) == 0)
				{
					if(state == STATE.ENDEL)
						state = STATE.ENDHDR;
					else
						state = STATE.STARTHDR;
				}				
				break;
			}
			else
			{
				_tag += ch;
			}
		}
	}
	
	
	/**
	 * parse_web_value: When the state is ENDHDR, try to get the value
	 *  until reaching the end of tag or doc
	 * @param void
	 * @return void
	 */
	private void parse_web_value() throws IOException
	{
		STATE html_state = STATE.OUTDOC;
		String _tag = "";
		int i_ch = 0;
		char ch;
		ArrayList<Character> l_char = new ArrayList<Character>();
		
		if(state != STATE.ENDHDR) return;
		
		// It's the content in TrecWeb, we just skip all contents between <>
		while((i_ch=buffer_rd.read()) != -1)
		{
			ch = (char)i_ch;
			
			if(ch == '<')
			{
				html_state = STATE.STARTEL;
				_tag += ch;
			}
			else if(ch == '/')
			{
				_tag += ch;
			}
			else if(ch == '>')
			{
				_tag += ch;
				if(html_state == STATE.STARTEL) 
				{
					if(_tag.compareToIgnoreCase(END_DOC_TAG) == 0)
					{
						state = STATE.ENDDOC;
						doc_state = STATE.OUTDOC;
						set_value("CONTENT", list_to_char_array(l_char));
						break;
					}
					else
					{
						html_state = STATE.ENDEL;
						_tag = "";
					}
				}
			}
			else
			{
				if(html_state == STATE.STARTEL)
				{
					// just skip html tag
					if(ch == 'D' || ch == 'O' || ch == 'C' )
						_tag += ch;
					continue;
				}
				else
				{
					l_char.add(ch);
				}
			}
		}
	}
	
	/**
	 * set_value: set doc's value of specified tag
	 * @param String key: tag name as key;
	 * @param Object v: value of the tag
	 * @return void
	 */
	private void set_value(String key, Object v)
	{
		if(doc == null)
		{
			doc = new HashMap<String, Object>();
		}
		doc.put(key, v);
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// Helper functions
	//////////////////////////////////////////////////////////////////////////////////
	/**
	 * current_datetime : get current data time as a String
	 * @param void
	 * @return String
	 */
	private String current_datetime()
	{
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	    return sdf.format(cal.getTime());
	}
	
	/**
	 * list_to_char_array: Convert from ArrayList<Character> to char[] array
	 * @param ArrayList<Character> list_char
	 * @return char[]
	 */
	private char[] list_to_char_array(ArrayList<Character> list_char)
	{
		int _size = list_char.size();
		Character _v[] = new Character[_size];
		char _ret_v[] = new char[_size];
	
		list_char.toArray(_v);
		
		for(int i=0; i<_size; i++)
		{
			_ret_v[i] = _v[i].charValue();
		}
		return _ret_v;
	}
}
