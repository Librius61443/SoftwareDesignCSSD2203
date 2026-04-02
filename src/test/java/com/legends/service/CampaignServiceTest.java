package com.legends.service;

import encounter.EncounterOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import unit.HeroClass;
import unit.Hero;
import unit.Party;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CampaignServiceTest {

    private PartyService partyService;
    private CampaignService campaignService;
    private CampaignRecordService campaignRecordService;

    @BeforeEach
    void setUp() {
        partyService = new PartyService();
        campaignRecordService = new CampaignRecordService();
        campaignService = new CampaignService(partyService, campaignRecordService);
    }

    @Test
    void campaignAdvancesOneRoomAtATime() {
        CampaignService.CampaignStepResult result = campaignService.playNextRoom("player", "Starter", HeroClass.ORDER);
        if (result.pendingBattle() != null) {
            result = campaignService.resolvePendingBattle("player", true, result.pendingBattle().party());
        }

        assertEquals(1, result.roomNumber());
        assertEquals(2, campaignService.getNextRoomNumber("player"));
        assertNotNull(result.encounterType());
        assertNotNull(result.party());
    }

    @Test
    void partyCannotBeSavedBeforeCampaignCompletion() {
        campaignService.startOrResumeCampaign("player", "Starter", HeroClass.ORDER);

        assertFalse(campaignService.saveCurrentParty("player"));

        for (int i = 0; i < 30; i++) {
            playOneResolvedRoom("player", HeroClass.ORDER);
        }

        assertTrue(campaignService.isCampaignCompleted("player"));
        assertTrue(campaignService.saveCurrentParty("player"));
    }

    @Test
    void starterClassChoiceIsAppliedToCreatedHero() {
        Party party = campaignService.startOrResumeCampaign("player", "Starter", HeroClass.CHAOS);

        assertEquals(HeroClass.CHAOS, party.heroes().get(0).currentClass());
    }

    @Test
    void innVisitExposesShopAndRecruitChoices() {
        campaignService.startOrResumeCampaign("player", "Starter", HeroClass.ORDER);

        CampaignService.CampaignStepResult innResult = null;
        for (int i = 0; i < 30 && innResult == null; i++) {
            CampaignService.CampaignStepResult step = campaignService.playNextRoom("player", "Starter", HeroClass.ORDER);
            if ("Inn".equals(step.encounterType())) {
                innResult = step;
            } else if (step.pendingBattle() != null) {
                campaignService.resolvePendingBattle("player", true, step.pendingBattle().party());
            }
        }

        assertNotNull(innResult);
        assertFalse(innResult.innItems().isEmpty());
    }

    @Test
    void completedCampaignCreatesHallOfFameEntry() {
        campaignService.startOrResumeCampaign("player", "Starter", HeroClass.ORDER);

        CampaignService.CampaignStepResult last = null;
        for (int i = 0; i <= 30; i++) {
            last = playOneResolvedRoom("player", HeroClass.ORDER);
        }

        assertNotNull(last);
        assertNotNull(last.campaignSummary());
        assertFalse(last.campaignSummary().hallOfFame().isEmpty());
    }

    @Test
    void battleLossReturnsPlayerToLastInn() {
        Party party = campaignService.startOrResumeCampaign("player", "Starter", HeroClass.ORDER);
        party.addGold(1_000);

        CampaignService.CampaignStepResult innResult = findInn("player");
        assertNotNull(innResult);

        CampaignService.CampaignStepResult battleResult = findBattle("player");
        assertNotNull(battleResult.pendingBattle());

        CampaignService.CampaignStepResult lossResult =
            campaignService.resolvePendingBattle("player", false, battleResult.pendingBattle().party());

        assertEquals("Inn", lossResult.encounterType());
        assertEquals(EncounterOutcome.LOSS, lossResult.outcome());
        assertFalse(lossResult.innItems().isEmpty());
    }

    @Test
    void innItemsApplyToChosenHero() {
        Party party = campaignService.startOrResumeCampaign("player", "Starter", HeroClass.ORDER);
        Hero secondHero = new Hero("Second", HeroClass.WARRIOR);
        party.addHero(secondHero);
        party.addGold(500);

        CampaignService.CampaignStepResult innResult = findInn("player");
        assertNotNull(innResult);

        Party activeParty = partyService.getActiveParty("player");
        activeParty.heroes().get(1).spendMana(25);
        partyService.setActiveParty("player", activeParty);

        Hero untouchedStarter = partyService.getActiveParty("player").heroes().get(0);
        Hero damagedHero = partyService.getActiveParty("player").heroes().stream()
            .filter(hero -> hero.name().equals("Second"))
            .findFirst()
            .orElseThrow();
        int starterManaBefore = untouchedStarter.mana();
        int damagedManaBefore = damagedHero.mana();

        assertTrue(campaignService.buyInnItem("player", "Water", "Second"));

        Hero updatedStarter = partyService.getActiveParty("player").heroes().get(0);
        Hero updatedSecond = partyService.getActiveParty("player").heroes().stream()
            .filter(hero -> hero.name().equals("Second"))
            .findFirst()
            .orElseThrow();
        assertEquals(starterManaBefore, updatedStarter.mana());
        assertEquals(damagedManaBefore + 10, updatedSecond.mana());
    }

    @Test
    void hallOfFamePersistsWhenStoragePathIsProvided() throws Exception {
        Path storagePath = Files.createTempFile("hall-of-fame", ".csv");
        Files.deleteIfExists(storagePath);
        CampaignRecordService persistentRecords = new CampaignRecordService(storagePath);
        Party party = Party.singleStarterHero("Starter", HeroClass.ORDER);

        persistentRecords.recordCompletion("player", party);

        CampaignRecordService reloaded = new CampaignRecordService(storagePath);
        assertEquals(1, reloaded.getHallOfFame().size());
        assertEquals("player", reloaded.getHallOfFame().get(0).username());
    }

    private CampaignService.CampaignStepResult playOneResolvedRoom(String username, HeroClass heroClass) {
        CampaignService.CampaignStepResult result = campaignService.playNextRoom(username, "Starter", heroClass);
        if (result.pendingBattle() != null) {
            result = campaignService.resolvePendingBattle(username, true, result.pendingBattle().party());
        }
        return result;
    }

    private CampaignService.CampaignStepResult findInn(String username) {
        for (int i = 0; i < 30; i++) {
            CampaignService.CampaignStepResult step = campaignService.playNextRoom(username, "Starter", HeroClass.ORDER);
            if ("Inn".equals(step.encounterType())) {
                return step;
            }
            if (step.pendingBattle() != null) {
                campaignService.resolvePendingBattle(username, true, step.pendingBattle().party());
            }
        }
        return null;
    }

    private CampaignService.CampaignStepResult findBattle(String username) {
        for (int i = 0; i < 30; i++) {
            CampaignService.CampaignStepResult step = campaignService.playNextRoom(username, "Starter", HeroClass.ORDER);
            if (step.pendingBattle() != null) {
                return step;
            }
        }
        return null;
    }
}
