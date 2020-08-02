package edu.ted.templator;

import java.util.HashMap;
import java.util.Map;

public class Element {
    private String elementType;
    private String elementName;
    private Map<String, String> parameters = new HashMap<>();

    public Element(String elementType, String elementName) {
        this.elementType = elementType;
        this.elementName = elementName;
    }

    public String getElementParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    public void setElementParameter(String parameterName, String parameterValue) {
        parameters.put(parameterName, parameterValue);
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
}
