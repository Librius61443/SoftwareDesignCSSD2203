package com.legends.service;

import campaign.Campaign;
import campaign.CampaignResult;
import campaign.GameConfig;
import encounter.Encounter;
import encounter.EncounterChancePolicy;
import encounter.EncounterOutcome;
import encounter.EncounterFactory;
import encounter.InnFactory;
import rng.JavaRng;
import rng.Rng;
import unit.EnemyFactory;
import unit.EnemyParty;
import unit.Hero;
import unit.HeroClass;
import unit.Party;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs the PvE dungeon loop and keeps each user's latest campaign party in the
 * shared party service so it can later be saved for PvP.
 */
public class CampaignService {

    public record CampaignStepResult(
        int roomNumber,
        String encounterType,
        EncounterOutcome outcome,
        boolean campaignCompleted,
        Party party,
        PendingBattle pendingBattle,
        List<InnItemOption> innItems,
        RecruitOption recruitOption,
        CampaignRecordService.CampaignSummary campaignSummary
    ) {}

    public record InnItemOption(String name, int cost, String effect) {}
    public record RecruitOption(String name, HeroClass heroClass, int level, int cost) {}
    public record PendingBattle(int roomNumber, Party party, EnemyParty enemies) {}

    private record InnVisitState(int roomNumber, List<InnItemOption> items, RecruitOption recruitOption) {}
    private record PendingBattleState(int roomNumber, EnemyParty enemies) {}

    private static final int TOTAL_ROOMS = 30;

    private final PartyService partyService;
    private final Rng rng;
    private final CampaignRecordService campaignRecordService;
    private final Map<String, Integer> nextRoomByUser = new ConcurrentHashMap<>();
    private final Map<String, Boolean> completedByUser = new ConcurrentHashMap<>();
    private final Map<String, InnVisitState> pendingInnVisits = new ConcurrentHashMap<>();
    private final Map<String, PendingBattleState> pendingBattles = new ConcurrentHashMap<>();
    private final Map<String, Integer> lastInnRoomByUser = new ConcurrentHashMap<>();

    public CampaignService(PartyService partyService) {
        this(partyService, new CampaignRecordService(), new JavaRng(new Random()));
    }

    public CampaignService(PartyService partyService, CampaignRecordService campaignRecordService) {
        this(partyService, campaignRecordService, new JavaRng(new Random()));
    }

    public CampaignService(PartyService partyService, CampaignRecordService campaignRecordService, Rng rng) {
        this.partyService = partyService;
        this.campaignRecordService = campaignRecordService;
        this.rng = rng;
    }

    public Party startOrResumeCampaign(String username, String starterHeroName, HeroClass starterClass) {
        Party party = partyService.getOrCreateActiveParty(username, starterHeroName, starterClass);
        nextRoomByUser.putIfAbsent(username, 1);
        completedByUser.putIfAbsent(username, false);
        lastInnRoomByUser.putIfAbsent(username, 1);
        return party;
    }

    public CampaignResult runCampaign(String username) {
        Party activeParty = partyService.getActiveParty(username);
        if (activeParty == null) {
            throw new IllegalStateException("No active party found for " + username);
        }

        Campaign campaign = new Campaign(
            new GameConfig(30),
            new EncounterChancePolicy(),
            new EncounterFactory(rng, new EnemyFactory(rng), new InnFactory(rng))
        );
        CampaignResult result = campaign.run(activeParty);
        partyService.setActiveParty(username, result.party());
        return result;
    }

    public boolean saveCurrentParty(String username) {
        return partyService.saveActiveParty(username);
    }

