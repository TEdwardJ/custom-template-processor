package edu.ted.templator.utils;

import java.io.*;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ResourceReader {
    public static List<String> readResourceToLines(String baseDirectory, String templateFile) throws FileNotFoundException {
        try (InputStream resourceAsStream = ResourceReader.class.getClassLoader().getResourceAsStream(baseDirectory + templateFile);
             InputStreamReader isReader = new InputStreamReader(resourceAsStream);
             BufferedReader reader = new BufferedReader(isReader)) {
            List<String> resourceLines = reader.lines().collect(toList());
            return resourceLines;
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new FileNotFoundException("Template file " + templateFile + " not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
