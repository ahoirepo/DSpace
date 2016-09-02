/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.zbw.EconStor.BibTeXGenerator;

import eu.zbw.EconStor.BibTeXGenerator.BibTexDOM.BibTeX;
import eu.zbw.EconStor.BibTeXGenerator.BibTexDOM.BibTeXEntry;
import eu.zbw.EconStor.BibTeXGenerator.BibTexDOM.BibTeXEntryFactory;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import eu.zbw.EconStor.app.util.SubfieldsUtil;
import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;

/**
 *
 * @author Riese Wolfgang
 */
public class Generator {

    /** log4j logger */
    private static Logger log = Logger.getLogger(Generator.class);

    /**
     * Used BibTeX Entry Fields
     */
    public static enum BibFields { ABSTRACT, ADDRESS, AUTHOR, BOOKTITLE, COPYRIGHT,
                            DAY, DOI, EDITION, EDITOR, ISBN, ISSN, JOURNAL,
                            KEYWORDS, LANGUAGE, LOCATION, MONTH, NOTE, NUMBER,
                            PAGES, PUBLISHER, SERIES, TITLE, TYPE, URL, VOLUME,
                            YEAR, SCHOOL, INSTITUTION };

    /**
     * 
     * @param dsItem
     * @return
     */
    public static BibTeX getBibTeXForItem(Item dsItem) {
        BibTeX myBibTex = null;
        boolean isThesis = false;

        myBibTex = new BibTeX();
        if(dsItem != null) {
            Metadatum[] typeBibTeX = dsItem.getDC("type", "bibtex", Item.ANY);
            String dsType = "";
            Metadatum[] typeTV = dsItem.getDC("type", "thesis", Item.ANY);
            if((typeTV != null) && (typeTV.length > 0) && (typeTV[0] != null)) {
                isThesis = true;
            }
            if((typeBibTeX != null) && (typeBibTeX.length > 0) && (typeBibTeX[0] != null)) {
                dsType = typeBibTeX[0].value;
            } else {
                if((typeTV != null) && (typeTV.length > 0) && (typeTV[0] != null)) {
                    dsType = typeTV[0].value;
                } else {
                    Metadatum[] typeV = dsItem.getDC("type", null, Item.ANY);
                    if((typeV != null) && (typeV.length > 0) && (typeV[0] != null)) {
                        dsType = typeV[0].value;
                    } else {
                        // For records without any type, set type to other
                        dsType = "Other";
                        log.error("missing type for item handle:'"+dsItem.getHandle()+"' - id:"+dsItem.getID());
                    }
                }
            }
            Metadatum[] authorsV = dsItem.getDC("contributor", "author", Item.ANY);
            Metadatum[] dateV = dsItem.getDC("date", "issued", Item.ANY);
            Metadatum[] titleV = dsItem.getDC("title", null, Item.ANY);
            HashMap<BibFields, ArrayList<String>> bibfields =
                        new HashMap<BibFields, ArrayList<String>>();

            try {
                BibTeXEntry bibEntry = BibTeXEntryFactory.createEntryForDspaceDocType(dsType);
                myBibTex.addEntry(bibEntry);
                String bibTexIdent = "";

                Metadatum[] values = dsItem.getMetadata("dc", Item.ANY, Item.ANY, Item.ANY);
                String handle = dsItem.getHandle();
                if(handle != null && !handle.isEmpty()) {
                    bibEntry.addFieldAndValue("url", "http://hdl.handle.net/"+handle);
                    bibTexIdent = "#" + handle;
                }
                if(authorsV == null || authorsV.length == 0 ) {
                    authorsV = dsItem.getDC("contributor", "editor", Item.ANY);
                }
                if (dateV == null || dateV.length == 0 ) {
                    dateV = dsItem.getDC("date", "accessioned", Item.ANY);
                }
                if(authorsV != null && authorsV.length > 0
                    && dateV != null && dateV.length > 0
                    && titleV != null && titleV.length > 0 ) {
                    bibTexIdent = makeBibTeXIdentifier(authorsV[0], dateV[0], titleV[0]);
                }
                if(authorsV != null && authorsV.length > 0) {
                    for (int i = 0; i < authorsV.length; i++) {
                        Metadatum subfA = authorsV[i];
                        if(subfA != null && subfA.value != null && !subfA.value.trim().isEmpty()) {
                            ArrayList<String> fieldValues = bibfields.get(BibFields.AUTHOR);
                            if(fieldValues == null) {
                                fieldValues = new ArrayList<String>();
                                bibfields.put(BibFields.AUTHOR, fieldValues);
                            }
                            String[] nameParts = subfA.value.split(",");
                            if(nameParts.length > 1) {
                                fieldValues.add(nameParts[1].trim()+" "+nameParts[0].trim());
                            } else {
                                fieldValues.add(subfA.value.trim());
                            }
                        }
                    }
                }
                bibEntry.setEntryName(bibTexIdent);
                for (int i = 0; i < values.length; i++) {
                    Metadatum dCValue = values[i];
                    if(dCValue.element.trim().equalsIgnoreCase("contributor")) {
                        bibfields = processContributor(dCValue, bibfields);
                    } else if(dCValue.element.trim().equalsIgnoreCase("date")) {
                        bibfields = processDate(dCValue, bibfields);
                    } else if(dCValue.element.trim().equalsIgnoreCase("description")) {
                        bibfields = processDescription(dCValue, bibfields);
                    } else if(dCValue.element.trim().equalsIgnoreCase("identifier")) {
                        if(dCValue.qualifier != null && !dCValue.qualifier.isEmpty()) {
                            if(dCValue.qualifier.trim().equalsIgnoreCase("citation")) {
                                bibfields = processIdentifierCitation(dsItem, dCValue, bibfields);
                            } else if(dCValue.qualifier.trim().equalsIgnoreCase("isbn")) {
                                if(dCValue.value != null) {
                                    bibfields = putValueInArrayList(dCValue.value.trim()
                                                                    , BibFields.ISBN
                                                                    , bibfields);
                                }
                            } else if(dCValue.qualifier.trim().equalsIgnoreCase("issn")) {
                                if(dCValue.value != null) {
                                    bibfields = putValueInArrayList(dCValue.value.trim()
                                                                    , BibFields.ISSN
                                                                    , bibfields);
                                }
                            } else if(dCValue.qualifier.trim().equalsIgnoreCase("doi")) {
                                if(dCValue.value != null) {
                                    bibfields = putValueInArrayList(dCValue.value.trim()
                                                                    , BibFields.DOI
                                                                    , bibfields);
                                    bibfields = putValueInArrayList("https://doi.org/" + dCValue.value.trim()
                                                                    , BibFields.URL
                                                                    , bibfields);
                                }
                            } /*
                              else if(dCValue.qualifier.trim().equalsIgnoreCase("url")) {
                                if(dCValue.value != null) {
                                    bibfields = putValueInArrayList(dCValue.value.trim()
                                                                    , BibFields.URL
                                                                    , bibfields);
                                }
                            }
                            */
                        }
                    } else if(dCValue.element.trim().equalsIgnoreCase("language")) {
                        if(dCValue.qualifier != null 
                                && dCValue.qualifier.trim().equalsIgnoreCase("iso")) {
                            if(dCValue.value != null) {
                                bibfields = putValueInArrayList(dCValue.value.trim()
                                                                , BibFields.LANGUAGE
                                                                , bibfields);
                            }
                        }
                    } else if(dCValue.element.trim().equalsIgnoreCase("publisher")) {
/*
                        Metadatum dcSubfA = SubfieldsUtil.getSubfield(dCValue, "a");
                        Metadatum dcSubfC = SubfieldsUtil.getSubfield(dCValue, "c");
*/
                        Metadatum dcSubfA = null;
                        Metadatum dcSubfC = null;
                        if(dcSubfA != null && dcSubfA.value != null
                                && !dcSubfA.value.trim().isEmpty()) {
                            bibfields = putValueInArrayList(dcSubfA.value.trim()
                                                                , BibFields.PUBLISHER
                                                                , bibfields);
                        }
                        if(dcSubfC != null && dcSubfC.value != null
                                && !dcSubfC.value.trim().isEmpty()) {
                            bibfields = putValueInArrayList(dcSubfC.value.trim()
                                                                , BibFields.ADDRESS
                                                                , bibfields);
                        }
                    } else if(dCValue.element.trim().equalsIgnoreCase("relation")) {
                        if (dCValue.qualifier != null && !dCValue.qualifier.trim().isEmpty()) {
                            if(dCValue.qualifier.trim().equalsIgnoreCase("ispartofseries")) {
//                                Metadatum[] dcSubfAs = SubfieldsUtil.getSubfield(dsItem.getDC("identifier", "citation", Item.ANY), "a");
                                Metadatum[] dcSubfAs = null;
                                if(dcSubfAs == null || dcSubfAs.length < 1
                                        || dcSubfAs[0].value.replaceAll("\\|a", "").trim().isEmpty()) {
/*
                                    Metadatum dcSubfA = SubfieldsUtil.getSubfield(dCValue, "a");
                                    Metadatum dcSubfX = SubfieldsUtil.getSubfield(dCValue, "x");
*/
                                    Metadatum dcSubfA = null;
                                    Metadatum dcSubfX = null;
                                    if(dcSubfA != null && dcSubfA.value != null
                                            && !dcSubfA.value.trim().isEmpty()) {
                                        if(bibEntry.getTypeName().equals("techreport")) {
                                            bibfields = putValueInArrayList(dcSubfA.value.trim()
                                                                            , BibFields.TYPE
                                                                            , bibfields);
                                        } else {
                                            bibfields = putValueInArrayList(dcSubfA.value.trim()
                                                                            , BibFields.SERIES
                                                                            , bibfields);
                                        }
                                    }
                                    if(dcSubfX != null && dcSubfX.value != null
                                            && !dcSubfX.value.trim().isEmpty()) {
//                                        Metadatum[] dcSubfHs = SubfieldsUtil.getSubfield(dsItem.getDC("identifier", "citation", Item.ANY), "h");
                                        Metadatum[] dcSubfHs = null;
                                        if(dcSubfHs == null || dcSubfHs.length < 1
                                                || dcSubfHs[0].value.replaceAll("\\|h", "").trim().isEmpty() ) {
                                            bibfields = putValueInArrayList(dcSubfX.value.trim()
                                                                                , BibFields.NUMBER
                                                                                , bibfields);
                                        }
                                    }
                                }
                            } else {
                                String prefix = "";
                                if(dCValue.qualifier.trim().equalsIgnoreCase("hasversion")) {
                                    prefix = "More recent version: ";
                                } else if(dCValue.qualifier.trim().equalsIgnoreCase("isreplacedby")) {
                                    prefix = "Is replaced by the following version: ";
                                } else  if(dCValue.qualifier.trim().equalsIgnoreCase("isversionof")) {
                                    prefix = "Older version: ";
                                } else if(dCValue.qualifier.trim().equalsIgnoreCase("references")) {
                                    prefix = "Review of ";
                                }
                                if(dCValue.value != null) {
                                    bibfields = putValueInArrayList(prefix+dCValue.value.trim()
                                                                    , BibFields.NOTE
                                                                    , bibfields);
                                }
                            }
                        }
                    } else if(dCValue.element.trim().equalsIgnoreCase("rights")) {
                        if(dCValue.value != null) {
                            bibfields = putValueInArrayList(dCValue.value.trim()
                                                            , BibFields.COPYRIGHT
                                                            , bibfields);
                        }
                    } else if(dCValue.element.trim().equalsIgnoreCase("subject")) {
                        if(dCValue.value != null) {
                            bibfields = putValueInArrayList(dCValue.value.trim()
                                                            , BibFields.KEYWORDS
                                                            , bibfields);
                        }
                    } else if(dCValue.element.trim().equalsIgnoreCase("title")) {
                        if(dCValue.value != null) {
                            bibfields = putValueInArrayList(dCValue.value.trim()
                                                            , BibFields.TITLE
                                                            , bibfields);
                        }
                    }
                }
                // add Location
                bibfields = putValueInArrayList("Hamburg"
                            , BibFields.ADDRESS
                            , bibfields);
                if (dsType.equalsIgnoreCase("report") || dsType.equalsIgnoreCase("Working Paper")) {
                    bibfields = putValueInArrayList("TU Hamburg"
                                , BibFields.INSTITUTION
                                , bibfields);
                }
                // add type field for phdthesis and masterthesis
                if (isThesis == true) {
                    if (dsType.equalsIgnoreCase("doctoralThesis")) {
                        bibfields = putValueInArrayList("Dissertation"
                                    , BibFields.TYPE
                                    , bibfields);
                    }
                    if (dsType.equalsIgnoreCase("masterThesis")) {
                        bibfields = putValueInArrayList("Master Thesis"
                                    , BibFields.TYPE
                                    , bibfields);
                    }
                    bibfields = putValueInArrayList("TU Hamburg"
                                , BibFields.SCHOOL
                                , bibfields);
                }


                // Customized Metadataschema
                Metadatum[] localvalues = dsItem.getMetadata("tuhh", Item.ANY, Item.ANY, Item.ANY);
                for (int j = 0; j < localvalues.length; j++) {
                    Metadatum tuhhValue = localvalues[j];
                    if(tuhhValue.element.trim().equalsIgnoreCase("publikation")) {
                        if(tuhhValue.qualifier != null && !tuhhValue.qualifier.isEmpty()) {
                            if(tuhhValue.qualifier.trim().equalsIgnoreCase("source")) {
                                bibfields = putValueInArrayList(tuhhValue.value.trim()
                                        , BibFields.BOOKTITLE
                                        , bibfields);
                            }
                        }
                    } else if(tuhhValue.element.trim().equalsIgnoreCase("series")) {
                        if(tuhhValue.qualifier != null && !tuhhValue.qualifier.isEmpty()) {
                            if(tuhhValue.qualifier.trim().equalsIgnoreCase("name")) {
                                bibfields = putValueInArrayList(tuhhValue.value.trim()
                                        , BibFields.SERIES
                                        , bibfields);
                            }
                        }
                    }
                }

                if(!bibfields.isEmpty()) {
                    for (Entry<BibFields, ArrayList<String>> mapE : bibfields.entrySet()) {
                        boolean first = true;
                        StringBuilder strBldVal = new StringBuilder();
                        switch(mapE.getKey()) {
                            case ABSTRACT:
                                for (String listVal : mapE.getValue()) {
                                    if(first) {
                                        first = false;
                                        strBldVal.append(listVal);
                                    } else {
                                        strBldVal.append(" -- ");
                                        strBldVal.append(listVal);
                                    }
                                }
                                bibEntry.addFieldAndValue(BibFields.ABSTRACT.toString().toLowerCase(),
                                                            strBldVal.toString());
                                break;
                            case AUTHOR:
                            case EDITOR:
                                for (String listVal : mapE.getValue()) {
                                    if(first) {
                                        first = false;
                                        strBldVal.append(listVal);
                                    } else {
                                        strBldVal.append(" and ");
                                        strBldVal.append(listVal);
                                    }
                                }
                                bibEntry.addFieldAndValue(mapE.getKey().toString().toLowerCase(),
                                                            strBldVal.toString());
                                break;
                            default:
                                for (String listVal : mapE.getValue()) {
                                    if(first) {
                                        first = false;
                                        strBldVal.append(listVal);
                                    } else {
                                        strBldVal.append("; ");
                                        strBldVal.append(listVal);
                                    }
                                }
                                bibEntry.addFieldAndValue(mapE.getKey().toString().toLowerCase(),
                                                            strBldVal.toString());
                                break;
                        }
                    }
                }
            } catch(CloneNotSupportedException cnex) {
                //TODO:
            }
        }

        return myBibTex;
    }

