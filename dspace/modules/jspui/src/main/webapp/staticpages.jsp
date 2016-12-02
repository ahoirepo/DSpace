<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Home page JSP
  -
  - Attributes:
  -    communities - Community[] all communities in DSpace
  -    recent.submissions - RecetSubmissions
  --%>

<%@page import="org.dspace.content.Bitstream"%>
<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="java.io.File" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="java.util.Locale"%>
<%@ page import="javax.servlet.jsp.jstl.core.*" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>
<%@ page import="org.dspace.core.I18nUtil" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.app.webui.components.RecentSubmissions" %>
<%@ page import="org.dspace.content.Community" %>
<%@ page import="org.dspace.core.ConfigurationManager" %>
<%@ page import="org.dspace.core.NewsManager" %>
<%@ page import="org.dspace.browse.ItemCounter" %>
<%@ page import="org.dspace.content.Item" %>

<%
    Community[] communities = (Community[]) request.getAttribute("communities");

    Locale[] supportedLocales = I18nUtil.getSupportedLocales();
    Locale sessionLocale = UIUtil.getSessionLocale(request);
    Config.set(request.getSession(), Config.FMT_LOCALE, sessionLocale);
    String topNews = NewsManager.readNewsFile(LocaleSupport.getLocalizedMessage(pageContext, "news-top.html"));

    boolean feedEnabled = ConfigurationManager.getBooleanProperty("webui.feed.enable");
    String feedData = "NONE";
    if (feedEnabled)
    {
        feedData = "ALL:" + ConfigurationManager.getProperty("webui.feed.formats");
    }
    
    ItemCounter ic = new ItemCounter(UIUtil.obtainContext(request));

    RecentSubmissions submissions = (RecentSubmissions) request.getAttribute("recent.submissions");
%>

<dspace:layout locbar="nolink" titlekey="jsp.home.title" feedData="<%= feedData %>">

<% 
String p = request.getParameter("incFile");

if (supportedLocales != null && supportedLocales.length > 1)
{
%>
        <form method="get" name="repost" action="">
          <input type ="hidden" name ="locale"/>
          <input type ="hidden" name ="incFile"/>
        </form>
<%
for (int i = supportedLocales.length-1; i >= 0; i--)
{
%>
        <a class ="langChangeOn"
                  onclick="javascript:document.repost.locale.value='<%=supportedLocales[i].toString()%>';
                  document.repost.incFile.value=<%=p%>;
                  document.repost.submit();">
                 <%= supportedLocales[i].getDisplayLanguage(supportedLocales[i])%>
        </a> &nbsp;
<%
}
}
%>
	<div class="jumbotron nopadding smaller">
        <%
            String fname = null;
            String langcode = sessionLocale.getLanguage();
            if (p.equals("1")) {
                if (langcode.equals("en")) {
                    fname = "static/policies_en.html";
                }
                else {
                    fname = "static/policies.html";
                }
            }
            if (p.equals("2")) {
                if (langcode.equals("en")) {
                    fname = "static/ansprechpartner_en.html";
                }
                else {
                    fname = "static/ansprechpartner.html";
                }
            }
            if (p.equals("3")) {
                if (langcode.equals("en")) {
                    fname = "static/doku_en.html";
                }
                else {
                    fname = "static/doku.html";
                }
            }
            if (p.equals("4")) {
                if (langcode.equals("en")) {
                    fname = "static/haftung_en.html";
                }
                else {
                    fname = "static/haftung.html";
                }
            }
            if (p.equals("5")) {
                if (langcode.equals("en")) {
                    fname = "static/impressum_en.html";
                }
                else {
                    fname = "static/impressum.html";
                }
            }
            if (p.equals("6")) {
                if (langcode.equals("en")) {
                    fname = "static/nutzung_en.html";
                }
                else {
                    fname = "static/nutzung.html";
                }
            }
            if (p.equals("7")) {
                if (langcode.equals("en")) {
                    fname = "static/diss_en.html";
                }
                else {
                    fname = "static/diss.html";
                }
            }
            if (p.equals("8")) {
                if (langcode.equals("en")) {
                    fname = "static/tubdok_opus_license_en.html";
                }
                else {
                    fname = "static/tubdok_opus_license.html";
                }
            }
            if (p.equals("9")) {
                if (langcode.equals("en")) {
                    fname = "static/tubdok_opus_pod_license_en.html";
                }
                else {
                    fname = "static/tubdok_opus_pod_license.html";
                }
            }
            if (p.equals("10")) {
                if (langcode.equals("en")) {
                    fname = "static/publ_note_en.html";
                }
                else {
                    fname = "static/publ_note.html";
                }
            }
            if (p.equals("11")) {
                if (langcode.equals("en")) {
                    fname = "static/depositlicense_en.html";
                }
                else {
                    fname = "static/depositlicense_de.html";
                }
            }
            if (fname.equals(null)) {
                if (langcode.equals("en")) {
                    fname = "static/error_en.html";
                }
                else {
                    fname = "static/error.html";
                }
            }
        %>
        <jsp:include page="<%=fname %>"></jsp:include>
	</div>

</dspace:layout>
