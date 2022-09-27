package com.spandigital.matt;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

public class TestCmdLineRunner {

    // no mutable internal state, so we can just initialise and use one.
    CmdLineRunner runner = new CmdLineRunner();

    @Test(expected = IllegalArgumentException.class)
    public void givenNoCmdInput_whenStart_throwException() {
        try {
            runner.resolveInputFile(new String[]{});
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Please supply the path to the input file.", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenMultipleCmdInputs_whenStart_throwException() {
        try {
            runner.resolveInputFile(new String[]{"maybe_a_file.txt", "but also something else"});
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Please supply ONLY the path to the input file.", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenFileNotExists_whenStart_throwException() {
        String sPath = "i_do_not_exist.txt";
        Path path = Path.of(sPath);
        try {
            runner.resolveInputFile(new String[]{sPath});
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("No file exists at given path: " + path.toAbsolutePath(),
                    e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenDirectory_whenStart_throwException() {
        String sPath = "src/test/resources/dummy_folder";
        Path path = Path.of(sPath);
        try {
            runner.resolveInputFile(new String[]{sPath});
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(
                    "Given path does not resolve to a file: " + path.toAbsolutePath() + "\nPossibly a directory?",
                    e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenUnreadableFile_whenStart_throwException() {
        String sPath = "src/test/resources/unreadable_file.txt";
        Path path = Path.of(sPath);
        try {
            path.toFile().createNewFile();
            path.toFile().setReadable(false);
            runner.resolveInputFile(new String[]{sPath});
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(
                    "We cannot read the file supplied: " + path.toAbsolutePath() + "\nPlease check file permissions and/or application privileges.",
                    e.getMessage());
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            path.toFile().delete();
        }
    }

    // Might as well add one 'integration test' for the sample file
    @Test
    public void givenSampleInputFile_whenRunMain_producesSampleOutput() {
        ByteArrayOutputStream stdoutRedirect = new ByteArrayOutputStream();
        PrintStream psRedirect = new PrintStream(stdoutRedirect);
        PrintStream oldStdout = System.out;
        System.setOut(psRedirect);
        CmdLineRunner.main(new String[]{"src/test/resources/sample1.txt"});
        System.out.flush();
        System.setOut(oldStdout);
        String expected = """
                1. Tarantulas, 6 pts
                2. Lions, 5 pts
                3. FC Awesome, 1 pt
                3. Snakes, 1 pt
                5. Grouches, 0 pts
                """.trim() + "\n";
        Assert.assertEquals(expected, stdoutRedirect.toString());
    }
}
