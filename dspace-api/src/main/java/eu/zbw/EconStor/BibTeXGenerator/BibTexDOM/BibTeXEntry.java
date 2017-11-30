/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.zbw.EconStor.BibTeXGenerator.BibTexDOM;

import java.util.Collection;
//import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Riese Wolfgang
 */
public class BibTeXEntry implements BibTexComponent, Cloneable {
    BibTeX _owningDocument = null;
    String _entryName = null;
    String _typeName = null;
    String _typeDescription = null;
    String[] _mandatoryFields = null;
    String[] _optionalFields = null;
    //LinkedHashMap<String, BibTeXField> _fields = new LinkedHashMap<String, BibTeXField>();
    Map<String, BibTeXField> _fields = new TreeMap<String, BibTeXField>();

    
    protected BibTeXEntry(String typeName, String typeDescription, String[] mandatoryF, String[] optionalF) {
        if(typeName != null && !typeName.isEmpty()) {
            _typeName = typeName;
            if(typeDescription != null) {
                _typeDescription = typeDescription;
            } else { _typeDescription = ""; }
            init(mandatoryF, optionalF);
        } else {
            if(typeName == null) {
                throw new NullPointerException("Required parameter typeName was null!");
            } else {
                throw new IllegalArgumentException("Required parameter typeName was empty!");
            }
        }
    }

    private void init(String[] mandatoryF, String[] optionalF) {
        if(mandatoryF != null) {
            _mandatoryFields = mandatoryF;
            for (int i = 0; i < mandatoryF.length; i++) {
                String mandatoryFN = mandatoryF[i];
                addFieldAndValue(mandatoryFN, "");
            }
        }
        if(optionalF != null) {
            _optionalFields = optionalF;
            for (int i = 0; i < optionalF.length; i++) {
                String optionalFN = optionalF[i];
                addFieldAndValue(optionalFN, "");
            }
        }
    }

    public void setEntryName(String entryName) {
        if(entryName != null) {
            _entryName = entryName;
        }
    }

    public final void addFieldAndValue(String fieldName, String fieldValue) {
        if(fieldName != null && !fieldName.isEmpty() && fieldValue != null) {
            _fields.put(fieldName, new BibTeXField(fieldName, fieldValue));
        }
    }

    public void addOwningBibTexDocument(BibTeX owner) {
        if(owner != null) {
            this._owningDocument = owner;
        } else {
            this._owningDocument = null;
        }
    }

    public boolean removeOwningBibTexDocument(BibTeX owner) {
        boolean succ = false;
        
        if(this._owningDocument == owner) {
            this._owningDocument = null;
            succ = true;
        }

        return succ;
    }

    public String getTypeDescription() {
        return _typeDescription;
    }

    public String getTypeName() {
        return _typeName;
    }

    public String[] getMandatoryFields() {
        return _mandatoryFields;
    }

    public String[] getOptionalFields() {
        return _optionalFields;
    }

    public boolean allMandatoryFieldsSet() {
        boolean allSet = true;

        if(_mandatoryFields != null && _fields != null) {
            for (int i = 0; i < _mandatoryFields.length; i++) {
                String mandatoryF = _mandatoryFields[i];
                if(mandatoryF.contains("|")) {
                    String[] alterns = mandatoryF.split("\\|");
                    boolean atleastone = false;
                    for(String altern : alterns) {
                        if(_fields.containsKey(altern)
                                && !((_fields.get(altern)).getValue().isEmpty())) {
                            atleastone = true;
                            break;
                        }
                    }
                    if(!atleastone) {
                        allSet = false;
                        break;
                    }
                } else {
                    if(!_fields.containsKey(mandatoryF)
                            || ((_fields.get(mandatoryF)).getValue().isEmpty())) {
                        allSet = false;
                        break;
                    }
                }
            }
        } else {
            allSet = false;
        }

        return allSet;
    }

    public boolean hasOptionalFieldsSet() {
        boolean hasOptional = false;

        for (int i = 0; i < _optionalFields.length; i++) {
            String optional = _optionalFields[i];
            if(_fields.containsKey(optional) && !(_fields.get(optional).getValue().isEmpty())) {
                hasOptional = true;
                break;
            }
        }

        return hasOptional;
    }

    public String getEntryName() {
        return _entryName;
    }



    @Override
    protected BibTeXEntry clone() throws CloneNotSupportedException {

        BibTeXEntry clown = new BibTeXEntry(this.getTypeName(),
                                            this.getTypeDescription(),
                                            this.getMandatoryFields(),
                                            this.getOptionalFields());

        if(this._mandatoryFields != null) {
            clown.addOwningBibTexDocument(_owningDocument);
        }
        
        return clown;
    }

    public String getBibTeXCode(boolean strict) {
        StringBuilder retStr = new StringBuilder();
        
        //if(allMandatoryFieldsSet()) {
            retStr.append('@').append(getTypeName()).append('{');
            if(getEntryName()!= null) { retStr.append(getEntryName()); }
            retStr.append(",\n");
            Collection<BibTeXField> fieldsC = _fields.values();
            int length = fieldsC.size();
            int count = 0;
            for (BibTeXField bibTeXField : fieldsC) {
                count++;
                if(!bibTeXField.getValue().isEmpty()) {
                    retStr.append(bibTeXField.getBibTeXCode(strict));
                    if(count < length) {
                        retStr.append(",\n");
                    } else {
                        retStr.append("\n");
                    }
                }
            }
            
            retStr.append("}\n");
        //}
        
        return retStr.toString();
    }


    public String getPlainString() {
        StringBuilder retStr = new StringBuilder();

        //if(allMandatoryFieldsSet()) {
            retStr.append('@').append(getTypeName()).append('{');
            if(getEntryName()!= null) { retStr.append(getEntryName()); }
            retStr.append(",\n");
            Collection<BibTeXField> fieldsC = _fields.values();
            int length = fieldsC.size();
            int count = 0;
            for (BibTeXField bibTeXField : fieldsC) {
                count++;
                if(!bibTeXField.getValue().isEmpty()) {
                    retStr.append(bibTeXField.getPlainString());
                    if(count < length) {
                        retStr.append(",\n");
                    } else {
                        retStr.append("\n");
                    }
                }
            }

            retStr.append("}\n");
        //}

        return retStr.toString();
    }
}
