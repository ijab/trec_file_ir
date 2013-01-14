package edu.pitt.sis.infsci2140.index;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.sun.jmx.snmp.Timestamp;

public class TrectextCollection extends TrecCollection implements DocumentCollection {
	
	private final static String CONTENT_TAG_NAME = "TEXT";
	
	
	// YOU SHOULD IMPLEMENT THIS METHOD
	public TrectextCollection( FileInputStream instream ) throws IOException {
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
						if(!is_tag_start && cur_tag_name.compareToIgnoreCase(end_tag_name) == 0) // a tag ended
						{
							if(ret_v == null)
							{ 
								ret_v = new HashMap<String, Object>();
							}
							
							// For efficiency, only handle needed tags
							if(cur_tag_name.compareToIgnoreCase(DOC_NO_TAG_NAME) == 0 
								|| cur_tag_name.compareToIgnoreCase(CONTENT_TAG_NAME) == 0)
							{
								// remove leading & tailing whiltesapces and newline [\r?\n \t  ]
								tag_value = trim_string(tag_value);
								
								if(cur_tag_name.compareToIgnoreCase(CONTENT_TAG_NAME) == 0)
								{
									ret_v.put("CONTENT", tag_value.toCharArray());
								}
								else
								{
									ret_v.put(cur_tag_name, new String(tag_value));
								}
							}
							// Reinit it
							tag_value = "";
						}
					}
				}
				else
				{
					tag_value += ch_content;
				}
			}
			
			// Read all data in block_data, not meet the requirements, try to read next block
			read_next_block();
		}
		return ret_v;
	}	
}
