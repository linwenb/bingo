package main;
import java.net.URL;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.ParserException;

public class Crawler
{
	private String url;
	public Crawler(String _url)
	{
		url = _url;
	}
	
	public String extractTitle() throws ParserException
	{	
		// extract title in url and return it
		Parser parser = new Parser();
		parser.setURL(url);
		Node node = (Node)parser.extractAllNodesThatMatch(new TagNameFilter ("title")).elementAt(0);

		return node.toPlainTextString();
	}
	
	public String[] extractWords() throws ParserException
	{
		// extract words in url and return them
        StringBean sb = new StringBean ();
        sb.setLinks(false);
        sb.setURL (url);

		return sb.getStrings().split("\\W+");
	}
	
	public URL[] extractLinks() throws ParserException
	{
		// extract links in url and return them
	    LinkBean lb = new LinkBean();
	    lb.setURL(url);
	    
	    return lb.getLinks();
	}
	
	public static void main (String[] args)
	{
		try
		{
			//Crawler crawler = new Crawler("http://hi.baidu.com/newneo/item/b4297f37098791f0e7bb7a9a");
			Crawler crawler = new Crawler("http://www.cs.ust.hk/~dlee/4321/");

			String[] words = crawler.extractWords();		
			
			System.out.println("Words in "+ crawler.url + ":");
			for(int i = 0; i < words.length; i++)
				System.out.print(words[i]+"\n");
			System.out.println("\n\n");
	
			URL[] links = crawler.extractLinks();
			System.out.println("Links in "+crawler.url+":");
			for(int i = 0; i < links.length; i++)		
				System.out.println(links[i]);
			System.out.println("");
			
			System.out.println(crawler.extractTitle());
			System.out.println(words.length);
		}
		catch (ParserException e)
        {
			e.printStackTrace ();
        }
	}
}
	
