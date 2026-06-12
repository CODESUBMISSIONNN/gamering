/**
 * Splashscreen for taiko.
 * teachers player the controls and allows them to change settings
 *
 *  * SOURCES:
 *  * 1. OLD CULMINATING
 *  * 2. swing docs
 *  * https://docs.oracle.com/javase/tutorial/uiswing/index.html
 *  * https://docs.oracle.com/javase/8/docs/api/java/awt/FlowLayout.html
 *  * 3. stackoverflow
 *  * https://stackoverflow.com/questions/16134549/how-to-make-a-splash-screen-for-gui
 *  * 4. this youtube guy
 *  * https://www.youtube.com/watch?v=hnUT83niszA
 * @author Joshua Chen
 * Last edited: June 12, 2026
 */

import javax.swing.*;
import java.awt.*;

public class TaikoSplashscreen extends JFrame {

    // text fields for users to type their settings into
    JTextField speedInput;
    JTextField offsetInput;
    JComboBox<String> levelSelect;

    /**
     * Makes the splashscreen, initiliases the gui, loads saved game data, and sets up the event listeners and buttons
     *
     * @author Joshua Chen
     * @throws Exception
     */
    public TaikoSplashscreen() throws Exception {
        // loads saved settings
        rhythmgame.loadGameData();

        // window setup
        setTitle("SUPER TAIKO!!11!! WOW!");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel();
        header.setBackground(Color.BLACK);
        JLabel title = new JLabel("SUPER TAIKO!!!!!", SwingConstants.CENTER);
        title.setFont(new Font("Poppins", Font.BOLD, 64));
        title.setForeground(Color.WHITE);
        header.add(title);
        header.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        add(header, BorderLayout.NORTH);

        // middle section
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.BLACK);

        // instructions
        JLabel instructions1 = new JLabel("HOW TO PLAY:");
        instructions1.setFont(new Font("Poppins", Font.BOLD, 32));
        instructions1.setForeground(Color.ORANGE);
        instructions1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructions2 = new JLabel("Press F or J for Red Notes (DON)");
        instructions2.setFont(new Font("Poppins", Font.PLAIN, 24));
        instructions2.setForeground(Color.RED);
        instructions2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructions3 = new JLabel("Press D or K for Blue Notes (KA)");
        instructions3.setFont(new Font("Poppins", Font.PLAIN, 24));
        instructions3.setForeground(Color.CYAN);
        instructions3.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructions4 = new JLabel("Hit SPACE to start the music once you load in!");
        instructions4.setFont(new Font("Poppins", Font.ITALIC, 24));
        instructions4.setForeground(Color.LIGHT_GRAY);
        instructions4.setAlignmentX(Component.CENTER_ALIGNMENT);

        // adds the instructions to the centre panel with some spacing
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(instructions1);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(instructions2);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(instructions3);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(instructions4);

        // settings, creates new panel specifically for the setting boxes
        JPanel settingsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        settingsPanel.setBackground(Color.BLACK);
        settingsPanel.setMaximumSize(new Dimension(800, 80));

        // creates the input boxes and fill them with the loaded data
        speedInput = new JTextField(String.valueOf(rhythmgame.scrollSpeed));
        offsetInput = new JTextField(String.valueOf(rhythmgame.offset));

        // level select
        String[] levels = {"Level 1", "Level 2"};
        levelSelect = new JComboBox<>(levels);
        levelSelect.setSelectedIndex(rhythmgame.beatmapFile.equals("level2.txt") ? 1 : 0);

        // labels
        settingsPanel.add(createWhiteLabel("Scroll Speed:"));
        settingsPanel.add(createWhiteLabel("Audio Offset (ms):"));
        settingsPanel.add(createWhiteLabel("Select Level:"));

        // input boxes
        settingsPanel.add(speedInput);
        settingsPanel.add(offsetInput);
        settingsPanel.add(levelSelect);

        // spacing
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(settingsPanel);
        add(centerPanel, BorderLayout.CENTER);

        // bottom part
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        footer.setBackground(Color.BLACK);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 80, 0));

        // back to arcade button
        JButton backButton = new JButton("BACK TO ARCADE");
        backButton.setFont(new Font("Poppins", Font.BOLD, 36));
        backButton.setBackground(Color.DARK_GRAY);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(400, 80));

        backButton.addActionListener(e -> {
            try {
                dispose(); // closes this screen
                new splashscreen(); // opens main slashscreen
            } catch (Exception ex) {}
        });

        // play now button
        JButton playButton = new JButton("PLAY NOW");
        playButton.setFont(new Font("Poppins", Font.BOLD, 36));
        playButton.setBackground(Color.PINK);
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setPreferredSize(new Dimension(300, 80));

        playButton.addActionListener(e -> {
            try {
                // reads the numbers the user typed into the text boxes
                rhythmgame.scrollSpeed = Double.parseDouble(speedInput.getText());
                rhythmgame.offset = Long.parseLong(offsetInput.getText());

                // level select
                if (levelSelect.getSelectedIndex() == 0) {
                    rhythmgame.beatmapFile = "level1.txt";
                } else {
                    rhythmgame.beatmapFile = "level2.txt";
                }

                rhythmgame.totalPlays++; // add 1 play to play counter
                rhythmgame.saveGameData(); // save settings to text file

                dispose(); // close the menu
                rhythmgame.launchGame(); // launches the game
            } catch (Exception ex) {}
        });

        // adds buttons
        footer.add(backButton);
        footer.add(playButton);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Method to make creating labels easier without having to customise them every time. Reduces redundancy.
     *
     * @author Joshua Chen
     * @param text
     * @return jlabel
     */
    private JLabel createWhiteLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Poppins", Font.PLAIN, 16));
        return label;
    }

    /**
     * splashscreen
     *
     * @author Joshua Chen
     * @throws Exception
     */
    public static void launch() throws Exception {
        new TaikoSplashscreen();
    }

    /**
     * launches without main splasscreen
     * @author Joshua CHen
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        launch();
    }
}