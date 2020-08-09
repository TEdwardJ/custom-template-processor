package edu.ted.templator.processor;

import edu.ted.templator.interfaces.StageProcessor;
import edu.ted.templator.utils.ResourceReader;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IncludeProcessor implements StageProcessor {
    private static final Pattern INCLUDE_INSTRUCTION_PATTERN = Pattern.compile("<#include \"(.+)\"[ ]*>");
    private final String baseDirectory;

    public IncludeProcessor(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public List<String> apply(List<String> strings, Map<String, Object> parametersMap) {
        return processWithInclude(strings);
    }

    List<String> processWithInclude(List<String> templateLines) {
        for (int i = 0; i < templateLines.size(); i++) {
            String templateLine = templateLines.get(i);

            Matcher includeMatcher = INCLUDE_INSTRUCTION_PATTERN.matcher(templateLine);

            if (includeMatcher.find()) {
                String fileDescriptor = includeMatcher.group(1);

                String includedFile;
                try {
                    includedFile = processIncludedFileAndGet(fileDescriptor);
                    templateLine = includeMatcher.replaceAll(Matcher.quoteReplacement(includedFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return templateLines;
                }
            }
            templateLines.set(i, templateLine);
        }
        return templateLines;
    }

    private String processIncludedFileAndGet(String fileName) throws FileNotFoundException {
        List<String> includedLines = ResourceReader.readResourceToLines(baseDirectory, fileName);
        List<String> processedIncludedLines = apply(includedLines, null);
        StringWriter writer = new StringWriter();
        processedIncludedLines.forEach(writer::write);
        return writer.toString();
    }

}
