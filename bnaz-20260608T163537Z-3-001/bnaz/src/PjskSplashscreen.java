/**
 * EPIC 4-LANE RHYTHM GAME SPLASH SCREEN
 * launcher menu for the 4-lane rhythm game.
 * teaches the player the controls and lets them configure their scroll speed and audio offset
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
 * Last Edited: June 12, 2026
 */

import javax.swing.*;
import java.awt.*;

public class PjskSplashscreen extends JFrame {

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
    public PjskSplashscreen() throws Exception {
        // loads saved settings
        pjsk.loadGameData();

        // window setup
        setTitle("EPIC 4-LANE RHYTHM GAME!!!");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null); // Centers the window on the screen
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel();
        header.setBackground(Color.BLACK);
        JLabel title = new JLabel("EPIC 4-LANE RHYTHM GAME", SwingConstants.CENTER);
        title.setFont(new Font("Poppins", Font.BOLD, 54));
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

        JLabel instructions2 = new JLabel("Press D, F, J, and K to hit notes in the 4 lanes.");
        instructions2.setFont(new Font("Poppins", Font.PLAIN, 24));
        instructions2.setForeground(Color.WHITE);
        instructions2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructions3 = new JLabel("D = Lane 1 | F = Lane 2 | J = Lane 3 | K = Lane 4");
        instructions3.setFont(new Font("Poppins", Font.PLAIN, 24));
        instructions3.setForeground(Color.WHITE);
        instructions3.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructions4 = new JLabel("Hit SPACE to start the music once you load in!");
        instructions4.setFont(new Font("Poppins", Font.ITALIC, 24));
        instructions4.setForeground(Color.LIGHT_GRAY);
        instructions4.setAlignmentX(Component.CENTER_ALIGNMENT);

        // adds the instructions to the centre  panel with some spacing
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
        speedInput = new JTextField(String.valueOf(pjsk.scrollSpeed));
        offsetInput = new JTextField(String.valueOf(pjsk.offset));

        // level select (pjsk only has 1 level)
        String[] levels = {"Level 1 (180 BPM)"};
        levelSelect = new JComboBox<>(levels);
        levelSelect.setSelectedIndex(0);

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
                new splashscreen(); // opens the main splashscreen
            } catch (Exception ex) {}
        });

        // play now button
        JButton playButton = new JButton("PLAY NOW");
        playButton.setFont(new Font("Poppins", Font.BOLD, 36));
        playButton.setBackground(Color.PINK);
        playButton.setForeground(Color.BLACK);
        playButton.setFocusPainted(false);
        playButton.setPreferredSize(new Dimension(300, 80));

        playButton.addActionListener(e -> {
            try {
                // reads the numbers the user typed into the text boxes
                pjsk.scrollSpeed = Double.parseDouble(speedInput.getText());
                pjsk.offset = Long.parseLong(offsetInput.getText());

                // only 1 level for this game
                if (levelSelect.getSelectedIndex() == 0) {
                    pjsk.beatmapFile = "pjsk_level1.txt";
                }

                pjsk.totalPlays++; // add 1 to the play counter
                pjsk.saveGameData(); // save settings to the text file

                dispose(); // close the menu
                pjsk.launch(); // starts game
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
        new PjskSplashscreen();
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