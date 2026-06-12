/**
 * Splashscreen for the results of the 4 lane rhythm game .
 * displays final stats and rank
 *
 *  * SOURCES:
 *  * 1. OLD CULMINATING
 *  * 2. swing docs
 *  * https://docs.oracle.com/javase/tutorial/uiswing/index.html
 *  * https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html
 *  * https://docs.oracle.com/javase/8/docs/api/javax/swing/BorderFactory.html
 *  * 3. stackoverflow
 *  * https://stackoverflow.com/questions/16134549/how-to-make-a-splash-screen-for-gui
 *  * 4. this youtube guy
 *  * https://www.youtube.com/watch?v=hnUT83niszA
 * @author Joshua Chen
 * Last edited: June 12, 2026
 */

import javax.swing.*;
import java.awt.*;

public class ResultsScreen extends JFrame {

    public ResultsScreen() throws Exception {
        // window setup
        setTitle("RESULTS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel();
        header.setBackground(Color.BLACK);

        JLabel title = new JLabel("LEVEL CLEARED!", SwingConstants.CENTER);
        title.setFont(new Font("Poppins", Font.BOLD, 64));
        title.setForeground(Color.PINK);
        header.add(title);
        header.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        add(header, BorderLayout.NORTH);

        // middle section
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.BLACK);

        // rank
        JLabel rankLabel = new JLabel("RANK: " + (rhythmgame.rank.isEmpty() ? "F" : rhythmgame.rank));
        rankLabel.setFont(new Font("Poppins", Font.BOLD, 100));
        rankLabel.setForeground(Color.YELLOW);
        rankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // grid
        JPanel statsPanel = new JPanel(new GridLayout(3, 2, 20, 10));
        statsPanel.setBackground(Color.BLACK);
        statsPanel.setMaximumSize(new Dimension(600, 200));

        // add to grid
        statsPanel.add(createStatLabel("Score: " + rhythmgame.score));
        statsPanel.add(createStatLabel("Max Combo: " + rhythmgame.maxCombo));
        statsPanel.add(createStatLabel("Perfect: " + rhythmgame.numPerfect));
        statsPanel.add(createStatLabel("Great: " + rhythmgame.numGreat));
        statsPanel.add(createStatLabel("OK: " + rhythmgame.numOk));
        statsPanel.add(createStatLabel("Miss: " + rhythmgame.numMiss));

        // spacing
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(rankLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(statsPanel);

        add(centerPanel, BorderLayout.CENTER);

        // bottom part
        JPanel footer = new JPanel();
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 80, 0));

        // play again button
        JButton returnButton = new JButton("RETURN TO LEVEL SELECT");
        returnButton.setFont(new Font("Poppins", Font.BOLD, 36));
        returnButton.setBackground(Color.PINK);
        returnButton.setForeground(Color.WHITE);
        returnButton.setFocusPainted(false);
        returnButton.setPreferredSize(new Dimension(600, 80));

        // return button logic
        returnButton.addActionListener(e -> {
            try {
                dispose(); // closes window
                TaikoSplashscreen.launch(); // relaunches game
            } catch (Exception ex) {}
        });

        footer.add(returnButton);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Method to make creating labels easier without having to customise them every time. Reduces redundancy.
     *
     * @author Joshua Chen
     * @param text
     * @return JLabel
     */
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Poppins", Font.BOLD, 24));
        return label;
    }

    /**
     * launches result screen
     * @author Joshua Chen
     * @throws Exception
     */
    public static void launch() throws Exception {
        new ResultsScreen();
    }
}