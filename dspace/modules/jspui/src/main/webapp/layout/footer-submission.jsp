<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Footer for submission
  --%>
<%@ page import="java.io.*"  %>
<%
Integer userID = (Integer) session.getAttribute("dspace.current.user.id");
String str = userID+"";
String nameOfTextFile = "/tmp/"+session.getId()+".txt";
try {   
    PrintWriter pw = new PrintWriter(new FileOutputStream(nameOfTextFile));
    pw.println(str);
    pw.close();
} catch(IOException e) {
   out.println(e.getMessage());
}
%>
<script type="text/javascript">
    var JSESSION = "<%= session.getId() %>";
</script>
<script type="text/javascript" src="<%= request.getContextPath() %>/static/js/getuser.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/static/js/gnd-lookup.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/static/js/urn-support.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/static/js/getISSNAuthority.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/static/js/jquery_manipulation.js"></script>
<%@ include file="footer-default.jsp" %>