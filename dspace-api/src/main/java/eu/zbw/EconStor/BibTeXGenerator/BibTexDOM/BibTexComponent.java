/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.zbw.EconStor.BibTeXGenerator.BibTexDOM;

/**
 *
 * @author Riese Wolfgang
 */
public interface BibTexComponent {
    public void addOwningBibTexDocument(BibTeX owner);
    public boolean removeOwningBibTexDocument(BibTeX owner);
}
