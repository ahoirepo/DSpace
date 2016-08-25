/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.zbw.EconStor.BibTeXGenerator.BibTexDOM;


/**
 *
 * @author Riese Wolfgang
 */
public class BibTeXField  {
    private String _name = null;
    private String _value = null;

    public BibTeXField(String name, String value) {
        if(name != null) {
            _name = name;
        }
        if(value != null) {
            _value = value;
        }
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        if(name != null) {
            this._name = name;
        }
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        if(value != null) {
            this._value = value;
        }
    }

    public String getBibTeXCode(boolean strict) {
        StringBuilder retStr = new StringBuilder();

        String name = getName();
        if(name != null && !name.isEmpty()) {
            String value = getValue();
            //value = value.replace("\"", "\\\"");
            value = BibTeXEntryFactory.mapUnicodeToTeXEntities(value);
            if(strict && (value.length() > BibTeX.STRICT_LENGTH)) {
                value = value.substring(0, BibTeX.STRICT_LENGTH);
            }
            retStr.append(name).append(" = ").append('{').append(value).append('}');
        }

        return retStr.toString();
    }

    public String getPlainString() {
        String retString = null;

        String name = getName();
        if(name != null && !name.isEmpty()) {
            String value = getValue();
            //value = value.replace("\"", "\\\"");
            retString = name + " = {" + value + "}";
        }

        return retString;
    }
}
