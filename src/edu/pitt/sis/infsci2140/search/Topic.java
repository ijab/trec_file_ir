package edu.pitt.sis.infsci2140.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.pitt.sis.infsci2140.index.TrecParser;

/**
 * A class that stores topic information.
 */
public class Topic {
	
	private Map<String, Object> t = null;
	private File topic_f = null;
	
	/**
	 * Constructor of Topic class
	 */
	public Topic(Map<String, Object> _t)
	{
		this.t = _t;
	}
	
	/**
	 * topicId : Get topic's ID
	 * @return
	 */
	public String topicId() {
		// you should implement this method
		
		// Use Topic number as topic ID
		String topic_id = null;		
		String str_t_num = null;
		
		if(this.t != null)
		{
			str_t_num = (String)this.t.get("NUM");
			if(str_t_num == null || str_t_num.isEmpty())
				return topic_id;
			
			int ix = str_t_num.indexOf(':');
			if(ix != -1)
			{
				topic_id = str_t_num.substring(ix+1);
				// Trim the value
				topic_id = topic_id.trim();
				topic_id = topic_id.replaceAll("\r\n|\n|\r", "");
			}
		}
		return topic_id;
	}
	
	/**
	 * setTopicContent : set parsed content to Topic object
	 * @param _t
	 */
	public void setTopic(Map<String, Object> _t)
	{
		this.t = _t;
	}
	
	/**
	 * getValueByTag : get value by tag name
	 * @param String _tag
	 */
	public String getValueByTag(String _tag)
	{
		String _v = null;
		
		if(this.t != null)
		{
			_v = (String)this.t.get(_tag.toUpperCase());
		}
		
		// Trim and remove \n\r in the end
		_v = _v.trim();
		
		int r_ix = _v.length() - 1;
		if(r_ix > -1)
		{
			char _last_c = _v.charAt(r_ix);
			while(_last_c == '\n' || _last_c == '\r')
			{
				_v = _v.substring(0, r_ix);
				r_ix = _v.length() - 1;
				if(r_ix < 0) break;
				_last_c = _v.charAt(r_ix);
			}
		}
		
		return _v;
	}
	
	/**
	 * set_result_file
	 */
	public void set_result_file(String file_path)
	{
		if(this.t == null) return;
		
		this.t.put("RESULT_FILE", file_path);
	}
	
	/**
	 * 
	 */
	public String get_result_file()
	{
		if(this.t == null) return null;
		
		return (String)this.t.get("RESULT_FILE");
	}
	
	/**
	 * Parse a list of TREC topics from the provided f.
	 * 
	 * @param f
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static List<Topic> parse( File f ){
		// you should implement this method
		List<Topic> topics = null;
		
		// Add tags needed to be parsed
		Set<String> _tags = new HashSet<String>();
		_tags.add("NUM");
		_tags.add("TITLE");
		_tags.add("DESC");
		_tags.add("NARR");
		
		try
		{
			TrecParser topic_parser = new TrecParser(new FileInputStream(f), _tags);
		
			// Parse topics file
			Map<String, Object> _t = null;
			while((_t = topic_parser.next_topic()) != null)
			{
				if(topics == null)
					topics = new ArrayList<Topic>();
				
				Topic _topic = new Topic(_t);
				// Set result file full path name
				_topic.set_result_file(f.getParent() + File.separator + _topic.topicId() + ".rst");
				topics.add(_topic);
			}
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return topics;
	}
	
}
