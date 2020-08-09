package edu.ted.templator.processor;

import edu.ted.templator.Element;
import edu.ted.templator.exception.NoListCanBeObtainedException;
import edu.ted.templator.interfaces.StageProcessor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListProcessor implements StageProcessor {

    private final static Pattern LIST_START_PATTERN = Pattern.compile("<#list[ ]+(?<mapKeyName>[^ ]+) as (?<listItemName>[^ ]+)[ ]*>");
    private final static Pattern LIST_PATTERN = Pattern.compile("<#list[ ]+(?<mapKeyName>[^ ]+) as (?<listItemName>[^ ]+)[ ]*>(?<listBody>.+)</#list>", Pattern.DOTALL + Pattern.MULTILINE);

    private final ElementProcessor elementProcessor;

    public ListProcessor(ElementProcessor elementProcessor) {
        this.elementProcessor = elementProcessor;
    }

    @Override
    public List<String> apply(List<String> lines, Map<String, Object> parametersMap) {
        return processWithLists(lines, parametersMap);
    }

    List<String> processWithLists(List<String> templateLines, Map<String, Object> parametersMap) {
        List<String> linesToReturn = new ArrayList<>();
        //<#list productList as product>
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < templateLines.size(); i++) {
            String line = templateLines.get(i);
            if (findStartListElement(line) == null) {
                linesToReturn.add(line);
            } else {
                Element listElement;
                do {
                    buffer.append(templateLines.get(i));
                    i++;
                } while ((listElement = findListElement(buffer.toString())) == null && i < templateLines.size());

                if (listElement != null) {
                    String listBody = listElement.getElementParameter("listBody");
                    String parameterName = listElement.getElementName();
                    String newParameterName = listElement.getElementParameter("listItemName");
                    if (parametersMap.containsKey(parameterName)) {
                        Iterable<Object> list = null;
                        try {
                            list = (Iterable<Object>) parametersMap.get(parameterName);
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                            throw new NoListCanBeObtainedException("Parameter named " + parameterName + " is not Iterable");
                        }
                        Map<String, Object> currentIterationParametersMap = new HashMap<>();
                        for (Object element : list) {
                            currentIterationParametersMap.put(newParameterName, element);
                            linesToReturn.add(elementProcessor.processLineWithElements(listBody, currentIterationParametersMap));
                        }
                    } else {
                        throw new NoListCanBeObtainedException("No list named " + parameterName + " exists");
                    }
                }
            }
        }
        return linesToReturn;
    }

    Element findStartListElement(String line) {
        Matcher listStartMatcher = LIST_START_PATTERN.matcher(line);
        if (listStartMatcher.find()) {
            String mapKeyName = listStartMatcher.group("mapKeyName");
            String listItemName = listStartMatcher.group("listItemName");
            Element listElement = new Element("list", mapKeyName);
            listElement.setElementParameter("listItemName", listItemName);
            return listElement;
        }
        return null;
    }

    Element findListElement(String line) {
        Matcher listMatcher = LIST_PATTERN.matcher(line);
        if (listMatcher.find()) {
            String mapKeyName = listMatcher.group("mapKeyName");
            String listItemName = listMatcher.group("listItemName");
            String body = listMatcher.group("listBody");
            Element listElement = new Element("list", mapKeyName);
            listElement.setElementParameter("listItemName", listItemName);
            listElement.setElementParameter("listBody", body);
            return listElement;
        }
        return null;
    }


}
