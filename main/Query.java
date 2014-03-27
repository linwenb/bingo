package main;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

public class Query {
	private StopStem stopStem;
	
	private RecordManager recman;
	
	private FileStruc titleInverted;
	private FileStruc bodyInverted;
	private FileStruc maxTf;
	private FileStruc wordID;
	private FileStruc phaseWeight;
	private FileStruc phaseTitle;
	
	private Hashtable<String, Double> score;
	private Hashtable<String, Double> weightScore;
	
	public Query(String db, StopStem st) throws IOException
	{
		stopStem = st;
		
		// open the database
		recman = RecordManagerFactory.createRecordManager(db);
		
		phaseWeight = new FileStruc(recman,"phaseWeight");
		phaseTitle = new FileStruc(recman,"phaseTitle");
		titleInverted = new FileStruc(recman,"titleInverted");
		bodyInverted = new FileStruc(recman,"bodyInverted");
		maxTf = new FileStruc(recman,"maxTf");
		wordID = new FileStruc(recman,"wordID");
		
		score = new Hashtable<String, Double> ();
		weightScore = new Hashtable<String, Double> ();
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Score> execute(String str) throws IOException
	{
		FileStruc resultBuffer = new FileStruc(recman,"resultBuffer");
		
		Vector<Score> vector = (Vector<Score>)resultBuffer.getEntry(str);
		
		if(vector != null)
			return vector;
		
		String temp;
		int index;
		while(str.contains("\""))
		{
			index = str.indexOf('"');
			temp = str.substring(0, index);
			str = str.substring(index+1);
			
			index = str.indexOf('"');
			phraseSearch(str.substring(0, index));
			
			str = temp + str.substring(index+1);
		}
		
		wordSearch(str);
		calculateCos();
	
		FileStruc pageRank = new FileStruc(recman,"pageRank");
		
		double PR;
		String pageId = null;
		
		vector = new Vector<Score>();
		Enumeration<String> iter = score.keys();
		
		while(iter.hasMoreElements())
		{
			pageId = iter.nextElement();
			PR = (Double)pageRank.getEntry(pageId);
			vector.add(new Score( pageId, score.get(pageId)+PR));
		}
		
		Collections.sort(vector);
		
		if(vector.size()>50)
		{
			Vector<Score> v = new Vector<Score>();
			for(int i = 0; i < 50; i++)
			{
				v.add(vector.elementAt(i));
			}
			resultBuffer.addEntry(str, v);
			return v;
		}
		else
		{
			resultBuffer.addEntry(str, vector);
			return vector;
		}
	}
	
	private void calculateCos() throws IOException
	{
		FileStruc docLength = new FileStruc(recman,"docLength");
		
		double length;
		Double result;
		String pageId = null;
		
		Enumeration<String> iter = weightScore.keys();
		while(iter.hasMoreElements())
		{
			pageId = iter.nextElement();
			length = (Double)docLength.getEntry(pageId);
			result = score.get(pageId);
			if(result == null)
				result = weightScore.get(pageId) / length;
			else
				result += weightScore.get(pageId) / length;
			score.put(pageId, result);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void wordSearch(String string) throws IOException
	{
		FileStruc wordWeight = new FileStruc(recman,"wordWeight");
		Hashtable<String, HashSet<Integer>> pageList = null;
		Hashtable<String, Double> weightTable = null;
		Enumeration<String> iter;
		
		String keyword = null;
		String wordId = null;
		String pageId = null;
		Double wordweight;
		
		for(String str : string.split("\\W+"))
		{
			if(stopStem.checkWord(str))
			{
				keyword = stopStem.stem(str);
				wordId = (String)wordID.getEntry(keyword);
				
				if(wordId != null)
				{
					weightTable = (Hashtable<String, Double>)wordWeight.getEntry(wordId);
					if(weightTable != null)
					{
						iter = weightTable.keys();
						while(iter.hasMoreElements())
						{
							pageId = iter.nextElement();
							
							wordweight = weightScore.get(pageId);
							if(wordweight == null)
								wordweight = weightTable.get(pageId);
							else
								wordweight += weightTable.get(pageId);
							weightScore.put(pageId, wordweight);
						}
					}
					
					pageList = (Hashtable<String, HashSet<Integer>>)titleInverted.getEntry(wordId);					
					if(pageList != null)
					{
						iter = pageList.keys();
						while(iter.hasMoreElements())
						{
							pageId = iter.nextElement();
							
							wordweight = score.get(pageId);
							if(wordweight == null)
								wordweight = 1.0;
							else
								wordweight += 1.0;
							score.put(pageId, wordweight);
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void phraseSearchHelper(String phrase, Vector<String> vector, FileStruc pw, FileStruc inverted, boolean titleOrBody) throws IOException
	{
		Hashtable<String, Double> table = (Hashtable<String, Double>) pw.getEntry(phrase);
		
		Enumeration<String> iter = null;
		
		Double weight;
		String pageId;
		
		if(table == null)
		{
			Hashtable<String, HashSet<Integer>> pageList = null;
			String wordId;
			int position;
			
			table = new Hashtable<String, Double>();
			
			wordId = (String)wordID.getEntry(vector.elementAt(0));
			pageList = (Hashtable<String, HashSet<Integer>>)inverted.getEntry(wordId);
	
			if(pageList != null)
			{
				HashSet<Integer> positionSet = null, tempPositionSet = null;
				Hashtable<String, HashSet<Integer>> temp = null;
				Vector<String> removePage = null;
				Vector<Integer> removePosition = null;
				Iterator<Integer> itt = null;
				
				for(int i = 1; i < vector.size(); i++)
				{
					wordId = (String)wordID.getEntry(vector.elementAt(i));
					temp = (Hashtable<String, HashSet<Integer>>)inverted.getEntry(wordId);
					if(temp == null)
					{
						pageList = null;
						break;
					}
			
					iter = temp.keys();
					removePage = new Vector<String>();
					while(iter.hasMoreElements())
					{
						pageId = iter.nextElement();
						positionSet = temp.get(pageId);
						tempPositionSet = pageList.get(pageId);
							
						if(tempPositionSet == null)
						{
							removePage.add(pageId);
							continue;
						}
						itt = positionSet.iterator();
						removePosition = new Vector<Integer>();
						while(itt.hasNext())
						{
							position = itt.next();
							if(!tempPositionSet.contains(position-1))
								removePosition.add(position);
						}

						for(int p : removePosition)
							positionSet.remove(p);
						temp.put(pageId, positionSet);
					}
					
					for(String p : removePage)
						temp.remove(p);
					
					pageList = temp;
				}
					
				if(pageList != null)
				{
					double maxtf;
					int df = pageList.size();
					iter = pageList.keys();
					while(iter.hasMoreElements())
					{
						pageId = iter.nextElement();
						positionSet = pageList.get(pageId);
						// tf / maxtf * log 2 (N / df)
						
						if(titleOrBody)
						{
							weight = 1.0 * positionSet.size();
						}
						else
						{
							maxtf = (Double)maxTf.getEntry(pageId);
							weight = positionSet.size() / maxtf * Math.log(400.0 / df);
						}
						table.put(pageId, weight);
					}
					pw.addEntry(phrase, table);
				}
			}
		}
		
		iter = table.keys();
		while(iter.hasMoreElements())
		{
			pageId = iter.nextElement();
			
			weight = score.get(pageId);
			if(weight == null)
				weight = table.get(pageId);
			else
				weight += table.get(pageId);
			score.put(pageId, weight);
		}
	}
	
	private void phraseSearch(String str) throws IOException
	{
		String keyword;
		String phrase = "";
		Vector<String> vector = new Vector<String>();
		for(String string : str.split("\\W+"))
		{
			if(stopStem.checkWord(string))
			{
				keyword = stopStem.stem(string);
				phrase += keyword;
				vector.add(keyword);
			}
		}
		
		if(phrase == "" || vector.size() <= 0)
			return;
		
		// phrase search in title
		phraseSearchHelper(phrase, vector, phaseTitle, titleInverted, true);
		
		// phrase search in body
		phraseSearchHelper(phrase, vector, phaseWeight, bodyInverted, false);
	}
	
	public void finalize() throws IOException
	{
		// close the database
		recman.commit();
		recman.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Started");
		
		StopStem st = new StopStem("stopwords.txt");
		Query test = new Query("final", st);
		
		long t1 = System.currentTimeMillis();
		
		Vector<Score> v = test.execute("hkust");
		
		long t2 = System.currentTimeMillis();
		
		test.finalize();
		System.out.println("Finished");		
		
		System.out.println("Total Time : " + (t2 - t1)/1000.0 + " Seconds");
	}
}