    public static String getBibTeXIdentifier(Item dsItem) {
        String retStr = "";
        if(dsItem != null) {
            Metadatum[] authorsV = dsItem.getDC("contributor", "author", Item.ANY);
            if(authorsV == null || authorsV.length == 0 ) {
                authorsV = dsItem.getDC("contributor", "editor", Item.ANY);
            }
            Metadatum[] dateV = dsItem.getDC("date", "issued", Item.ANY);
            if (dateV == null || dateV.length == 0 ) {
                dateV = dsItem.getDC("date", "accessioned", Item.ANY);
            }
            Metadatum[] titleV = dsItem.getDC("title", null, Item.ANY);
            if(authorsV != null && authorsV.length > 0
                    && dateV != null && dateV.length > 0
                    && titleV != null && titleV.length > 0 ) {
                retStr = makeBibTeXIdentifier(authorsV[0], dateV[0], titleV[0]);
            }
        }

        return retStr;
    }


    /**
     *
     * @param author
     * @param date
     * @param title
     * @return
     */
    private static String makeBibTeXIdentifier(Metadatum author, Metadatum date, Metadatum title) {
         StringBuilder retStrB = new StringBuilder();

         String authN[] = author.value.split(",");
         String authFN = "anonymous";
         if(authN != null && authN[0] != null && !authN[0].trim().isEmpty()) {
             authFN = authN[0].trim();
             authFN = Normalizer
                        .normalize(authFN, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "")
                        .replaceAll("\\s", "");
         }
         retStrB.append(authFN);
         Pattern myPat = Pattern.compile("\\d{4}");
         Matcher myMatcher = myPat.matcher(date.value);
         String dateissued = "";
         if(myMatcher.find()) {
             dateissued = date.value.substring(myMatcher.start(), myMatcher.end());
         }
         retStrB.append(dateissued);
/*
         Pattern myPat2 = Pattern.compile("\\w{4,}");
         Matcher myMatcher2 = myPat2.matcher(SubfieldsUtil.getSubfield(title, "a").value);
         String firstTitleW = "";
         if(myMatcher2.find()) {
             firstTitleW = title.value.substring(myMatcher2.start(), myMatcher2.end());
         }
         retStrB.append(firstTitleW);
*/
         return retStrB.toString();
    }

