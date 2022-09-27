package com.spandigital.matt;

import io.vavr.Tuple2;
import io.vavr.control.Either;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class CmdLineRunner {

    CmdLineRunner() {
    }

    public static void main(String[] args) {
        try {
            CmdLineRunner runner = new CmdLineRunner();
            Path inputPath = runner.resolveInputFile(args);
            TeamRanker ranker = TeamRanker.createDefault();
            runner.printRankings(inputPath, ranker);
        } catch (Throwable t) { // Not my favourite way of doing things, but probably appropriate
            // for the task at hand.
            System.err.println(t.getMessage());
        }
    }

    void printRankings(Path inputPath, TeamRanker ranker) {
        try (Stream<String> lines = Files.lines(inputPath)) {
            Either<List<String>, List<Tuple2<Long, Team>>> rankings =
                    ranker.calculateRankings(lines);
            if (rankings.isLeft()) {
                System.err.println(
                        "Failed to determine rankings; there were problems with the following lines:");
                rankings.getLeft().forEach(System.err::println);
            } else {
                rankings.get().forEach(ranking -> System.out.println(
                        ranking._1() + ". " + ranking._2().getName() + ", " + ranking._2()
                                .getPoints() + " " + (ranking._2().getPoints() == 1 ? "pt"
                                                                                    : "pts")));
            }
        } catch (
                IOException e) { // There was a problem while reading the file. Not much to do...
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the cmdLine argument supplied to the program to obtain the input file.
     *
     * @param runtimeArgs arguments passed to the program
     * @return Path to the input file (if it exists, is a file, and is readable)
     * @throws IllegalArgumentException When any of the following are true:<br>
     *                                  1. No arguments were supplied.<br>
     *                                  2. More than one argument was supplied.<br>
     *                                  3. No file exists at specified path.<br>
     *                                  4. The specified 'file' is a directory.<br>
     *                                  5. The specified file is not readable.<br>
     */
    Path resolveInputFile(String[] runtimeArgs) {
        if (runtimeArgs.length == 0) {
            throw new IllegalArgumentException("Please supply the path to the input file.");
        } else if (runtimeArgs.length > 1) {
            throw new IllegalArgumentException("Please supply ONLY the path to the input file.");
        }
        Path path = Path.of(runtimeArgs[0]);
        File file = path.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "No file exists at given path: " + path.toAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(
                    "Given path does not resolve to a file: " + path.toAbsolutePath() + "\nPossibly a directory?");
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException(
                    "We cannot read the file supplied: " + path.toAbsolutePath() + "\nPlease check file permissions and/or application privileges.");
        }
        return path;
    }
}