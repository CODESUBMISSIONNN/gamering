/**
 * This is the taiko no tatsujin inspired rhythm game.
 * The program is a java swing based rhythm game inspired by Taiko no Tatsujin.
 * Features include:
 * - Audio synced game clock
 * - Beatmap and setting parsing from game files
 * - Audio pooling to avoid overlapping sound effects
 * - Fully working Taiko no Tatsujin game, with 2 levels featuring the song Guren no Yumiya.
 *
 * SOURCES:
 * 1. game logic
 * https://www.gamedeveloper.com/programming/music-syncing-in-rhythm-games
 * 2. audio
 * https://docs.oracle.com/javase/tutorial/sound/playing.html
 * 3. swing
 * https://docs.oracle.com/javase/tutorial/uiswing/misc/timer.html
 * 4. general game loop, drawing, most things that aren't the music syncing
 * FROM OLD CULMINATING.
 * 5. audio syncing and delta time
 * help from friend +  https://www.gamedeveloper.com/programming/music-syncing-in-rhythm-games again
 * 6. object pooling for the audio
 * https://gameprogrammingpatterns.com/object-pool.html
 * 7. extra graphical support
 * https://docs.oracle.com/javase/tutorial/uiswing/painting/
 * 8. General rhythm game support:
 * https://www.youtube.com/watch?v=cZzf1FQQFA0
 * @author Joshua Chen
 * @since 2026-06-08
 */

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

// need to utilise audio based clock instead of frame rate
// need to calculate elapsed time isntead of relying on frame rate

public class rhythmgame extends JPanel implements KeyListener, ActionListener { // jpanel is the visuals, keylistener is for keyboard inputs, actionmlistenr is used for game loop


    //CONFIGURABLE SETTINGS
    static double scrollSpeed = 0.7; // this affects how fast the notes appear to move.
    static long offset = -50; // adjusts audio and visual sync, can be used to account for offset like bluetooth or hardware limitations
    static int hitWindowMs = 150; // margin of error (lower = less forgiving, higher = more forgiving)
    static String beatmapFile = "level1.txt"; // file being loaded
    static int highScore = 0; // variables where high score is stored
    static int totalPlays = 0; // variables where total plays is stored
    static final String DATA_FILE = "gamedata.txt"; // this is where the data is saved.

    //VISUALS
    private static final int TRACK_Y = 360; // y axis
    private static final int JUDGE_X = 200; // x axis
    private static final int NOTE_SIZE = 60; // note size
    private static final int BIG_NOTE_SIZE = 90; // this was made with the idea that there would be different note sizes, now it's just used as a placeholder size.

    //COLORS
    private static final Color DON_COLOUR = Color.red;
    private static final Color KA_COLOUR = Color.cyan;
    private static final Color TRACK_LINE_COLOUR = Color.darkGray;

    // VARIABLES FOR AUDIO AND TIMING
    Clip audioClip; // holds music for background
    Clip[] donClips = new Clip[10]; //holds an array of 10 sfx to prevent audio cutoffs
    Clip[] kaClips = new Clip[10]; // same thing but for ka instead
    int donIndex = 0; // keeps track of which don to play
    int kaIndex = 0; // keeps track of what ka to play
    long startTime = 0; // system time of spacebar press
    boolean isPlaying = false;
    boolean isGameOver = false;
    Timer gameLoop; // loop that triggers actionperformed

    ArrayList<Note> notes; // holds the notes fore the level
    static int score = 0;
    static int maxPossibleScore = 0; // used to calcaulte the percentage needed for each respective letter grade
    static int combo = 0;
    static int maxCombo = 0;
    static int numPerfect = 0;
    static int numGreat = 0;
    static int numOk = 0;
    static int numMiss = 0;
    String feedback = "Press SPACE to start!";
    static String rank = ""; // letter grade
    boolean passed = false;

    /**
     * Represents a singular interactable note on the track.
     * This is an improved version of my previous note class.
     */

    class Note {
        long timeMs; // timing of note thats supposed to be hit
        int type; // 0 don, 1 ka
        boolean isHit = false; // if player hits it's true
        boolean isMissed = false; // true if it makes it past the window