    /**
     *
     * @param dCValue
     * @param bibfields
     * @return
     */
    private static HashMap<BibFields, ArrayList<String>> processIdentifierCitation(Item dsItem, Metadatum dCValue,
            HashMap<BibFields, ArrayList<String>> bibfields) {

        return bibfields;
/*
        Metadatum subfA = SubfieldsUtil.getSubfield(dCValue, "a");
        Metadatum subfB = SubfieldsUtil.getSubfield(dCValue, "b");
        Metadatum subfC = SubfieldsUtil.getSubfield(dCValue, "c");
        Metadatum subfD = SubfieldsUtil.getSubfield(dCValue, "d");
        Metadatum subfE = SubfieldsUtil.getSubfield(dCValue, "e");
        Metadatum subfF = SubfieldsUtil.getSubfield(dCValue, "f");
        //Metadatum subfG = SubfieldsUtil.getSubfield(dCValue, "g");
        Metadatum subfH = SubfieldsUtil.getSubfield(dCValue, "h");
        Metadatum subfI = SubfieldsUtil.getSubfield(dCValue, "i");
        //Metadatum subfJ = SubfieldsUtil.getSubfield(dCValue, "j");
        Metadatum subfK = SubfieldsUtil.getSubfield(dCValue, "k");
        Metadatum subfL = SubfieldsUtil.getSubfield(dCValue, "l");
        Metadatum subfM = SubfieldsUtil.getSubfield(dCValue, "m");
        Metadatum subfN = SubfieldsUtil.getSubfield(dCValue, "n");
        Metadatum subfO = SubfieldsUtil.getSubfield(dCValue, "o");
        Metadatum subfP = SubfieldsUtil.getSubfield(dCValue, "p");
        Metadatum subfQ = SubfieldsUtil.getSubfield(dCValue, "q");
        Metadatum subfR = SubfieldsUtil.getSubfield(dCValue, "r");
        Metadatum subfS = SubfieldsUtil.getSubfield(dCValue, "s");
        Metadatum subfT = SubfieldsUtil.getSubfield(dCValue, "t");
        //Metadatum subfU = SubfieldsUtil.getSubfield(dCValue, "u");
        Metadatum subfV = SubfieldsUtil.getSubfield(dCValue, "v");
        //Metadatum subfW = SubfieldsUtil.getSubfield(dCValue, "w");
        Metadatum subfX = SubfieldsUtil.getSubfield(dCValue, "x");
        Metadatum subfY = SubfieldsUtil.getSubfield(dCValue, "y");
        //Metadatum subfZ = SubfieldsUtil.getSubfield(dCValue, "z");
        

        if(subfA.value != null && !subfA.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfA.value.trim(), BibFields.JOURNAL, bibfields);
        }
        if(subfB.value != null && !subfB.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfB.value.trim(), BibFields.SERIES, bibfields);
        }
        if(subfC.value != null && !subfC.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfC.value.trim(), BibFields.ISSN, bibfields);
        }
        if(subfM.value != null && !subfM.value.trim().isEmpty()) {
            ArrayList<String> fieldValues = bibfields.get(BibFields.NOTE);
            StringBuilder conferenceSB = new StringBuilder("Conference: ");
            if(fieldValues == null) {
                fieldValues = new ArrayList<String>();
                bibfields.put(BibFields.NOTE, fieldValues);
            }
            if(subfD.value != null && !subfD.value.trim().isEmpty()) {
                conferenceSB.append(subfD.value.trim()).append(" ");
            }
            conferenceSB.append(subfM.value.trim());
            fieldValues.add(conferenceSB.toString());
        }
        if(subfE.value != null && !subfE.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfE.value.trim(), BibFields.EDITOR, bibfields);
        }
        if(subfF.value != null && !subfF.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfF.value.trim(), BibFields.BOOKTITLE, bibfields);
        }
        if(subfH.value != null && !subfH.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfH.value.trim(), BibFields.NUMBER, bibfields);
        }
        if(subfI.value != null && !subfI.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfI.value.trim(), BibFields.EDITION, bibfields);
        }
        if(subfK.value != null && !subfK.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfK.value.trim(), BibFields.EDITOR, bibfields);
        }
        if(subfL.value != null && !subfL.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfL.value.trim(), BibFields.ADDRESS, bibfields);
        }
        if(subfN.value != null && !subfN.value.trim().isEmpty()) {
            Metadatum[] dcSubfAs = SubfieldsUtil.getSubfield(dsItem.getDC("publisher", null, Item.ANY), "a");
            if(dcSubfAs == null || dcSubfAs.length < 1
                    || dcSubfAs[0].value.replaceAll("\\|a", "").trim().isEmpty()) {
                bibfields = putValueInArrayList(subfN.value.trim(), BibFields.PUBLISHER
                                                , bibfields);
            }
        }
        if(subfO.value != null && !subfO.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfO.value.trim(), BibFields.LOCATION, bibfields);
        }
        if(subfP.value != null && !subfP.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfP.value.trim(), BibFields.PAGES, bibfields);
        }
        if(subfQ.value != null && !subfQ.value.trim().isEmpty()) {
            Metadatum[] idpiValue = dsItem.getDC("identifier", "pi", Item.ANY);
            boolean nodoi = true;
            if(idpiValue != null && idpiValue.length > 0) {
                for (Metadatum dCValue1 : idpiValue) {
                    if(dCValue1.value.trim().toLowerCase().startsWith("doi:")) {
                        nodoi = false;
                        break;
                    }
                }
            }
            if(nodoi) {
                if(subfQ.value.trim().toLowerCase().startsWith("doi:")) {
                    bibfields = putValueInArrayList(subfQ.value.trim().substring(4), BibFields.DOI, bibfields);
                } else {
                    bibfields = putValueInArrayList(subfQ.value.trim(), BibFields.NOTE, bibfields);
                }
            }
        }
        Metadatum[] dcSubfDIs = dsItem.getDC("date", "issued", Item.ANY);
        if(dcSubfDIs == null || dcSubfDIs.length < 1
                || dcSubfDIs[0].value.replaceAll("\\|a", "").trim().isEmpty()) {
            if(subfY.value != null && !subfY.value.trim().isEmpty()) {
                bibfields = putValueInArrayList(subfY.value.trim(), BibFields.YEAR, bibfields);
            } else if(subfR.value != null && !subfR.value.trim().isEmpty()) {
                Pattern myPat = Pattern.compile("\\d{4}");
                Matcher matcher = myPat.matcher(subfR.value);
                if(matcher.find()) {
                    String matchS = matcher.group();
                    if(matchS != null && !matchS.isEmpty()) {
                        bibfields = putValueInArrayList(matchS, BibFields.YEAR, bibfields);
                    }
                }
            }
        }
        if(subfS.value != null && !subfS.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfS.value.trim(), BibFields.ISBN, bibfields);
        }
        if(subfT.value != null && !subfT.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfT.value.trim(), BibFields.BOOKTITLE, bibfields);
        }
        if(subfV.value != null && !subfV.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfV.value.trim(), BibFields.VOLUME, bibfields);
        }
        /* Nicht mehr erwünscht
        if(subfW.value != null && !subfW.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfW.value.trim(), BibFields.URL, bibfields);
        }
         */
/*
        if(subfX.value != null && !subfX.value.trim().isEmpty()) {
            bibfields = putValueInArrayList(subfX.value.trim(), BibFields.NUMBER, bibfields);
        }

        return bibfields;
*/
    }

