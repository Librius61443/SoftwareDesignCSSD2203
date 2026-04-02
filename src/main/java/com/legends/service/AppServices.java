package com.legends.service;

import com.legends.dao.UserDAO;

import java.nio.file.Path;

/**
 * Simple shared service registry for the desktop app.
 */
public final class AppServices {

    private static final PartyService PARTY_SERVICE = new PartyService();
    private static final UserDAO USER_DAO = new UserDAO();
    private static final MatchmakingService MATCHMAKING_SERVICE = new MatchmakingService(PARTY_SERVICE);
    private static final CampaignRecordService CAMPAIGN_RECORD_SERVICE =
        new CampaignRecordService(Path.of("data", "hall_of_fame.csv"));
    private static final CampaignService CAMPAIGN_SERVICE = new CampaignService(PARTY_SERVICE, CAMPAIGN_RECORD_SERVICE);
    private static final PvpBattleService PVP_BATTLE_SERVICE = new PvpBattleService(PARTY_SERVICE, USER_DAO);

    private AppServices() {}

    public static UserDAO userDAO() {
        return USER_DAO;
    }

    public static PartyService partyService() {
        return PARTY_SERVICE;
    }

    public static MatchmakingService matchmakingService() {
        return MATCHMAKING_SERVICE;
    }

    public static CampaignService campaignService() {
        return CAMPAIGN_SERVICE;
    }

    public static CampaignRecordService campaignRecordService() {
        return CAMPAIGN_RECORD_SERVICE;
    }

    public static PvpBattleService pvpBattleService() {
        return PVP_BATTLE_SERVICE;
    }
}
