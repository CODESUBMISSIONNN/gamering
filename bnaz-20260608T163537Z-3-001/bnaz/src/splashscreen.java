/**
 * This is the splashscreen for the game. It's the launcher for the four games.
 * Displays game titles, description, and high scores.
 * SOURCES:
 * 1. OLD CULMINATING
 * 2. swing docs
 * https://docs.oracle.com/javase/tutorial/uiswing/index.html
 * 3. stackoverflow
 * https://stackoverflow.com/questions/16134549/how-to-make-a-splash-screen-for-gui
 * 4. this youtube guy
 * https://www.youtube.com/watch?v=hnUT83niszA
 *
 * @author Joshua Chen
 * Last edited June 12, 2026
 */

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class splashscreen extends JFrame{

    // arraylist to store the games, description, and high score.
    static ArrayList<String> games = new ArrayList<>();
    static ArrayList<String> description = new ArrayList<>();
    static ArrayList<Integer> highScore = new ArrayList<>();

    /**
     * Constructs the splashscreen GUI, sets up layout, buttons, and event listener
     *
     * @author Joshua Chen
     * @throws Exception
     */
    public splashscreen() throws Exception { //j
        initialise();
        setTitle("Arcade Hub");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720); //16:9
        setLocationRelativeTo(null); //centres
        setLayout(new BorderLayout()); //sets to borderlayout so its easy to centre stuff, splits it into north, south, etc.

        //header
        JPanel header = new JPanel();
        header.setBackground(new Color(0, 0, 0)); // colour
        JLabel title = new JLabel("Arcade Hub", SwingConstants.CENTER); // centres the text
        title.setFont(new Font("Poppins", Font.PLAIN, 48)); //size and look (got a list of fonts from some random forum from years ago)
        title.setForeground(Color.WHITE); // temp colour
        header.add(title);
        add(header, BorderLayout.NORTH); // brings eveyrhting to north that from the borderlayout earlier

        JPanel gamePanel = new JPanel(new GridLayout(2, 2, 20, 20)); //grid
        gamePanel.setBackground(new Color(15, 15, 20)); // grid background
        gamePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // margins

        // loops through number of games
        for(int i =0; i < games.size(); i++) {
            // extracts data for respective game
            String gameName = games.get(i);
            String gameDescription = description.get(i);
            int currentHighScore = highScore.get(i);

            // button setup
            JButton button = new JButton();
            button.setLayout(new BorderLayout()); // for positioning (centre)
            button.setBackground(new Color(40, 44, 52)); //bg
            button.setBorder(BorderFactory.createLineBorder(Color.pink, 2)); //border
            button.setFocusPainted(false); // makes it look pretty (removes the java default lines)

            // game title labels
            JLabel nameLabel = new JLabel(gameName, SwingConstants.CENTER); // game title
            nameLabel.setFont(new Font("Poppins", Font.BOLD, 24));
            nameLabel.setForeground(Color.white);

            // game description and high score
            JLabel descLabel = new JLabel(gameDescription + "High Score: " + currentHighScore, SwingConstants.CENTER); // desc + highscpore
            descLabel.setFont(new Font("Poppins", Font.BOLD, 18));
            descLabel.setForeground(Color.white);

            button.add(nameLabel, BorderLayout.CENTER);
            button.add(descLabel, BorderLayout.SOUTH);

            // maps button clicks to actually launching the respective thing it's supposed to launch
            button.addActionListener(e -> {
                if (gameName.equals("Pac-Man")){
                    dispose();
                    PacMan.launch();
                } else if (gameName.equals("Flappy Bird")){
                    dispose();
                    FlappyBird.launch();
                } else if(gameName.equals("Taiko")){
                    dispose();
                    try {
                        TaikoSplashscreen.launch();
                    } catch (Exception ex) {}
                }else if(gameName.equals("PJSK")){
                    dispose();
                    try{
                        PjskSplashscreen.launch();
                    } catch (Exception ex) {}
                }
            }); // this is where the player would be taken to the game
            gamePanel.add(button); // adds to grid
        }

        add(gamePanel, BorderLayout.CENTER);
        setVisible(true); // Always make sure to set visible at the very end
    }

    /**
     * Initialises data by populating the arrays with game titles, descriptions, and high scores
     *
     * @author Joshua Chen
     * @throws Exception
     */
    public static void initialise() throws Exception { // fills array with baseline
        // clears lists
        games.clear();
        description.clear();
        highScore.clear();

        // adds games to arraylist
        games.add("PJSK");
        games.add("Flappy Bird");
        games.add("Pac-Man");
        games.add("Taiko");

        // adds descriptions
        description.add("A four lane rhythm game! | ");
        description.add("A classic game of Flappy Bird! | ");
        description.add("The classic retro game! | ");
        description.add("Hit the notes on the beat! | ");

        //adds high scores
        highScore.add(getPjskHighscore());
        highScore.add(0);
        highScore.add(0);
        highScore.add(getTaikoHighScore());
    }

    /**
     * Reads gamedata.txt to get the high score from taiko (rhythmgame.java)
     *
     * @author Joshua Chen
     * @return the high score
     * @throws Exception
     */
    private static int getTaikoHighScore() throws Exception {
        File file = new File("gamedata.txt");
        if (!file.exists()) {
            return 0;
        } // if file doesn't exist for whatever reason returns 0 high score

        // reads high score
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String[] parts = scanner.nextLine().split("=");
            if (parts.length == 2 && parts[0].trim().equals("highScore")) {
                int score = Integer.parseInt(parts[1].trim());
                scanner.close();
                return score;
            }
        }
        scanner.close();
        return 0;
    }

    /**
     * reads pjsk data and gets the highscore from the 4 lane rhythm game
     *
     * @author Joshua chen
     * @return
     * @throws Exception
     */
    private static int getPjskHighscore() throws Exception {
        File file = new File("pjsk_data.txt");
        if(!file.exists()){
            return 0;
        }

        Scanner scanner  = new Scanner(file);
        while (scanner.hasNextLine()){
            String[] parts = scanner.nextLine().split("=");
            if (parts.length == 2 && parts[0].trim().equals("highScore")) {
                int score = Integer.parseInt(parts[1].trim());
                scanner.close();
                return score;
            }
        }
        scanner.close();
        return 0;
    }

    /**
     * main method
     *
     * @author Joshua CHen
     * @param args
     * @throws Exception
     */
     public static void main(String[] args) throws Exception {
        new splashscreen();
    }
}