package main;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import org.htmlparser.util.ParserException;

public class Spider {
	
	private RecordManager recman;
	
	private Indexer indexer;
	
	private FileStruc pageID;
	private int pageCount;
	
	public Spider(String recordmanager) throws IOException
	{
		// constructor, open the database, initialize Indexer
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		indexer = new Indexer(recman);
		
		pageID = new FileStruc(recman,"pageID");
		pageCount = pageID.getSize();
	}
	
	public void finalize() throws IOException
	{
		// save and close database
		recman.commit();
		recman.close();				
	}
	
	private String newPage(String url) throws IOException
	{
		// assign a new page id
		String id = String.format("%04d", pageCount);
		pageID.addEntry(url, id);
		pageCount++;
		
		return id;
	}
	
	private String getPageId(String url) throws IOException
	{
		// return page id
		return (String)pageID.getEntry(url);
	}
	
	public void excute(String startURL, int maxPage) throws IOException, ParserException
	{
		// crawl pages, save and calculate
		System.out.println("Started");
		
		Crawler crawler = null;
		UrlCon urlCon = null;
		
		String[] wordList = null;
		URL[] linkList = null;
		String pageId = null;
		String title = null;
		String url = null;
		
		Queue<String> q = new LinkedList<String>();
		q.add(startURL);		
		
		System.out.println("initialized");
		
		while(pageCount < maxPage && !q.isEmpty())
		{
			url = q.remove();
			
			pageId = getPageId(url);

			if(pageId != null) // do not crawl this crawled page again
				continue;
			
			pageId = newPage(url);
			
			System.out.println(pageId + " " + url);
			
			crawler = new Crawler(url); // crawl this page
			wordList = crawler.extractWords();			
			linkList = crawler.extractLinks();
			title = crawler.extractTitle();
						
			urlCon = new UrlCon(url); // get the last modification date and size

			indexer.buildBasicInfo(pageId, urlCon, title, wordList.length, linkList);
			
			indexer.buildFile(pageId, wordList,false);//body words
			
			indexer.buildFile(pageId, title.split("\\W+"), true);// title words
			
			for(URL s : linkList)
				q.add(s.toString());// crawl its children pages later
		}

		indexer.calculate(pageID);
		
		System.out.println("Finished");
	}
	
	public static void main(String[] arg) throws IOException, ParserException
	{
		String db = "final";
		//String startUrl = "http://www.cse.ust.hk/";
		String startUrl = "http://www.cse.ust.hk/~ericzhao/COMP4321/TestPages/testpage.htm";
		int maxPage = 500;
		
		long t1 = System.currentTimeMillis();
		
		Spider spider = new Spider(db);
		spider.excute(startUrl, maxPage);
		spider.finalize();
		
		long t2 = System.currentTimeMillis();
		
		System.out.println("Total Time : " + (t2 - t1)/1000.0 + " Seconds");
	}
}
