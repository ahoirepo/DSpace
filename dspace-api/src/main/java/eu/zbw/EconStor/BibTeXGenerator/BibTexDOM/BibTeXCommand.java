/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.zbw.EconStor.BibTeXGenerator.BibTexDOM;

/**
 *
 * @author Riese Wolfgang
 */
public class BibTeXCommand {
    private String _command = null;
    private String _name = null;
    private String _value = null;

    protected BibTeXCommand(String command, String name, String value) {
        if(command != null && !command.isEmpty()) {
            if(name != null && !name.isEmpty()) {
                _name = name;
            }
            if(value != null & !value.isEmpty()) {
                _value = value;
            }
        } else {
            if(command == null) {
                throw new NullPointerException("Parameter command was NULL!");
            } else {
                throw new IllegalArgumentException("Parameter command was empty!");
            }
        }
    }

    public String setName(String name) {
        String oldName = null;

        if(name != null) {
            oldName = _name;
            _name = name;
        } else {
            throw new NullPointerException("Parameter name was NULL!");
        }

        return oldName;
    }

    public String setValue(String value) {
        String oldvalue = null;

        if(value != null) {
            oldvalue = _value;
            _value = value;
        } else {
            throw new NullPointerException("Parameter value was NULL!");
        }

        return oldvalue;
    }

    public String getType() {
        return _command;
    }

    public String getName() {
        return _name;
    }

    public String getValue() {
        return _value;
    }

    public String getBibTeXCode(boolean strict) {
        StringBuilder retStr = new StringBuilder();

        retStr.append('@').append(getType()).append('{');
        if(getName() != null && !getName().isEmpty()) {
            retStr.append(getName()).append(" = ");
        }

        if(getValue() != null) {
            String value = getValue();
            if(!value.isEmpty()) {
                if(getType().equalsIgnoreCase("string")) {
                    value = value.replace("\"", "\\\"");
                    value = BibTeXEntryFactory.mapUnicodeToTeXEntities(value);
                }
                if(strict && (value.length() > BibTeX.STRICT_LENGTH)) {
                    value = value.substring(0, BibTeX.STRICT_LENGTH);
                }
                retStr.append('"').append(value).append('"');
            }
        }
        retStr.append('}');
        return retStr.toString();
    }

    public String getPlainString() {
        StringBuilder retStr = null;
        retStr.append('@').append(getType()).append('{');
        if(getName() != null && !getName().isEmpty()) {
            retStr.append(getName()).append(" = ");
        }
        if(getValue() != null) {
            String value = getValue();
            value = value.replace("\"", "\\\"");
            retStr.append('"').append(value).append('"');
        }
        retStr.append('}');
        
        return retStr.toString();
    }
}
