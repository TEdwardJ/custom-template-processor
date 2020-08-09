package edu.ted.templator.processor;

import edu.ted.templator.exception.NoValueCanBeObtainedException;
import edu.ted.templator.interfaces.StageProcessor;
import edu.ted.templator.utils.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElementProcessor implements StageProcessor {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("(\\$\\{(?<valuePath>[^}?]+)(\\?*[^}]*)})");
    private static final String NAMED_TOKEN_PATTERN = "(\\$\\{(<tokenName>)(\\?*[^}]*)\\})";

    private static final Pattern FIELD_PATH_SPLITTING_PATTERN = Pattern.compile("(?<mapKey>[^.]+)(\\.(?<objectPath>.+))*");

    @Override
    public List<String> apply(List<String> lines, Map<String, Object> parametersMap) {
        return processWithElements(lines, parametersMap);
    }

    String processLineWithElements(String templateString, Map<String, Object> parametersMap) {
        Matcher matcher = TOKEN_PATTERN.matcher(templateString);
        while (matcher.find()) {
            String valuePath = matcher.group("valuePath");
            Object tokenValue = getTokenValue(valuePath, parametersMap);

            Pattern compiledParticularTokenPattern = Pattern.compile(NAMED_TOKEN_PATTERN.replaceAll("<tokenName>", valuePath));
            Matcher tokenMatcher = compiledParticularTokenPattern.matcher(templateString);
            if (tokenMatcher.find()) {
                templateString = tokenMatcher.replaceAll(Matcher.quoteReplacement(tokenValue.toString()));
            }
        }
        return templateString;
    }

    List<String> processWithElements(List<String> templateLines, Map<String, Object> parametersMap) {
        for (int i = 0; i < templateLines.size(); i++) {
            String templateString = templateLines.get(i);
            templateLines.set(i, processLineWithElements(templateString, parametersMap));
        }
        return templateLines;
    }

    private Object getTokenValue(String valuePath, Map<String, Object> parametersMap) {
        Matcher splitterRegexp = FIELD_PATH_SPLITTING_PATTERN.matcher(valuePath);
        if (splitterRegexp.find()) {
            String mapKey = splitterRegexp.group("mapKey");
            String objectPath = splitterRegexp.group("objectPath");
            Object objectFromParametersMap = Optional.ofNullable(parametersMap.get(mapKey)).orElse("null");
            if (objectPath == null) {
                return objectFromParametersMap;
            }
            try {
                return ReflectionUtils.getFieldValueByPass(objectPath, objectFromParametersMap);
            } catch (NoSuchFieldException e) {
                throw new NoValueCanBeObtainedException(e);
            }
        }
        return null;
    }
}
