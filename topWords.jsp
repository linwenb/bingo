		
<html>
<head>
 <link rel="icon" href="icon.png"/>
	<title> Bingo </title>
</head>
<body>
<%@ page import="pt.tumba.spell.*" %>
<%@ page import = "java.util.Vector" %>
<%@ page import = "main.*"%>
<%@ page import = "java.net.URL" %>
<%@ include file="header.html" %>

<div>
<form name="topwords" method = "post" action = "result.jsp">  
<table>
<tr height = "40px">
<td colspan="5" align="center">Select the keywords and <input type = "submit" value = "Search"></td> 
</tr>
<%
	Print print = new Print("/comp4321/wlinab/public_html/final");
	Vector<String> topwords = print.getTopWords();
	 print.finalize();
	
	for(int i = 0; i < 100; i++)
	{
		out.print("<tr>");
	for(int j =0; j < 5; j++)
	{
		out.print("<td>");
		out.print("<input type=\"checkbox\" name=\"checkbox[]\" value=\""+topwords.elementAt(5*i+j)+"\"> "+topwords.elementAt(5*i+j));
		out.print("</td>");
	}
		out.print("</tr>");
	}
	
	out.print("<tr height = \"40px\">");
	out.print("<td colspan=\"5\" align=\"center\">");
	out.print("<input type=\"submit\" value=\"Submit to search\">");
	out.print("</td>");
	out.print("</tr>");
%>
</form>
</table>
</body>
</html>