    public CampaignStepResult playNextRoom(String username, String starterHeroName, HeroClass selectedClass) {
        Party party = startOrResumeCampaign(username, starterHeroName, selectedClass);
        party.setCurrentClassForAll(selectedClass);

        int nextRoom = nextRoomByUser.getOrDefault(username, 1);
        if (nextRoom > TOTAL_ROOMS) {
            completedByUser.put(username, true);
            CampaignRecordService.CampaignSummary summary = campaignRecordService.recordCompletion(username, party.copy());
            return new CampaignStepResult(
                TOTAL_ROOMS,
                "Campaign Complete",
                EncounterOutcome.NEUTRAL,
                true,
                party.copy(),
                null,
                List.of(),
                null,
                summary
            );
        }

        Encounter encounter = createEncounterFactory().createEncounter(new EncounterChancePolicy().chancesFor(party));
        if ("Battle".equals(describeEncounter(encounter))) {
            EnemyParty enemies = new EnemyFactory(rng).createForParty(party);
            pendingBattles.put(username, new PendingBattleState(nextRoom, enemies));
            partyService.setActiveParty(username, party);
            return new CampaignStepResult(
                nextRoom,
                "Battle",
                EncounterOutcome.NEUTRAL,
                false,
                party,
                new PendingBattle(nextRoom, party, enemies),
                List.of(),
                null,
                null
            );
        }

        EncounterOutcome outcome = encounter.resolve(new encounter.RoomContext(nextRoom), party);
        if (outcome == EncounterOutcome.LOSS) {
            party.fullRestore();
        }

        List<InnItemOption> innItems = List.of();
        RecruitOption recruitOption = null;
        if ("Inn".equals(describeEncounter(encounter))) {
            lastInnRoomByUser.put(username, nextRoom);
            innItems = defaultInnItems();
            recruitOption = createRecruitOption(party, nextRoom);
            pendingInnVisits.put(username, new InnVisitState(nextRoom, innItems, recruitOption));
        } else {
            pendingInnVisits.remove(username);
        }

        nextRoomByUser.put(username, nextRoom + 1);
        boolean completed = nextRoom >= TOTAL_ROOMS;
        completedByUser.put(username, completed);
        partyService.setActiveParty(username, party);

        CampaignRecordService.CampaignSummary summary = completed
            ? campaignRecordService.recordCompletion(username, party.copy())
            : null;

        return new CampaignStepResult(
            nextRoom,
            describeEncounter(encounter),
            outcome,
            completed,
            party.copy(),
            null,
            innItems,
            recruitOption,
            summary
        );
    }

    public CampaignStepResult resolvePendingBattle(String username, boolean playerWon, Party battledParty) {
        PendingBattleState battle = pendingBattles.remove(username);
        if (battle == null) {
            throw new IllegalStateException("No pending battle for " + username);
        }

        if (playerWon) {
            int totalExp = battle.enemies().units().stream().mapToInt(unit -> 50 * unit.level()).sum();
            int totalGold = battle.enemies().units().stream().mapToInt(unit -> 75 * unit.level()).sum();
            battledParty.addGold(totalGold);
            List<Hero> standing = battledParty.heroes().stream().filter(Hero::isAlive).toList();
            if (!standing.isEmpty()) {
                int share = totalExp / standing.size();
                standing.forEach(hero -> hero.gainExp(share));
            }
        } else {
            battledParty.loseGoldPercent(10);
            battledParty.applyLossExpPenalty(30);
            battledParty.fullRestore();
            int innRoom = lastInnRoomByUser.getOrDefault(username, 1);
            List<InnItemOption> innItems = defaultInnItems();
            RecruitOption recruitOption = createRecruitOption(battledParty, innRoom);
            pendingInnVisits.put(username, new InnVisitState(innRoom, innItems, recruitOption));
            nextRoomByUser.put(username, Math.min(TOTAL_ROOMS + 1, innRoom + 1));
            completedByUser.put(username, false);
            partyService.setActiveParty(username, battledParty);
            return new CampaignStepResult(
                innRoom,
                "Inn",
                EncounterOutcome.LOSS,
                false,
                battledParty.copy(),
                null,
                innItems,
                recruitOption,
                null
            );
        }

        int nextRoom = battle.roomNumber() + 1;
        boolean completed = battle.roomNumber() >= TOTAL_ROOMS;
        nextRoomByUser.put(username, nextRoom);
        completedByUser.put(username, completed);
        partyService.setActiveParty(username, battledParty);

        CampaignRecordService.CampaignSummary summary = completed
            ? campaignRecordService.recordCompletion(username, battledParty.copy())
            : null;

        return new CampaignStepResult(
            battle.roomNumber(),
            "Battle",
            playerWon ? EncounterOutcome.WIN : EncounterOutcome.LOSS,
            completed,
            battledParty.copy(),
            null,
            List.of(),
            null,
            summary
        );
    }

    public int getNextRoomNumber(String username) {
        return nextRoomByUser.getOrDefault(username, 1);
    }

    public boolean isCampaignCompleted(String username) {
        return completedByUser.getOrDefault(username, false);
    }

    public List<InnItemOption> getPendingInnItems(String username) {
        InnVisitState visit = pendingInnVisits.get(username);
        return visit == null ? List.of() : visit.items();
    }

    public RecruitOption getPendingRecruit(String username) {
        InnVisitState visit = pendingInnVisits.get(username);
        return visit == null ? null : visit.recruitOption();
    }

    public boolean buyInnItem(String username, String itemName) {
        return buyInnItem(username, itemName, null);
    }

    public boolean buyInnItem(String username, String itemName, String heroName) {
        InnVisitState visit = pendingInnVisits.get(username);
        Party party = partyService.getActiveParty(username);
        if (visit == null || party == null) return false;

        InnItemOption item = visit.items().stream()
            .filter(option -> option.name().equals(itemName))
            .findFirst()
            .orElse(null);
        if (item == null || party.gold() < item.cost()) return false;

        party.spendGold(item.cost());
        if (!applyInnItem(party, item.name(), heroName)) {
            party.addGold(item.cost());
            return false;
        }
        party.recordItemPurchase(item.cost());
        partyService.setActiveParty(username, party);
        return true;
    }

