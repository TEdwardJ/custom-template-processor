package edu.ted.templator.processor;

import edu.ted.templator.interfaces.StageProcessor;
import edu.ted.templator.processor.IncludeProcessor;
import edu.ted.templator.utils.ResourceReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IncludeProcessorTest {

    private final StageProcessor includeProcessor = new IncludeProcessor("");
    private List<String> testLines;

    @BeforeEach
    public void init() throws FileNotFoundException {
        testLines = ResourceReader.readResourceToLines("","pageWithInclude.html");
    }
    @Test
    public void givenTestWithIncludeInstruction_whenContentLoaded_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        final List<String> processedLines = includeProcessor.apply(testLines, parametersMap);
        StringWriter writer = new StringWriter();
        processedLines.forEach(line -> writer.write(line));

        assertTrue(writer.toString().contains("<SPAN>It is included Content</SPAN>"));
        assertTrue(writer.toString().contains("${divContent}"));
        assertFalse(writer.toString().contains("<#include"));
    }

    @Test
    public void givenTestWithIncludeNonExistingFile_whenContentLoaded_thenCorrect() {
        testLines.add("<#include \"nonExisting.html\">");
        Map<String, Object> parametersMap = new HashMap<>();
        final List<String> processedLines = includeProcessor.apply(testLines, parametersMap);
        StringWriter writer = new StringWriter();
        processedLines.forEach(line -> writer.write(line));

        assertTrue(writer.toString().contains("<SPAN>It is included Content</SPAN>"));
        assertTrue(writer.toString().contains("${divContent}"));
        assertTrue(writer.toString().contains("<#include"));
    }
}