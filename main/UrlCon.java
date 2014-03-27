package main;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

@SuppressWarnings("serial")
public class UrlCon implements Serializable
{
	public String url;
	public String date;
	public int length;
	
	public UrlCon(String _url) throws IOException
	{
		url = _url;
		
		URL connectionURL = new URL(_url);
		URLConnection connection = connectionURL.openConnection();
		
		long milliseconds = connection.getLastModified();
		
		if(milliseconds == 0) // if page does not contain Last Modified date
			milliseconds = connection.getDate();
		
		length = connection.getContentLength();
		
		/*
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		Date resultdate = new Date(milliseconds);
		date = sdf.format(resultdate);*/
		
		date = new Date(milliseconds).toString();
	}
}