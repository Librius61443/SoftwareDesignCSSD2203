package com.legendsofswordandwand.gui;

import com.legendsofswordandwand.battle.*;
import com.legendsofswordandwand.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Main Java Swing GUI for the PvP turn-based battle system.
 *
 * Layout:
 * ┌─────────────────────────────────────────────┐
 * │  Title Bar + Round Info                     │
 * ├──────────────┬───────────────┬──────────────┤
 * │  Party 1     │  Turn Order   │  Party 2     │
 * │  Hero Panels │  Queue        │  Hero Panels │
 * ├──────────────┴───────────────┴──────────────┤
 * │  Action Panel (Attack/Defend/Wait/Cast)      │
 * ├─────────────────────────────────────────────┤
 * │  Battle Log (scrollable)                    │
 * └─────────────────────────────────────────────┘
 */
public class BattleGUI extends JFrame implements BattleEventListener {

    private PvPBattleController controller;

    // Header
    private JLabel roundLabel;
    private JLabel activeHeroLabel;

    // Hero panels
    private Map<Hero, HeroPanel> heroPanelMap = new LinkedHashMap<>();
    private JPanel party1Panel;
    private JPanel party2Panel;

    // Turn order
    private JList<String> turnOrderList;
    private DefaultListModel<String> turnOrderModel;

    // Action panel
    private JPanel actionPanel;
    private JButton attackBtn;
    private JButton defendBtn;
    private JButton waitBtn;
    private JButton castBtn;
    private JComboBox<String> targetCombo;
    private JComboBox<String> abilityCombo;

    // Battle log
    private JTextArea logArea;

    // State tracking
    private List<Hero> currentTargets = new ArrayList<>();

