package com.legends.controller;

import com.legends.battle.InteractiveBattleSession;
import com.legends.model.User;
import com.legends.service.AppServices;
import com.legends.service.CampaignRecordService;
import com.legends.service.CampaignService;
import com.legends.service.PartyService;
import com.legends.view.BattleView;
import com.legends.view.HomeView;
import com.legends.view.PveView;
import unit.Hero;
import unit.HeroClass;
import unit.Party;
import unit.Team;
import unit.Unit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Controls the PvE campaign screen.
 * <p>
 * Key levelling bug fix: the old code passed {@code result.pendingBattle().party()}
 * (the PRE-battle snapshot) into {@code resolvePendingBattle}.  XP and gold were
 * applied to that stale copy which was then discarded, so heroes never actually
 * levelled up.  This controller rebuilds the post-battle {@link Party} directly
 * from the live {@link InteractiveBattleSession#players()} list before handing it
 * to the service.
 */
public class PveController {

    private final PveView               view;
    private final HomeView              homeView;
    private final User                  user;
    private final CampaignService       campaignService;
    private final PartyService          partyService;
    private final CampaignRecordService campaignRecordService;

    public PveController(PveView view, HomeView homeView, User user) {
        this.view                 = view;
        this.homeView             = homeView;
        this.user                 = user;
        this.campaignService      = AppServices.campaignService();
        this.partyService         = AppServices.partyService();
        this.campaignRecordService = AppServices.campaignRecordService();

        view.addNextRoomListener(new NextRoomListener());
        view.addSavePartyListener(new SavePartyListener());
        view.addViewPartyListener(new ViewPartyListener());
        view.addHallOfFameListener(new HallOfFameListener());
        view.addExitListener(new ExitListener());

        // Restore progress label for returning players
        view.setProgress(
            campaignService.getNextRoomNumber(user.getUsername()),
            campaignService.isCampaignCompleted(user.getUsername())
        );
        view.log("Campaign ready. Choose your class and press Next Room.");
    }

    // ── Listeners ────────────────────────────────────────────────────────────

    class NextRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            HeroClass selectedClass = view.getSelectedClass();
            CampaignService.CampaignStepResult result = campaignService.playNextRoom(
                user.getUsername(),
                user.getUsername() + " Hero",
                selectedClass
            );

            if (result.pendingBattle() != null) {
                result = resolveBattle(result);
                if (result == null) return; // battle window closed abnormally
            }

            view.setProgress(
                campaignService.getNextRoomNumber(user.getUsername()),
                result.campaignCompleted()
            );

            if ("Inn".equals(result.encounterType())) {
                handleInnVisit(result);
            }

            if (result.campaignSummary() != null) {
                view.showInfo("Campaign complete! Score: " + result.campaignSummary().score()
                    + "\n\nHall of Fame:\n" + formatHallOfFame(result.campaignSummary().hallOfFame()));
                return;
            }

            Party p = result.party();
            StringBuilder sb = new StringBuilder();
            sb.append("Room ").append(result.roomNumber())
              .append(" → ").append(result.encounterType())
              .append(" (").append(result.outcome()).append(")");
            sb.append("\n  Party: ").append(p.heroes().size()).append(" hero(es)");
            sb.append(", cumulative level ").append(p.cumulativeLevel());
            sb.append(", gold ").append(p.gold());
            for (Hero h : p.heroes()) {
                sb.append("\n    ").append(h.name())
                  .append(" — Level ").append(h.level())
                  .append(", HP ").append(h.hp()).append("/").append(h.maxHp())
                  .append(", ATK ").append(h.attack())
                  .append(", DEF ").append(h.defense());
            }
            view.log(sb.toString());
        }
    }

    class SavePartyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean saved = campaignService.saveCurrentParty(user.getUsername());
            if (saved) {
                int count = partyService.getSavedParties(user.getUsername()).size();
                view.showInfo("Party saved for PvP! (" + count + "/" + PartyService.MAX_SAVED_PARTIES + " slots used)");
            } else {
                view.showError("Could not save party — no active party yet, or all 5 slots are full.");
            }
        }
    }

    class ViewPartyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Party p = partyService.getActiveParty(user.getUsername());
            if (p == null) {
                view.showError("No active party yet. Press Next Room to start.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Active Party  |  Gold: ").append(p.gold())
              .append("  |  Next room: ").append(campaignService.getNextRoomNumber(user.getUsername()))
              .append("\n");
            for (Hero h : p.heroes()) {
                sb.append("\n  ").append(h.name())
                  .append(" [").append(h.currentClass().displayName()).append("]")
                  .append("  Level ").append(h.level())
                  .append("  HP ").append(h.hp()).append("/").append(h.maxHp())
                  .append("  MP ").append(h.mana()).append("/").append(h.maxMana())
                  .append("  ATK ").append(h.attack())
                  .append("  DEF ").append(h.defense());
                if (h.specializationClass() != null)
                    sb.append("  ★ Specialization: ").append(h.specializationClass().displayName());
                if (h.hybridClass() != null)
                    sb.append("  ★ Hybrid: ").append(h.hybridClass().displayName());
            }
            view.showInfo(sb.toString());
        }
    }

    class HallOfFameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.showInfo("Hall of Fame:\n" + formatHallOfFame(campaignRecordService.getHallOfFame()));
        }
    }

    class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Save progress before returning home
            try {
                campaignService.saveCampaignProgress(user.getUsername());
            } catch (IllegalStateException ignored) {
                // mid-battle save not possible — still go home
            }
            view.dispose();
            homeView.setStatus("Campaign saved. Room " +
                campaignService.getNextRoomNumber(user.getUsername()) + " of 30.");
            homeView.setVisible(true);
        }
    }

    // ── Battle resolution (levelling bug fix is here) ─────────────────────────

    /**
     * Opens the battle dialog and returns the resolved campaign step.
     *
     * <p><b>Levelling fix:</b> after the battle the session's {@code players()} list
     * holds the actual {@link Hero} objects that fought — with updated HP and, once
     * XP is awarded by {@code resolvePendingBattle}, updated levels.  We reconstruct
     * a {@link Party} from those live objects (preserving gold from the pre-battle
     * snapshot) and pass <em>that</em> to the service instead of the stale snapshot.
     */
    private CampaignService.CampaignStepResult resolveBattle(CampaignService.CampaignStepResult result) {
        BattleView battleView = new BattleView(view, "Battle — Room " + result.roomNumber());
        InteractiveBattleSession session = new InteractiveBattleSession(
            result.pendingBattle().party(),
            result.pendingBattle().enemies()
        );
        BattleController controller = new BattleController(session, battleView);
        InteractiveBattleSession.BattleOutcome outcome = controller.runBattle();

        // Rebuild post-battle party from the live session heroes (not the stale snapshot)
        Party postBattleParty = buildPostBattleParty(session, result.pendingBattle().party());

        boolean playerWon = outcome.winner() == Team.PLAYER;
        return campaignService.resolvePendingBattle(
            user.getUsername(),
            playerWon,
            postBattleParty   // ← fixed: was result.pendingBattle().party()
        );
    }

    /**
     * Reconstructs a {@link Party} from the heroes that actually fought.
     * Gold is carried over from the pre-battle snapshot (it lives on Party, not Hero).
     */
    private Party buildPostBattleParty(InteractiveBattleSession session, Party preBattleParty) {
        Party rebuilt = Party.empty();
        // Transfer gold from the snapshot
        rebuilt.addGold(preBattleParty.gold());
        // Add the heroes from the live session (they have current HP/XP state)
        for (Unit u : session.players()) {
            if (u instanceof Hero hero) {
                rebuilt.addHero(hero);
            }
        }
        return rebuilt;
    }

    // ── Inn handling ──────────────────────────────────────────────────────────

    private void handleInnVisit(CampaignService.CampaignStepResult result) {
        view.log("You reached an Inn! Heroes have been healed.");
        List<CampaignService.InnItemOption> items = result.innItems();
        List<String> heroNames = result.party().heroes().stream().map(Hero::name).toList();

        while (!items.isEmpty()) {
            String choice = view.promptInnPurchase(
                items.stream().map(i -> i.name() + " (" + i.cost() + "g — " + i.effect() + ")").toList()
            );
            if (choice == null) break;
            String itemName = choice.substring(0, choice.indexOf(" ("));
            String heroTarget = view.promptHeroTarget(heroNames, itemName);
            if (heroTarget == null) break;
            if (!campaignService.buyInnItem(user.getUsername(), itemName, heroTarget)) {
                view.showError("Could not buy " + itemName + " — not enough gold?");
                break;
            }
            view.log("Bought " + itemName + " for " + heroTarget + ".");
        }

        CampaignService.RecruitOption recruit = campaignService.getPendingRecruit(user.getUsername());
        if (recruit != null) {
            boolean accepted = view.confirmRecruit(
                recruit.name() + " — " + recruit.heroClass().displayName()
                + " Level " + recruit.level() + " for " + recruit.cost() + " gold"
            );
            if (accepted) {
                if (!campaignService.recruitFromInn(user.getUsername())) {
                    view.showError("Could not recruit hero — party full or not enough gold.");
                } else {
                    view.log("Recruited " + recruit.name() + "!");
                }
            }
        }
        campaignService.endInnVisit(user.getUsername());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String formatHallOfFame(List<CampaignRecordService.HallOfFameEntry> entries) {
        if (entries.isEmpty()) return "No completed campaigns yet.";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            CampaignRecordService.HallOfFameEntry e = entries.get(i);
            sb.append(i + 1).append(". ").append(e.username()).append(" — ").append(e.score());
            if (i < entries.size() - 1) sb.append('\n');
        }
        return sb.toString();
    }
}
