package hr.lknezevic.brassbirmingham.model.ui;

public record PlayerStats(
        String username,
        int played,
        int wins,
        int losses,
        double score
) { }