    /**
     *
     * @param dCValue
     * @param bibfields
     * @return
     */
    private static HashMap<BibFields, ArrayList<String>> processDescription(Metadatum dCValue,
            HashMap<BibFields, ArrayList<String>> bibfields) {
        if(dCValue.qualifier != null && !dCValue.qualifier.trim().isEmpty()) {
            if(dCValue.qualifier.trim().equalsIgnoreCase("abstract")
                    || dCValue.qualifier.trim().equalsIgnoreCase("abstracttrans")) {
                if(dCValue.value != null && !dCValue.value.trim().isEmpty()) {
                    ArrayList<String> fieldValues = bibfields.get(BibFields.ABSTRACT);
                    if(fieldValues == null) {
                        fieldValues = new ArrayList<String>();
                        bibfields.put(BibFields.ABSTRACT, fieldValues);
                    }
                    if(dCValue.qualifier.trim().equalsIgnoreCase("abstract")) {
                        fieldValues.add(0, dCValue.value.trim());
                    } else {
                        fieldValues.add(" -- " + dCValue.value.trim());
                    }
                }
            } else if(dCValue.qualifier.trim().equalsIgnoreCase("other")) {
                if(dCValue.value != null && !dCValue.value.trim().isEmpty()) {
                    bibfields = putValueInArrayList("Additional information: "
                                                        + dCValue.value.trim()
                                                        , BibFields.NOTE //"note"
                                                        , bibfields);
                }
            }
        }
        
        return bibfields;
    }

