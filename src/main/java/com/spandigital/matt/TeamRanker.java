package com.spandigital.matt;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class TeamRanker {

    static final long WIN_POINTS = 3L;
    static final long DRAW_POINTS = 1L;
    static final long LOSS_POINTS = 0L;

    private final long winPoints;
    private final long drawPoints;
    private final long lossPoints;

    TeamRanker(long winPoints, long drawPoints, long lossPoints) {
        this.winPoints = winPoints;
        this.drawPoints = drawPoints;
        this.lossPoints = lossPoints;
    }

    static TeamRanker createDefault() {
        return new TeamRanker(WIN_POINTS, DRAW_POINTS, LOSS_POINTS);
    }

    /**
     * Takes a list of games (each between two teams) and computes the total points and global
     * ranking for each team. Results are sorted by rank (descending), where ties in points result
     * in the same rank number but are ordered alphabetically (ascending, case sensitive).
     * @param lines a stream of game result lines from the input file/source.
     * @return If any of the lines were faulty, an {@link Either.Left} with a list of Strings
     * containing the faulty line contents, along with associated line numbers, is returned. If the
     * lines were all parsed successfully, then an {@link Either.Right} containing a sorted list of
     * 2-tuples is returned, where each tuple contains the rank and the Team.
     */
    Either<List<String>, List<Tuple2<Long, Team>>> calculateRankings(Stream<String> lines) {
        Map<String, Team> teams = new HashMap<>();
        List<String> badLines = new LinkedList<>();
        AtomicLong lineNumber = new AtomicLong(0);
        lines.forEach(line -> {
            long ln = lineNumber.incrementAndGet();
            Either<String, Tuple2<Team, Team>> parsedLine = parseLine(ln, line);
            if (parsedLine.isLeft()) {
                badLines.add(parsedLine.getLeft());
            } else {
                Tuple2<Team, Team> result = parsedLine.get();
                teams.merge(result._1()
                                .getName(), result._1(),
                        (tTotal, tResult) -> tTotal.addPoints(tResult.getPoints()));
                teams.merge(result._2()
                                .getName(), result._2(),
                        (tTotal, tResult) -> tTotal.addPoints(tResult.getPoints()));
            }
        });
        if (badLines.size() > 0) { // no point going any further if we have bad lines
            return Either.left(badLines);
        }

        List<Tuple2<Long, Team>> rankings = new LinkedList<>();
        // Rank may remain the same between teams, but we need to track the total number of teams
        // processed so that we can jump to next rank when required.
        AtomicLong rank = new AtomicLong(0);
        AtomicInteger processed = new AtomicInteger(0);
        AtomicLong lastPoints = new AtomicLong(
                Long.MAX_VALUE); // hopefully this suffices... but there is an edge case here
        teams.values().stream().sorted(Comparator.comparing(Team::getPoints).reversed()
                .thenComparing(Team::getName)).forEach(team -> {
            processed.incrementAndGet();
            if (team.getPoints() < lastPoints.get()) {
                rank.set(processed.get());
                lastPoints.set(team.getPoints());
            }
            rankings.add(Tuple.of(rank.get(), team));
        });
        return Either.right(rankings);
    }

    /**
     * Parses a single line of the input file to determine how many points each of the two teams
     * should receive for the game. Faulty line entries return the faulty line along with its line
     * number for subsequent display.
     *
     * @param lineNumber line number of the supplied line within the input file
     * @param line       single line of input file with a game's scores
     * @return If the line was faulty, an {@link Either.Left} with a String containing the faulty
     * line contents along with its line number is returned. If the line was parsed successfully,
     * then an {@link Either.Right} containing a 2-tuple of both teams' points for the game
     * is returned.
     */
    Either<String, Tuple2<Team, Team>> parseLine(long lineNumber, String line) {
        String[] scores = line.split(",");
        if (scores.length != 2) {
            return Either.left(formatBadLine(lineNumber, line));
        } else {
            String[] team1 = scores[0].split(" ");
            String[] team2 = scores[1].split(" ");
            if (team1.length < 2 || team2.length < 2) {
                return Either.left(formatBadLine(lineNumber, line));
            } else {
                try {
                    String team1Name = Arrays.stream(team1).limit(team1.length - 1)
                            .collect(Collectors.joining(" ")).trim();
                    int team1Score = Integer.parseInt(team1[team1.length - 1]);
                    String team2Name = Arrays.stream(team2).limit(team2.length - 1)
                            .collect(Collectors.joining(" ")).trim();
                    int team2Score = Integer.parseInt(team2[team2.length - 1]);
                    if (team1Name.isEmpty() || team2Name.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Team name must have at least one non-whitespace character");
                    }
                    if (team1Name.equals(team2Name)) {
                        throw new IllegalArgumentException("A team cannot play itself! (I assume)");
                    }
                    if (team1Score == team2Score) {
                        return Either.right(Tuple.of(new Team(team1Name, drawPoints),
                                new Team(team2Name, drawPoints)));
                    } else if (team1Score > team2Score) {
                        return Either.right(Tuple.of(new Team(team1Name, winPoints),
                                new Team(team2Name, lossPoints)));
                    } else {
                        return Either.right(Tuple.of(new Team(team1Name, lossPoints),
                                new Team(team2Name, winPoints)));
                    }
                } catch (
                        IllegalArgumentException e) { // NumberFormatException is subclass of IllegalArgumentException. Could break down if specific errors required.
                    return Either.left(formatBadLine(lineNumber, line));
                }
            }
        }
    }

    private String formatBadLine(long lineNumber, String line) {
        return lineNumber + ":  " + line;
    }
}
