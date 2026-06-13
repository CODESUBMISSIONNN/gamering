/**
 * FLAPPY BIRD
 * Classic Flappy Bird clone. Tap SPACE to flap, gravity pulls you back down.
 * Green pipes scroll in from the right with random gap heights, and you get
 * 1 point for every pipe you make it past. Touching a pipe, the ground or
 * the ceiling ends the run. Your best score is saved to a file so it
 * survives between runs and shows up on the arcade hub. SPACE retries after
 * a crash, and the Back to Menu button returns to the hub.
 *
 * Can run on its own (main method) or get launched from the splashscreen
 * with FlappyBird.launch().
 *
 * @author Aiden Ndreu
 * Created: June 5, 2026
 * Last Edited: June 12, 2026 (Aiden Ndreu)
 *
 * SOURCES:
 * * 1. swing gui (JFrame, JPanel)
 * * https://docs.oracle.com/javase/tutorial/uiswing/
 * * 2. drawing with Graphics (fillRect, fillOval, drawString)
 * * https://docs.oracle.com/javase/tutorial/2d/geometry/
 * * 3. Color / Font / Dimension
 * * same oracle 2d tutorial as 2
 * * 4. swing Timer (the game loop)
 * * https://docs.oracle.com/javase/tutorial/uiswing/misc/timer.html
 * * 5. event handling (ActionListener, KeyAdapter, KeyEvent, WindowAdapter)
 * * https://docs.oracle.com/javase/tutorial/uiswing/events/
 * * 6. extends / implements (inheritance and interfaces)
 * * https://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html
 * * 7. the @Override annotation
 * * https://docs.oracle.com/javase/tutorial/java/annotations/predefined.html
 * * 8. anonymous inner classes (the new KeyAdapter() {...} things)
 * * https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html
 * * 9. JButton + BorderLayout (the back to menu button)
 * * https://docs.oracle.com/javase/tutorial/uiswing/components/button.html
 * * https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
 * * 10. java.util.Random (random pipe gap heights)
 * * https://docs.oracle.com/javase/8/docs/api/java/util/Random.html
 * * 11. private / protected / final access modifiers
 * * https://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html
 * * 12. writing my own constructor
 * * https://docs.oracle.com/javase/tutorial/java/javaOO/constructors.html
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;

public class FlappyBird extends JPanel implements ActionListener {

    // game settings
    private static final int WIDTH = 600; // window size in pixels
    private static final int HEIGHT = 700;
    private static final int GROUND_HEIGHT = 60; // the strip at the bottom
    private static final int TICK_MS = 30; // game loop speed (about 33fps)
    private static final String SCORE_FILE = "flappy_highscore.txt"; // this is where the high score is saved

    // the bird
    private static final int BIRD_SIZE = 30;
    private static final int BIRD_X = 150; // the bird never moves left/right, only the pipes move
    private static final int GRAVITY = 1; // pulls the bird down a bit every tick
    private static final int JUMP_STRENGTH = -12; // a flap sets the speed to this (negative = up)

    // the pipes
    private static final int PIPE_WIDTH = 70;
    private static final int PIPE_GAP = 180; // vertical gap you fly through
    private static final int PIPE_SPEED = 4; // how fast pipes move left
    private static final int PIPE_SPACING = 280; // pixels between pipes
    private static final int NUM_PIPES = 3; // pipes on screen at once

    // bird state
    private int birdY; // vertical position
    private int birdVelocity; // falling speed

    // pipes as parallel arrays, index i is the same pipe in all three
    private int[] pipeX; // left edge of each pipe
    private int[] pipeGapY; // top of each pipe's gap
    private boolean[] pipeScored; // so a pipe only counts for score once

    // stats
    private int score; // current run's score
    private int highScore; // the saved best score

    // game state
    private boolean gameStarted; // false until the first flap
    private boolean gameOver;

    private Timer gameTimer; // the game loop, fires every TICK_MS

    // the score file writer. opened once in the constructor because opening
    // is the only part that can fail, println/flush never do
    private PrintWriter scoreWriter;

    private java.util.Random rng; // for the random gap heights

    /**
     * Constructor.
     * Sets up the panel, key listener, loads the saved high score, opens the
     * score file for saving, places the bird and pipes and starts the game
     * loop.
     *
     * @author Aiden Ndreu
     * @throws Exception
     */
    public FlappyBird() throws Exception {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(135, 206, 235)); // sky blue
        setFocusable(true); // lets the panel receive key presses

        // key listener (anonymous inner class), sends every press to handleKey
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
            } // keyPressed ends
        });

        rng = new java.util.Random();

        highScore = loadHighScore(); // 0 if there's no file yet

        // opens the score file once and immediately rewrites the best, so the
        // file always holds the current record even if the player quits early
        scoreWriter = new PrintWriter(SCORE_FILE);
        scoreWriter.println(highScore);
        scoreWriter.flush(); // pushes the text into the file right away

        resetGame(); // bird and pipes to their starting spots

        // the game loop. calls actionPerformed every TICK_MS milliseconds
        gameTimer = new Timer(TICK_MS, this);
        gameTimer.start();
    } // FlappyBird ends

    /**
     * Returns the current score
     *
     * @author Aiden Ndreu
     * @return the player's score
     */
    public int getScore() {
        return score;
    } // getScore ends

    /**
     * Returns the best score saved so far
     *
     * @author Aiden Ndreu
     * @return the high score
     */
    public int getHighScore() {
        return highScore;
    } // getHighScore ends

    /**
     * Returns true if the run has ended (bird crashed)
     *
     * @author Aiden Ndreu
     * @return true on a game over
     */
    public boolean isGameOver() {
        return gameOver;
    } // isGameOver ends

    /**
     * Setter for the high score. Only accepts the value if it's actually
     * higher than the current best so the record can never go down.
     *
     * @author Aiden Ndreu
     * @param newScore the score to consider as the new best
     */
    public void setHighScore(int newScore) {
        if (newScore > highScore) {
            highScore = newScore;
        }
    } // setHighScore ends

    /**
     * Reads the saved high score from the file. The file just holds numbers
     * (one per save) and the last one is the current best. Starts at 0 if
     * the file isn't there yet.
     *
     * @author Aiden Ndreu
     * @return the saved high score, or 0 if there isn't one
     */
    private int loadHighScore() throws Exception {
        File f = new File(SCORE_FILE);
        if (!f.exists()) {
            return 0; // first ever launch, no file yet
        }

        Scanner reader = new Scanner(f);
        int saved = 0;
        // keep reading and keep the last number (the newest save).
        // hasNextInt also stops an empty file from breaking anything
        while (reader.hasNextInt()) {
            saved = reader.nextInt();
        }
        reader.close();
        return saved;
    } // loadHighScore ends

    /**
     * Writes the current high score into the file so it's still there next
     * run. println and flush can't fail, so this is safe to call from the
     * middle of the game loop.
     *
     * @author Aiden Ndreu
     */
    private void saveHighScore() {
        scoreWriter.println(highScore);
        scoreWriter.flush(); // pushes it into the file right away
    } // saveHighScore ends

    /**
     * Stops the game loop timer. Called when the game window closes so an
     * invisible game doesn't keep running in the background.
     *
     * @author Aiden Ndreu
     */
    public void stopGame() {
        gameTimer.stop();
    } // stopGame ends

    /**
     * Resets the bird and pipes back to the starting state. The saved high
     * score is kept so the player has something to beat.
     *
     * @author Aiden Ndreu
     */
    private void resetGame() {
        // bird in the middle, not moving, fresh run
        birdY = HEIGHT / 2;
        birdVelocity = 0;
        score = 0;
        gameStarted = false;
        gameOver = false;

        // pipes start off the right side of the screen, evenly spaced, each
        // with its own random gap
        pipeX = new int[NUM_PIPES];
        pipeGapY = new int[NUM_PIPES];
        pipeScored = new boolean[NUM_PIPES];
        for (int i = 0; i < NUM_PIPES; i++) {
            pipeX[i] = WIDTH + i * PIPE_SPACING;
            pipeGapY[i] = randomGapY();
            pipeScored[i] = false;
        }
    } // resetGame ends

    /**
     * Picks a random Y for the top of a pipe's gap using the default
     * margins (60px) by calling the overloaded version below.
     *
     * @author Aiden Ndreu
     * @return the Y pixel for the top of the gap
     */
    private int randomGapY() {
        return randomGapY(60, 60);
    } // randomGapY(no params) ends

    /**
     * Overload of randomGapY that lets the caller control how close to the
     * top and bottom of the screen a gap can spawn. Could be used later for
     * a harder mode with meaner gap positions.
     *
     * @author Aiden Ndreu
     * @param topMargin minimum pixels between the top and the gap
     * @param bottomMargin minimum pixels between the gap and the ground
     * @return the Y pixel for the top of the gap
     */
    private int randomGapY(int topMargin, int bottomMargin) {
        // the gap can start anywhere from topMargin down to
        // (screen - ground - gap - bottomMargin) down
        int highest = topMargin;
        int lowest = HEIGHT - GROUND_HEIGHT - PIPE_GAP - bottomMargin;
        return highest + rng.nextInt(lowest - highest);
    } // randomGapY(2 params) ends

    /**
     * Handles a key press. SPACE flaps (or starts the game, or retries
     * after a crash), anything else just gets ignored.
     *
     * @author Aiden Ndreu
     * @param keyCode the key code from the KeyEvent
     */
    private void handleKey(int keyCode) {
        if (keyCode == KeyEvent.VK_SPACE) {
            if (gameOver) {
                // retry after crashing
                resetGame();
            } else {
                // first flap starts the game, every flap pushes up
                gameStarted = true;
                birdVelocity = JUMP_STRENGTH;
            }
        }
    } // handleKey ends

    /**
     * The game loop. Applies gravity, moves the pipes, checks scoring and
     * collisions, then repaints. Runs every tick.
     *
     * @author Aiden Ndreu
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // before the first flap or after a crash the world is frozen, but
        // we still repaint so the start / game over text shows
        if (!gameStarted || gameOver) {
            repaint();
            return;
        }

        // gravity
        birdVelocity = birdVelocity + GRAVITY;
        birdY = birdY + birdVelocity;

        // move the pipes left. a pipe that goes fully off the left edge
        // gets sent back to the right with a new random gap
        for (int i = 0; i < pipeX.length; i++) {
            pipeX[i] = pipeX[i] - PIPE_SPEED;
            if (pipeX[i] + PIPE_WIDTH < 0) {
                pipeX[i] = pipeX[i] + NUM_PIPES * PIPE_SPACING;
                pipeGapY[i] = randomGapY();
                pipeScored[i] = false;
            }
        }

        // scoring: when the bird fully passes a pipe, count it once. the
        // setter updates the high score if it got beaten
        for (int i = 0; i < pipeX.length; i++) {
            if (!pipeScored[i] && pipeX[i] + PIPE_WIDTH < BIRD_X) {
                score = score + 1;
                pipeScored[i] = true;
                setHighScore(score);
            }
        }

        checkCollisions();

        repaint(); // redraw the frame
    } // actionPerformed ends

    /**
     * Ends the run if the bird hits the ground, the ceiling, or any pipe,
     * and saves the high score when that happens.
     *
     * @author Aiden Ndreu
     */
    private void checkCollisions() {
        // the ground or the ceiling
        if (birdY < 0 || birdY + BIRD_SIZE > HEIGHT - GROUND_HEIGHT) {
            gameOver = true;
            saveHighScore();
            return;
        }

        // the pipes. each pipe is a top part and a bottom part, the bird
        // crashes if it overlaps either while inside the pipe's column
        for (int i = 0; i < pipeX.length; i++) {
            boolean inPipeColumn =
                    BIRD_X + BIRD_SIZE > pipeX[i] &&
                            BIRD_X < pipeX[i] + PIPE_WIDTH;
            if (inPipeColumn) {
                boolean aboveGap = birdY < pipeGapY[i];
                boolean belowGap = birdY + BIRD_SIZE > pipeGapY[i] + PIPE_GAP;
                if (aboveGap || belowGap) {
                    gameOver = true;
                    saveHighScore();
                    return;
                }
            }
        }
    } // checkCollisions ends

    /**
     * Method is called every time repaint(); is called
     * Draws the pipes, ground, bird, scores, and the start / game over text.
     *
     * @author Aiden Ndreu
     * @param g the Graphics object swing gives us to draw with
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // clears the panel to the sky color

        // the pipes (each one is two green rectangles with the gap between)
        g.setColor(new Color(34, 139, 34));
        for (int i = 0; i < pipeX.length; i++) {
            // top pipe: from the top of the screen down to the gap
            g.fillRect(pipeX[i], 0, PIPE_WIDTH, pipeGapY[i]);
            // bottom pipe: from the bottom of the gap to the ground
            int bottomTop = pipeGapY[i] + PIPE_GAP;
            g.fillRect(pipeX[i], bottomTop, PIPE_WIDTH,
                    HEIGHT - GROUND_HEIGHT - bottomTop);
        }

        // the ground
        g.setColor(new Color(222, 184, 135));
        g.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);

        // the bird, with a little eye and beak for character
        g.setColor(Color.YELLOW);
        g.fillOval(BIRD_X, birdY, BIRD_SIZE, BIRD_SIZE);
        g.setColor(Color.BLACK);
        g.fillOval(BIRD_X + 20, birdY + 8, 5, 5);
        g.setColor(Color.ORANGE);
        g.fillRect(BIRD_X + BIRD_SIZE, birdY + 12, 8, 5);

        // hud: score big at the top, the saved best in the corner
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        g.drawString("" + score, WIDTH / 2 - 10, 60);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("Best: " + highScore, 10, 25);

        // start message before the first flap
        if (!gameStarted) {
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            g.drawString("Press SPACE to start", 150, HEIGHT / 2 - 40);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("Flap through the pipe gaps, don't touch anything!",
                    100, HEIGHT / 2 - 5);
        }

        // game over screen with your score, the saved best, and how to retry
        if (gameOver) {
            g.setColor(Color.BLACK);
            g.fillRect(80, HEIGHT / 2 - 80, WIDTH - 160, 160);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 34));
            g.drawString("GAME OVER", 200, HEIGHT / 2 - 30);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString("Score: " + score, 200, HEIGHT / 2 + 5);
            g.drawString("Best: " + highScore, 200, HEIGHT / 2 + 35);
            g.drawString("Press SPACE to retry", 160, HEIGHT / 2 + 65);
        }
    } // paintComponent ends

    /**
     * Creates the game window. The arcade hub calls this from its Flappy
     * Bird button. Throws Exception because building the game opens the
     * score file.
     *
     * @author Aiden Ndreu
     * @throws Exception
     */
    public static void launch() throws Exception {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();

        // back to menu button, closes this window and reopens the hub
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                // the menu's constructor throws Exception and button
                // code isn't allowed to, so this needs the try. the menu's
                // own buttons do the same thing
                try {
                    new splashscreen();
                } catch (Exception ex) { ex.printStackTrace(); } // prints the error if something breaks
            } // actionPerformed ends
        });

        // stops the game loop when the window closes so the game doesn't
        // keep running invisibly in the background
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                game.stopGame();
            } // windowClosed ends
        });

        // button bar on top, game in the middle
        frame.add(backButton, BorderLayout.NORTH);
        frame.add(game, BorderLayout.CENTER);
        frame.pack(); // sizes the window to fit everything
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // closing the game doesn't kill the whole program
        frame.setLocationRelativeTo(null); // centres
        frame.setVisible(true);
        game.requestFocusInWindow(); // so the panel gets the key presses
    } // launch ends

    /**
     * testable without launcher
     *
     * @author Aiden Ndreu
     * @param args command line arguments (not used)
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        launch();
    } // main ends
} // FlappyBird class ends