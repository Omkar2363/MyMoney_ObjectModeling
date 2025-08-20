package com.geektrust.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("App Test")
class AppTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        createInvalidCommandFile();
    }

    private void createInvalidCommandFile() {
        try {
            new File("sample_input").mkdirs();
            FileWriter writer = new FileWriter("sample_input/invalid_command_input.txt");
            writer.write("INVALID_COMMAND 1000 2000\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test Input File 1")
    void runTestInput1() {
        // Arrange
        String arguments = "sample_input/input1.txt";

        String expectedOutput = "19050 5120 16412\n" +
                "16500 3300 13200\n" +
                "42807 9129 41808\n" +
                "36813 8242 30771\n" +
                "59002 11800 47201";

        // Act
        App.run(arguments);

        // Normalize both expected and actual outputs before comparing
        String expected = normalize(expectedOutput);
        String actual = normalize(outputStreamCaptor.toString());

        // Assert
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test Input File 2")
    void runTestInput2() {
        // Arrange
        String arguments = "sample_input/input2.txt";

        String expectedOutput = "15937 14552 6187\n" +
                "23292 16055 7690\n" +
                "CANNOT_REBALANCE";

        // Act
        App.run(arguments);

        // Normalize both expected and actual outputs before comparing
        String expected = normalize(expectedOutput);
        String actual = normalize(outputStreamCaptor.toString());

        // Assert
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test NoSuchCommandException Handling")
    void runTestInvalidCommand() {
        // Arrange
        String arguments = "sample_input/invalid_command_input.txt";

        // Act
        App.run(arguments);

        // Assert
        String output = outputStreamCaptor.toString().trim();

        Assertions.assertTrue(
                output.contains("No such command") ||
                        output.contains("INVALID_COMMAND") ||
                        output.contains("Command not found") ||
                        output.contains("Unknown command"),
                "Expected error message not found. Actual output: " + output
        );
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
        try {
            new File("sample_input/invalid_command_input.txt").delete();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    // Utility method to remove excess whitespace and normalize line endings
    private String normalize(String input) {
        return input.trim().replaceAll("\\s+", " ");
    }
}
