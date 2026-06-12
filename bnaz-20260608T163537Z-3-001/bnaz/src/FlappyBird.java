/*
 * File:        FlappyBird.java
 * Author:      Aiden Ndreu
 * Created:     2025-06-05
 * Modified:    2025-06-05
 *
 * Description: Flappy Bird minigame for the Arcade project. The player
 *              taps SPACE to make the bird flap upward; gravity pulls
 *              it back down. Green pipes scroll in from the right with
 *              random gap heights. The score goes up by 1 for every
 *              pipe the bird passes. The game ends if the bird hits a
 *              pipe, the ground, or the ceiling. The game-over screen
 *              shows the score and the best score so far this session.
 *
 *              This file can run on its own (its main method opens the
 *              game directly), or it can be launched from the arcade
 *              menu by calling FlappyBird.launch().
 *
 *  SOURCES FOR THINGS NOT TAUGHT IN CLASS
 *
 *  Things we learned in class: Scanner input, System.out.println,
 *  if/else, while/for loops, methods (void/return/parameters/
 *  overloading), 1D arrays, parallel arrays, 2D arrays, ArrayList,
 *  File I/O, String methods.
 *
 *  Everything below is sourced. Each USE in the code also has an
 *  inline comment pointing back here.
 *
 *  1) Java Swing GUI (JFrame, JPanel)
 *     Source: Oracle's "Creating a GUI With Swing",
 *     https://docs.oracle.com/javase/tutorial/uiswing/
 *
 *  2) Drawing with java.awt.Graphics (fillRect, fillOval, drawString,
 *     setColor, setFont).
 *     Source: Oracle's "Working with Geometry",
 *     https://docs.oracle.com/javase/tutorial/2d/geometry/
 *
 *  3) java.awt.Color, java.awt.Font, java.awt.Dimension.
 *     Source: same Oracle 2D tutorial as 2.
 *
 *  4) javax.swing.Timer (fires an event every X milliseconds; this is
 *     the game loop).
 *     Source: Oracle's "How to Use Swing Timers",
 *     https://docs.oracle.com/javase/tutorial/uiswing/misc/timer.html
 *
 *  5) Event-handling interfaces: ActionListener / ActionEvent /
 *     KeyListener / KeyAdapter / KeyEvent.
 *     Source: Oracle's "Writing Event Listeners",
 *     https://docs.oracle.com/javase/tutorial/uiswing/events/
 *
 *  6) Inheritance and interfaces: "extends JPanel" and "implements
 *     ActionListener".
 *     Source: Oracle's "Inheritance" lesson,
 *     https://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html
 *
 *  7) @Override annotation (tells the compiler this method replaces
 *     one from a parent class or interface).
 *     Source: Oracle's "Predefined Annotations",
 *     https://docs.oracle.com/javase/tutorial/java/annotations/predefined.html
 *
 *  8) Anonymous inner classes (the "new KeyAdapter() { ... }" pattern
 *     used for the key listener).
 *     Source: Oracle's "Anonymous Classes",
 *     https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html
 *
 *  9) java.util.Random (for random pipe gap heights).
 *     Source: Java SE API docs,
 *     https://docs.oracle.com/javase/8/docs/api/java/util/Random.html
 *
 * 10) Access modifiers: private, protected, final (the rubric asks
 *     for private fields with getters; final makes a value constant).
 *     Source: Oracle's "Controlling Access to Members of a Class",
 *     https://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html
 *
 * 11) Constructors (writing my own "public FlappyBird() { ... }"
 *     method that builds a new object).
 *     Source: Oracle's "Providing Constructors",
 *     https://docs.oracle.com/javase/tutorial/java/javaOO/constructors.html
 */

// NEW (see source 1): javax.swing classes. JFrame is the window,
// JPanel is a region we draw on, Timer fires events repeatedly.
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

// NEW (see source 3): java.awt classes for colors, fonts, sizes.
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

// NEW (see source 2): Graphics is the object Java gives us for drawing
// shapes and text on a panel.
import java.awt.Graphics;

// NEW (see source 5): event-handling classes for detecting button
// events and key presses.
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/*
 * NEW (see source 6): "extends JPanel" means FlappyBird IS a JPanel
 * (inheritance), so it can be added to a window and drawn on.
 * "implements ActionListener" means FlappyBird promises to provide an
 * actionPerformed method - the Timer calls this method each tick.
 */
public class FlappyBird extends JPanel implements ActionListener {

