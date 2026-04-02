package com.legends.service;

import com.legends.dao.UserDAO;
import com.legends.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import unit.Hero;
import unit.HeroClass;
import unit.Party;

import static org.junit.jupiter.api.Assertions.*;

public class GameFlowIntegrationTest {

    private UserDAO userDAO;
    private PartyService partyService;
    private CampaignService campaignService;
    private PvpBattleService pvpBattleService;

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
        userDAO.resetInMemoryStore();
        partyService = new PartyService();
        campaignService = new CampaignService(partyService);
        pvpBattleService = new PvpBattleService(partyService, userDAO);

        userDAO.createUser(new User("alice", "pw"));
        userDAO.createUser(new User("bob", "pw"));
    }

    @Test
    public void activePvePartyCanBeSavedAndUsedForPvp() {
        Party aliceParty = campaignService.startOrResumeCampaign("alice", "Alice Hero", HeroClass.MAGE);
        aliceParty.addHero(new Hero("Alice Mage", HeroClass.ORDER));
        partyService.setActiveParty("alice", aliceParty);

        Party bobParty = campaignService.startOrResumeCampaign("bob", "Bob Hero", HeroClass.WARRIOR);
        bobParty.addHero(new Hero("Bob Warrior", HeroClass.CHAOS));
        partyService.setActiveParty("bob", bobParty);

        for (int i = 0; i < 30; i++) {
            playResolvedRoom("alice", "Alice Hero", HeroClass.MAGE);
            playResolvedRoom("bob", "Bob Hero", HeroClass.WARRIOR);
        }

        assertTrue(campaignService.saveCurrentParty("alice"));
        assertTrue(campaignService.saveCurrentParty("bob"));

        PvpBattleService.PvpBattleResult result = pvpBattleService.battleFirstSavedParties("alice", "bob");

        assertTrue(result.winner().equals("alice") || result.winner().equals("bob"));
        assertNotEquals(result.winner(), result.loser());

        User alice = userDAO.getUser("alice");
        User bob = userDAO.getUser("bob");
        assertEquals(1, alice.getWins() + alice.getLosses());
        assertEquals(1, bob.getWins() + bob.getLosses());
    }

    private void playResolvedRoom(String username, String starterName, HeroClass heroClass) {
        CampaignService.CampaignStepResult result = campaignService.playNextRoom(username, starterName, heroClass);
        if (result.pendingBattle() != null) {
            campaignService.resolvePendingBattle(username, true, result.pendingBattle().party());
        }
    }
}
