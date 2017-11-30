/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.zbw.EconStor.BibTeXGenerator.BibTexDOM;

import java.util.ArrayList;

/**
 *
 * @author Riese Wolfgang
 */
public class BibTeX {
    public final static int STRICT_LENGTH = 1000;

    private ArrayList<BibTeXCommand> _commands = null;
    private ArrayList<BibTeXEntry> _entries = null;

    public BibTeX() {
        _commands = new ArrayList<BibTeXCommand>();
        _entries = new ArrayList<BibTeXEntry>();
    }

    public boolean addCommand(BibTeXCommand command) {
        boolean succ = false;

        if(command != null && _commands != null) {
            succ = _commands.add(command);
        }

        return succ;
    }

    public boolean removeCommand(BibTeXCommand command) {
        boolean succ = false;

        if(command != null && _commands != null && _commands.contains(command)) {
            succ = _commands.remove(command);
        }

        return succ;
    }

    public boolean addEntry(BibTeXEntry entry) {
        boolean succ = true;

        if(entry != null && _entries != null) {
            _entries.add(entry);
            entry.addOwningBibTexDocument(this);
        } else {
            succ = false;
        }

        return succ;
    }

    public boolean removeEntry(BibTeXEntry entry) {
        boolean succ = false;
        if(entry != null && _entries != null && _entries.contains(entry)) {
            succ = _entries.remove(entry);
            succ = succ && entry.removeOwningBibTexDocument(this);
        }
        return succ;
    }

    public String getBibTeXCode(boolean strict) {
        StringBuilder retStr = new StringBuilder();

        for (BibTeXCommand bibTeXCommand : _commands) {
            retStr.append(bibTeXCommand.getBibTeXCode(strict));
            retStr.append("\n");
        }
        retStr.append("\n");
        for (BibTeXEntry bibTeXEntry : _entries) {
            retStr.append(bibTeXEntry.getBibTeXCode(strict));
            retStr.append("\n");
        }
        
        return retStr.toString();
    }

}
