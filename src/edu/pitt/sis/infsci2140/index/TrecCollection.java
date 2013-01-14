package edu.pitt.sis.infsci2140.index;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jmx.snmp.Timestamp;

public class TrecCollection {
	
	protected FileInputStream trec_fis = null;
	protected BufferedReader buffer_is = null;
	protected boolean is_doc_start = false;
	protected boolean is_tag_start = false;
	protected String full_cur_tag = "";
	protected String cur_tag_name = "";
	protected String end_tag_name = "";
	
	protected int doc_number = 0;
	
	protected final static String START_DOC_TAG = "<DOC>";
	protected final static String END_DOC_TAG = "</DOC>";
	protected final static String DOC_NO_TAG_NAME = "DOCNO";
	
	protected Pattern regex_lws = Pattern.compile("^\\s+");
	protected Pattern regex_tws = Pattern.compile("\\s+$");
	protected Pattern regex_html_tags = Pattern.compile("\\<.*?\\>");
	
	protected char block_data[];
	protected int BUFFER_SIZE = 64*1024;
	protected int block_size = 0;
	protected int block_pos = 0;
	
	/**
	 * @Constructor
	 */
	public TrecCollection(FileInputStream instream ) throws IOException {
		// This constructor should take an inputstream of the collection file as the input parameter.
		this.trec_fis = instream;
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(instream);
		this.buffer_is = new BufferedReader(new InputStreamReader(in));
	}
	
	/**
	 * Read through file to get the doc start tag value and set some states
	 * @method: get_doc_start_tag
	 * @param: boolean: is_buffer Read from buffer block or not
	 * @return: String: full tag
	 */
	protected String get_doc_start_tag(boolean is_buffer) throws IOException
	{
		String _val = "";
		char ch_content;
		int i_content;
		
		while(true)
		{
			if(is_buffer)
			{
				if(block_pos >= block_size) read_next_block();
				
				i_content = block_data[block_pos++];			
			}
			else
			{
				i_content= (char)buffer_is.read();
			}
			
			if(is_buffer && block_size == -1 ) return null;
			if(!is_buffer && i_content == -1) return null;
			
			ch_content = (char)i_content;
			// try to go far by 4 characters to check whether it's <DOC> tag or not(doc start tag)
			if(ch_content == '<')
			{
				_val += ch_content;
				for(int i = 0; i < START_DOC_TAG.length()-1; i++)
				{
					if(is_buffer)
					{
						if(block_pos >= block_size) read_next_block();
						
						ch_content = block_data[block_pos++];			
					}
					else
					{
						ch_content= (char)buffer_is.read();
					}
					_val += ch_content;
				}
				
				if(_val.compareToIgnoreCase(START_DOC_TAG) == 0)
				{
					is_doc_start = true;
					doc_number++;
					
					java.util.Date date= new java.util.Date();
					System.out.println("Start at " + new Timestamp(date.getTime()) + "to handle Document " + doc_number);
					
					return START_DOC_TAG;
				}
				else
				{
					// skip all reading content, go on looking for <DOC>
					_val = "";
				}
			}
		}
	}
	
	/**
	 * Read through file to get the whole tag value and set some states
	 * @method: get_tag
	 * @param: void
	 * @return: String
	 */
	protected String get_tag(boolean is_buffer) throws IOException
	{
		String _val = "<";
		
		char ch_content;
		if(is_buffer)
		{
			if(block_pos >= block_size) read_next_block();
			
			ch_content = block_data[block_pos++];
		}
		else
		{
			ch_content= (char)buffer_is.read();
		}
		
		while(true)
		{
			// full current tag with < and / or >
			_val += ch_content;
			
			// if next char is /, this may be a end tag
			if(ch_content == '/')
			{
				is_tag_start = false;
			}			
			
			// Read next char until encountering >
			if(is_buffer)
			{
				if(block_pos >= block_size) read_next_block();
				
				ch_content = block_data[block_pos++];			
			}
			else
			{
				ch_content= (char)buffer_is.read();
			}
			
			if(ch_content == '>')
			{
				_val += ch_content;
				break;
			}
		}
		
		return _val;
	}
	
	
	
	/**
	 * Read through file to get the whole tag value and set some states
	 * @method: get_tag_name
	 * @param: String: full_tag. Like <DOCNO>
	 * @return: String: tag name without <, > or /
	 */
	protected String get_tag_name(String full_tag)
	{
		String _val = full_tag;
		
		_val = _val.replaceAll("^</", "");
		_val = _val.replaceAll("^<", "");
		_val = _val.replaceAll(">$", "");
		
		return _val;
	}
	
	/**
	 * @Method: Remove the leading & trailing white spaces in a string [\r? \n \t  ]
	 * @param: str_val String
	 * @return: String: return string without leading and tailing white spaces
	 */
	protected String trim_string(String str_val)
	{
		String _val;
		
		//_val = str_val.replaceAll("^\\s+", "");
		//_val = _val.replaceAll("\\s+$", "");
		Matcher matcher_l = regex_lws.matcher(str_val);
		_val = matcher_l.replaceAll("");
		Matcher matcher_t = regex_tws.matcher(_val);
		_val = matcher_t.replaceAll("");
		
		return _val;
	}
	
	/**
	 * @method: Read a block from file
	 * @param: void
	 * @return: int: readed data size or -1 while encountering stream end
	 */
	protected int read_next_block() throws IOException
	{
		block_data = new char[BUFFER_SIZE];
		block_pos = 0;
		
		block_size = buffer_is.read(block_data);
		return block_size;
	}
}
