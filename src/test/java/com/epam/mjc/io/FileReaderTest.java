package com.epam.mjc.io;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileReaderTest {

    private final static Path TEST_FILE = Path.of("src/main/resources/Profile.txt");
    private final static Path READER_CLASS = Path.of("src/main/java/com/epam/mjc/io/FileReader.java");
    private static final String PROFILE_PARAM_VALUE_DELIMETER = ": ";
    private static final String SOURCE_CODE_LOCATION = "src/main/java";


    @Test
    public void testProgramReadsDataCorrectly () {
        FileReader fileReader = new FileReader();
        Profile actual = fileReader.getDataFromFile(new File(TEST_FILE.toUri()));
        Profile expected = readFile(TEST_FILE);
        assertEquals(expected, actual);
    }

    @Test
    public void testCodeWithoutExternalUtils() throws IOException {
        final Path sources = Path.of(SOURCE_CODE_LOCATION);
        Files.walk(sources)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(this::assertSourceWithoutExternalUtils);
    }

    @Test
    public void testCodeHasStreamClosing() {
        String sourceCode = readFileIntoString(READER_CLASS);
        assertTrue(sourceCode.contains("try (") || sourceCode.contains("try(") || sourceCode.contains(".close()"));
    }
    @Test
    public void testCodeHasNio() {
        String sourceCode = readFileIntoString(READER_CLASS);
        assertTrue(sourceCode.contains(".nio"));
    }

    private void assertSourceWithoutExternalUtils(Path path) {
        String sourceCode = readFileIntoString(path);
        assertFalse(sourceCode.contains("FileUtils"));
        assertFalse(sourceCode.contains("IOUtils"));
        assertFalse(sourceCode.contains("Scanner"));
        assertFalse(sourceCode.contains("StreamTokenizer"));
    }

    private String readFileIntoString(Path sourcePath) {
        try {
            return Files.readString(sourcePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Profile readFile(Path path) {
        Profile profile = new Profile();
        try {
            Files.lines(path).forEach(line -> setValueToProfile(line, profile));
            return profile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setValueToProfile(String line, Profile profile) {
        Pair<String, String> keyValue = parseIntoParamValue(line);
        String paramName = keyValue.getLeft();
        String paramValue = keyValue.getRight();
        if (paramName.equalsIgnoreCase("name")) {
            profile.setName(paramValue);
        } else if (paramName.equalsIgnoreCase("age")) {
            profile.setAge(Integer.parseInt(paramValue));
        } else if (paramName.equalsIgnoreCase("email")) {
            profile.setEmail(paramValue);
        } else if (paramName.equalsIgnoreCase("phone")) {
            profile.setPhone(Long.parseLong(paramValue));
        }
    }

    private Pair<String, String> parseIntoParamValue(String line) {
        List<String> parsedString = Arrays.stream(line.split(PROFILE_PARAM_VALUE_DELIMETER)).collect(Collectors.toList());
        return Pair.of(parsedString.get(0), parsedString.get(1));
    }
}
