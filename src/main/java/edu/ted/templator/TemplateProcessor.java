package edu.ted.templator;

import edu.ted.templator.exception.NoValueCanBeObtainedException;
import edu.ted.templator.utils.ReflectionUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;


public class TemplateProcessor {
    private static final Pattern FIELD_PATH_SPLITTING_PATTERN = Pattern.compile("([^.]+)(\\.(.+))*");

    private static final Pattern TOKEN_PATTERN = Pattern.compile("(\\$\\{([^}?]+)(\\?*[^}]*)\\})");
    private static final String NAMED_TOKEN_PATTERN = "(\\$\\{(<tokenName>)(\\?*[^}]*)\\})";

    private static final Pattern INCLUDE_INSTRUCTION_PATTERN = Pattern.compile("<#include \"(.+)\"[ ]*>");

    private final static Pattern LIST_START_PATTERN = Pattern.compile("<#list[ ]+([^ ]+) as ([^ ]+)[ ]*>");
    private final static Pattern LIST_PATTERN = Pattern.compile("<#list[ ]+([^ ]+) as ([^ ]+)[ ]*>(.+)</#list>", Pattern.DOTALL + Pattern.MULTILINE);

    private final String baseDirectory;

    public TemplateProcessor(String baseDirectory) {
            this.baseDirectory = baseDirectory;
    }

    List<String> processWithInclude(List<String> templateLines, Map<String, Object> parametersMap) {
        for (int i = 0; i < templateLines.size(); i++) {
            String templateLine = templateLines.get(i);

            Matcher includeMatcher = INCLUDE_INSTRUCTION_PATTERN.matcher(templateLine);

            if (includeMatcher.find()) {
                String fileDescriptor = includeMatcher.group(1);

                String includedFile = processIncludedFileAndGet(fileDescriptor, parametersMap);
                templateLine = includeMatcher.replaceAll(includedFile);
            }
            templateLines.set(i, templateLine);
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
                        Iterable<Object> list = (Iterable) parametersMap.get(parameterName);
                        for (Object element : list) {
                            Map<String, Object> currentIterationParametersMap = new HashMap<>();
                            currentIterationParametersMap.put(newParameterName, element);
                            linesToReturn.addAll(processLineWithElements(Arrays.asList(new String[]{listBody}), currentIterationParametersMap));
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
        Matcher listStartMatcher = LIST_START_PATTERN.matcher(line);
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
        Matcher listMatcher = LIST_PATTERN.matcher(line);
        if (listMatcher.find()) {
            String parameterName = listMatcher.group(1);
            String body = listMatcher.group(3);
            Element listElement = new Element("list", parameterName);
            listElement.setElementParameter("listBody", body);
            return listElement;
        }
        return null;
    }

    private String processIncludedFileAndGet(String fileName, Map<String, Object> parametersMap) {
        try {
            StringWriter writer = new StringWriter();
            process(fileName, parametersMap, writer);
            return writer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    String processLineWithElements(String templateString, Map<String, Object> parametersMap) {
        Matcher matcher = TOKEN_PATTERN.matcher(templateString);
        while (matcher.find()) {
            String valuePath = matcher.group(2);
            Object tokenValue = getTokenValue(valuePath, parametersMap);

            Pattern compiledParticularTokenPattern = Pattern.compile(NAMED_TOKEN_PATTERN.replaceAll("<tokenName>", valuePath));
            Matcher tokenMatcher = compiledParticularTokenPattern.matcher(templateString);
            if (tokenMatcher.find()) {
                templateString = tokenMatcher.replaceAll(Matcher.quoteReplacement(tokenValue.toString()));
            }
        }
        return templateString;
    }

    public List<String> processLineWithElements(List<String> templateLines, Map<String, Object> parametersMap) {
        for (int i = 0; i < templateLines.size(); i++) {
            String templateString = templateLines.get(i);
            templateLines.set(i, processLineWithElements(templateString, parametersMap));
        }
        return templateLines;
    }

    private Object getTokenValue(String valuePath, Map<String, Object> parametersMap) {
        Matcher splitterRegexp = FIELD_PATH_SPLITTING_PATTERN.matcher(valuePath);
        if (splitterRegexp.find()) {
            String mapPath = splitterRegexp.group(1);
            String objectPath = splitterRegexp.group(3);
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
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(baseDirectory + templateFile);
             InputStreamReader isReader = new InputStreamReader(resourceAsStream);
             BufferedReader reader = new BufferedReader(isReader)) {
            List<String> resourceLines = reader.lines().collect(toList());
            process(resourceLines, parametersMap, writer);
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new FileNotFoundException("Template file " + templateFile + " not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(List<String> linesList, Map<String, Object> parametersMap, Writer writer) throws
            IOException {
        List<String> linesWithInclude = processWithInclude(linesList, parametersMap);
        List<String> linesWithLists = processWithLists(linesWithInclude, parametersMap);
        List<String> linesWithElements = processLineWithElements(linesWithLists, parametersMap);
        for (String line : linesWithElements) {
            writer.write(line);
        }
        writer.flush();
    }
}