    /**
     *
     * @param dCValue
     * @param bibfields
     * @return
     */
    private static HashMap<BibFields, ArrayList<String>> processDate(Metadatum dCValue,
            HashMap<BibFields, ArrayList<String>> bibfields) {
        if(dCValue.qualifier != null && !dCValue.qualifier.trim().isEmpty()) {
            if(dCValue.qualifier.trim().equalsIgnoreCase("issued")) {
                if(dCValue.value != null && !dCValue.value.trim().isEmpty()) {
                    String[] datetimeparts = dCValue.value.trim().split("T");
                    String[] dateparts = datetimeparts[0].split("-");
                    if(dateparts.length > 0) {
                        bibfields = putValueInArrayList(dateparts[0].trim()
                                                        , BibFields.YEAR
                                                        , bibfields);
                    }
                    if(dateparts.length > 1) {
                        try {
                            int month = Integer.parseInt(dateparts[1].trim());
                            String monthS = "";
                            switch(month) {
                                case 1: monthS = "jan";
                                    break;
                                case 2: monthS = "feb";
                                    break;
                                case 3: monthS = "mar";
                                    break;
                                case 4: monthS = "apr";
                                    break;
                                case 5: monthS = "may";
                                    break;
                                case 6: monthS = "jun";
                                    break;
                                case 7: monthS = "jul";
                                    break;
                                case 8: monthS = "aug";
                                    break;
                                case 9: monthS = "sep";
                                    break;
                                case 10: monthS = "oct";
                                    break;
                                case 11: monthS = "nov";
                                    break;
                                case 12: monthS = "dec";
                                    break;
                                default: monthS = "";
                                    break;
                            }
                            /* we don't want month to get set
                            bibfields = putValueInArrayList(monthS
                                                        , BibFields.MONTH
                                                        , bibfields);
                            */
                        } catch(NumberFormatException nfex) {
                            //do nothing
                        }
                    }
                    if(dateparts.length > 2) {
                        bibfields = putValueInArrayList(dateparts[2].trim()
                                                        , BibFields.DAY
                                                        , bibfields);
                    }
                }
            }
        }

        return bibfields;
    }

