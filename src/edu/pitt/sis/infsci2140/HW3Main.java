package edu.pitt.sis.infsci2140;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import edu.pitt.sis.infsci2140.analysis.StopwordsRemover;
import edu.pitt.sis.infsci2140.index.MyIndexReader;
import edu.pitt.sis.infsci2140.search.MyRetrievalModel;
import edu.pitt.sis.infsci2140.search.SearchResult;
import edu.pitt.sis.infsci2140.search.Topic;

/**
 * !!! YOU CANNOT CHANGE ANYTHING IN THIS CLASS !!!
 * 
 * Main class for running your HW3.
 * 
 */
public class HW3Main {
	
	public static void main(String[] args) {
		
		if( args==null || args.length<2 ) {
			System.out.println("Usage:");
			System.out.println("  args[0]: path of the index's directory.");
			System.out.println("  args[1]: path of the topic file.");
			System.exit(0);
		}
		
		String path_dir = args[0];
		String path_topic = args[1];
		String path_stopwords = null;
		if(args.length > 2)
			path_stopwords = args[2];
		
		MyIndexReader ixreader = null;
		try {
			// Initiate the index reader ...
			ixreader = new MyIndexReader(path_dir);
		}catch(Exception e){
			System.out.println("ERROR: cannot initiate index directory.");
			e.printStackTrace();
		}
		
		FileInputStream instream_stopwords = null;
		StopwordsRemover stoprmv = null;
		
		try{
			// Loading the stopword list file and initiate the StopwordsRemover class
			if(path_stopwords != null)
			{
				instream_stopwords = new FileInputStream(path_stopwords);
				stoprmv = new StopwordsRemover(instream_stopwords);
			}
		}catch(IOException e){
			System.out.println("ERROR: cannot load stopwords file.");
			e.printStackTrace();
		}
		
		MyRetrievalModel model = new MyRetrievalModel().setIndex(ixreader);
		if(stoprmv != null) model.setStopwordsRemover(stoprmv);
		
		List<Topic> topics = Topic.parse( new File(path_topic) );
		
		if( topics!=null ) {
			for( Topic topic:topics ) {
				try{
					List<SearchResult> results = model.search(topic, 500);
					if( results!=null ) {
						int rank = 1;
						for( SearchResult result:results ){
							System.out.println(topic.topicId()+" Q0 "+result.docno()+" "+rank+" "+result.score()+" MYRUN");
							rank++;
						}
					}
				}catch(IOException e){
					System.err.println(" >> cannot read index ");
					e.printStackTrace();
				}
			}
		}
		
		try{
			ixreader.close();
		}catch(Exception e){}
		
	}
	
}