    // CONSTANTS - values that never change.
    // NEW (see source 10): "private static final" means private (only
    // this class can see it), static (one shared copy), final (the
    // value can never change). We learned static in class but not
    // private or final.

    // Window size in pixels.
    private static final int WIDTH = 600;
    private static final int HEIGHT = 700;

    // How tall the green ground strip is at the bottom.
    private static final int GROUND_HEIGHT = 60;

    // How often the game loop fires, in milliseconds (about 33 fps).
    private static final int TICK_MS = 30;

    // The bird's size and fixed left/right position on screen.
    private static final int BIRD_SIZE = 30;
    private static final int BIRD_X = 150;

    // Gravity pulls the bird down a little each tick. A flap sets the
    // bird's speed to JUMP_STRENGTH (negative = upward).
    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = -12;

    // Pipe settings.
    private static final int PIPE_WIDTH = 70;
    private static final int PIPE_GAP = 180;   // vertical gap to fly through
    private static final int PIPE_SPEED = 4;   // how fast pipes move left
    private static final int PIPE_SPACING = 280; // pixels between pipes
    private static final int NUM_PIPES = 3;    // how many pipes on screen

    // GAME STATE FIELDS.
    // NEW (see source 10): "private" hides these fields from outside
    // code. The rubric asks for private fields with getter methods.

    // The bird's vertical position and falling speed.
    private int birdY;
    private int birdVelocity;

    // Pipes stored as parallel arrays (covered in class). pipeX[i] is
    // the left edge of pipe i, pipeGapY[i] is the top of its gap, and
    // pipeScored[i] tracks whether we've already counted it for score.
    private int[] pipeX;
    private int[] pipeGapY;
    private boolean[] pipeScored;

    // Score for this run and the best score so far this session.
    private int score;
    private int highScore;

    // Game state flags.
    private boolean gameStarted;
    private boolean gameOver;

    // NEW (see source 4): Timer is a Swing class that fires an
    // ActionEvent every X milliseconds. This is the game loop.
    private Timer gameTimer;

    // NEW (see source 9): Random is a class for generating random
    // numbers, used here to pick the height of each pipe's gap.
    private java.util.Random rng;

    // CONSTRUCTOR.
    // NEW (see source 11): the constructor is a special method with
    // the same name as the class and no return type. It runs once when
    // we do "new FlappyBird()". In class we only used the constructor
    // for Scanner ("new Scanner(System.in)"), never wrote our own.

    /**
     * Builds the Flappy Bird game panel: sets the window size, the key
     * listener for the SPACE key, the random number generator, the
     * game timer, and the starting positions.
     */
    public FlappyBird() {

        // NEW (see source 1): setPreferredSize tells Java how big this
        // JPanel should be. Dimension holds width and height together
        // (see source 3).
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // NEW (see source 1): setBackground paints the background sky
        // blue. This method is inherited from JPanel (see source 6).
        setBackground(new Color(135, 206, 235));

        // NEW (see source 1): setFocusable(true) lets this panel
        // receive key-press events.
        setFocusable(true);

        // NEW (see sources 5 and 8): addKeyListener attaches code that
        // runs when a key is pressed. "new KeyAdapter() { ... }" is an
        // anonymous inner class. KeyAdapter lets us write only the one
        // method we need (keyPressed).
        addKeyListener(new KeyAdapter() {
            // NEW (see source 7): @Override tells the compiler "I am
            // replacing a method from the parent class".
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
            }
        });

        // NEW (see source 9): "new java.util.Random()" creates a random
        // number generator object.
        rng = new java.util.Random();

        // Set the starting positions of the bird and pipes.
        resetGame();

