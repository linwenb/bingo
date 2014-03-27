package main;
import java.io.Serializable;
import java.net.URL;
import java.util.HashSet;
import java.util.Vector;

@SuppressWarnings("serial")
public class PageInfo implements Serializable
{
	// contains all information according to the output format requirement
	public String url;
	public String title;
	public String date;
	public int size;
	
	public URL[] children;
	public HashSet<String> parent;	
	public Vector<WordInfo> wordlist;
	
	public PageInfo(String _url, String _title, String _date, int _size, URL[] _children)
	{
		url = _url;
		title = _title;
		date = _date;
		size = _size;
		children = _children;
		
		parent = null;
		wordlist = new Vector<WordInfo>();
	}
}