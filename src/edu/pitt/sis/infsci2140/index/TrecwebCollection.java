package edu.pitt.sis.infsci2140.index;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import com.sun.jmx.snmp.Timestamp;

public class TrecwebCollection extends TrecCollection implements DocumentCollection {
	
	private boolean is_content_start = false;
	private final static String CONTENT_TAG = "</DOCHDR>";
	
	// YOU SHOULD IMPLEMENT THIS METHOD
	public TrecwebCollection( FileInputStream instream ) throws IOException {
		// This constructor should take an inputstream of the collection file as the input parameter.
		super(instream);
	}
	
	// YOU SHOULD IMPLEMENT THIS METHOD
	public Map<String, Object> nextDocument() throws IOException {
		// Read the definition of this method from edu.pitt.sis.infsci2140.index.DocumentCollection interface 
		// and follow the assignment instructions to implement this method.
		
		// Read char by char. Do not do it by readline for we can merge all the content 
		// into one line
		String tag_value = "";
		Map<String, Object> ret_v = null;
		
		// init some values
		is_tag_start = false;
		is_doc_start = false;
		is_content_start = false;
		
		if(block_pos >= block_size)
		{
			read_next_block();
		}
		if(block_size == -1)
		{
			return null;
		}
		
		while(block_size != -1)
		{
			// Try to go through to find the <DOC> tag first: doc start postion
			if(get_doc_start_tag(true) == null)
			{
				java.util.Date date= new java.util.Date();
				System.out.println("Finsiedh handing document at " + new Timestamp(date.getTime()));
				return null;	// Can't find any doc in this file
			}
			
			for(; block_pos < block_size; )
			{
				char ch_content = block_data[block_pos++];
			
				if(is_content_start)
				{
					if(ch_content == '<')
					{
						// to check it's a doc end tag or not
						Map<String, Object> _check_tag = is_end_doc_tag(true);
						Object[] _keys =  _check_tag.keySet().toArray();
						
						if((boolean)_check_tag.get((String)_keys[0]))
						{
							// end a doc
							is_doc_start = false;
							
							// put content to the return value
							if(ret_v == null)
							{ 
								ret_v = new HashMap<String, Object>();
							}
							
							// remove leading & tailing whiltesapces and newline [\n \t  ]
							tag_value = trim_string(tag_value);
							
							// remove all HTML tags
							//tag_value = tag_value.replaceAll("\\<.*?\\>", "");
							Matcher matcher_html = regex_html_tags.matcher(tag_value);
							tag_value = matcher_html.replaceAll("");
							
							ret_v.put("CONTENT", tag_value.toCharArray());
							
							// Reinit it
							tag_value = "";
							
							return ret_v;
						}
						else
						{
							tag_value += ch_content;
							tag_value += _keys[0];
						}
					}
					else
					{
						tag_value += ch_content;
					}
				}
				else
				{
					if(ch_content == '<')	// a tag started
					{
						is_tag_start = true;
											
						full_cur_tag = get_tag(true);
						String _tag_name = get_tag_name(full_cur_tag);
						if(is_tag_start)
						{
							cur_tag_name = _tag_name;
							tag_value = "";
						}
						else
						{
							end_tag_name = _tag_name;
						}
						
						if(full_cur_tag.compareToIgnoreCase(END_DOC_TAG) == 0)
						{
							// end a doc
							is_doc_start = false;
							return ret_v;
						}
						else
						{	
							if(!is_tag_start && !is_content_start
									&& cur_tag_name.compareToIgnoreCase(end_tag_name) == 0) // a tag ended
							{
								// For efficiency, only handle needed tags
								if(cur_tag_name.compareToIgnoreCase(DOC_NO_TAG_NAME) == 0 )
								{
									// remove leading & tailing whiltesapces and newline [\n \t  ]
									tag_value = trim_string(tag_value);
									
									if(ret_v == null)
									{ 
										ret_v = new HashMap<String, Object>();
									}
									
									ret_v.put(cur_tag_name, new String(tag_value));							
								}
								// Reinit it
								tag_value = "";
							}
							
							// check whether it's start of content </DOCHDR>
							if(full_cur_tag.compareToIgnoreCase(CONTENT_TAG) == 0)
							{
								// Content started
								is_content_start = true;
							}						
						}
					}
					else
					{
						tag_value += ch_content;
					}
				}
			}
			
			// Read all data in block_data, not meet the requirements, try to read next block
			read_next_block();
		}
		return ret_v;
	}
	
	/**
	 * @Method: Try to check if the tag is end doc or not </DOC>
	 * @param: boolean is_buffer
	 * @return: Map<String, Object>: characters readed while checking the tag
	 */
	private Map<String, Object> is_end_doc_tag(boolean is_buffer) throws IOException
	{
		Map<String, Object> _rv = new HashMap<String, Object>();
		String _v_tag = "<";
		String _read_v = "";
		
		for(int i = 0; i < END_DOC_TAG.length()-1; i++)
		{
			// Read 5 more extra characters to check it's end doc tag or not
			char _ch;
			if(is_buffer)
			{
				if(block_pos >= block_size) read_next_block();
				
				_ch = block_data[block_pos++];			
			}
			else
			{
				_ch = (char)buffer_is.read();
			}
			
			_read_v += _ch;
			
			if(_ch != '/' && i == 0) // Next one is not '/', just break
			{
				break;
			}
		}
		
		_v_tag = _v_tag + _read_v;
		boolean _is_end_doc = false;
		
		if(_v_tag.compareToIgnoreCase(END_DOC_TAG) == 0)
		{
			_is_end_doc = true;
		}
		
		_rv.put(_read_v, _is_end_doc);
			
		return _rv;
	}
	
}
