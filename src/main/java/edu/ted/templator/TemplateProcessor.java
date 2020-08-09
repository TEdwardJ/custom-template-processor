package edu.ted.templator;

import edu.ted.templator.configuration.TemplateProcessorConfiguration;
import edu.ted.templator.interfaces.StageProcessor;
import edu.ted.templator.utils.ResourceReader;

import java.io.*;
import java.util.*;

public class TemplateProcessor {
    private TemplateProcessorConfiguration configuration;

    private final String baseDirectory;

    public TemplateProcessor(String baseDirectory) {
        this.baseDirectory = baseDirectory;
        configuration = TemplateProcessorConfiguration.getTypicalTemplateProcessorConfiguration(baseDirectory);
    }

    public void process(String templateFile, Map<String, Object> parametersMap, Writer writer) throws FileNotFoundException {
        List<String> resourceLines = ResourceReader.readResourceToLines(baseDirectory, templateFile);
        try {
            process(resourceLines, parametersMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(List<String> linesList, Map<String, Object> parametersMap, Writer writer) throws IOException {
        List<String> processedLines = linesList;
        for (StageProcessor stageProcessor : configuration.getStageProcessors()) {
            processedLines = stageProcessor.apply(processedLines, parametersMap);
        }
        for (String line : processedLines) {
            writer.write(line);
        }
        writer.flush();
    }
}
