package main;
import IRUtilities.*;
import java.io.*;
import java.util.HashSet;

public class StopStem
{
	private Porter porter;
	private HashSet<String> stopWords;
	
	public boolean isStopWord(String str)
	{
		return stopWords.contains(str);	
	}
	
	@SuppressWarnings("deprecation")
	public StopStem(String fileName)
	{
		super();
		porter = new Porter();
		stopWords = new HashSet<String>();

		// read stop words
		File file = new File(fileName);
	    FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;

	    try{
	    	fis = new FileInputStream(file);
	    	bis = new BufferedInputStream(fis);
	    	dis = new DataInputStream(bis);

	    	while (dis.available() != 0){
	    		stopWords.add(dis.readLine());
	    	}

	    	fis.close();
	    	bis.close();
	    	dis.close();
	    }
	    catch(FileNotFoundException e){
	    	e.printStackTrace();
	    }
	    catch(IOException e){
	    	e.printStackTrace();
	    }
	}
	
	public boolean checkWord(String keyword)
	{
		// return false if the word is empty or stopword
		if(keyword == null || keyword.equals(""))
			return false;
		if(isStopWord(keyword))
			return false;
		if(stem(keyword).equals(""))
			return false;
		
		return true;
	}
	
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}
	
	public static void main(String[] arg)
	{
		StopStem stopStem = new StopStem("stopwords.txt");
		String input="";
		try{
			do
			{
				System.out.print("Please enter a single English word: ");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				input = in.readLine();
				if(input.length()>0)
				{	
					if (stopStem.isStopWord(input))
						System.out.println("It should be stopped");
					else
			   			System.out.println("The stem of it is \"" + stopStem.stem(input)+"\"");
				}
			}
			while(input.length()>0);
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
	}
}
