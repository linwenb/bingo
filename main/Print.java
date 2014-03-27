package main;

import java.io.IOException;
import java.util.Vector;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class Print
{
	private RecordManager recman;
	
	private FileStruc pageInfo;
	private FileStruc topWords;
	
	public Print(String db) throws IOException
	{
		// open the database
		recman = RecordManagerFactory.createRecordManager(db);
		
		topWords = new FileStruc(recman,"topWords");
		pageInfo = new FileStruc(recman,"pageInfo");
	}
	
	public void finalize() throws IOException
	{
		// close the database
		recman.commit();
		recman.close();
	}

	public Vector<PageInfo> getPageInfo(Vector<Score> vector) throws IOException
	{
		Vector<PageInfo> v = new Vector<PageInfo>();
		
		for(Score eachPage : vector)
			v.add((PageInfo)pageInfo.getEntry(eachPage.Id));
		
		return v;
	}
	
	public Vector<String> getTopWords() throws IOException
	{
		HTree hashtable = topWords.getHash();		
		FastIterator iter = hashtable.keys();
		String keyword = null;
		Vector<String> v = new Vector<String>();
		while( (keyword=(String)iter.next()) != null)
			v.add((String)topWords.getEntry(keyword));
		
		return v;
	}

	public static void main(String[] args) throws IOException
	{
		System.out.println("Started");
		
		Print test = new Print("final");
		
		Vector<PageInfo> v1= test.getPageInfo(new Vector<Score>());
		Vector<String> v2 = test.getTopWords();
		test.finalize();

		System.out.println("Finished");
	}
}
