package edu.ted.templator.utils;

import edu.ted.templator.Element;
import edu.ted.templator.exception.NoValueCanBeObtainedException;
import edu.ted.templator.utils.ReflectionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionUtilsTest {

    public static class A {
        private int intValue = 122;
        private B field1;
        private C field2;
        private F field3;

        public A() {
        }

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public B getField1() {
            return field1;
        }

        public void setField1(B field1) {
            this.field1 = field1;
        }

        public C getField2() {
            return field2;
        }

        public void setField2(C field2) {
            this.field2 = field2;
        }

        public F getField3() {
            return field3;
        }

        public void setField3(F field3) {
            this.field3 = field3;
        }
    }

    @Test
    public void test1() throws NoSuchFieldException {
        Element element = new Element("item", "itemName");
        Object objectField1 = ReflectionUtils.getFieldValue("elementType", element);
        assertEquals(element.getElementType(),objectField1.toString());
        Object objectField2 = ReflectionUtils.getFieldValue("elementName", element);
        assertEquals(element.getElementName(),objectField2.toString());
    }

    @Test
    public void test2() throws NoSuchFieldException {
        Element element = new Element("item", "itemName");
        Object objectField1 = ReflectionUtils.getFieldValueByPass("elementType", element);
        assertEquals(element.getElementType(),objectField1.toString());
        Object objectField2 = ReflectionUtils.getFieldValue("elementName", element);
        assertEquals(element.getElementName(),objectField2.toString());
    }

    @Test
    public void test3() throws NoSuchFieldException {
        A objectA = new A();
        B objectB = new B();
        objectB.setField1("objectB.field1Value");
        objectA.setField1(objectB);
        Object result = ReflectionUtils.getFieldValueByPass("field1.field1", objectA);
        assertTrue(result instanceof String);
        assertEquals("objectB.field1Value", result);
    }

    @Test
    public void test3_1() throws NoSuchFieldException {
        A objectA = new A();

        Object result = ReflectionUtils.getFieldValueByPass("intValue", objectA);
        //assertTrue(result instanceof String);
        assertEquals("122", result.toString());
    }

    @Test
    public void capitalizeTest(){
        String string1 = "value";
        String string2 = "fieldName";
        String string3 = "elementType";
        assertEquals("Value", ReflectionUtils.capitalize(string1));
        assertEquals("FieldName", ReflectionUtils.capitalize(string2));
        assertEquals("ElementType", ReflectionUtils.capitalize(string3));
    }

    @Test
    public void testGetValueOfFieldWithoutGetter() throws NoSuchFieldException {
        B objectB = new B();
        objectB.setField1("objectB.field1Value");
        Throwable thrown = assertThrows(NoValueCanBeObtainedException.class, ()->ReflectionUtils.getFieldValueByPass("fieldWithoutGetter", objectB));

    }

    public class C {
        private F field1;

        public C() {
        }
    }

    public static class B {
        private String field1;
        private String field2;
        private F field3;
        private String fieldWithoutGetter;

        public B() {
            fieldWithoutGetter = "fieldWithoutGetterValue";
        }

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public String getField2() {
            return field2;
        }

        public void setField2(String field2) {
            this.field2 = field2;
        }

        public F getField3() {
            return field3;
        }

        public void setField3(F field3) {
            this.field3 = field3;
        }
    }

    public class F {
        private A field1;

        public F() {
        }
    }
}