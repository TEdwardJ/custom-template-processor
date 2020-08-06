package edu.ted.templator;

import edu.ted.templator.utils.ReflectionUtilsTest;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class TemplateProcessorTest {

    private TemplateProcessor processor = new TemplateProcessor("");

    @Test
    public void givenTemplate_whenFieldsSubstitutedForValue_thenCorrect(){
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have 3 fields with the values ${field1}, ${field2}, ${field3} respectfully";
        parametersMap.put("field1", "value1");
        parametersMap.put("field2", "value2");
        parametersMap.put("field3", "value3");
        String preparedString = processor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have 3 fields with the values value1, value2, value3 respectfully", preparedString);
    }

    @Test
    public void givenTemplateWithFieldsWithFormattingOptions_whenSubstitutedForValue_thenCorrect(){
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have 3 fields with the values ${field1}, ${field2?string.@price}, ${field3} respectfully";
        parametersMap.put("field1", "value1");
        parametersMap.put("field2", new BigDecimal(56));
        parametersMap.put("field3", "value3");
        String preparedString = processor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have 3 fields with the values value1, 56, value3 respectfully", preparedString);
    }

    @Test
    public void givenComplexObjectAndComplexFieldPath_whenSubstitutedForValue_thenCorrect(){
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have fields with the value ${object.field1.field1}";
        ReflectionUtilsTest.A objectA = new ReflectionUtilsTest.A();
        ReflectionUtilsTest.B objectB = new ReflectionUtilsTest.B();
        objectB.setField1("objectB.field1Value");
        objectA.setField1(objectB);
        parametersMap.put("object", objectA);
        String preparedString = processor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have fields with the value objectB.field1Value", preparedString);
    }

    @Test
    public void givenComplexObjectAndComplexFieldPath_whenSubstituteForValueWithRegexpSpecialCharacter_thenCorrect(){
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have fields with the value ${object.field1.field1}";
        ReflectionUtilsTest.A objectA = new ReflectionUtilsTest.A();
        ReflectionUtilsTest.B objectB = new ReflectionUtilsTest.B();
        objectB.setField1("objectB.$field1Value");
        objectA.setField1(objectB);
        parametersMap.put("object", objectA);
        String preparedString = processor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have fields with the value objectB.$field1Value", preparedString);
    }

    @Test
    public void test2() throws FileNotFoundException {
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have 3 fields with the values ${field1}, ${field2}, ${field3} respectfully";
        parametersMap.put("testPageTitle", "Template Processor Test Page");
        parametersMap.put("testPageBody", "Hello World!!!");
        parametersMap.put("field3", "value3");
        StringWriter writer = new StringWriter();
        processor.process("test.html", parametersMap, writer);

        assertTrue( writer.toString().contains("<title>Template Processor Test Page</title>"));
        assertTrue( writer.toString().contains("Hello World!!!"));
    }


    @Test
    public void test2_1() throws FileNotFoundException {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("testPageTitle", "Template Processor Test Page");
        parametersMap.put("testPageBody", "Hello World!!!");
        parametersMap.put("field3", "value3");
        StringWriter writer = new StringWriter();
        processor.process("pageWithInclude.html", parametersMap, writer);

        assertTrue( writer.toString().contains("<SPAN>It is included Content</SPAN>"));
        assertFalse( writer.toString().contains("<#include"));
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

        assertTrue( writer.toString().contains("<SPAN>product1</SPAN>"));
        assertTrue( writer.toString().contains("<SPAN>product2</SPAN>"));
        assertTrue( writer.toString().contains("<SPAN>product3</SPAN>"));
        assertFalse( writer.toString().contains("<#list"));
    }

    @Test
    public void givenNonExistingTemplateFile_whenFileNotFoundException_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("testPageTitle", "Template Processor Test Page");
        parametersMap.put("testPageBody", "Hello World!!!");
        parametersMap.put("field3", "value3");
        StringWriter writer = new StringWriter();
        Throwable thrown = assertThrows(FileNotFoundException.class, ()->processor.process("test2.html", parametersMap, writer));

        assertEquals("Template file test2.html not found", thrown.getMessage());
    }


}