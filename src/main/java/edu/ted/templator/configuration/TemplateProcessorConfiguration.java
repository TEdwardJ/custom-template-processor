package edu.ted.templator.configuration;

import edu.ted.templator.interfaces.StageProcessor;
import edu.ted.templator.processor.ElementProcessor;
import edu.ted.templator.processor.IncludeProcessor;
import edu.ted.templator.processor.ListProcessor;

import java.util.Arrays;
import java.util.List;

public class TemplateProcessorConfiguration {
    private List<StageProcessor> stageProcessors;

    public TemplateProcessorConfiguration(StageProcessor... stageProcessors) {
        this.stageProcessors = Arrays.asList(stageProcessors);
    }

    public List<StageProcessor> getStageProcessors() {
        return stageProcessors;
    }

    public static TemplateProcessorConfiguration getTypicalTemplateProcessorConfiguration(String baseDirectory){
        StageProcessor includeProcessor = new IncludeProcessor(baseDirectory);
        StageProcessor elementsProcessor = new ElementProcessor();
        StageProcessor listProcessor = new ListProcessor((ElementProcessor)elementsProcessor);
        return new TemplateProcessorConfiguration(includeProcessor, listProcessor, elementsProcessor);
    }
}
