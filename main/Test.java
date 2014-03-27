package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class Test
{
	private RecordManager recman;
	
	private FileStruc bodyInverted;
	private FileStruc wordWeight;
	private FileStruc pageInfo;
	private FileStruc topWords;
	
	public Test(String db) throws IOException
	{
		// open the database
		recman = RecordManagerFactory.createRecordManager(db);
		
		topWords = new FileStruc(recman,"topWords");
		pageInfo = new FileStruc(recman,"pageInfo");
		wordWeight = new FileStruc(recman,"wordWeight");
		bodyInverted = new FileStruc(recman,"bodyInverted");
	}
	
	public void finalize() throws IOException
	{
		// close the database
		recman.commit();
		recman.close();
	}
	
	@SuppressWarnings("unchecked")
	public void printBodyInverted(String fileName) throws IOException
	{
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);

		HTree hashtable = bodyInverted.getHash();
		
		Hashtable<String, HashSet<Integer>> table = null;
		FastIterator iter = hashtable.keys();
		Enumeration<String> en = null;
		String pageId = null;
		String keyword = null;
		
		while( (keyword=(String)iter.next()) != null)
		{ 			
			out.write(keyword + "\r\n");
			table = (Hashtable<String, HashSet<Integer>>)hashtable.get(keyword);
			en = table.keys();
			while(en.hasMoreElements())
			{
				pageId = en.nextElement();
				out.write(pageId + " : ");
				for(int i : table.get(pageId))
				{
					out.write(" " + i);
				}
				out.write("\r\n");
			}
		}
		out.close();
	}
	
	@SuppressWarnings("unchecked")
	public void printWordWeight(String fileName) throws IOException
	{
		// print each page's words and weights
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);

		Hashtable<String, Double> wordhash = null;
		HTree hashtable = wordWeight.getHash();
		
		FastIterator iter = hashtable.keys();
		Enumeration<String> en = null;

		String pageId = null;
		String keyword = null;

		while( (pageId=(String)iter.next()) != null)
		{ 			
			out.write(pageId + "\r\n");
			wordhash = (Hashtable<String, Double>)hashtable.get(pageId);
			en = wordhash.keys();
			while(en.hasMoreElements())
			{
				keyword = en.nextElement();
				out.write( keyword + " " + wordhash.get(keyword) + " ");
			}
			out.write("\r\n------------------------------------------------------------------------------------\r\n");
		}
		
		out.close();
	}

	public void printPageInfo(String fileName) throws IOException
	{
		//
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);

		HTree hashtable = pageInfo.getHash();		
		FastIterator iter = hashtable.keys();
		String pageId = null;

		boolean first = true;
		boolean no1;
		int size;
		
		PageInfo element = null;
		WordInfo keyword = null;
		
		while( (pageId=(String)iter.next()) != null)
		{
			element = (PageInfo)pageInfo.getEntry(pageId);
			
			if(first)
				first = false;
			else
				out.write("----------------------------------------------------------------\r\n");
			
			out.write(element.title + "\r\n");
			out.write(element.url + "\r\n");
			out.write("Page Date: " + element.date + ", Page Size: " + element.size + "\r\n");
			
			size = element.wordlist.size();
			no1 = true;
			for(int i = 0 ; i < 5 && i < size ; i++)
			{
				if(no1)
					no1 = false;
				else
					out.write("; ");
				
				keyword = element.wordlist.elementAt(i);
				out.write(keyword.word + " " + keyword.freq );
			}			
			out.write("\r\n");
			
			out.write("Parent link\r\n");
			for(String url : element.parent)
			{
				out.write("\t" + url + "\r\n");
			}
			
			out.write("Child link\r\n");
			for(URL url : element.children)
			{
				out.write("\t" + url.toString() + "\r\n");
			}
		}

		out.close();
	}
	
	public void printAllWords(String fileName) throws IOException
	{
		// print all words, stored in database
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);

		HTree hashtable = topWords.getHash();		
		FastIterator iter = hashtable.keys();
		String keyword = null;
		
		while( (keyword=(String)iter.next()) != null)
		{
			out.write(topWords.getEntry(keyword) + "\r\n");
		}
		out.close();
	}

	public static void main(String[] args) throws IOException
	{
		System.out.println("Started");
		
		Test test = new Test("final");
		//test.printPageInfo("spider_result.txt");
		test.printAllWords("top_words.txt");
		//test.printWordWeight("word_weight.txt");
		//test.printBodyInverted("body_inverted.txt");
		test.finalize();
		
		System.out.println("Finished");
	}
}
