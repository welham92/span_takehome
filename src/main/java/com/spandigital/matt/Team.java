package com.spandigital.matt;

import java.util.Objects;

final class Team {
    private final String name;
    private long points;

    Team(String name, long points) {
        this.name = Objects.requireNonNull(name);
        this.points = points;
    }

    Team addPoints(long additionalPoints) {
        this.points += additionalPoints;
        return this;
    }

    public String getName() {
        return name;
    }

    public long getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return points == team.points && name.equals(team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, points);
    }

    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                ", points=" + points +
                '}';
    }
}