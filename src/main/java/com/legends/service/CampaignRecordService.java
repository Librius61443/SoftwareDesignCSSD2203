package com.legends.service;

import unit.Party;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CampaignRecordService {

    public record HallOfFameEntry(String username, int score) {}
    public record CampaignSummary(int score, List<HallOfFameEntry> hallOfFame) {}

    private final List<HallOfFameEntry> hallOfFame = new ArrayList<>();
    private final Path storagePath;

    public CampaignRecordService() {
        this(null);
    }

    public CampaignRecordService(Path storagePath) {
        this.storagePath = storagePath;
        loadFromDisk();
    }

    public CampaignSummary recordCompletion(String username, Party party) {
        int score = calculateScore(party);
        hallOfFame.add(new HallOfFameEntry(username, score));
        normalizeHallOfFame();
        saveToDisk();
        return new CampaignSummary(score, getHallOfFame());
    }

    public List<HallOfFameEntry> getHallOfFame() {
        return new ArrayList<>(hallOfFame);
    }

    public void reset() {
        hallOfFame.clear();
        saveToDisk();
    }

    private int calculateScore(Party party) {
        int heroLevelScore = party.heroes().stream().mapToInt(hero -> hero.level() * 100).sum();
        return heroLevelScore + (party.gold() * 10) + party.itemScore();
    }

    private void normalizeHallOfFame() {
        hallOfFame.sort(Comparator.comparingInt(HallOfFameEntry::score).reversed());
        if (hallOfFame.size() > 10) {
            hallOfFame.subList(10, hallOfFame.size()).clear();
        }
    }

    private void loadFromDisk() {
        if (storagePath == null || !Files.exists(storagePath)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(storagePath, StandardCharsets.UTF_8)) {
                String[] parts = line.split(",", 2);
                if (parts.length != 2) {
                    continue;
                }
                hallOfFame.add(new HallOfFameEntry(parts[0], Integer.parseInt(parts[1])));
            }
            normalizeHallOfFame();
        } catch (IOException | NumberFormatException ignored) {
            hallOfFame.clear();
        }
    }

    private void saveToDisk() {
        if (storagePath == null) {
            return;
        }
        try {
            Path parent = storagePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<String> lines = hallOfFame.stream()
                .map(entry -> entry.username() + "," + entry.score())
                .toList();
            Files.write(storagePath, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
