package main;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class Indexer {

	private StopStem stopStem;
	
	private RecordManager recman;
	private FileStruc titleInverted;
	private FileStruc bodyInverted;
	private FileStruc wordWeight;
	private FileStruc pageInfo;
	private FileStruc wordID;
	private FileStruc word;
	
	private int wordCount; 
	
	public Indexer(RecordManager _recman) throws IOException
	{
		// constructor, initialize the stopstem and six dababase
		stopStem = new StopStem("stopwords.txt");		
		
		recman = _recman;
		titleInverted = new FileStruc(recman,"titleInverted");
		bodyInverted = new FileStruc(recman,"bodyInverted");
		wordWeight = new FileStruc(recman,"wordWeight");
		pageInfo = new FileStruc(recman,"pageInfo");
		wordID = new FileStruc(recman,"wordID");
		word = new FileStruc(recman,"word");

		wordCount = wordID.getSize();
	}
	
	public long getRecordDate(String pageid) throws IOException
	{
		// return the recorded last modification date
		return 0;
	}
	
	private String newWord(String keyword) throws IOException
	{
		// assign a new word id for the keyword
		String id = String.format("%06d", wordCount);
		wordID.addEntry(keyword, id);
		word.addEntry(id, keyword);
		wordCount++;
		return id;
	}

	private String getWordId(String keyword) throws IOException
	{
		// return the word id from mapping table 
		String id = (String)wordID.getEntry(keyword);
		
		if(id == null)
			id = newWord(keyword);
		
		return id;
	}
	
	@SuppressWarnings("unchecked")
	private void insertIntoInverted(FileStruc file, String pageId, Hashtable<String, HashSet<Integer>> hash) throws IOException
	{
		// insert all contents in the hash table into inverted index files
		Enumeration<String> en = hash.keys();
		Hashtable<String, HashSet<Integer>> table = null;
		
		String keyword = null;
		String wordId = null;
		
		while(en.hasMoreElements())
		{
			keyword = en.nextElement();
			
			wordId = getWordId(keyword);
			
			table = (Hashtable<String, HashSet<Integer>>)file.getEntry(wordId);
			if(table == null)
				table = new Hashtable<String, HashSet<Integer>>();
			
			table.put(pageId, hash.get(keyword));
			file.addEntry(wordId, table);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void insertWordFreq(String pageId, Hashtable<String, HashSet<Integer>> hashtable) throws IOException
	{
		// insert the word frequency contents into the page information
		String keyword = null;
		HashSet<Integer> positionSet = null;
		Enumeration<String> en = hashtable.keys();
		Vector<WordInfo> vector = new Vector<WordInfo>();
					
		while(en.hasMoreElements())
		{
			keyword = en.nextElement();

			positionSet = hashtable.get(keyword);
			vector.add(new WordInfo(keyword, positionSet.size()));
		}
					
		Collections.sort(vector);
		/*
		for(int i = 0; i<5 && i< vector.size();i++)
		{
			System.out.print(vector.elementAt(i).freq+" ");
		}
		System.out.println(" ");
		*/
		PageInfo p = (PageInfo)pageInfo.getEntry(pageId);
		p.wordlist = vector;
		pageInfo.addEntry(pageId, p);
	}
	
	public void buildFile(String pageId, String[] wordList, boolean titleOrBody) throws IOException
	{
		// organize all words, and then insert into database
		Hashtable<String, HashSet<Integer>> hashtable = new Hashtable<String, HashSet<Integer>>();
		HashSet<Integer> positionSet = null;
		int position = 0;
		String keyword = null;
		
		for(String str : wordList)
		{
			if(stopStem.checkWord(str))
			{
				keyword = stopStem.stem(str);
			
				positionSet = hashtable.get(keyword);
				if(positionSet == null)
					positionSet = new HashSet<Integer>();		
				
				positionSet.add(position);
				hashtable.put(keyword, positionSet);
				
				position++;
			}
		}
		
		if(titleOrBody)
		{
			// title words
			insertIntoInverted(titleInverted, pageId, hashtable);
		}
		else
		{
			// body words
			insertIntoInverted(bodyInverted, pageId, hashtable);
			insertWordFreq(pageId, hashtable);
		}
	}
	
	public void buildBasicInfo(String pageId, UrlCon urlCon, String title, int size, URL[] v) throws IOException
	{
		// store all basic information of each page
		int s;
		if(urlCon.length < 0)
			s = size;
		else
			s = urlCon.length;
		
		PageInfo p = new PageInfo(urlCon.url, title, urlCon.date, s, v);
		
		pageInfo.addEntry(pageId, p);
	}
	
	public void calculate(FileStruc pageID) throws IOException
	{
		// do calculations after inverted index files are ready
		System.out.println("Start match parent url");
		getParent();
		System.out.println("Start calculate word weight");
		getWeight();
		System.out.println("Start sort top words");
		getTopWords();
		System.out.println("Start page ranking");
		getPageRank(pageID);
	}
	
	@SuppressWarnings("unchecked")
	private void getTopWords() throws IOException {
		HTree hash = wordWeight.getHash();
		Hashtable<String, Double> table = null;
		Enumeration<String> enu = null;
		String wordId,pageId;
		Vector<Score> vector = new Vector<Score>();
		FastIterator iter = hash.keys();
		double score;
		while( (wordId=(String)iter.next()) != null)
		{
			table = (Hashtable<String, Double>)wordWeight.getEntry(wordId);
			score = 0;
			enu = table.keys();
			while(enu.hasMoreElements())
			{
				pageId = enu.nextElement();
				score += table.get(pageId);
			}
			vector.add(new Score(wordId, score));
		}
		Collections.sort(vector);
		
		String key;
		Vector<String> v = new Vector<String>();
		int c = 0;
		for(int i = 0 ; i < vector.size(); i++)
		{
			key = (String)word.getEntry( vector.elementAt(i).Id);
			if(key.length() < 4 && key.charAt(0)<'a')
				continue;
			if(!stopStem.isStopWord(key))
			{
				v.add(key);
				c++;
			}
			if(c > 500)
				break;
		}
		Collections.sort(v);
		
		FileStruc topWords = new FileStruc(recman,"topWords");

		String id;
		for(int i = 0 ; i < v.size(); i++)
		{
			id = String.format("%03d", i);
			topWords.addEntry(id, v.elementAt(i));
		}
	}

	private void getPageRank(FileStruc pageID) throws IOException
	{
		Hashtable<String, Vector<String>> parentUrl = new Hashtable<String, Vector<String>>();
		Hashtable<String, Vector<String>> childUrl = new Hashtable<String, Vector<String>>();
		Hashtable<String, Double> hub = new Hashtable<String, Double>();
		Hashtable<String, Double> authority = new Hashtable<String, Double>();
		
		Vector<String> vector;
		Vector<String> pageList = new Vector<String>();
		
		HTree hash = pageInfo.getHash();

		String pageId = null;
		String id = null;
		PageInfo element = null;
		
		FastIterator iter = hash.keys();
		while( (pageId=(String)iter.next()) != null)
		{
			pageList.add(pageId);
			
			hub.put(pageId, 1.0);
			authority.put(pageId, 1.0);
			
			element = (PageInfo)hash.get(pageId);
			vector = new Vector<String>();
			for(URL url : element.children)
			{
				id = (String)pageID.getEntry(url.toString());
				if(id != null)
					vector.add(id);
			}
			childUrl.put(pageId, vector);
			
			vector = new Vector<String>();
			for(String url : element.parent)
			{
				id = (String)pageID.getEntry(url);
				if(id != null)
					vector.add(id);
			}
			parentUrl.put(pageId, vector);
		}
		
		Hashtable<String, Double> temp_hub = new Hashtable<String, Double>();;
		Hashtable<String, Double> temp_authority = new Hashtable<String, Double>();
		
		Double score,total_h,total_a;
		for(int i = 0; i < 20; i++)
		{
			total_a = 0.0;
			total_h = 0.0;
			for(String page : pageList)
			{
				score = 0.0;
				vector = parentUrl.get(page);
				for(String parent : vector)
				{
					score += hub.get(parent);
				}
				total_a += Math.pow(score, 2.0);
				temp_authority.put(page, score);
				
				score = 0.0;
				vector = childUrl.get(page);
				for(String child : vector)
				{
					score += authority.get(child);
				}
				total_h += Math.pow(score, 2.0);
				temp_hub.put(page, score);
			}
			
			total_a = Math.sqrt(total_a);
			total_h = Math.sqrt(total_h);
			
			for(String page : pageList)
			{
				score = temp_authority.get(page) / total_a;
				authority.put(page, score);
				
				score = temp_hub.get(page) / total_h;
				hub.put(page, score);
			}
		}

		FileStruc pageRank = new FileStruc(recman,"pageRank");
		for(String page : pageList)
		{
			pageRank.addEntry(page, authority.get(page) + hub.get(page) );
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getWeight() throws IOException
	{
		// calculate the word weight for each page
		FileStruc docLength = new FileStruc(recman,"docLength");
		FileStruc maxTf = new FileStruc(recman,"maxTf");
		
		Hashtable<String, Double> hashtable = null;
		HTree hash = pageInfo.getHash();
		
		int df;
		double weight,maxtf,dLength;
		String pageId = null;
		String wordId = null;
		PageInfo element = null;
		
		FastIterator iter = hash.keys();
		while( (pageId=(String)iter.next()) != null)
		{
			dLength = 0.0;
			
			element = (PageInfo)hash.get(pageId);
			maxtf = (element.wordlist).elementAt(0).freq;
						
			for(WordInfo wi : element.wordlist)
			{
				wordId = (String)wordID.getEntry(wi.word);
				df = ((Hashtable)bodyInverted.getEntry(wordId)).size();
				
				// tf / maxtf * log 2 (N / df)
				weight = wi.freq / maxtf * Math.log(400.0 / df);
				dLength += weight * weight;
				
				hashtable =(Hashtable<String, Double>)wordWeight.getEntry(wordId);
				if(hashtable == null)
					hashtable = new Hashtable<String, Double>();
				
				hashtable.put(pageId, weight);
				wordWeight.addEntry(wordId, hashtable);
			}
			
			maxTf.addEntry(pageId, maxtf);
			docLength.addEntry(pageId, Math.sqrt(dLength));
		}
	}
	
	private void getParent() throws IOException
	{
		// match parent url for each page
		Hashtable<String, HashSet<String>> hashtable = new Hashtable<String, HashSet<String>>();
		HTree hash = pageInfo.getHash();
		HashSet<String> parentSet = null;
		
		String child = null;
		String pageId = null;
		String sourceUrl = null;
		PageInfo element = null;
		
		FastIterator iter = hash.keys();
		while( (pageId=(String)iter.next()) != null)
		{
			element = (PageInfo)hash.get(pageId);
			sourceUrl = element.url;			
			for(URL childUrl : element.children)
			{
				child = childUrl.toString();
				parentSet = hashtable.get(child);
				if(parentSet == null)
					parentSet = new HashSet<String>();
				parentSet.add(sourceUrl);
				hashtable.put(child, parentSet);
			}
		}
		
		iter = hash.keys();
		while( (pageId=(String)iter.next()) != null)
		{
			element = (PageInfo)hash.get(pageId);
			sourceUrl = element.url;			
			element.parent = hashtable.get(sourceUrl);
			hash.put(pageId, element);
		}
	}
}