        // turns "new Note(5000, 0)" into
        // timeMs = 5000;
        // type = 0;
        public Note(long timeMs, int type) {
            this.timeMs = timeMs;
            this.type = type;
        }
    }
    /**
     *Constructor.
     *This creates the initial game state, UI, beatmap data, audio, game loop, and resets variables.
     * @author Joshua Chen
     * @throws Exception if beatmap parsing or audio fails
     */
    public rhythmgame() throws Exception {
        // all variables are set to 0 in case of replays
        score = 0;
        combo = 0;
        maxCombo = 0;
        numPerfect = 0;
        numGreat = 0;
        numOk = 0;
        numMiss = 0;
        rank = "";
        passed = false;
        isPlaying = false;
        isGameOver = false;
        startTime = 0;
        feedback = "Press SPACE to start!";

        setFocusable(true); // makes keylistener work
        addKeyListener(this); // makes keylistener work
        // makes list to hold the notes
        notes = new ArrayList<>();

        // reads the text files and loads the audio
        loadBeatMap(beatmapFile);
        loadAudio();

        // starts the the game loop that does actionevent every 16ms
        gameLoop = new Timer(16, this);
        gameLoop.start();
    }

    /**
     * Reads the game settings and high scores from text file.
     * @author Joshua Chen
     * @throws Exception
     */
    static void loadGameData() throws Exception {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return; // if the file doesn't exist, aborts.
        }
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            // splits the lines (for example, "scrollSpeed=0.8" turns into "scrollSpeed, 0.8")
            String[] parts = scanner.nextLine().split("=");
            if (parts.length == 2) { // if split correctly into 2 pieces, accidental spaces are removed
                String key = parts[0].trim();
                String val = parts[1].trim();

                // assigns values based off the key
                switch(key) {
                    case "scrollSpeed": scrollSpeed = Double.parseDouble(val); break;
                    case "offset": offset = Long.parseLong(val); break;
                    case "highScore": highScore = Integer.parseInt(val); break;
                    case "totalPlays": totalPlays = Integer.parseInt(val); break;
                    case "beatmapFile": beatmapFile = val; break;
                }
            }
        }
        scanner.close();
    }

    // overwrites to save file with the current variabels

    /**
     * Saves the data into the text file
     * @author Joshua Chen
      * @throws Exception
     */
    static void saveGameData() throws Exception {
        PrintWriter out = new PrintWriter(DATA_FILE);
        out.println("scrollSpeed=" + scrollSpeed);
        out.println("offset=" + offset);
        out.println("highScore=" + highScore);
        out.println("totalPlays=" + totalPlays);
        out.println("beatmapFile=" + beatmapFile);
        out.close(); // close so my ide doesn't yell at me
    }

    /**
     * Parses the text file to generate the list of notes for selected level.
     * @author Joshua Chen
     * @param filename the name of the text file with the data
     * @throws Exception if file format doesn't exist or is corrupted
     */
    private void loadBeatMap(String filename) throws Exception {
        notes.clear();
        maxPossibleScore = 0;

        File file = new File(filename);

        // if the file somehow doesn't exist it'l play this short level
        if (!file.exists()) {
            int currentTime = 3000;
            for(int i = 0; i < 30; i++){
                notes.add(new Note(currentTime, i % 2));
                currentTime += 350;
            }
        } else {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length == 2) {
                    long timeMs = Long.parseLong(parts[0].trim());
                    int type = Integer.parseInt(parts[1].trim());
                    notes.add(new Note(timeMs, type));
                }
            }
            scanner.close();
        }

        // calculates the max score and scales by rank
        int tempCombo = 0;
        for(int i = 0; i < notes.size(); i++){
            tempCombo++;
            double comboMultiplier = 1.0 + Math.min((tempCombo / 10.0) * 0.1, 1.0);
            maxPossibleScore += (int)(300 * comboMultiplier);
        }
    }

    /**
     * Checks if the timing of the players input aligns with the timing of the latest note.
     * @author Joshua Chen
     * @param hitType represents the key pressed (0 = don, 1 = ka)
     */
    private void checkHit(int hitType) {
        long currentAudioTime = currentGameTime();

        for (Note n : notes) {
            // checks if hit color is right
            if (!n.isHit && !n.isMissed && n.type == hitType) {
                // calcualtes teh distance between current time and notes target time
                long difference = Math.abs(n.timeMs - currentAudioTime);

                // if it's within game window, the note is resolved
                if (difference <= hitWindowMs) {
                    n.isHit = true;
                    combo++;

                    //assigns points based on the difference
                    if (difference <= 50) {
                        feedback = "PERFECT!";
                        addScore(300);
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
                        combo = 0; //combo breaks
                        numMiss++;
                    }
                    return; // one note per keypress, stops checking other notes.
                }
            }
        }
    }

    // loads the audio files into the CLip object

    /**
     * Initialises background music and preloads the don and ka sfx
     * @author Joshua Chen
      * @throws Exception if audio formats like mp3s are played or if files don't exist
     */
    private void loadAudio() throws Exception {
        File musicFile = new File("music.wav");
        if (musicFile.exists()) {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            // audio playback + volume change. This is really just for testing
            FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-15.0f);
        }

        // loads 10 of the don file inti into an array of 10 clips so they can overlap when they are playing instead of cutting each other off.
        File dFile = new File("don.wav");
        if (dFile.exists()) {
            for (int i = 0; i < donClips.length; i++) {
                AudioInputStream donStream = AudioSystem.getAudioInputStream(dFile);
                donClips[i] = AudioSystem.getClip();
                donClips[i].open(donStream);
                FloatControl gainControl = (FloatControl) donClips[i].getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-5.0f);
            }
        }

        // loads 10 of the ka file inti into an array of 10 clips so they can overlap when they are playing instead of cutting each other off.
        File kFile = new File("ka.wav");
        if (kFile.exists()) {
            for (int i = 0; i < kaClips.length; i++) {
                AudioInputStream kaStream = AudioSystem.getAudioInputStream(kFile);
                kaClips[i] = AudioSystem.getClip();
                kaClips[i].open(kaStream);
                FloatControl gainControl = (FloatControl) kaClips[i].getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-5.0f);
            }
        }
    }

    /**
     * Adds scaled points to the players total score based on their current combo.
     * @author Joshua Chen
     * @param points
     */
    public void addScore(int points) {
        // combo mult caps at 2x
        double comboMultiplier = 1.0 + Math.min((combo / 10.0) * 0.1, 1.0);
        score += (int)(points * comboMultiplier);
        checkPassFail();

        // saves high score
        if (score > highScore) {
            highScore = score;
            try {
                saveGameData();
            } catch (Exception e) {}
        }
    }

    // this is the big boy logic that won't make it frame based
    // it calculates true elapsed time

    /**
     * Calculates the true elapsed time of the level
     * @author Joshua Chen
     * @return The current time in ms since the song started, adjusted for offset.
     */
    private long currentGameTime() {
        if (!isPlaying) return 0;
        // if music is playing use the audio files internal clock
        if (audioClip != null && audioClip.isRunning()) {
            return (audioClip.getMicrosecondPosition() / 1000) + offset;
        } else {
            // uses system time if no music is loaded for whatever reason
            return System.currentTimeMillis() - startTime;
        }
    }


    /**
     * Calculates the players grade by comparing their score to the max possible score.
     * @author Joshua Chen
     * @return a boolean that represents teh player passed or not
     */
    public boolean checkPassFail() {

        if (maxPossibleScore == 0) {
            rank = "F";
            return passed;
        }

        double percentage = (double) score / maxPossibleScore;

        if (percentage >= 0.95) { rank = "S"; }
        else if (percentage >= 0.80) {
            rank = "A";}
        else if (percentage >= 0.70) {
            rank = "B";}
        else if (percentage >= 0.60) {
            rank = "C";}
        else {
            rank = "F";}

        return passed;
    }

    // input handler

    /**
     * keylistener method
     * @author Joshua Chen
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // logic for actual game play (will steal from my old code and improve later)
        if(notes.isEmpty()) return;
        int key = e.getKeyCode();

        //starts game
        if(key == KeyEvent.VK_SPACE && !isPlaying){
            isPlaying = true;
            startTime = System.currentTimeMillis();
            feedback = "GO!!!!";
            if (audioClip != null) {
                audioClip.setFramePosition(0);
                audioClip.start();
            }
            return;
        }

        if(!isPlaying) return;

        // F/J for DON
        if(key == KeyEvent.VK_F || key == KeyEvent.VK_J) {
            if (donClips[donIndex] != null) {
                donClips[donIndex].setFramePosition(0);
                donClips[donIndex].start();
                donIndex = (donIndex + 1) % donClips.length; // the modulo loops it back to 0 when the array length htis 10
            }
            checkHit(0);
        }
        // D/K for KA
        else if(key == KeyEvent.VK_D || key == KeyEvent.VK_K) {
            if (kaClips[kaIndex] != null) {
                kaClips[kaIndex].setFramePosition(0);
                kaClips[kaIndex].start();
                kaIndex = (kaIndex + 1) % kaClips.length;
            }
            checkHit(1);
        }
    }
    // unused but if removed the progrma explodes
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * game loop, checks for missed notes, game over, and updates the screen.
     * @author Joshua Chen
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e){
        //triggered every 16ms by teh timer
        if (isPlaying && !isGameOver){
            long currentTime = currentGameTime();

            // checks for missed notes
            for(Note n : notes){
                // if current time is past the note's target time + hit window, it's a miss
                if (!n.isHit && !n.isMissed && currentTime > n.timeMs + hitWindowMs){
                    n.isMissed = true;
                    if(combo > maxCombo) maxCombo = combo;
                    combo = 0; // break combo
                    feedback = "MISS";
                    numMiss++;
                }
            }

            // checks for game over
            if (notes.size() > 0) {
                long lastNoteTime = notes.get(notes.size() - 1).timeMs;
                // if current time is 2 seconds past the final note the game ends
                if (currentTime > lastNoteTime + 2000) {
                    isGameOver = true;
                    if (combo > maxCombo) maxCombo = combo;
                    showResultsScreen();
                }
            }
        }
        // screen updates with new note position
        repaint();
    }

    /**
     * Stops processes and goes into the end screen.
     * @author Joshua Chen
     */
    private void showResultsScreen() {
        gameLoop.stop(); // stops game loop because not needed anymkore
        if (audioClip != null) audioClip.stop();
        // whatever window the game is on gets closed
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) frame.dispose();

        try {
            ResultsScreen.launch(); // opens the ui for post game
        } catch (Exception ex) {}
    }

    /**
     * This is the main rendering logic.
     * @author Joshua Chen
     * @param g the <code>Graphics</code> object to protect
     */
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // for anti aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //bg track
        g2d.setColor(TRACK_LINE_COLOUR);
        int trackHeight = BIG_NOTE_SIZE + 20;
        g2d.fillRect(0, TRACK_Y - (trackHeight/2), getWidth(), trackHeight);

        //hit circle
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        g2d.drawOval(JUDGE_X - (NOTE_SIZE/2), TRACK_Y - (NOTE_SIZE/2), NOTE_SIZE, NOTE_SIZE);

        //draws the notes
        long currentTime = currentGameTime();
        for (Note n : notes) {
            if (!n.isHit && !n.isMissed) {
                // takes the amount of time left until the note hits (n.timeMs - currentTime)
                // multiplies it by the scrollSpeed to figure out  how many pixels away it should be drawn
                int currentX = JUDGE_X + (int)((n.timeMs - currentTime) * scrollSpeed);
                // te note is only drawn if it's ON SCREEN
                if (currentX > -100 && currentX < getWidth() + 100) {
                    if (n.type == 0) g2d.setColor(DON_COLOUR);
                    else g2d.setColor(KA_COLOUR);
                    g2d.fillOval(currentX - 20, TRACK_Y - 20, 40, 40);
                }
            }
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Poppins", Font.BOLD, 24));
        g2d.drawString("Score: " + score, 20, 40);
        g2d.drawString("High Score: " + highScore, 20, 70);
        g2d.drawString("Combo: " + combo, 20, 100);
        g2d.drawString("Total Plays: " + totalPlays, 20, 130);
        g2d.drawString(feedback, getWidth() / 2 - 50, 100);
        g2d.drawString("Rank: " + rank, 20, 160);
    }

    /**
     * initiates teh jframe
     * @author Joshua CHen
     * @throws Exception
     */
    public static void launchGame() throws Exception {
        JFrame frame = new JFrame("SUPER TAIKO!!11!!");
        rhythmgame game = new rhythmgame();
        frame.add(game);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocusInWindow();
    }

    /**
     * for testing when the splashcreen is broke
     * @author Joshua Chen
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // test the window
        TaikoSplashscreen.launch();
    }
}