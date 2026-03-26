package com.legendsofswordandwand.gui;

import com.legendsofswordandwand.model.Hero;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * A panel showing stats for a single hero.
 * Highlights the active hero in gold.
 */
public class HeroPanel extends JPanel {

    private final Hero hero;
    private JLabel nameLabel;
    private JLabel statsLabel;
    private JProgressBar hpBar;
    private JProgressBar manaBar;
    private JLabel statusLabel;

    private static final Color ACTIVE_COLOR = new Color(255, 230, 100);
    private static final Color DEAD_COLOR   = new Color(180, 180, 180);
    private static final Color NORMAL_COLOR = new Color(240, 248, 255);

    public HeroPanel(Hero hero) {
        this.hero = hero;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                hero.getName(),
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 11)));
        setPreferredSize(new Dimension(160, 130));
        setBackground(NORMAL_COLOR);

        nameLabel = new JLabel(hero.getHeroClass().getClassName() + " Lv." + hero.getLevel());
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        hpBar = new JProgressBar(0, hero.getMaxHp());
        hpBar.setValue(hero.getCurrentHp());
        hpBar.setStringPainted(true);
        hpBar.setForeground(new Color(60, 180, 75));
        hpBar.setFont(new Font("Arial", Font.PLAIN, 9));

        manaBar = new JProgressBar(0, hero.getMaxMana());
        manaBar.setValue(hero.getCurrentMana());
        manaBar.setStringPainted(true);
        manaBar.setForeground(new Color(70, 130, 180));
        manaBar.setFont(new Font("Arial", Font.PLAIN, 9));

        statsLabel = new JLabel(String.format("ATK:%d DEF:%d SHD:%d",
                hero.getAttack(), hero.getDefense(), hero.getShieldHp()));
        statsLabel.setAlignmentX(CENTER_ALIGNMENT);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        statusLabel = new JLabel(" ");
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        statusLabel.setForeground(Color.RED);

        add(nameLabel);
        add(Box.createVerticalStrut(2));
        add(makeLabeledBar("HP: ", hpBar));
        add(makeLabeledBar("MP: ", manaBar));
        add(statsLabel);
        add(statusLabel);
        add(Box.createVerticalGlue());
    }

    private JPanel makeLabeledBar(String label, JProgressBar bar) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 10));
        p.add(lbl, BorderLayout.WEST);
        p.add(bar, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(150, 18));
        return p;
    }

    /** Refresh displayed stats from the hero model. */
    public void refresh(boolean isActive) {
        hpBar.setMaximum(hero.getMaxHp());
        hpBar.setValue(hero.getCurrentHp());
        hpBar.setString(hero.getCurrentHp() + "/" + hero.getMaxHp());

        manaBar.setMaximum(hero.getMaxMana());
        manaBar.setValue(hero.getCurrentMana());
        manaBar.setString(hero.getCurrentMana() + "/" + hero.getMaxMana());

        statsLabel.setText(String.format("ATK:%d DEF:%d SHD:%d",
                hero.getAttack(), hero.getDefense(), hero.getShieldHp()));
        nameLabel.setText(hero.getHeroClass().getClassName() + " Lv." + hero.getLevel());

        // Status
        if (!hero.isAlive()) {
            setBackground(DEAD_COLOR);
            statusLabel.setText("DEFEATED");
            statusLabel.setForeground(Color.GRAY);
        } else if (hero.isStunned()) {
            setBackground(new Color(255, 200, 200));
            statusLabel.setText("STUNNED");
        } else if (isActive) {
            setBackground(ACTIVE_COLOR);
            statusLabel.setText("► ACTING");
            statusLabel.setForeground(new Color(180, 100, 0));
        } else if (hero.isWaiting()) {
            statusLabel.setText("waiting...");
            statusLabel.setForeground(Color.BLUE);
            setBackground(NORMAL_COLOR);
        } else {
            setBackground(NORMAL_COLOR);
            statusLabel.setText(" ");
        }

        repaint();
    }

    public Hero getHero() { return hero; }
}
