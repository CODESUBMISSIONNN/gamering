/**
 * EPIC 4 LANE RHYTHM GAME
 * This is a vertical scrolling rhythm game. Instead of one drum, you have 4 lanes.
 * different columns (lanes). Notes fall from the top of the screen to the bottom.
 *
 * @author Joshua Chen
 * Last Edited: June 12, 2026
 *
 * SOURCES:
 * * 1. game logic
 * * https://www.gamedeveloper.com/programming/music-syncing-in-rhythm-games
 * * 2. audio
 * * https://docs.oracle.com/javase/tutorial/sound/playing.html
 * * 3. swing
 * * https://docs.oracle.com/javase/tutorial/uiswing/index.html
 * * 4. general game loop, drawing, most things that aren't the music syncing
 * * FROM OLD CULMINATING.
 * * 5. audio syncing and delta time
 * * help from friend + https://www.gamedeveloper.com/programming/music-syncing-in-rhythm-games again
 * * 6. object pooling for the audio
 * * https://gameprogrammingpatterns.com/object-pool.html
 * * 7. extra graphical support
 * * https://docs.oracle.com/javase/tutorial/uiswing/painting/
 * * 8. General rhythm game support:
 * * https://www.youtube.com/watch?v=cZzf1FQQFA0
 * *https://www.youtube.com/watch?v=vmnc6Ujzp7w
 *
 * NOTE: this is basically just taiko but with extra steps.
 */

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class pjsk extends JPanel implements KeyListener, ActionListener {

    // game settings
    static double scrollSpeed = 0.8; // this affects how fast the notes appear to move.
    static long offset = 0; // adjusts audio and visual sync, can be used to account for offset like bluetooth or hardware limitations
    static int hitWindowMs = 150; // margin of error (lower = less forgiving, higher = more forgiving)
    static String beatmapFile = "pjsk_level1.txt"; // level file being loaded
    static int highScore = 0; // variable where high score is stored
    static int totalPlays = 0; // variable where total plays is stored
    static final String DATA_FILE = "pjsk_data.txt"; // this is where the pjsk data is saved

    // visuals
    private static final int HIT_Y = 600; // y coordinates of the hit line
    private static final int LANE_WIDTH = 100; // width of columns
    private static final int NOTE_HEIGHT = 30; // how tall the falling notes are
    private static final int LANE_START_X = 440; // x coordinates used to centre the lanes


    // colors
    private static final Color LANE_COLOUR = new Color(20, 20, 25); // darker dark gray, default java dark gray looks ugly for this.
    private static final Color LANE_LINE_COLOUR = new Color(50, 50, 60); // divider lines, just slightly lighter
    private static final Color HIT_LINE_COLOUR = Color.white; // the hitline colour
    private static final Color NOTE_COLOUR = Color.pink;// falling note colour

    // audio
    Clip audioClip; // audio player
    long startTime = 0; // stores the clock time when you start playing
    boolean isPlaying = false; // tracks if the game is running
    boolean isGameOver = false; // tracks if the leve is over
    Timer gameLoop; // updates the screen

    // visual feedback
    int[] laneFlash = {0, 0, 0, 0}; // lane flash for the input, numbers in the array control how bright the flash is with 0 = no flash and 150 = flash

    // stats
    ArrayList<Note> notes;
    static int score = 0; // stores the user curent score
    static int maxPossibleScore = 0; // the max possible score if the player does perfectly
    static int combo = 0; // stores the users current combo
    static int maxCombo = 0; // stores the users max combo of the level
    static int numPerfect = 0; // stores the number of perfect hits
    static int numGreat = 0; // stores the number of great hits
    static int numOk = 0; // stores the number of ok hits
    static int numMiss = 0; // stores the number of missed hits
    static String rank = ""; // stores the letter rank (S, A, B, C, F)
    String feedback = "Press SPACE to start!"; // text on screen, starts as press space to start then changes to note feedback (e.g pefect, great, etc.)

    /**
     * Represents a singular interactable note on the track.
     * This is an improved version of my previous note class.
     */

    class Note {
        long timeMs; // timing of note thats supposed to be hit
        int lane; // 0 = d, 1 = f, 2 = j, 3 = k
        boolean isHit = false; // if player hits it's true
        boolean isMissed = false; // true if it makes it past the window

        /**
         * this is what creates a new note
         * @param timeMs the exact time it needs to be hit
         * @param lane the column (0, 1, 2, 3)
         */
        public Note(long timeMs, int lane) {
            this.timeMs = timeMs;
            this.lane = lane;
        }
    }


    /**
     * *Constructor.
     * This creates the initial game state, UI, beatmap data, audio, game loop, and resets variables
     * @author Joshua Chen
     * @throws Exception
     */
    public pjsk() throws Exception {
        // resets all score variables
        score = 0;
        combo = 0;
        maxCombo = 0;
        numPerfect = 0;
        numGreat = 0;
        numOk = 0;
        numMiss = 0;
        rank = "";
        isPlaying = false;
        isGameOver = false;
        startTime = 0;
        feedback = "Press SPACE to start!";

        setFocusable(true);
        addKeyListener(this);
        notes = new ArrayList<>();

        // reads the text files and loads the audio
        loadGameData();
        loadBeatMap(beatmapFile);
        loadAudio();

        // starts the the game loop that does actionevent every 16ms
        gameLoop = new Timer(16, this);
        gameLoop.start();
    }

    /**
     * Opens the data file and loads the players settings and high score
     * @author Joshua chen
     * @throws Exception
     */
    static void loadGameData() throws Exception {
        File file = new File(DATA_FILE);

        if (!file.exists()) {
            return; // if the file doesn't exist, aborts.
        }

        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String[] parts = scanner.nextLine().split("="); // splits the lines (for example, "scrollSpeed=0.8" turns into "scrollSpeed, 0.8")

            if (parts.length == 2) { // if split correctly into 2 pieces, accidental spaces are removed
                String key = parts[0].trim();
                String val = parts[1].trim();

                switch(key) { // reads the file and assigns values to correct variables
                    case "scrollSpeed": scrollSpeed = Double.parseDouble(val); break;
                    case "offset": offset = Long.parseLong(val); break;
                    case "highScore": highScore = Integer.parseInt(val); break;
                    case "totalPlays": totalPlays = Integer.parseInt(val); break;
                    case "beatmapFile": beatmapFile = val; break;
                }
            }
        }
        scanner.close(); // closes scanner so my ide doesn't yell at me
    }

    /**
     * Creates / overwrite save file to store the current settings
     * @author Joshua Chen
     * @throws Exception
     */
    static void saveGameData() throws Exception {
        PrintWriter Writer = new PrintWriter(DATA_FILE);
        Writer.println("scrollSpeed=" + scrollSpeed);
        Writer.println("offset=" + offset);
        Writer.println("highScore=" + highScore);
        Writer.println("totalPlays=" + totalPlays);
        Writer.println("beatmapFile=" + beatmapFile);
        Writer.close();
    }

    /**
     * Reads the level text file and creates the notes
     * Simulates a perfect run and calculates the max possible score
     *
     * @author Joshua Chen
     * @param filename the name of the text file
     * @throws Exception
     */
    private void loadBeatMap(String filename) throws Exception {
        notes.clear();
        maxPossibleScore = 0;

        File file = new File(filename);

        if (!file.exists()) {
            int currentTime = 2000;
            for(int i = 0; i < 50; i++) {
                int randomLane = (int)(Math.random() * 4);
                notes.add(new Note(currentTime, randomLane));
                currentTime += 250;
            }
        } else {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length == 2) {
                    long timeMs = Long.parseLong(parts[0].trim());
                    int lane = Integer.parseInt(parts[1].trim());
                    notes.add(new Note(timeMs, lane));
                }
            }
            scanner.close();
        }

        int tempCombo = 0;
        for(int i = 0; i < notes.size(); i++){
            tempCombo++;
            double comboMultiplier = 1.0 + Math.min((tempCombo / 10.0) * 0.1, 1.0);
            maxPossibleScore += (int)(300 * comboMultiplier);
        }
    }

    /**
     * Loads the background music
     *
     * @author Joshua chen
     * @throws Exception
     */
    private void loadAudio() throws Exception {
        File musicFile = new File("music.wav");
        if (musicFile.exists()) {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);

            // SUPER QUIET MUSIC (-25 decibels)
            FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-25.0f);
        }
    }

    /**
     * Determines how many milliseconds have passed since the game started
     *
     * @author Joshua chen
     * @return the elapsed time in milliseconds
     */
    private long currentGameTime() {
        if (!isPlaying) return 0;
        if (audioClip != null && audioClip.isRunning()) {
            return (audioClip.getMicrosecondPosition() / 1000) + offset;
        } else {
            return System.currentTimeMillis() - startTime;
        }
    }

    /**
     * Checks if the button player pressed matches the falliung note
     *
     * @author Joshua Chen
     * @param targetLane the column the player pressed
     */
    private void checkHit(int targetLane) {
        long currentAudioTime = currentGameTime();

        for (Note n : notes) {
            if (!n.isHit && !n.isMissed && n.lane == targetLane) {
                long difference = Math.abs(n.timeMs - currentAudioTime);

                if (difference <= hitWindowMs) {
                    n.isHit = true;
                    combo++;

                    if (difference <= 50) {
                        feedback = "PERFECT!";
                        addScore(200, 1.5);
                        numPerfect++;
                    } else if (difference <= 100) {
                        feedback = "GREAT!";
                        addScore(100);
                        numGreat++;
                    } else if (difference <= 120){
                        feedback = "OK";
                        addScore(50);
                        numOk++;
                    } else {
                        feedback = "MISS!";
                        if(combo > maxCombo) maxCombo = combo;
                        combo = 0;
                        numMiss++;
                    }
                    return;
                }
            }
        }
    }

    /**
     * Calculates the score multiplier, assigns grades, and saves high scores.
     *
     * @author Joshua Chen
     * @param points
     */
    public void addScore(int points) {
        // multiplier caps at 2x
        double comboMultiplier = 1.0 + Math.min((combo / 10.0) * 0.1, 1.0);
        score += (int)(points * comboMultiplier);

        // letter grade by percentage of max possible score
        if (maxPossibleScore > 0) {
            double percentage = (double) score / maxPossibleScore;
            if (percentage >= 0.95) { rank = "S"; }
            else if (percentage >= 0.80) { rank = "A"; }
            else if (percentage >= 0.70) { rank = "B"; }
            else if (percentage >= 0.60) { rank = "C"; }
            else { rank = "F"; }
        }

        // if the player beats their high score, update.
        if (score > highScore) {
            highScore = score;
            try {
                saveGameData();
            } catch (Exception e) {}
        }
    }
    /**
     * Adds score with a custom multiplier (Overloaded method)
     *
     * @author Joshua Chen
     * @param points the base points
     * @param bonusMultiplier the custom multiplier
     */
    public void addScore(int points, double bonusMultiplier) {
        int bonusPoints = (int)(points * bonusMultiplier);
        addScore(bonusPoints);
    }



    /**
     * checks pass/fail against a specific requirement
     *
     * @author Joshua Chen
     * @param customRequirement the score needed to pass
     * @return true if passed, false otherwise
     */
    public boolean checkPassFail(int customRequirement) {
        return score >= customRequirement;
    }

    /**
     * Returns the current score
     *
     * @author Joshua Chen
     * @return the player's score
     */
    public int getCurrentScore() {
        return score;
    }

    /**
     * Returns the current score plus a bonus
     *
     * @author Joshua Chen
     * @param bonus points to temporarily add
     * @return the inflated score
     */
    public int getCurrentScore(int bonus) {
        return score + bonus;
    }

    /**
     * Triggers when player presses key on keyboard
     *
     * @author Joshua chen
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if(notes.isEmpty()) {
            return; // when there are no notes, this is irrelevant so it ignores the keyboard
        }
        int key = e.getKeyCode(); // asks which key is pressed

        if(key == KeyEvent.VK_SPACE && !isPlaying){ // starts the game with spacebar
            isPlaying = true;
            startTime = System.currentTimeMillis(); // records start time
            feedback = "GO!!!!";

            // rewinds music to 0 and plays
            if (audioClip != null) {
                audioClip.setFramePosition(0);
                audioClip.start();
            }
            return;
        }

        if(!isPlaying) {
            return;
        }

        // if they press a lane key the lane flashes
        if(key == KeyEvent.VK_D) {
            laneFlash[0] = 150; checkHit(0);
        } else if(key == KeyEvent.VK_F) {
            laneFlash[1] = 150; checkHit(1);
        } else if(key == KeyEvent.VK_J) {
            laneFlash[2] = 150; checkHit(2);
        } else if(key == KeyEvent.VK_K) {
            laneFlash[3] = 150; checkHit(3);
        }
    }

    // required
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * The game loop. Runs at 60fps. Handles some visuals, missed notes, and ending the level.
     *
     * @author Joshua Chen
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e){
        if (isPlaying && !isGameOver){
            long currentTime = currentGameTime();

            // decreases the flash in every lane by 15opacity per frame until it hits 0
            for (int i = 0; i < 4; i++) {
                if (laneFlash[i] > 0) {
                    laneFlash[i] = Math.max(0, laneFlash[i] - 15);
                }
            }

            // miss detection
            for(Note n : notes){
                if (!n.isHit && !n.isMissed && currentTime > n.timeMs + hitWindowMs){ // if wasn't hit, wasn't already missed, and is past the hit window that means it's missed.
                    n.isMissed = true; // mark as missed
                    if(combo > maxCombo) maxCombo = combo; // saves combo record
                    combo = 0; // breaks combo
                    feedback = "MISS"; // feedback
                    numMiss++;
                }
            }

            // checks for end of the level
            if (notes.size() > 0) {
                long lastNoteTime = notes.get(notes.size() - 1).timeMs; // finds the last note in the whole level
                // if the current time is 2000 ms past the final
                if (currentTime > lastNoteTime + 2000) {
                    isGameOver = true; // game stops
                    if (combo > maxCombo) maxCombo = combo;
                    showResultsScreen(); // post game opens
                }
            }
        }
        // updates at 60fps
        repaint();
    }

    /**
     * Stops gme engine, closes window, opens post game
     *
     * @author Joshua Chen
     */
    private void showResultsScreen() {
        if(!checkPassFail(maxPossibleScore / 2)){
            rank = "F";
        }
        gameLoop.stop(); // turns off game timer
        if (audioClip != null) audioClip.stop(); // stops music

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.dispose(); // closes the game window the game currently is in
        }

        try {
            PjskResultsScreen.launch(); // launches the results gui
        } catch (Exception ex) {}
    }

    /**
     * Method is called every time repaint(); is called
     * Draws background, lanes, flashes, falling notes, etc.
     *
     * @author Joshua CHen
     * @param g the <code>Graphics</code> object to protect
     */
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // antialiasing

        // background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 4 lanes
        g2d.setColor(LANE_COLOUR);
        g2d.fillRect(LANE_START_X, 0, LANE_WIDTH * 4, getHeight());

        // vertical divider between lanes
        g2d.setColor(LANE_LINE_COLOUR);
        for (int i = 0; i <= 4; i++) { // loops through  each divider that needs to be drawn
            int lineX = LANE_START_X + (i * LANE_WIDTH); // calculates the x coordinate needed to draw the line
            g2d.drawLine(lineX, 0, lineX, getHeight()); // draws the line
        }

        // draws the lane flashes
        for (int i = 0; i < 4; i++) {
            if (laneFlash[i] > 0) { // if the opacity is greater than 0, make white color
                g2d.setColor(new Color(255, 255, 255, laneFlash[i]));
                g2d.fillRect(LANE_START_X + (i * LANE_WIDTH), 0, LANE_WIDTH, getHeight());
            }
        }

        // hit line
        g2d.setColor(HIT_LINE_COLOUR);
        g2d.fillRect(LANE_START_X, HIT_Y, LANE_WIDTH * 4, 10);

        // the button target things
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(LANE_START_X + 10, HIT_Y - 10, LANE_WIDTH - 20, 30);
        g2d.drawRect(LANE_START_X + LANE_WIDTH + 10, HIT_Y - 10, LANE_WIDTH - 20, 30);
        g2d.drawRect(LANE_START_X + (LANE_WIDTH*2) + 10, HIT_Y - 10, LANE_WIDTH - 20, 30);
        g2d.drawRect(LANE_START_X + (LANE_WIDTH*3) + 10, HIT_Y - 10, LANE_WIDTH - 20, 30);

        // letters under the lanes
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Poppins", Font.BOLD, 24));
        String[] keys = {"D", "F", "J", "K"};
        for (int i = 0; i < 4; i++) {
            int textX = LANE_START_X + (i * LANE_WIDTH) + (LANE_WIDTH / 2) - 10; // calculates the centre of the lane, minus 10 px to the left. This centres the letter.
            g2d.drawString(keys[i], textX, HIT_Y + 50);
        }

        // falling ntoes
        long currentTime = currentGameTime();

        // looks at every note in the list
        for (Note n : notes) {
            // if it nhasn't been hit or missed, draws note if the Y coordinate is visible on the screen.
            if (!n.isHit && !n.isMissed) {
                int currentY = HIT_Y - (int)((n.timeMs - currentTime) * scrollSpeed); // calculates distance from target line
                if (currentY > -100 && currentY < getHeight() + 100) { // draws if y coordinate is visible
                    int noteX = LANE_START_X + (n.lane * LANE_WIDTH); // calcualtes which x pixel the note belongs to based on it's lane number
                    g2d.setColor(NOTE_COLOUR);
                    g2d.fillRect(noteX + 5, currentY - (NOTE_HEIGHT/2), LANE_WIDTH - 10, NOTE_HEIGHT); // draws ntoes a bit smaller than the columns to make it look cleaner
                }
            }
        }

        // hud
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Poppins", Font.BOLD, 24));
        g2d.drawString("Score: " + score, 20, 40);
        g2d.drawString("High Score: " + highScore, 20, 70);
        g2d.drawString("Combo: " + combo, 20, 100);
        g2d.drawString("Rank: " + rank, 20, 130);
        g2d.drawString(feedback, getWidth() / 2 - 50, HIT_Y - 150);
    }

    /**
     * Creates the game window
     *
     * @author Joshua Chen
     * @throws Exception
     */
    public static void launch() throws Exception {
        JFrame frame = new JFrame("EPIC 4-LANE RHYTHM GAME!!!");
        pjsk game = new pjsk();
        frame.add(game);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocusInWindow();
    }

    /**
     * testable without launcher
     * @author Joshua Chen
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        launch();
    }
}