    public BattleGUI(PvPBattleController controller) {
        this.controller = controller;
        controller.addListener(this);

        setTitle("Legends of Sword and Wand — PvP Battle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 680);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        initComponents();
        refreshAll();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(new Color(30, 30, 50));

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Center: parties + turn order
        JPanel center = new JPanel(new BorderLayout(8, 0));
        center.setOpaque(false);

        party1Panel = createPartyPanel(controller.getParty1(), "⚔ " + controller.getParty1().getOwnerName());
        party2Panel = createPartyPanel(controller.getParty2(), "⚔ " + controller.getParty2().getOwnerName());

        JPanel turnOrderPanel = createTurnOrderPanel();

        center.add(party1Panel, BorderLayout.WEST);
        center.add(turnOrderPanel, BorderLayout.CENTER);
        center.add(party2Panel, BorderLayout.EAST);
        center.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(center, BorderLayout.CENTER);

        // South: actions + log
        JPanel south = new JPanel(new BorderLayout(5, 5));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        actionPanel = createActionPanel();
        south.add(actionPanel, BorderLayout.NORTH);

        JPanel logPanel = createLogPanel();
        south.add(logPanel, BorderLayout.CENTER);

        add(south, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(20, 20, 40));
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        roundLabel = new JLabel("Round 1", SwingConstants.CENTER);
        roundLabel.setFont(new Font("Georgia", Font.BOLD, 18));
        roundLabel.setForeground(new Color(255, 215, 0));

        activeHeroLabel = new JLabel("Waiting...", SwingConstants.CENTER);
        activeHeroLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        activeHeroLabel.setForeground(Color.WHITE);

        header.add(roundLabel);
        header.add(activeHeroLabel);
        return header;
    }

    private JPanel createPartyPanel(Party party, String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                title, TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Georgia", Font.BOLD, 13), new Color(255, 215, 0)));
        panel.setPreferredSize(new Dimension(175, 300));

        for (Hero hero : party.getHeroes()) {
            HeroPanel hp = new HeroPanel(hero);
            heroPanelMap.put(hero, hp);
            panel.add(hp);
            panel.add(Box.createVerticalStrut(4));
        }
        return panel;
    }

    private JPanel createTurnOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.CYAN, 1),
                "Turn Order", TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 11), Color.CYAN));
        panel.setPreferredSize(new Dimension(200, 300));

        turnOrderModel = new DefaultListModel<>();
        turnOrderList = new JList<>(turnOrderModel);
        turnOrderList.setBackground(new Color(20, 20, 40));
        turnOrderList.setForeground(Color.WHITE);
        turnOrderList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        turnOrderList.setFixedCellHeight(22);

        JScrollPane sp = new JScrollPane(turnOrderList);
        sp.setOpaque(false);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        panel.setBackground(new Color(40, 40, 70));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Actions", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 11), Color.WHITE));

        // Target selector
        JLabel targetLbl = new JLabel("Target:");
        targetLbl.setForeground(Color.WHITE);
        targetCombo = new JComboBox<>();
        targetCombo.setPreferredSize(new Dimension(130, 26));
        targetCombo.setToolTipText("Select target enemy");

        // Ability selector
        JLabel abilityLbl = new JLabel("Ability:");
        abilityLbl.setForeground(Color.WHITE);
        abilityCombo = new JComboBox<>();
        abilityCombo.setPreferredSize(new Dimension(140, 26));

        // Buttons
        attackBtn = makeActionButton("⚔ Attack", new Color(180, 60, 60));
        defendBtn = makeActionButton("🛡 Defend", new Color(60, 120, 180));
        waitBtn   = makeActionButton("⏳ Wait",   new Color(100, 100, 60));
        castBtn   = makeActionButton("✨ Cast",   new Color(120, 60, 180));

        attackBtn.addActionListener(e -> handleAttack());
        defendBtn.addActionListener(e -> handleDefend());
        waitBtn.addActionListener(e -> handleWait());
        castBtn.addActionListener(e -> handleCast());

        panel.add(targetLbl);
        panel.add(targetCombo);
        panel.add(attackBtn);
        panel.add(defendBtn);
        panel.add(waitBtn);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(abilityLbl);
        panel.add(abilityCombo);
        panel.add(castBtn);

        return panel;
    }

    private JButton makeActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 32));
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        return btn;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(880, 150));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Battle Log", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 11), Color.WHITE));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 15, 30));
        logArea.setForeground(new Color(200, 255, 200));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(logArea);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ---- Action handlers ----

    private void handleAttack() {
        Hero target = getSelectedTarget();
        if (target == null) { showError("Please select a target."); return; }
        String msg = controller.attack(target);
        appendLog(msg);
        refreshAll();
    }

    private void handleDefend() {
        String msg = controller.defend();
        appendLog(msg);
        refreshAll();
    }

    private void handleWait() {
        String msg = controller.wait_();
        appendLog(msg);
        refreshAll();
    }

    private void handleCast() {
        Hero active = controller.getActiveHero();
        if (active == null) return;
        List<SpecialAbility> abilities = active.getHeroClass().getSpecialAbilities();
        int idx = abilityCombo.getSelectedIndex();
        if (idx < 0 || idx >= abilities.size()) { showError("No ability selected."); return; }
        SpecialAbility ability = abilities.get(idx);
        Hero target = getSelectedTarget();
        List<Hero> targets = (target != null) ? Collections.singletonList(target) : new ArrayList<>();
        String msg = controller.castAbility(ability, targets);
        appendLog(msg);
        refreshAll();
    }

    private Hero getSelectedTarget() {
        int idx = targetCombo.getSelectedIndex();
        if (idx < 0) return null;
        Hero active = controller.getActiveHero();
        if (active == null) return null;
        List<Hero> enemies = controller.getEnemiesOf(active);
        if (idx >= enemies.size()) return null;
        return enemies.get(idx);
    }

    // ---- Refresh methods ----

    private void refreshAll() {
        Hero active = controller.getActiveHero();

        // Update hero panels
        for (Map.Entry<Hero, HeroPanel> entry : heroPanelMap.entrySet()) {
            entry.getValue().refresh(entry.getKey() == active);
        }

        // Update round label
        roundLabel.setText("Round " + controller.getRoundNumber());

        if (active != null) {
            Party activeParty = controller.getPartyOf(active);
            String owner = (activeParty != null) ? activeParty.getOwnerName() : "?";
            activeHeroLabel.setText(owner + "'s turn: " + active.getName()
                    + " [" + active.getHeroClass().getClassName() + " Lv." + active.getLevel() + "]");
        }

        // Update turn order list
        turnOrderModel.clear();
        int i = 1;
        for (Hero h : controller.getCurrentTurnOrder()) {
            Party p = controller.getPartyOf(h);
            String owner = (p != null) ? p.getOwnerName() : "?";
            String marker = (h == active) ? "► " : "  ";
            turnOrderModel.addElement(marker + i + ". " + h.getName() + " (" + owner + ")");
            i++;
        }

        // Update target combo
        targetCombo.removeAllItems();
        if (active != null) {
            for (Hero enemy : controller.getEnemiesOf(active)) {
                targetCombo.addItem(enemy.getName() + " (HP:" + enemy.getCurrentHp() + ")");
            }
        }

        // Update ability combo
        abilityCombo.removeAllItems();
        if (active != null) {
            for (SpecialAbility ab : active.getHeroClass().getSpecialAbilities()) {
                abilityCombo.addItem(ab.getName() + " (" + ab.getManaCost() + " MP)");
            }
        }

        // Disable actions if battle is over
        boolean canAct = !controller.isBattleOver() && active != null;
        attackBtn.setEnabled(canAct);
        defendBtn.setEnabled(canAct);
        waitBtn.setEnabled(canAct);
        castBtn.setEnabled(canAct);
        targetCombo.setEnabled(canAct);
        abilityCombo.setEnabled(canAct);

        revalidate();
        repaint();
    }

    private void appendLog(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Action Required", JOptionPane.WARNING_MESSAGE);
    }

    // ---- BattleEventListener ----

    @Override
    public void onRoundStart(int roundNumber, List<Hero> turnOrder) {
        SwingUtilities.invokeLater(() -> {
            appendLog("\n--- Round " + roundNumber + " begins ---");
            refreshAll();
        });
    }

    @Override
    public void onAction(Hero actor, BattleAction action, String logMessage) {
        // Log already handled by action handlers
    }

    @Override
    public void onBattleEnd(BattleResult result) {
        SwingUtilities.invokeLater(() -> {
            refreshAll();
            appendLog("\n🏆 " + result.getWinner().getOwnerName() + " WINS the battle!");
            appendLog("Surviving heroes: " + result.getSurvivingWinners().size());

            String winMsg = "🏆 " + result.getWinner().getOwnerName() + " wins the PvP Battle!\n"
                    + "Surviving heroes: " + result.getSurvivingWinners().size();
            JOptionPane.showMessageDialog(this, winMsg, "Battle Over!", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // ---- Entry point for demo ----

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Build demo parties
            Party p1 = new Party("Alice");
            Hero h1 = new Hero("Aria", new OrderClass());
            Hero h2 = new Hero("Rex", new WarriorClass());

            // Boost Alice's heroes to Level 10 so they have enough stats/mana to function
            for (int i = 1; i < 10; i++) {
                h1.levelUp();
                h2.levelUp();
            }

            p1.addHero(h1);
            p1.addHero(h2);

            Party p2 = new Party("Bob");
            Hero h3 = new Hero("Zara", new ChaosClass());
            Hero h4 = new Hero("Merlin", new MageClass());

            // Boost Bob's heroes to Level 10 so they have enough stats/mana to function
            for (int i = 1; i < 10; i++) {
                h3.levelUp();
                h4.levelUp();
            }

            p2.addHero(h3);
            p2.addHero(h4);

            PvPBattleController ctrl = new PvPBattleController(p1, p2);
            BattleGUI gui = new BattleGUI(ctrl);
            gui.setVisible(true);
        });
    }
}
