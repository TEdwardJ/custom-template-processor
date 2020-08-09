package edu.ted.templator.processor;

import edu.ted.templator.Element;
import edu.ted.templator.exception.NoListCanBeObtainedException;
import edu.ted.templator.processor.ElementProcessor;
import edu.ted.templator.processor.ListProcessor;
import edu.ted.templator.utils.ResourceReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ListProcessorTest {

    private final ListProcessor listProcessor = new ListProcessor(new ElementProcessor());
    private List<String> testLines;

    @BeforeEach
    public void init() throws FileNotFoundException {
        testLines = ResourceReader.readResourceToLines("", "pageWithList.html");
    }

    @Test
    public void givenTemplateLinesWithList_whenListSubstituted_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        List<String> productsList = new ArrayList<>();
        productsList.add("product1");
        productsList.add("product2");
        productsList.add("product3");
        parametersMap.put("productList", productsList);

        StringWriter writer = new StringWriter();
        final List<String> lines = listProcessor.processWithLists(testLines, parametersMap);
        lines.forEach(writer::write);
        assertTrue(writer.toString().contains("<SPAN>product1</SPAN>"));
        assertTrue(writer.toString().contains("<SPAN>product2</SPAN>"));
        assertTrue(writer.toString().contains("<SPAN>product3</SPAN>"));
        assertFalse(writer.toString().contains("<#list"));
    }

    @Test
    void findStartListElement() {
        Map<String, Object> parametersMap = new HashMap<>();

        List<String> linesList = Arrays.asList("Line 1", "Line2", "Line3");
        parametersMap.put("linesList", linesList);
        String templateLine = "<#list linesList as line>${line} </#list>";
        final Element startListElement = listProcessor.findStartListElement(templateLine);
        assertEquals("list", startListElement.getElementType());
        assertEquals("linesList", startListElement.getElementName());
        assertEquals("line", startListElement.getElementParameter("listItemName"));
    }

    @Test
    void findListElement() {
        Map<String, Object> parametersMap = new HashMap<>();

        List<String> linesList = Arrays.asList("Line 1", "Line2", "Line3");
        parametersMap.put("linesList", linesList);
        String templateLine = "<#list linesList as line>${line} </#list>";
        final Element listElement = listProcessor.findListElement(templateLine);
        assertEquals("list", listElement.getElementType());
        assertEquals("linesList", listElement.getElementName());
        assertEquals("${line} ", listElement.getElementParameter("listBody"));
    }

    @Test
    void givenListTemplateAndEmptyParametersMap_whenException_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        List<String> productsList = new ArrayList<>();
        productsList.add("product1");
        productsList.add("product2");
        productsList.add("product3");
        parametersMap.put("productList2", productsList);

        testLines.clear();
        testLines.add("<span>First Line</span>\n");
        testLines.add("<#list productList as product>\n");
        testLines.add("  <SPAN>${product}</SPAN>\n");
        testLines.add("</#list>");

        Throwable thrown = assertThrows(NoListCanBeObtainedException.class, () -> listProcessor.processWithLists(testLines, parametersMap));
        assertEquals("No list named productList exists",thrown.getMessage());
    }

    @Test
    void givenListTemplateAndNonIterableObjectInParametersMap_whenException_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();

        parametersMap.put("productList", new Object());

        testLines.clear();
        testLines.add("<span>First Line</span>\n");
        testLines.add("<#list productList as product>\n");
        testLines.add("  <SPAN>${product}</SPAN>\n");
        testLines.add("</#list>");

        StringWriter writer = new StringWriter();
        Throwable thrown = assertThrows(NoListCanBeObtainedException.class, () -> listProcessor.processWithLists(testLines, parametersMap));
        assertEquals("Parameter named productList is not Iterable",thrown.getMessage());
    }
}