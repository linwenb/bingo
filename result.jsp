<html>
<head>
<title>Bingo</title>
<link rel="icon" href="icon.png"/>
<script src="jquery.min.js"></script>
<script>
	jQuery(document).ready(
	function() 
    {
  		jQuery(".content").hide();
  		//toggle the componenet with class msg_body
  		jQuery(".link").click(
  			function()
  			{
  			  jQuery(this).next(".content").slideToggle();
 			}
 		);
	}
);</script>
<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
<%@ page import="pt.tumba.spell.*" %>
<%@ page import = "java.util.Vector" %>
<%@ page import = "main.*"%>
<%@ page import = "java.net.URL" %>

<%@ include file="header.html" %>

<%
String input = "";

String topwords[] = request.getParameterValues("checkbox[]");
if(topwords != null)
{
	for(String tt : topwords)
	{
		input += tt + " ";
	}
}
else
	input = request.getParameter("txtname");



if(input !=null && !input.equals(""))
{
	StopStem st = new StopStem("/comp4321/wlinab/public_html/stopwords.txt");
	Query query = new Query("/comp4321/wlinab/public_html/final",st);
	
	long t1 = System.currentTimeMillis();
	
	Vector<Score> v_score = query.execute(input);
	
	long t2 = System.currentTimeMillis();
	
	query.finalize();

	if(v_score.size() == 0)
		response.sendRedirect("empty.jsp");

	Print print = new Print("/comp4321/wlinab/public_html/final");

	Vector<PageInfo> v_pageInfo = print.getPageInfo(v_score);

	print.finalize();
	
	if(request.getParameter("lucky")!= null)	
		response.sendRedirect(v_pageInfo.elementAt(0).url);
	
	out.print("<p>Query Time : " + (t2 - t1)/1000.0 + " Seconds</p>");	

	for(int i = 0; i < v_score.size(); i++)
	{
		out.print("<div class = \"result\">");
		out.print("<a href=" + v_pageInfo.elementAt(i).url+"><b><h>" + v_pageInfo.elementAt(i).title + "</h></b></a><br>");
		out.print("<font>Score: " + v_score.elementAt(i).score + "</font><br>");
       		out.print("<a href=" + v_pageInfo.elementAt(i).url + ">" + v_pageInfo.elementAt(i).url + "</a><br>");
		out.println("<font>Last Modified Date: " + v_pageInfo.elementAt(i).date + ", Page Size: " + v_pageInfo.elementAt(i).size + "</font><br>");

		int size = v_pageInfo.elementAt(i).wordlist.size();
		boolean no1 = true;
		String similarPage = "";
		for(int k = 0 ; k < 5 && k < size ; k++)
		{
			if(no1)
				no1 = false;
			else
				out.print("; ");
				
			WordInfo keyword = v_pageInfo.elementAt(i).wordlist.elementAt(k);
			similarPage += keyword.word + " ";
				
			out.print(keyword.word + " " + keyword.freq );
		}

	out.print("<br>");
		//parent links
        out.print("<div class=\"link\">" + "Parent Link:" + "</div>");
        out.print("<div class=\"content\">");
        for(String url: v_pageInfo.elementAt(i).parent)
        {
            out.print("<a href="+url+">" + url + "</a><br>");
        }
        out.print("</div>");
	
        //children links
        out.print("<div class=\"link\">" + "Children Link:" + "</div>");
        out.print("<div class=\"content\">");
        for(URL url: v_pageInfo.elementAt(i).children)
        {
            out.print("<a href="+url.toString()+">"+url.toString()+"</a><br>");
        }
        out.print("</div>");
	
	   	out.print("<form method=\"post\" action=\"result.jsp\"><input type=\"hidden\" name=\"txtname\" value=\""
			+ similarPage 
			+ "\"><input type=\"submit\" value=\"Get similar pages\"></form>");
		out.print("</div>");
	}
}
else
{
	response.sendRedirect("empty.jsp");
}

%>
</body>
</html>
