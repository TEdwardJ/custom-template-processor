package edu.ted.templator.utils;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceReaderTest {

    @Test
    void givenExistingResourceFile_whenReadAndReturnsLines_thenCorrect() throws FileNotFoundException {
        final List<String> lines = ResourceReader.readResourceToLines("", "test.html");
        assertFalse(lines.isEmpty());
        assertEquals("<!DOCTYPE html>", lines.get(0));
        assertEquals("</html>", lines.get(lines.size()-1));
    }

    @Test
    void givenNonExistingResourceFile_whenThrowsFileNotFoundException_thenCorrect() throws FileNotFoundException {
        assertThrows(FileNotFoundException.class,()->ResourceReader.readResourceToLines("", "test44.html"));

    }
}