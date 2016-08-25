/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.zbw.EconStor.BibTeXGenerator.BibTexDOM;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;

import org.dspace.core.ConfigurationManager;

/**
 *
 * @author Riese Wolfgang
 */
public class BibTeXEntryFactory {
    private static Properties _typeMapper = null;
    private static Properties _unicodeTeXMapper = null;
    private static HashMap<String, BibTeXEntry> _bibTeXEntryTypes = null;
    private static String _baseDir = null;
    //private static Properties _allKnownFields = null;

    private static void init() {
        _baseDir = ConfigurationManager.getProperty("dspace.dir")
                                + "/config/BibTeX/";
 
        if(_typeMapper == null) {
            _typeMapper = loadMapping("EconStorTypeToEntryMap.properties");
        }
        if(_unicodeTeXMapper == null) {
            _unicodeTeXMapper = loadMapping("UnicodeTeX.properties");
        }
        if(_bibTeXEntryTypes == null) {
            getEntryTypes();
        }
    }

    private static void getEntryTypes() {
        Properties mandatoryFields = null;
        Properties optionalFields = null;
        Properties allFields = null;
        Properties types = null;
        String mandfile = /*_baseDir + */"EntryMandatoryFields.properties";
        String optfile = /*_baseDir + */"EntryOptionalFields.properties";
        String allfile = /*_baseDir + */"Fields.properties";
        String typefile = /*_baseDir + */"EntryTypes.properties";

        mandatoryFields = loadMapping(mandfile);
        optionalFields = loadMapping(optfile);
        allFields = loadMapping(allfile);
        types = loadMapping(typefile);

        if(mandatoryFields != null && optionalFields != null
                && allFields != null && types != null) {
            _bibTeXEntryTypes = new HashMap<String, BibTeXEntry>();
            Enumeration<String> typeNames = (Enumeration<String>)types.propertyNames();
            while (typeNames.hasMoreElements()) {
                String typeName = typeNames.nextElement();
                String typeDescription = types.getProperty(typeName);
                String mandatory = mandatoryFields.getProperty(typeName, "");
                String optional = optionalFields.getProperty(typeName, "");
                String[] mandatoryF = mandatory.split(",");
                String[] optionalF = optional.split(",");
                BibTeXEntry bibentry = new BibTeXEntry(typeName, typeDescription, mandatoryF, optionalF);
                _bibTeXEntryTypes.put(typeName, bibentry);
            }
        } else {
            if(mandatoryFields == null) { throw new NullPointerException("mandatoryFields is NULL!"); }
            if(optionalFields == null) { throw new NullPointerException("optionalFields is NULL!"); }
            if(allFields == null) { throw new NullPointerException("allFields is NULL!"); }
            if(types == null) { throw new NullPointerException("types is NULL!"); }
        }

    }

    private static Properties loadMapping(String filename) {
        Properties retProp = new Properties();
        String file = _baseDir + filename;

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            retProp.load(in);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BibTeXEntryFactory.class).error(
                    "Mapping '" + filename + "' for BibTex not found!"
                    , ex);
            retProp = null;
        } catch (IOException ioex) {
            Logger.getLogger(BibTeXEntryFactory.class).error(
                    "Instantiation of '" + filename + "' for BibTex failed!"
                    , ioex);
            retProp = null;
        }

        return retProp;
    }

    public static BibTeXEntry createEntryForDspaceDocType(String dSpaceDocType)
                                                throws CloneNotSupportedException {
        BibTeXEntry newBibTexEntry = null;
        
        if(_typeMapper == null) { init(); }

        if(_typeMapper != null && !_typeMapper.isEmpty()
                && dSpaceDocType != null && !dSpaceDocType.isEmpty()) {

            String bibTexEntryType  = _typeMapper.getProperty(dSpaceDocType, "misc");

            if(_bibTeXEntryTypes != null
                    && _bibTeXEntryTypes.containsKey(bibTexEntryType)) {
                BibTeXEntry master = _bibTeXEntryTypes.get(bibTexEntryType);
                newBibTexEntry = master.clone();
            }  else {
              if(_bibTeXEntryTypes == null) { throw new NullPointerException("No BibTeXEntryTypes defined!"); }
              else { throw new CloneNotSupportedException("No BibTeXEntryType for :" + bibTexEntryType); }
            }
        } else { throw new NullPointerException("No type mapping defined!"); }

        return newBibTexEntry;
    }

    public static BibTeXEntry createEntryForType(String bibTexEntryType)
                                                throws CloneNotSupportedException {
        BibTeXEntry newBibTexEntry = null;

        if(_typeMapper == null) { init(); }

        if(_bibTeXEntryTypes != null
                && _bibTeXEntryTypes.containsKey(bibTexEntryType)) {
            BibTeXEntry master = _bibTeXEntryTypes.get(bibTexEntryType);
            newBibTexEntry = master.clone();
        }

        return newBibTexEntry;
    }

    public static String mapUnicodeToTeXEntities(String input) {
        String retStr = input;

        Set<Entry<Object, Object>> mapping = _unicodeTeXMapper.entrySet();
        for (Iterator<Entry<Object, Object>> it = mapping.iterator(); it.hasNext();) {
            Entry<Object, Object> entry = it.next();
            String unicode = (String)entry.getKey();
            String teXEntity = (String)entry.getValue();
            retStr = retStr.replace(unicode, teXEntity);
        }

        return retStr;
    }
}
