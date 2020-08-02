package edu.ted.templator;

import edu.ted.templator.exception.NoValueCanBeObtainedException;
import edu.ted.templator.utils.ReflectionUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public class TemplateProcessor {
    private final Pattern fieldPathSplitPattern = Pattern.compile("([^.]+)(\\.(.+))*");

    private final Pattern tokenPattern = Pattern.compile("(\\$\\{([^}?]+)(\\?*[^}]*)\\})");
    private final String particularTokenPattern = "(\\$\\{(<tokenName>)(\\?*[^}]*)\\})";

    private final Pattern includeStartPattern = Pattern.compile("<#include");
    private final Pattern includePattern = Pattern.compile("<#include \"(.+)\"[ ]*>");

    private final Pattern listStartPattern = Pattern.compile("<#list[ ]+([^ ]+) as ([^ ]+)[ ]*>");
    private final Pattern listPattern = Pattern.compile("<#list[ ]+([^ ]+) as ([^ ]+)[ ]*>(.+)</#list>", Pattern.DOTALL + Pattern.MULTILINE);

    private final String baseDirectory;

    public TemplateProcessor(String baseDirectory) {
        if (baseDirectory.isEmpty()) {
            this.baseDirectory = baseDirectory;
        } else {
            this.baseDirectory = baseDirectory;
        }

    }

    List<String> processWithInclude(List<String> templateLines, Map<String, Object> parametersMap) {
        for (int i = 0; i < templateLines.size(); i++) {
            String templateString = templateLines.get(i);
            String line = templateString;

            Matcher includeStartMatcher = includeStartPattern.matcher(line);
            Matcher includeMatcher = includePattern.matcher(line);
            if (includeStartMatcher.find()) {
                System.out.println(templateString);
                if (includeMatcher.find()) {
                    String instruction = includeMatcher.group(0);

                    String includedFile = getIncludedFile(instruction, parametersMap);
                    templateString = includeMatcher.replaceAll(includedFile);
                }
                templateLines.set(i, templateString);
            }
        }
        return templateLines;
    }

    List<String> processWithLists(List<String> templateLines, Map<String, Object> parametersMap) {
        List<String> linesToReturn = new ArrayList<>();
        //<#list productList as product>
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < templateLines.size(); i++) {
            String line = templateLines.get(i);

            Element startListElement = findStartListElement(line);

            if (startListElement != null) {
                Element listElement;
                do {
                    buffer.append(templateLines.get(i));
                    listElement = findListElement(buffer.toString());
                    i++;
                } while (listElement == null && i < templateLines.size());

                if (listElement != null) {
                    String listBody = listElement.getElementParameter("listBody");
                    String parameterName = startListElement.getElementName();
                    String newParameterName = startListElement.getElementParameter("listItemName");
                    if (parametersMap.containsKey(parameterName)) {
                        Iterable list = (Iterable) parametersMap.get(parameterName);
                        for (Object element : list) {
                            Map<String, Object> iterMap = new HashMap<>();
                            iterMap.put(newParameterName, element);
                            linesToReturn.add(processWithElements(listBody, iterMap));
                        }
                    }
                }
            } else {
                linesToReturn.add(line);
            }
        }
        return linesToReturn;
    }

    Element findStartListElement(String line) {
        Matcher listStartMatcher = listStartPattern.matcher(line);
        if (listStartMatcher.find()) {
            String parameterName = listStartMatcher.group(1);
            String listItemName = listStartMatcher.group(2);
            Element listElement = new Element("list", parameterName);
            listElement.setElementParameter("listItemName", listItemName);
            return listElement;
        }
        return null;
    }

    Element findListElement(String line) {
        Matcher listMatcher = listPattern.matcher(line);
        if (listMatcher.find()) {
            String parameterName = listMatcher.group(1);
            String listItemName = listMatcher.group(2);
            String body = listMatcher.group(3);
            Element listElement = new Element("list", parameterName);
            listElement.setElementParameter("listBody", body);
            return listElement;
        }
        return null;
    }

    private String getIncludedFile(String instruction, Map<String, Object> parametersMap) {
        String fileName;
        Matcher matcher = includePattern.matcher(instruction);
        if (matcher.find()) {
            fileName = matcher.group(1);
            StringWriter writer = new StringWriter();
            try {
                process(fileName, parametersMap, writer);
                return writer.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String processWithElements(String templateString, Map<String, Object> parametersMap) {
        Matcher matcher = tokenPattern.matcher(templateString);
        while (matcher.find()) {
            String tokenName = matcher.group(2);
            Object tokenValue = getTokenValue(tokenName, parametersMap);

            final Pattern compiledParticularTokenPattern = Pattern.compile(particularTokenPattern.replaceAll("<tokenName>", tokenName));
            Matcher tokenMatcher = compiledParticularTokenPattern.matcher(templateString);
            if (tokenMatcher.find()) {
                templateString = tokenMatcher.replaceAll(Matcher.quoteReplacement(tokenValue.toString()));
            }
        }
        return templateString;
    }

    private Object getTokenValue(String tokenName, Map<String, Object> parametersMap) {
        Matcher splitter = fieldPathSplitPattern.matcher(tokenName);
        if (splitter.find()) {
            String mapPath = splitter.group(1);
            String objectPath = splitter.group(3);
            final Object objectFromParametersMap = Optional.ofNullable(parametersMap.get(mapPath)).orElse("null");
            if (objectPath != null) {
                try {
                    Object value = ReflectionUtils.getFieldValueByPass(objectPath, objectFromParametersMap);
                    return value;
                } catch (NoSuchFieldException e) {
                    throw new NoValueCanBeObtainedException(e);
                }
            } else {
                return objectFromParametersMap;
            }
        }
        return null;
    }

    public void process(String templateFile, Map<String, Object> parametersMap, Writer writer) throws FileNotFoundException {
        final URL resource = getClass().getClassLoader().getResource(baseDirectory + templateFile);
        if (resource == null) {
            throw new FileNotFoundException("Resource File " + templateFile + " not found");
        }
        Path path = null;
        try {
            path = Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            List<String> resourceLines = Files.lines(path).collect(toList());
            process(resourceLines, parametersMap, writer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(List<String> linesList, Map<String, Object> parametersMap, Writer writer) throws IOException {
        List<String> linesWithInclude = processWithInclude(linesList, parametersMap);
        List<String> linesWithLists = processWithLists(linesWithInclude, parametersMap);
        List<String> linesWithElements = linesWithLists
                .stream()
                .map(line -> processWithElements(line, parametersMap))
                .collect(toList());
        for (String line : linesWithElements) {
            writer.write(line);
        }
        writer.flush();
    }
}