    /**
     *
     * @param dCValue
     * @param bibfields
     * @return
     */
    private static HashMap<BibFields, ArrayList<String>> processContributor(Metadatum dCValue,
            HashMap<BibFields, ArrayList<String>> bibfields) {

        if(dCValue.qualifier != null && !dCValue.qualifier.trim().isEmpty()) {
            if(dCValue.qualifier.trim().equalsIgnoreCase("advisor")) {
                if(dCValue.value != null && !dCValue.value.trim().isEmpty()) {
                    bibfields = putValueInArrayList("Advisor: "
                                                        + dCValue.value.trim()
                                                        , BibFields.NOTE
                                                        , bibfields);
                }
            } else if(dCValue.qualifier.trim().equalsIgnoreCase("author")) {
                Metadatum subfA = null;
                if(subfA != null && subfA.value != null && !subfA.value.trim().isEmpty()) {
                    ArrayList<String> fieldValues = bibfields.get(BibFields.AUTHOR);
                    if(fieldValues == null) {
                        fieldValues = new ArrayList<String>();
                        bibfields.put(BibFields.AUTHOR, fieldValues);
                    }
                    String[] nameParts = subfA.value.split(",");
                    if(nameParts.length > 1) {
                        fieldValues.add(nameParts[1].trim()+" "+nameParts[0].trim());
                    } else {
                        fieldValues.add(subfA.value.trim());
                    }
                }
            } else if(dCValue.qualifier.trim().equalsIgnoreCase("editor")) {
//                Metadatum subfA = SubfieldsUtil.getSubfield(dCValue, "a");
                Metadatum subfA = null;
                if(subfA != null && subfA.value != null && !subfA.value.trim().isEmpty()) {
                    ArrayList<String> fieldValues = bibfields.get(BibFields.EDITOR);
                    if(fieldValues == null) {
                        fieldValues = new ArrayList<String>();
                        bibfields.put(BibFields.EDITOR, fieldValues);
                    }
                    String[] nameParts = subfA.value.split(",");
                    if(nameParts.length > 1) {
                        fieldValues.add(nameParts[1].trim()+" "+nameParts[0].trim());
                    } else {
                        fieldValues.add(subfA.value.trim());
                    }
                }
            }
        }

        return bibfields;
    }

    /**
     *
     * @param dcValueS
     * @param fieldname
     * @param bibfields
     * @return
     */
    private static HashMap<BibFields, ArrayList<String>> putValueInArrayList(String dcValueS
                            , BibFields fieldname
                            , HashMap<BibFields, ArrayList<String>> bibfields) {
        if(dcValueS != null && fieldname != null && bibfields != null) {
            ArrayList<String> fieldValues = bibfields.get(fieldname);
            if(fieldValues == null) {
                fieldValues = new ArrayList<String>();
                bibfields.put(fieldname, fieldValues);
            }
            fieldValues.add(dcValueS);
        }

        return bibfields;
    }
}
