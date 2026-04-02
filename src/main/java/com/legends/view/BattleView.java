package com.legends.view;

import unit.HeroAbility;
import unit.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class BattleView extends JDialog {

    private final JLabel roundLabel = new JLabel();
    private final JLabel actorLabel = new JLabel();
    private final DefaultListModel<String> allyModel = new DefaultListModel<>();
    private final DefaultListModel<String> enemyModel = new DefaultListModel<>();
    private final JList<String> allyList = new JList<>(allyModel);
    private final JList<String> enemyList = new JList<>(enemyModel);
    private final JButton attackButton = new JButton("Attack");
    private final JButton defendButton = new JButton("Defend");
    private final JButton waitButton = new JButton("Wait");
    private final JComboBox<HeroAbility> abilityBox = new JComboBox<>();
    private final JButton castButton = new JButton("Cast");
    private final JTextArea logArea = new JTextArea();

    public BattleView(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.add(roundLabel);
        header.add(actorLabel);
        add(header, BorderLayout.NORTH);

        allyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        enemyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
        center.add(wrapList("Allies", allyList));
        center.add(wrapList("Enemies", enemyList));
        add(center, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout());
        controls.add(attackButton);
        controls.add(defendButton);
        controls.add(waitButton);
        controls.add(abilityBox);
        controls.add(castButton);
        add(controls, BorderLayout.SOUTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setMinimumSize(new java.awt.Dimension(220, 100));
        logScroll.setPreferredSize(new java.awt.Dimension(220, 400));
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(new JLabel("Battle Log", SwingConstants.CENTER), BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);
        logPanel.setMinimumSize(new java.awt.Dimension(220, 100));
        logPanel.setPreferredSize(new java.awt.Dimension(220, 400));
        add(logPanel, BorderLayout.EAST);
    }

    private JPanel wrapList(String title, JList<String> list) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    public void addAttackListener(ActionListener listener) { attackButton.addActionListener(listener); }
    public void addDefendListener(ActionListener listener) { defendButton.addActionListener(listener); }
    public void addWaitListener(ActionListener listener) { waitButton.addActionListener(listener); }
    public void addCastListener(ActionListener listener) { castButton.addActionListener(listener); }

    public void render(int round, Unit actor, List<Unit> allies, List<Unit> enemies, List<HeroAbility> abilities, List<String> logs, boolean canWait) {
        roundLabel.setText("Round: " + round);
        actorLabel.setText(actor == null ? "Battle complete" : "Current actor: " + actor.name() + " (" + actor.team() + ")");
        fillModel(allyModel, allies);
        fillModel(enemyModel, enemies);
        abilityBox.removeAllItems();
        abilities.forEach(abilityBox::addItem);
        waitButton.setEnabled(canWait);
        attackButton.setEnabled(actor != null);
        defendButton.setEnabled(actor != null);
        castButton.setEnabled(actor != null && !abilities.isEmpty());
        List<String> recentLogs = logs.size() > 30 ? logs.subList(logs.size() - 30, logs.size()) : logs;
        logArea.setText(String.join("\n", recentLogs));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void fillModel(DefaultListModel<String> model, List<Unit> units) {
        model.clear();
        for (Unit unit : units) {
            model.addElement(unit.name() + " | HP " + unit.hp() + "/" + unit.maxHp() + " | MP " + unit.mana() + "/" + unit.maxMana());
        }
    }

    public int[] selectedEnemyIndices() {
        return enemyList.getSelectedIndices();
    }

    public int[] selectedAllyIndices() {
        return allyList.getSelectedIndices();
    }

    public HeroAbility selectedAbility() {
        return (HeroAbility) abilityBox.getSelectedItem();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Battle Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Battle", JOptionPane.INFORMATION_MESSAGE);
    }
}