    public boolean recruitFromInn(String username) {
        InnVisitState visit = pendingInnVisits.get(username);
        Party party = partyService.getActiveParty(username);
        if (visit == null || party == null || visit.recruitOption() == null || party.isFull()) return false;

        RecruitOption recruit = visit.recruitOption();
        if (party.gold() < recruit.cost()) return false;
        if (recruit.cost() > 0) {
            party.spendGold(recruit.cost());
        }

        Hero hero = new Hero(recruit.name(), recruit.heroClass());
        while (hero.level() < recruit.level()) {
            hero.gainExp(999_999);
        }
        party.addHero(hero);
        partyService.setActiveParty(username, party);
        pendingInnVisits.put(username, new InnVisitState(visit.roomNumber(), visit.items(), null));
        return true;
    }

    public void endInnVisit(String username) {
        pendingInnVisits.remove(username);
    }

    /**
     * Saves the current campaign progress (room number, party) for the user so
     * they can resume later (Use Case 5 – exit PvE campaign).
     * May only be called when the user is NOT currently in a battle.
     *
     * @throws IllegalStateException if there is a pending unresolved battle.
     */
    public boolean saveCampaignProgress(String username) {
        if (pendingBattles.containsKey(username)) {
            throw new IllegalStateException("Cannot exit during a battle.");
        }
        // Party state is already kept by PartyService; just persist the room number
        // by relying on the in-memory maps (they survive for the session).
        // For a database-backed implementation, persist nextRoomByUser.get(username) here.
        return partyService.saveActiveParty(username);
    }

    /**
     * Loads a previously saved campaign state for the user (Use Case 6 – continue
     * an incomplete PvE campaign).  After this call the user's in-progress maps are
     * re-initialised and {@link #getNextRoomNumber} will return the saved room.
     *
     * @param username the user whose save should be loaded
     * @param savedRoom the room number to restore (1-based, clamped to [1, 30])
     * @return true if a saved party was available and loaded
     */
    public boolean loadCampaignProgress(String username, int savedRoom) {
        Party savedParty = partyService.getActiveParty(username);
        if (savedParty == null) {
            return false;
        }
        int room = Math.max(1, Math.min(TOTAL_ROOMS, savedRoom));
        nextRoomByUser.put(username, room);
        completedByUser.put(username, false);
        lastInnRoomByUser.put(username, Math.max(1, room - 1));
        pendingBattles.remove(username);
        pendingInnVisits.remove(username);
        return true;
    }

    private EncounterFactory createEncounterFactory() {
        return new EncounterFactory(rng, new EnemyFactory(rng), new InnFactory(rng));
    }

    private String describeEncounter(Encounter encounter) {
        String simpleName = encounter.getClass().getSimpleName();
        return simpleName.contains("Inn") ? "Inn" : "Battle";
    }

    private List<InnItemOption> defaultInnItems() {
        return List.of(
            new InnItemOption("Bread", 200, "+20 HP"),
            new InnItemOption("Cheese", 500, "+50 HP"),
            new InnItemOption("Steak", 1000, "+200 HP"),
            new InnItemOption("Water", 150, "+10 mana"),
            new InnItemOption("Juice", 400, "+30 mana"),
            new InnItemOption("Wine", 750, "+100 mana"),
            new InnItemOption("Elixir", 2000, "Revive + Full HP + Full mana")
        );
    }

    private RecruitOption createRecruitOption(Party party, int roomNumber) {
        if (roomNumber > 10 || party.isFull()) {
            return null;
        }
        HeroClass[] classes = HeroClass.values();
        HeroClass heroClass = classes[rng.nextInt(0, classes.length - 1)];
        int level = rng.nextInt(1, 4);
        int cost = level == 1 ? 0 : level * 200;
        return new RecruitOption("Recruit-" + roomNumber, heroClass, level, cost);
    }

    private boolean applyInnItem(Party party, String itemName, String heroName) {
        Hero target = findTargetHero(party, heroName);
        if (target == null) {
            return false;
        }
        switch (itemName) {
            case "Bread" -> target.heal(20);
            case "Cheese" -> target.heal(50);
            case "Steak" -> target.heal(200);
            case "Water" -> target.restoreMana(10);
            case "Juice" -> target.restoreMana(30);
            case "Wine" -> target.restoreMana(100);
            case "Elixir" -> target.fullRestore();
            default -> {
                return false;
            }
        }
        return true;
    }

    private Hero findTargetHero(Party party, String heroName) {
        if (heroName == null || heroName.isBlank()) {
            return party.heroes().isEmpty() ? null : party.heroes().get(0);
        }
        return party.heroes().stream()
            .filter(hero -> hero.name().equals(heroName))
            .findFirst()
            .orElse(null);
    }
}