        // NEW (see source 4): "new Timer(delay, listener)" creates a
        // timer that calls our actionPerformed method every `delay`
        // milliseconds. We pass `this` because FlappyBird implements
        // ActionListener (see source 6).
        gameTimer = new Timer(TICK_MS, this);
        gameTimer.start();
    }

    // GETTERS - the rubric asks for private fields with getter methods.

    /**
     * @return the player's current score
     */
    public int getScore() {
        return score;
    }

    /**
     * @return the best score so far this session
     */
    public int getHighScore() {
        return highScore;
    }

    /**
     * @return true if the game has ended (bird crashed)
     */
    public boolean isGameOver() {
        return gameOver;
    }

    // GAME SETUP.

    /**
     * Resets the bird and pipes back to a starting state. Keeps the
     * high score so the player can try to beat it.
     */
    private void resetGame() {
        // Bird starts in the middle, not moving.
        birdY = HEIGHT / 2;
        birdVelocity = 0;
        score = 0;
        gameStarted = false;
        gameOver = false;

        // NEW: "new int[NUM_PIPES]" creates arrays of that size (we
        // learned arrays in class). Each pipe gets a starting X off the
        // right side of the screen, spaced PIPE_SPACING apart, and a
        // random gap height.
        pipeX = new int[NUM_PIPES];
        pipeGapY = new int[NUM_PIPES];
        pipeScored = new boolean[NUM_PIPES];
        for (int i = 0; i < NUM_PIPES; i++) {
            pipeX[i] = WIDTH + i * PIPE_SPACING;
            pipeGapY[i] = randomGapY();
            pipeScored[i] = false;
        }
    }

    /**
     * Picks a random Y position for the top of a pipe's gap, keeping
     * the gap fully on screen (not too high, not into the ground).
     *
     * @return the Y pixel for the top of the gap
     */
    private int randomGapY() {
        // The gap can start anywhere from 60 pixels down to
        // (screen height - ground - gap - 60) pixels down.
        int highest = 60;
        int lowest = HEIGHT - GROUND_HEIGHT - PIPE_GAP - 60;
        // NEW (see source 9): rng.nextInt(n) returns a random whole
        // number from 0 up to n-1. Adding `highest` shifts the range.
        return highest + rng.nextInt(lowest - highest);
    }

    // INPUT HANDLING.

    /**
     * Handles a key press. SPACE flaps the bird (or starts the game,
     * or restarts after a game over).
     *
     * @param keyCode the key code from the KeyEvent
     */
    private void handleKey(int keyCode) {
        // NEW (see source 5): KeyEvent.VK_SPACE is the constant for the
        // space bar.
        if (keyCode == KeyEvent.VK_SPACE) {
            if (gameOver) {
                // Restart after crashing.
                resetGame();
            } else {
                // First flap starts the game; every flap pushes up.
                gameStarted = true;
                birdVelocity = JUMP_STRENGTH;
            }
        }
        // Any other key is ignored, so invalid input can't crash the
        // game (rubric: handle invalid input).
    }

    // GAME LOOP - the Timer calls this every TICK_MS ms.

    /**
     * Game tick. Applies gravity, moves the pipes, checks for scoring
     * and collisions, then repaints.
     *
     * @param e the timer event (unused but required by ActionListener)
     */
    @Override  // NEW (see source 7): @Override annotation
    public void actionPerformed(ActionEvent e) {
        // Before the first flap, or after a crash, the world is frozen
        // (only the bird sits still). We still repaint so the start /
        // game-over text shows.
        if (!gameStarted || gameOver) {
            repaint();
            return;
        }

        // 1) Gravity pulls the bird down.
        birdVelocity = birdVelocity + GRAVITY;
        birdY = birdY + birdVelocity;

        // 2) Move every pipe to the left. If a pipe goes fully off the
        // left edge, send it back to the right with a new random gap.
        for (int i = 0; i < pipeX.length; i++) {
            pipeX[i] = pipeX[i] - PIPE_SPEED;
            if (pipeX[i] + PIPE_WIDTH < 0) {
                pipeX[i] = pipeX[i] + NUM_PIPES * PIPE_SPACING;
                pipeGapY[i] = randomGapY();
                pipeScored[i] = false;
            }
        }

        // 3) Score: when the bird fully passes a pipe, count it once.
        for (int i = 0; i < pipeX.length; i++) {
            if (!pipeScored[i] && pipeX[i] + PIPE_WIDTH < BIRD_X) {
                score = score + 1;
                pipeScored[i] = true;
                if (score > highScore) {
                    highScore = score;
                }
            }
        }

        // 4) Check for crashes.
        checkCollisions();

        // 5) NEW (see source 2): repaint() asks Java to redraw this
        // panel by calling paintComponent again.
        repaint();
    }

    // COLLISION DETECTION.

    /**
     * Ends the game if the bird hits the ground, the ceiling, or any
     * pipe.
     */
    private void checkCollisions() {
        // Hit the ground or the ceiling.
        if (birdY < 0 || birdY + BIRD_SIZE > HEIGHT - GROUND_HEIGHT) {
            gameOver = true;
            return;
        }

        // Hit a pipe. A pipe has a top part (above the gap) and a
        // bottom part (below the gap). The bird crashes if it overlaps
        // either part.
        for (int i = 0; i < pipeX.length; i++) {
            boolean inPipeColumn =
                    BIRD_X + BIRD_SIZE > pipeX[i] &&
                            BIRD_X < pipeX[i] + PIPE_WIDTH;
            if (inPipeColumn) {
                boolean aboveGap = birdY < pipeGapY[i];
                boolean belowGap = birdY + BIRD_SIZE > pipeGapY[i] + PIPE_GAP;
                if (aboveGap || belowGap) {
                    gameOver = true;
                    return;
                }
            }
        }
    }

    // DRAWING.

    /**
     * Draws the whole game: pipes, ground, bird, score, and the start
     * or game-over text.
     *
     * @param g the Graphics object Swing gives us to draw with
     */
    @Override  // NEW (see source 7): @Override annotation
    /*
     * NEW (see source 2): paintComponent is a method we INHERIT from
     * JPanel (see source 6). When Swing wants to draw our panel, it
     * calls this method with a Graphics object.
     *
     * NEW (see source 10): "protected" means only this class or its
     * subclasses can call this directly, but Swing's drawing machinery
     * is allowed to.
     */
    protected void paintComponent(Graphics g) {
        // NEW (see source 2): super.paintComponent(g) clears the panel
        // to the background color first. Always call this first when
        // overriding paintComponent.
        super.paintComponent(g);

        // Draw the pipes (green). Each pipe is two rectangles.
        g.setColor(new Color(34, 139, 34));
        for (int i = 0; i < pipeX.length; i++) {
            // NEW (see source 2): fillRect draws a filled rectangle.
            // Top pipe: from the top of the screen down to the gap.
            g.fillRect(pipeX[i], 0, PIPE_WIDTH, pipeGapY[i]);
            // Bottom pipe: from the bottom of the gap to the ground.
            int bottomTop = pipeGapY[i] + PIPE_GAP;
            g.fillRect(pipeX[i], bottomTop, PIPE_WIDTH,
                    HEIGHT - GROUND_HEIGHT - bottomTop);
        }

        // Draw the ground.
        g.setColor(new Color(222, 184, 135));
        g.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);

        // Draw the bird as a yellow circle.
        g.setColor(Color.YELLOW);
        // NEW (see source 2): fillOval draws a filled circle/oval.
        g.fillOval(BIRD_X, birdY, BIRD_SIZE, BIRD_SIZE);
        // A small black eye and orange beak for character.
        g.setColor(Color.BLACK);
        g.fillOval(BIRD_X + 20, birdY + 8, 5, 5);
        g.setColor(Color.ORANGE);
        g.fillRect(BIRD_X + BIRD_SIZE, birdY + 12, 8, 5);

        // Draw the score at the top.
        g.setColor(Color.WHITE);
        // NEW (see source 2): setFont picks the font; drawString draws
        // text at a pixel position.
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        g.drawString("" + score, WIDTH / 2 - 10, 60);

        // Start message before the first flap.
        if (!gameStarted) {
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            g.drawString("Press SPACE to start", 150, HEIGHT / 2 - 40);
        }

        // Game-over screen.
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
    }

    // ENTRY POINTS - how to start the game.

    /**
     * Opens the Flappy Bird game in its own window. The arcade main
     * menu can call this from its "Flappy Bird" button.
     */
    public static void launch() {
        // NEW (see source 1): JFrame is the window class.
        JFrame frame = new JFrame("Flappy Bird");
        // NEW (see source 11): "new FlappyBird()" calls our constructor.
        FlappyBird game = new FlappyBird();
        // NEW (see source 1): frame.add adds the game panel to the
        // window; frame.pack sizes the window to fit it.
        frame.add(game);
        frame.pack();
        // NEW (see source 1): DISPOSE_ON_CLOSE means closing this
        // window just closes it - it does NOT shut down the program,
        // so the arcade menu keeps running.
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // NEW (see source 1): setLocationRelativeTo(null) centers it.
        frame.setLocationRelativeTo(null);
        // NEW (see source 1): setVisible(true) actually shows it.
        frame.setVisible(true);
        // NEW (see source 1): requestFocusInWindow makes our panel the
        // focused component so it receives key presses.
        game.requestFocusInWindow();
    }

    /**
     * Lets you run Flappy Bird on its own (without the arcade menu) for
     * testing.
     *
     * @param args command-line args (not used)
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        // EXIT_ON_CLOSE here because running this file directly should
        // close everything when the window closes.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocusInWindow();
    }
}