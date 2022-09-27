package com.spandigital.matt;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class TestTeamRanker {

    // no mutable internal state, so we can just initialise and use one.
    private static final long WIN_POINTS = TeamRanker.WIN_POINTS;
    private static final long DRAW_POINTS = TeamRanker.DRAW_POINTS;
    private static final long LOSS_POINTS = TeamRanker.LOSS_POINTS;
    private final TeamRanker tr = new TeamRanker(WIN_POINTS, DRAW_POINTS, LOSS_POINTS);

    @Test
    public void givenInsufficientElements_whenParseLine_returnBadString() {
        String line = "Team1, Team2 5";
        Either<String, Tuple2<Team, Team>> result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "Team1 5, Team2";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "Team1, Team2";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "5, Team2 5";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "Team1 5, 5";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "5, 5";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());
    }

    @Test
    public void givenMalformedScore_whenParseLine_returnBadString() {
        String line = "Team1 X, Team2 5";
        Either<String, Tuple2<Team, Team>> result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "Team1 5, Team2 X";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "Team1 X, Team2 Y";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());
    }

    @Test
    public void givenEmptyTeamName_whenParseLine_returnBadString() {
        String line = " \t  5, Team2 5";
        Either<String, Tuple2<Team, Team>> result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "Team1 5,    \t   X";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "   \t   X,  \t   Y";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());
    }

    @Test
    public void givenSameTeamName_whenParseLine_returnBadString() {
        String line = "Team1 5, Team1 5";
        Either<String, Tuple2<Team, Team>> result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());

        line = "    \tTeam1 5,  \tTeam1    5";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("1:  " + line, result.getLeft());
    }

    @Test
    public void givenValidGame_whenParseLine_returnResult() {
        String team1 = "Team1";
        String team2 = "Team2";
        String line = team1 + " 1, " + team2 + " 1";
        Either<String, Tuple2<Team, Team>> result = tr.parseLine(1, line);
        Assert.assertTrue(result.isRight());
        Assert.assertEquals(new Team(team1, DRAW_POINTS), result.get()._1());
        Assert.assertEquals(new Team(team2, DRAW_POINTS), result.get()._2());

        line = team1 + " 2, " + team2 + " 1";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isRight());
        Assert.assertEquals(new Team(team1, WIN_POINTS), result.get()._1());
        Assert.assertEquals(new Team(team2, LOSS_POINTS), result.get()._2());

        line = team1 + " 1, " + team2 + " 2";
        result = tr.parseLine(1, line);
        Assert.assertTrue(result.isRight());
        Assert.assertEquals(new Team(team1, LOSS_POINTS), result.get()._1());
        Assert.assertEquals(new Team(team2, WIN_POINTS), result.get()._2());
    }

    @Test
    public void givenMultipartTeamNames_whenParseLine_returnResult() {
        // Opting for draws
        String team1 = "The best team";
        String team2 = "Also the best team";
        String line = team1 + " 1, " + team2 + " 1";
        Either<String, Tuple2<Team, Team>> result = tr.parseLine(1, line);
        Assert.assertTrue(result.isRight());
        Assert.assertEquals(new Team(team1, DRAW_POINTS), result.get()._1());
        Assert.assertEquals(new Team(team2, DRAW_POINTS), result.get()._2());
    }

    @Test // We're choosing to be happy with an empty file
    public void givenEmptyFile_whenCalculateRankings_returnResult() {
        List<String> lines = new LinkedList<>();
        Either<List<String>, List<Tuple2<Long, Team>>> rankings =
                tr.calculateRankings(lines.stream());
        Assert.assertTrue(rankings.isRight());
        Assert.assertEquals(List.of(), rankings.get());
    }

    @Test
    public void givenInvalidLines_whenCalculateRankings_returnBadLinesWithCorrectLineNumbers() {
        // Probably a bit excessive, but will test for bad line at beginning, middle, and end.

        List<String> lines = new LinkedList<>();
        lines.add("Bad, Team2 1");
        lines.add("Team1 1, Team2 1");
        lines.add("Team1 2, Team2 1");
        Either<List<String>, List<Tuple2<Long, Team>>> rankings =
                tr.calculateRankings(lines.stream());
        Assert.assertTrue(rankings.isLeft());
        Assert.assertEquals(List.of("1:  Bad, Team2 1"), rankings.getLeft());

        lines = new LinkedList<>();
        lines.add("Team1 1, Team2 1");
        lines.add("Bad 1, Bad 1");
        lines.add("Team1 2, Team2 1");
        rankings = tr.calculateRankings(lines.stream());
        Assert.assertTrue(rankings.isLeft());
        Assert.assertEquals(List.of("2:  Bad 1, Bad 1"), rankings.getLeft());

        lines = new LinkedList<>();
        lines.add("Team1 1, Team2 1");
        lines.add("Team1 2, Team2 1");
        lines.add("Bad 2, 5");
        rankings = tr.calculateRankings(lines.stream());
        Assert.assertTrue(rankings.isLeft());
        Assert.assertEquals(List.of("3:  Bad 2, 5"), rankings.getLeft());
    }

    @Test
    public void givenValidGames_whenCalculateRankings_returnSortedResult() {
        // Ordering of team names and input lines loosely chosen to avoid any happens-to-be-in-the-
        // right-order scenarios: want the sort to take effect.

        List<String> lines = new LinkedList<>();
        lines.add("Ants 1, Badgers 1");
        lines.add("Badgers 2, Ants 1");
        Either<List<String>, List<Tuple2<Long, Team>>> rankings =
                tr.calculateRankings(lines.stream());
        Assert.assertTrue(rankings.isRight());
        Assert.assertEquals(
                List.of(Tuple.of(1L, new Team("Badgers", DRAW_POINTS + WIN_POINTS)),
                        Tuple.of(2L, new Team("Ants", DRAW_POINTS + LOSS_POINTS))),
                rankings.get());

        // We're case-sensitive, as an arbitrary choice.
        // So Ants > Badgers > ants > badgers
        lines = new LinkedList<>();
        lines.add("ants 1, Ants 1");
        lines.add("Ants 1, ants 1");
        lines.add("badgers 1, Badgers 1");
        lines.add("Badgers 1, badgers 1");
        rankings = tr.calculateRankings(lines.stream());
        Assert.assertTrue(rankings.isRight());
        Assert.assertEquals(
                List.of(Tuple.of(1L, new Team("Ants", DRAW_POINTS + DRAW_POINTS)),
                        Tuple.of(1L, new Team("Badgers", DRAW_POINTS + DRAW_POINTS)),
                        Tuple.of(1L, new Team("ants", DRAW_POINTS + DRAW_POINTS)),
                        Tuple.of(1L, new Team("badgers", DRAW_POINTS + DRAW_POINTS))),
                rankings.get());

        // Test rank gaps (1, 1, 3, 4, 4) and some name sorting
        lines = new LinkedList<>();
        lines.add("Ants 1, Badgers 2");
        lines.add("Cats 2, Dogs 1");
        lines.add("Emus 1, Ants 2");
        lines.add("Badgers 2, Dogs 1");
        lines.add("Cats 2, Emus 1");
        rankings = tr.calculateRankings(lines.stream());
        Assert.assertTrue(rankings.isRight());
        Assert.assertEquals(
                List.of(Tuple.of(1L, new Team("Badgers", WIN_POINTS + WIN_POINTS)),
                        Tuple.of(1L, new Team("Cats", WIN_POINTS + WIN_POINTS)),
                        Tuple.of(3L, new Team("Ants", LOSS_POINTS + WIN_POINTS)),
                        Tuple.of(4L, new Team("Dogs", LOSS_POINTS + LOSS_POINTS)),
                        Tuple.of(4L, new Team("Emus", LOSS_POINTS + LOSS_POINTS))),
                rankings.get());
    }
}
