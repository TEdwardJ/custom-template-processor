package edu.ted.templator.interfaces;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface StageProcessor extends BiFunction<List<String>, Map<String, Object>, List<String>> {
}
