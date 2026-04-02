package com.legends.view;

import unit.HeroAbility;
import unit.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Non-modal battle window shown to one player during a PvP match.
 * Two of these are displayed simultaneously — one per player.
 * Action buttons are enabled only on this player's heroes' turns.
 */
public class PvpBattleView extends JFrame {

    private final JLabel roundLabel  = new JLabel("Round: -", SwingConstants.CENTER);
    private final JLabel actorLabel  = new JLabel("Waiting...", SwingConstants.CENTER);
    private final JLabel yourTurnLabel = new JLabel(" ", SwingConstants.CENTER);

    private final DefaultListModel<String> allyModel  = new DefaultListModel<>();
    private final DefaultListModel<String> enemyModel = new DefaultListModel<>();
    private final JList<String> allyList  = new JList<>(allyModel);
    private final JList<String> enemyList = new JList<>(enemyModel);

    private final JButton attackButton = new JButton("Attack");
    private final JButton defendButton = new JButton("Defend");
    private final JButton waitButton   = new JButton("Wait");
    private final JComboBox<HeroAbility> abilityBox = new JComboBox<>();
    private final JButton castButton   = new JButton("Cast Ability");

    private final JTextArea logArea = new JTextArea();

    public PvpBattleView(String playerName, int xPosition) {
        super("PvP Battle — " + playerName);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(860, 580);
        setLocation(xPosition, 80);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new GridLayout(3, 1));
        roundLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        actorLabel.setFont(new Font("Arial", Font.BOLD, 13));
        yourTurnLabel.setFont(new Font("Arial", Font.BOLD, 14));
        yourTurnLabel.setForeground(new Color(0, 130, 0));
        header.add(roundLabel);
        header.add(actorLabel);
        header.add(yourTurnLabel);
        add(header, BorderLayout.NORTH);

        // Unit lists
        allyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enemyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.add(wrapList("Your Party", allyList));
        center.add(wrapList("Enemy Party", enemyList));
        add(center, BorderLayout.CENTER);

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        controls.add(attackButton);
        controls.add(defendButton);
        controls.add(waitButton);
        controls.add(new JLabel("Ability:"));
        controls.add(abilityBox);
        controls.add(castButton);
        add(controls, BorderLayout.SOUTH);

        // Battle log
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(220, 0));
        add(logScroll, BorderLayout.EAST);
    }

    private JPanel wrapList(String title, JList<String> list) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        return p;
    }

    // ── Listener wiring ──────────────────────────────────────────────────────

    public void addAttackListener(ActionListener l) { attackButton.addActionListener(l); }
    public void addDefendListener(ActionListener l) { defendButton.addActionListener(l); }
    public void addWaitListener  (ActionListener l) { waitButton.addActionListener(l);   }
    public void addCastListener  (ActionListener l) { castButton.addActionListener(l);   }

    // ── Render ────────────────────────────────────────────────────────────────

    /**
     * Refresh the entire view.
     *
     * @param isMyTurn  true when the current actor belongs to this player
     */
    public void render(int round, Unit actor, List<Unit> myParty, List<Unit> theirParty,
                       List<HeroAbility> abilities, List<String> log, boolean canWait,
                       boolean isMyTurn) {
        roundLabel.setText("Round " + round);
        if (actor == null) {
            actorLabel.setText("Battle complete");
            yourTurnLabel.setText(" ");
        } else {
            actorLabel.setText("Acting: " + actor.name()
                + "  HP " + actor.hp() + "/" + actor.maxHp()
                + "  MP " + actor.mana() + "/" + actor.maxMana());
            yourTurnLabel.setText(isMyTurn ? "⚔ YOUR TURN — choose an action" : "⏳ Opponent's turn…");
            yourTurnLabel.setForeground(isMyTurn ? new Color(0, 130, 0) : Color.GRAY);
        }

        fillModel(allyModel,  myParty);
        fillModel(enemyModel, theirParty);

        abilityBox.removeAllItems();
        abilities.forEach(abilityBox::addItem);

        // Only enable controls when it is this player's turn
        attackButton.setEnabled(isMyTurn);
        defendButton.setEnabled(isMyTurn);
        waitButton  .setEnabled(isMyTurn && canWait);
        castButton  .setEnabled(isMyTurn && !abilities.isEmpty());
        abilityBox  .setEnabled(isMyTurn && !abilities.isEmpty());

        logArea.setText(String.join("\n", log));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void fillModel(DefaultListModel<String> model, List<Unit> units) {
        model.clear();
        for (Unit u : units) {
            String status = u.isAlive() ? "" : " [DEAD]";
            model.addElement(u.name() + "  HP " + u.hp() + "/" + u.maxHp()
                + "  MP " + u.mana() + "/" + u.maxMana() + status);
        }
    }

    // ── Input getters ─────────────────────────────────────────────────────────

    public int[] selectedEnemyIndices() { return enemyList.getSelectedIndices(); }
    public int[] selectedAllyIndices()  { return allyList.getSelectedIndices();  }
    public HeroAbility selectedAbility() { return (HeroAbility) abilityBox.getSelectedItem(); }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Battle Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showResult(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Battle Over", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
