/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dspace.app.webui.servlet;

import eu.zbw.EconStor.BibTeXGenerator.BibTexDOM.BibTeX;
import eu.zbw.EconStor.BibTeXGenerator.Generator;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.Constants;
import org.dspace.handle.HandleManager;

/**
 *
 * @author Riese Wolfgang
 */
public class BibTeXExportServlet extends DSpaceServlet {
    /** log4j category */
    private static Logger log = Logger.getLogger(BibTeXExportServlet.class);

    protected void doDSGet(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {
        String handle = null;
        String extraPathInfo = null;
        DSpaceObject dso = null;
        String path = request.getPathInfo();
        response.setContentType("text/x-bibtex; charset=UTF-8");

        if (path != null)
        {
            path = path.substring(1);

            try
            {
                // Extract the Handle
                int firstSlash = path.indexOf('/');
                int secondSlash = path.indexOf('/', firstSlash + 1);

                if (secondSlash != -1)
                {
                    // We have extra path info
                    handle = path.substring(0, secondSlash);
                    extraPathInfo = path.substring(secondSlash);
                }
                else
                {
                    // The path is just the Handle
                    handle = path;
                }
            }
            catch (NumberFormatException nfe)
            {
                // Leave handle as null
            }
        }

        // Find out what the handle relates to
        if (handle != null)
        {
            dso = HandleManager.resolveToObject(context, handle);
        }

        if (dso == null)
        {
            return;
        }

        // OK, we have a valid Handle. What is it?
        if (dso.getType() == Constants.ITEM)
        {
            Item item = (Item) dso;
            BibTeX myBibTeX = Generator.getBibTeXForItem(item);
            PrintWriter out = response.getWriter();
            out.println(myBibTeX.getBibTeXCode(false).trim());
            out.flush();
            out.close();
        }
    }

    protected void doDSPost(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {
        // Treat as a GET
        doDSGet(context, request, response);
    }
}
