package edu.ted.templator;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class TemplateProcessorTest {

    private TemplateProcessor processor = new TemplateProcessor("");

    @Test
    public void test2() throws FileNotFoundException {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("testPageTitle", "Template Processor Test Page");
        parametersMap.put("testPageBody", "Hello World!!!");
        parametersMap.put("field3", "value3");
        StringWriter writer = new StringWriter();
        processor.process("test.html", parametersMap, writer);

        assertTrue(writer.toString().contains("<title>Template Processor Test Page</title>"));
        assertTrue(writer.toString().contains("Hello World!!!"));
    }


    @Test
    public void test2_1() throws FileNotFoundException {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("testPageTitle", "Template Processor Test Page");
        parametersMap.put("testPageBody", "Hello World!!!");
        parametersMap.put("field3", "value3");
        StringWriter writer = new StringWriter();
        processor.process("pageWithInclude.html", parametersMap, writer);

        assertTrue(writer.toString().contains("<SPAN>It is included Content</SPAN>"));
        assertFalse(writer.toString().contains("<#include"));
    }


    @Test
    public void test2_2() throws FileNotFoundException {
        Map<String, Object> parametersMap = new HashMap<>();
        List<String> productsList = new ArrayList<>();
        productsList.add("product1");
        productsList.add("product2");
        productsList.add("product3");
        parametersMap.put("productList", productsList);

        StringWriter writer = new StringWriter();
        processor.process("pageWithList.html", parametersMap, writer);

        assertTrue(writer.toString().contains("<SPAN>product1</SPAN>"));
        assertTrue(writer.toString().contains("<SPAN>product2</SPAN>"));
        assertTrue(writer.toString().contains("<SPAN>product3</SPAN>"));
        assertFalse(writer.toString().contains("<#list"));
    }

    @Test
    public void givenNonExistingTemplateFile_whenFileNotFoundException_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("testPageTitle", "Template Processor Test Page");
        parametersMap.put("testPageBody", "Hello World!!!");
        parametersMap.put("field3", "value3");
        StringWriter writer = new StringWriter();
        Throwable thrown = assertThrows(FileNotFoundException.class, () -> processor.process("test2.html", parametersMap, writer));

        assertEquals("Template file test2.html not found", thrown.getMessage());
    }


}