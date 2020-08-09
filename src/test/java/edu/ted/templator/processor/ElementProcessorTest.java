package edu.ted.templator.processor;

import edu.ted.templator.processor.ElementProcessor;
import edu.ted.templator.utils.ReflectionUtilsTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ElementProcessorTest {

    private final ElementProcessor elementProcessor = new ElementProcessor();

    @Test
    public void givenTemplateLine_whenFieldsSubstitutedForNullValue_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have 3 fields with the values ${field1}, ${field2}, ${field3} respectfully";
        parametersMap.put("field1", null);
        parametersMap.put("field2", "value2");
        parametersMap.put("field3", "value3");
        String preparedString = elementProcessor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have 3 fields with the values null, value2, value3 respectfully", preparedString);
    }

    @Test
    public void givenTemplateLine_whenFieldsSubstitutedForValue_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have 3 fields with the values ${field1}, ${field2}, ${field3} respectfully";
        parametersMap.put("field1", "value1");
        parametersMap.put("field2", "value2");
        parametersMap.put("field3", "value3");
        String preparedString = elementProcessor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have 3 fields with the values value1, value2, value3 respectfully", preparedString);
    }

    @Test
    public void givenTemplateLines_whenFieldsSubstitutedForValue_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        List<String> templateLines = Arrays.asList("Let`s suppose we have 3 fields with the values ${field1},"," ${field2}, ${field3} respectfully");
        parametersMap.put("field1", "value1");
        parametersMap.put("field2", "value2");
        parametersMap.put("field3", "value3");
        List<String> preparedString = elementProcessor.processWithElements(templateLines, parametersMap);

        assertEquals("Let`s suppose we have 3 fields with the values value1,", preparedString.get(0));
        assertEquals(" value2, value3 respectfully", preparedString.get(1));
    }

    @Test
    public void givenTemplateWithFieldsWithFormattingOptions_whenSubstitutedForValue_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have 3 fields with the values ${field1}, ${field2?string.@price}, ${field3} respectfully";
        parametersMap.put("field1", "value1");
        parametersMap.put("field2", new BigDecimal(56));
        parametersMap.put("field3", "value3");
        String preparedString = elementProcessor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have 3 fields with the values value1, 56, value3 respectfully", preparedString);
    }

    @Test
    public void givenComplexObjectAndComplexFieldPath_whenSubstitutedForValue_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have fields with the value ${object.field1.field1}";
        ReflectionUtilsTest.A objectA = new ReflectionUtilsTest.A();
        ReflectionUtilsTest.B objectB = new ReflectionUtilsTest.B();
        objectB.setField1("objectB.field1Value");
        objectA.setField1(objectB);
        parametersMap.put("object", objectA);
        String preparedString = elementProcessor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have fields with the value objectB.field1Value", preparedString);
    }
    @Test
    public void givenComplexObjectAndComplexFieldPath_whenSubstituteForValueWithRegexpSpecialCharacter_thenCorrect() {
        Map<String, Object> parametersMap = new HashMap<>();
        String templateString = "Let`s suppose we have fields with the value ${object.field1.field1}";
        ReflectionUtilsTest.A objectA = new ReflectionUtilsTest.A();
        ReflectionUtilsTest.B objectB = new ReflectionUtilsTest.B();
        objectB.setField1("objectB.${aaa}field1Value");
        objectA.setField1(objectB);
        parametersMap.put("object", objectA);
        String preparedString = elementProcessor.processLineWithElements(templateString, parametersMap);

        assertEquals("Let`s suppose we have fields with the value objectB.${aaa}field1Value", preparedString);
    }

}