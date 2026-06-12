/*
 * File:        PacMan.java
 * Author:      Aiden Ndreu
 * Created:     2025-05-29
 * Modified:    2025-05-29
 *
 * Description: Pac-Man minigame for the Arcade project. The player
 *              controls Pac-Man with the arrow keys, trying to eat
 *              every pellet in the maze without being caught by one of
 *              the four ghosts. Eating a power pellet lets Pac-Man eat
 *              ghosts for a few seconds. The player has 3 lives. The
 *              game ends in a win when all pellets are eaten, or in a
 *              loss when all lives are gone.
 *
 *              This file can run on its own (its main method opens the
 *              game directly), or it can be launched from the arcade
 *              menu by calling PacMan.launch().
 *
 *  SOURCES FOR EVERYTHING NOT TAUGHT IN CLASS
 * What we learned in class: Scanner,
 * System.out.println, if/else, while loops, methods, primitive types
 * (int, boolean, String) and basic 1D arrays. Everything else used in
 * this file is listed below with a source. Each USE of that thing
 * inside the code also has an inline comment pointing back to here.
 *
 *   1) Java Swing GUI (JFrame, JPanel)
 *      Source: Oracle's "Creating a GUI With Swing",
 *      https://docs.oracle.com/javase/tutorial/uiswing/
 *
 *   2) Drawing with java.awt.Graphics (fillRect, fillOval, fillArc,
 *      drawString, setColor, setFont).
 *      Source: Oracle's "Working with Geometry",
 *      https://docs.oracle.com/javase/tutorial/2d/geometry/index.html
 *
 *   3) java.awt.Color, java.awt.Font, java.awt.Dimension
 *      Source: same Oracle 2D tutorial as above.
 *
 *   4) javax.swing.Timer (fires an event every X milliseconds - this
 *      is the game loop).
 *      Source: Oracle's "How to Use Swing Timers",
 *      https://docs.oracle.com/javase/tutorial/uiswing/misc/timer.html
 *
 *   5) Event-handling interfaces: ActionListener / ActionEvent /
 *      KeyListener / KeyAdapter / KeyEvent.
 *      Source: Oracle's "Writing Event Listeners",
 *      https://docs.oracle.com/javase/tutorial/uiswing/events/
 *
 *   6) Inheritance and interfaces: "extends JPanel" and "implements
 *      ActionListener".
 *      Source: Oracle's "Inheritance" lesson,
 *      https://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html
 *
 *   7) @Override annotation (tells the compiler this method replaces
 *      one from the parent class or interface).
 *      Source: Oracle's "Annotations" tutorial,
 *      https://docs.oracle.com/javase/tutorial/java/annotations/predefined.html
 *
 *   8) Anonymous inner classes (the "new KeyAdapter() { ... }" pattern
 *      used to handle key presses).
 *      Source: Oracle's "Anonymous Classes",
 *      https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html
 *
 *   9) 2D arrays (int[][]) for the maze grid.
 *      Source: Oracle's "Arrays" tutorial,
 *      https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html
 *
 *  10) Array initializer literals like new int[]{ 9, 9, 10, 10 }
 *      Source: same Oracle Arrays tutorial as above.
 *
 *  11) java.util.Random (for random ghost direction picking).
 *      Source: Java SE API docs,
 *      https://docs.oracle.com/javase/8/docs/api/java/util/Random.html
 *
 *  12) Access modifiers private and final (rubric requires private
 *      fields with getters/setters; final makes a value constant).
 *      Source: Oracle's "Controlling Access to Members of a Class",
 *      https://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html
 *
 *  13) Constructors (the "public PacMan() { ... }" method that builds
 *      a new object).
 *      Source: Oracle's "Providing Constructors for Your Classes",
 *      https://docs.oracle.com/javase/tutorial/java/javaOO/constructors.html
 *
 *  14) System.exit and other small details have their own inline
 *      sources where used.
 */

// NEW (not taught - see source #1 above): javax.swing classes.
// JFrame is the window, JPanel is a region we draw on, Timer fires
// an event repeatedly.
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

// NEW (not taught - see source #3 above): java.awt classes for colors,
// fonts and sizes.
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

// NEW (not taught - see source #2 above): Graphics is the object Java
// gives us for drawing shapes and text on a panel.
import java.awt.Graphics;

// NEW (not taught - see source #5 above): event-handling classes for
// detecting button-like events and key presses.
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/*
 * NEW (not taught - see source #6 above): "extends JPanel" means PacMan
 * IS a JPanel (inheritance), so it can be added to a window and drawn
 * on. "implements ActionListener" means PacMan promises to provide an
 * actionPerformed method - that method is what the Timer calls each
 * tick of the game loop.
 */
public class PacMan extends JPanel implements ActionListener {

    // CONSTANTS - values that never change
    // NEW (not taught - see source #12 above): "private static final"
    // means: private (only this class can see it), static (one shared
    // copy for the whole class), final (the value can never change).
    // We learned `static` in class but not private or final.

    // The pixel size of one maze tile. Everything (Pac-Man, ghosts,
    // walls, pellets) is drawn on a grid of these tiles.
    private static final int TILE = 28;

    // The maze is 19 tiles wide and 21 tiles tall.
    private static final int COLS = 19;
    private static final int ROWS = 21;

    // How often the game loop fires, in milliseconds.
    // 150 ms is about 6-7 ticks per second.
    private static final int TICK_MS = 150;

    // How long Pac-Man can eat ghosts after grabbing a power pellet
    // (in ticks - so 40 * 150 ms = 6 seconds).
    private static final int POWER_TICKS = 40;

    // Codes used inside the 2D maze grid to mark what's in each tile.
    private static final int EMPTY  = 0;
    private static final int WALL   = 1;
    private static final int PELLET = 2;
    private static final int POWER  = 3;

    // Direction codes for Pac-Man and the ghosts.
    private static final int DIR_NONE  = 0;
    private static final int DIR_UP    = 1;
    private static final int DIR_DOWN  = 2;
    private static final int DIR_LEFT  = 3;
    private static final int DIR_RIGHT = 4;

    /*
     * NEW (not taught - see source #9 above): int[][] is a 2D array -
     * basically an array of arrays. MAZE_TEMPLATE[row][col] gets the
     * value at that row and column.
     *
     * NEW (not taught - see source #10 above): the { {1,1,...}, {1,0,...} }
     * syntax is an array initializer that creates and fills the array
     * in one step.
     *
     * 1 = wall, 0 = pellet space, 3 = power pellet, 9 = empty path
     * (ghost pen and side tunnel).
     */
    private static final int[][] MAZE_TEMPLATE = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
            {1,3,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,3,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,0,1},
            {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
            {1,1,1,1,0,1,1,1,9,1,9,1,1,1,0,1,1,1,1},
            {9,9,9,1,0,1,9,9,9,9,9,9,9,1,0,1,9,9,9},
            {1,1,1,1,0,1,9,1,1,9,1,1,9,1,0,1,1,1,1},
            {9,9,9,9,0,9,9,1,9,9,9,1,9,9,0,9,9,9,9},
            {1,1,1,1,0,1,9,1,1,1,1,1,9,1,0,1,1,1,1},
            {9,9,9,1,0,1,9,9,9,9,9,9,9,1,0,1,9,9,9},
            {1,1,1,1,0,1,9,1,1,1,1,1,9,1,0,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
            {1,3,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,3,1},
            {1,1,0,1,0,1,0,1,1,1,1,1,0,1,0,1,0,1,1},
            {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
            {1,0,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    // GAME STATE
    // NEW (not taught - see source #12): "private" hides these fields
    // from outside code. The rubric requires private fields with getters.

    // Live copy of the maze the game actually plays on.
    // 2D int array - same "new (not taught)" note as above.
    private int[][] maze;

    // Pac-Man's grid position and direction of movement.
    private int pacRow;
    private int pacCol;
    private int pacDir;
    // The direction the player WANTS to go (might not be possible yet
    // because of a wall).
    private int nextDir;

    // Score, lives and pellets-left counter.
    private int score;
    private int lives;
    private int pelletsLeft;

    // How many ticks of power mode are left.
    private int powerTicksLeft;

    /*
     * The four ghosts stored as parallel arrays - ghostRow[0],
     * ghostCol[0], ghostDir[0], ghostColor[0] all belong to ghost #0.
     * Parallel arrays were touched on in class with 1D arrays.
     *
     * Color[] is an array of Color objects - NEW because Color is a
     * non-primitive type from java.awt (see source #3 above).
     */
    private int[] ghostRow;
    private int[] ghostCol;
    private int[] ghostDir;
    private Color[] ghostColor;
    private int[] ghostStartRow;
    private int[] ghostStartCol;

    // Game state flags.
    private boolean gameRunning;
    private boolean gameWon;
    private boolean gameLost;

    // NEW (not taught - see source #4 above): Timer is a Swing class
    // that fires an ActionEvent every X milliseconds. We use it to run
    // our game loop. The Timer object is stored here so we can start
    // and stop it.
    private Timer gameTimer;

    // NEW (not taught - see source #11 above): Random is a class for
    // generating random numbers. We use it to pick ghost directions.
    // java.util.Random is referenced by its full name here instead of
    // an import, just to keep imports short.
    private java.util.Random rng;


    // CONSTRUCTOR
    // NEW (not taught - see source #13 above): the constructor is a
    // special method with the same name as the class and no return
    // type. It runs once when we make a new PacMan object with
    // "new PacMan()". In class we only used the constructor for
    // Scanner: "new Scanner(System.in)" - we never wrote our own.


    /**
     * Builds the Pac-Man game panel: sets up the window size, the
     * key listener for arrow-key input, the game timer, and the
     * starting state of the maze, Pac-Man and the ghosts.
     */
    public PacMan() {

        // NEW (not taught): setPreferredSize tells Java how big this
        // JPanel should be when the window asks. Dimension holds a
        // width and a height together.
        // Source: see #2 (Graphics tutorial) above.
        setPreferredSize(new Dimension(COLS * TILE, ROWS * TILE + 60));

        // NEW (not taught): setBackground paints the background. This
        // method is inherited from JPanel (see #6 above on extends).
        setBackground(Color.BLACK);

        // NEW (not taught): setFocusable(true) means this panel can
        // receive key-press events. Without it the arrow keys are
        // ignored.
        // Source: https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html
        setFocusable(true);

        /*
         * NEW (not taught - see sources #5 and #8 above): addKeyListener
         * attaches code that runs whenever a key is pressed.
         *
         * "new KeyAdapter() { ... }" is an ANONYMOUS INNER CLASS - it's
         * a one-off class definition wrapped in parentheses. We use
         * KeyAdapter (instead of KeyListener) because it lets us only
         * write the one method we need (keyPressed) and ignore the
         * other key event methods.
         */
        addKeyListener(new KeyAdapter() {
            // NEW (not taught - see source #7 above): @Override tells
            // the compiler "I am replacing a method that already exists
            // in the parent class". If we typoed the method name, the
            // compiler would catch it.
            @Override
            public void keyPressed(KeyEvent e) {
                // KeyEvent's getKeyCode returns an int telling which
                // key was pressed (we compare it to VK_UP, VK_DOWN etc.
                // inside handleKey).
                handleKey(e.getKeyCode());
            }
        });

        // NEW (not taught - see source #11): "new java.util.Random()"
        // creates a random number generator object.
        rng = new java.util.Random();

        // Set up the starting position of everything.
        resetGame();

        /*
         * NEW (not taught - see source #4): "new Timer(delay, listener)"
         * creates a timer that fires its listener's actionPerformed
         * method every `delay` milliseconds. We pass `this` because
         * PacMan implements ActionListener.
         */
        gameTimer = new Timer(TICK_MS, this);
        gameTimer.start();
    }


    // GETTERS - the rubric asks for private fields with getter methods.
    // Each one just returns the value of one private field.


    /** @return the player's current score */
    public int getScore() {
        return score;
    }

    /** @return how many lives the player has left */
    public int getLives() {
        return lives;
    }

    /** @return how many pellets are still on the board */
    public int getPelletsLeft() {
        return pelletsLeft;
    }

    /** @return true if the player has eaten every pellet */
    public boolean isGameWon() {
        return gameWon;
    }

    /** @return true if the player has run out of lives */
    public boolean isGameLost() {
        return gameLost;
    }

    /**
     * Adds points to the score. Kept private since only this class
     * should change the score.
     * @param points how many points to add
     */
    private void addScore(int points) {
        if (points > 0) {
            score = score + points;
        }
    }


    // GAME SETUP


    /**
     * Resets the maze, Pac-Man and the ghosts back to a starting state.
     */
    private void resetGame() {
        // Start at full lives, zero score, no power mode.
        score = 0;
        lives = 3;
        powerTicksLeft = 0;
        gameRunning = true;
        gameWon = false;
        gameLost = false;

        /*
         * NEW (not taught): "new int[ROWS][COLS]" creates a 2D array of
         * the given size. All values start at 0.
         * Source: see #9 (Arrays tutorial) above.
         */
        maze = new int[ROWS][COLS];
        pelletsLeft = 0;

        // Nested for loops to fill every cell of the 2D array. The
        // outer loop walks through rows, the inner loop through
        // columns of that row.
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int t = MAZE_TEMPLATE[r][c];
                if (t == 1) {
                    maze[r][c] = WALL;
                }
                else if (t == 3) {
                    maze[r][c] = POWER;
                    pelletsLeft = pelletsLeft + 1;
                }
                else if (t == 9) {
                    maze[r][c] = EMPTY;
                }
                else {
                    maze[r][c] = PELLET;
                    pelletsLeft = pelletsLeft + 1;
                }
            }
        }

        // Place Pac-Man in the lower middle of the maze.
        pacRow = 16;
        pacCol = 9;
        pacDir = DIR_NONE;
        nextDir = DIR_NONE;

        /*
         * NEW (not taught - see source #10): "new int[]{ 9, 9, 10, 10 }"
         * is an array initializer literal - it creates an int array
         * and fills it with those values in one step. We learned
         * arrays in class but only with "new int[5]" (empty) and then
         * setting values one at a time.
         */
        ghostRow = new int[]{ 9, 9, 10, 10 };
        ghostCol = new int[]{ 8, 10, 8,  10 };
        ghostStartRow = new int[]{ 9, 9, 10, 10 };
        ghostStartCol = new int[]{ 8, 10, 8,  10 };
        ghostDir = new int[]{ DIR_UP, DIR_UP, DIR_UP, DIR_UP };

        // NEW (not taught): Color[] is an array of Color objects. Color
        // is from java.awt (see source #3). "new Color(r, g, b)" makes
        // a custom color from red/green/blue values 0-255.
        ghostColor = new Color[]{
                new Color(255, 0, 0),     // red
                new Color(255, 184, 222), // pink
                new Color(0, 255, 255),   // cyan
                new Color(255, 184, 82)   // orange
        };
    }


    // INPUT HANDLING


    /**
     * Reads an arrow-key press and stores it as the next direction
     * Pac-Man should try to turn.
     * @param keyCode the key code from the KeyEvent
     */
    private void handleKey(int keyCode) {
        /*
         * NEW (not taught): KeyEvent.VK_UP, VK_DOWN, VK_LEFT, VK_RIGHT
         * are constants (built-in int values) for each arrow key.
         * Source: https://docs.oracle.com/javase/8/docs/api/java/awt/event/KeyEvent.html
         */
        if (keyCode == KeyEvent.VK_UP) {
            nextDir = DIR_UP;
        }
        else if (keyCode == KeyEvent.VK_DOWN) {
            nextDir = DIR_DOWN;
        }
        else if (keyCode == KeyEvent.VK_LEFT) {
            nextDir = DIR_LEFT;
        }
        else if (keyCode == KeyEvent.VK_RIGHT) {
            nextDir = DIR_RIGHT;
        }
        // Any other key is ignored - this means invalid input doesn't
        // crash the game (rubric: handle invalid input).
    }


    // GAME LOOP - the Timer calls this method every TICK_MS milliseconds


    /**
     * Game tick. Moves Pac-Man, moves the ghosts, checks for
     * collisions, eats pellets, and checks for win/loss. Then asks the
     * panel to redraw itself.
     * @param e the timer event (unused but required by ActionListener)
     */
    @Override  // NEW (not taught - see source #7): @Override annotation
    public void actionPerformed(ActionEvent e) {
        if (!gameRunning) {
            return;
        }

        // 1) Try to apply the player's requested direction.
        if (nextDir != DIR_NONE && canMove(pacRow, pacCol, nextDir)) {
            pacDir = nextDir;
        }

        // 2) Move Pac-Man.
        if (pacDir != DIR_NONE && canMove(pacRow, pacCol, pacDir)) {
            int[] next = step(pacRow, pacCol, pacDir);
            pacRow = next[0];
            pacCol = next[1];
        }

        // 3) Eat any pellet under Pac-Man.
        if (maze[pacRow][pacCol] == PELLET) {
            maze[pacRow][pacCol] = EMPTY;
            addScore(10);
            pelletsLeft = pelletsLeft - 1;
        }
        else if (maze[pacRow][pacCol] == POWER) {
            maze[pacRow][pacCol] = EMPTY;
            addScore(50);
            pelletsLeft = pelletsLeft - 1;
            powerTicksLeft = POWER_TICKS;
        }

        // 4) Move all four ghosts.
        moveGhosts();

        // 5) Check Pac-Man vs ghost collisions.
        checkGhostCollisions();

        // 6) Count down the power timer.
        if (powerTicksLeft > 0) {
            powerTicksLeft = powerTicksLeft - 1;
        }

        // 7) Win check.
        if (pelletsLeft <= 0) {
            gameWon = true;
            gameRunning = false;
            gameTimer.stop();
        }

        /*
         * 8) NEW (not taught): repaint() asks Java to redraw this panel
         * by calling our paintComponent method again on the next
         * frame. It's inherited from JComponent (parent of JPanel).
         * Source: https://docs.oracle.com/javase/tutorial/uiswing/painting/closer.html
         */
        repaint();
    }


    // MOVEMENT HELPERS


    /**
     * Checks whether the thing at (row, col) can move one step in the
     * given direction without hitting a wall.
     * @param row the current row
     * @param col the current column
     * @param dir the direction to try
     * @return true if the next tile that way is not a wall
     */
    private boolean canMove(int row, int col, int dir) {
        int[] next = step(row, col, dir);
        int nr = next[0];
        int nc = next[1];
        if (nr < 0 || nr >= ROWS) {
            return false;
        }
        if (nc < 0 || nc >= COLS) {
            return false;
        }
        return maze[nr][nc] != WALL;
    }

    /**
     * Returns the (row, col) you would be at after taking one step in
     * the given direction. Handles the left/right "tunnel" on the
     * middle row by wrapping around.
     * @param row the starting row
     * @param col the starting column
     * @param dir the direction (DIR_UP, DIR_DOWN, DIR_LEFT, DIR_RIGHT)
     * @return a 2-element array {newRow, newCol}
     */
    private int[] step(int row, int col, int dir) {
        int nr = row;
        int nc = col;
        if (dir == DIR_UP) {
            nr = row - 1;
        }
        else if (dir == DIR_DOWN) {
            nr = row + 1;
        }
        else if (dir == DIR_LEFT) {
            nc = col - 1;
        }
        else if (dir == DIR_RIGHT) {
            nc = col + 1;
        }
        // Wrap-around tunnel on row 10.
        if (row == 10 && nc < 0) {
            nc = COLS - 1;
        }
        else if (row == 10 && nc >= COLS) {
            nc = 0;
        }
        // NEW (not taught - see source #10): returning an array
        // initializer literal directly. Same idea as new int[]{...}
        // assigned to a variable, just done in the return statement.
        return new int[]{ nr, nc };
    }

    /**
     * Moves every ghost one step. Ghosts try to keep going in their
     * current direction; if they hit a wall, they pick a new random
     * legal direction.
     */
    private void moveGhosts() {
        for (int i = 0; i < ghostRow.length; i++) {
            int dir = ghostRow[i] >= 0 ? ghostDir[i] : DIR_NONE;
            int row = ghostRow[i];
            int col = ghostCol[i];

            if (!canMove(row, col, dir)) {
                dir = pickGhostDir(row, col, ghostDir[i]);
                ghostDir[i] = dir;
            }
            else {
                /*
                 * NEW (not taught - see source #11): rng.nextInt(n)
                 * returns a random whole number from 0 up to n-1. So
                 * nextInt(4) gives 0, 1, 2 or 3 - we use == 0 to mean
                 * a 25% chance.
                 */
                if (rng.nextInt(4) == 0) {
                    int newDir = pickGhostDir(row, col, ghostDir[i]);
                    if (newDir != DIR_NONE) {
                        dir = newDir;
                        ghostDir[i] = dir;
                    }
                }
            }

            if (canMove(row, col, dir)) {
                int[] next = step(row, col, dir);
                ghostRow[i] = next[0];
                ghostCol[i] = next[1];
            }
        }
    }

    /**
     * Picks a random direction the ghost CAN move from (row, col),
     * preferring not to turn around (180 degrees).
     * @param row the ghost's row
     * @param col the ghost's column
     * @param currentDir the direction the ghost was just moving
     * @return one of DIR_UP/DOWN/LEFT/RIGHT, or DIR_NONE if stuck
     */
    private int pickGhostDir(int row, int col, int currentDir) {
        int opposite = oppositeDir(currentDir);
        int[] options = new int[4];
        int count = 0;
        // Collect every legal direction that isn't a 180-turn.
        for (int d = DIR_UP; d <= DIR_RIGHT; d++) {
            if (d != opposite && canMove(row, col, d)) {
                options[count] = d;
                count = count + 1;
            }
        }
        if (count > 0) {
            return options[rng.nextInt(count)];
        }
        if (canMove(row, col, opposite)) {
            return opposite;
        }
        return DIR_NONE;
    }

    /**
     * Returns the opposite of a direction.
     * @param dir a direction code
     * @return the direction that's a 180-degree turn from it
     */
    private int oppositeDir(int dir) {
        if (dir == DIR_UP)    return DIR_DOWN;
        if (dir == DIR_DOWN)  return DIR_UP;
        if (dir == DIR_LEFT)  return DIR_RIGHT;
        if (dir == DIR_RIGHT) return DIR_LEFT;
        return DIR_NONE;
    }

    /**
     * Checks every ghost for a collision with Pac-Man and handles it.
     */
    private void checkGhostCollisions() {
        for (int i = 0; i < ghostRow.length; i++) {
            if (ghostRow[i] == pacRow && ghostCol[i] == pacCol) {
                if (powerTicksLeft > 0) {
                    // Pac-Man eats the ghost.
                    addScore(200);
                    ghostRow[i] = ghostStartRow[i];
                    ghostCol[i] = ghostStartCol[i];
                    ghostDir[i] = DIR_UP;
                }
                else {
                    // Ghost eats Pac-Man.
                    lives = lives - 1;
                    if (lives <= 0) {
                        gameLost = true;
                        gameRunning = false;
                        gameTimer.stop();
                    }
                    else {
                        // Respawn everyone.
                        pacRow = 16;
                        pacCol = 9;
                        pacDir = DIR_NONE;
                        nextDir = DIR_NONE;
                        powerTicksLeft = 0;
                        for (int g = 0; g < ghostRow.length; g++) {
                            ghostRow[g] = ghostStartRow[g];
                            ghostCol[g] = ghostStartCol[g];
                            ghostDir[g] = DIR_UP;
                        }
                    }
                    return;
                }
            }
        }
    }


    // DRAWING


    /**
     * Draws the whole game: maze walls, pellets, power pellets,
     * Pac-Man, ghosts, the HUD, and any win/loss message.
     * @param g the Graphics object Swing gives us to draw with
     */
    @Override  // NEW (not taught - see source #7): @Override annotation
    /*
     * NEW (not taught - see source #2): paintComponent is a method we
     * INHERIT from JPanel (see source #6 on extends). When Swing wants
     * to draw our panel, it calls this method and gives us a Graphics
     * object to draw with.
     *
     * "protected" is an access modifier between public and private -
     * the method can only be called by this class or subclasses, but
     * Swing's drawing machinery is allowed to call it.
     * Source: https://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html
     */
    protected void paintComponent(Graphics g) {
        // NEW (not taught): super.paintComponent(g) calls the parent
        // class's version of this method, which clears the panel to
        // the background color. Always call this first when overriding
        // paintComponent.
        // Source: https://docs.oracle.com/javase/tutorial/uiswing/painting/closer.html
        super.paintComponent(g);

        // Draw every tile in the maze.
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = c * TILE;
                int y = r * TILE;
                int tile = maze[r][c];

                if (tile == WALL) {
                    /*
                     * NEW (not taught - see source #2):
                     * g.setColor(color) picks the color for drawing.
                     * g.fillRect(x, y, w, h) draws a filled rectangle
                     * at pixel (x, y) with the given width and height.
                     */
                    g.setColor(new Color(33, 33, 222));
                    g.fillRect(x + 2, y + 2, TILE - 4, TILE - 4);
                }
                else if (tile == PELLET) {
                    g.setColor(Color.WHITE);
                    // NEW (not taught): fillOval draws a filled circle/
                    // oval inside the given x, y, width, height box.
                    g.fillOval(x + TILE/2 - 2, y + TILE/2 - 2, 4, 4);
                }
                else if (tile == POWER) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + TILE/2 - 6, y + TILE/2 - 6, 12, 12);
                }
            }
        }

        // Draw the ghosts.
        for (int i = 0; i < ghostRow.length; i++) {
            int gx = ghostCol[i] * TILE;
            int gy = ghostRow[i] * TILE;
            if (powerTicksLeft > 0) {
                // Power mode: ghosts turn dark blue and flash white near
                // the end of the timer.
                if (powerTicksLeft < 10 && (powerTicksLeft % 2 == 0)) {
                    g.setColor(Color.WHITE);
                }
                else {
                    g.setColor(new Color(33, 33, 222));
                }
            }
            else {
                g.setColor(ghostColor[i]);
            }
            g.fillOval(gx + 3, gy + 3, TILE - 6, TILE - 6);
            // Eyes - only when ghost is normal.
            if (powerTicksLeft <= 0) {
                g.setColor(Color.WHITE);
                g.fillOval(gx + 7, gy + 9, 5, 5);
                g.fillOval(gx + TILE - 12, gy + 9, 5, 5);
            }
        }

        // Draw Pac-Man as a yellow arc.
        int px = pacCol * TILE;
        int py = pacRow * TILE;
        g.setColor(Color.YELLOW);
        /*
         * NEW (not taught - see source #2): fillArc draws a "pie slice"
         * - a piece of a filled circle. The 5th argument is the start
         * angle in degrees, the 6th is the sweep angle. By drawing
         * 300 degrees of a 360-degree circle we get Pac-Man's open
         * mouth shape.
         */
        g.fillArc(px + 3, py + 3, TILE - 6, TILE - 6, pacMouthStart(),
                pacMouthSweep());

        // Draw the HUD (score, lives, power timer) under the maze.
        int hudY = ROWS * TILE + 20;
        g.setColor(Color.WHITE);
        // NEW (not taught): setFont picks the font for drawn text.
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        // NEW (not taught): drawString draws text at pixel (x, y).
        g.drawString("SCORE: " + score, 12, hudY);
        g.drawString("LIVES: " + lives, 200, hudY);
        if (powerTicksLeft > 0) {
            g.setColor(Color.YELLOW);
            g.drawString("POWER!", 360, hudY);
        }

        // Big win/loss banner if the game is over.
        if (gameWon || gameLost) {
            g.setColor(Color.BLACK);
            g.fillRect(40, ROWS * TILE / 2 - 40, COLS * TILE - 80, 80);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            if (gameWon) {
                g.drawString("YOU WIN! Score: " + score, 100,
                        ROWS * TILE / 2 + 10);
            }
            else {
                g.drawString("GAME OVER. Score: " + score, 100,
                        ROWS * TILE / 2 + 10);
            }
        }
    }

    /**
     * Picks the starting angle of Pac-Man's mouth arc, based on his
     * facing direction.
     * @return an angle in degrees for Graphics.fillArc
     */
    private int pacMouthStart() {
        if (pacDir == DIR_RIGHT) return 30;
        if (pacDir == DIR_UP)    return 120;
        if (pacDir == DIR_LEFT)  return 210;
        if (pacDir == DIR_DOWN)  return 300;
        return 30;
    }

    /**
     * Returns the sweep angle of Pac-Man's mouth (300 degrees of a
     * 360-degree circle, leaving a 60-degree mouth gap).
     * @return the sweep angle in degrees
     */
    private int pacMouthSweep() {
        return 300;
    }


    // ENTRY POINTS - how to start the game


    /**
     * Opens the Pac-Man game in a fullscreen window. The arcade main
     * menu can call this from its "Pac-Man" button:
     *
     *     pacManButton.addActionListener(e -> PacMan.launch());
     *
     * Each call opens a fresh game in its own window.
     */
    public static void launch() {
        // NEW (not taught - see source #1): JFrame is the window class.
        JFrame frame = new JFrame("Pac-Man");
        // NEW (not taught - see source #13): "new PacMan()" calls our
        // constructor, which builds and returns a new PacMan object.
        PacMan game = new PacMan();
        // NEW (not taught): frame.add adds the game panel to the window.
        frame.add(game);
        /*
         * NEW (not taught): frame.pack() sizes the window so it's just
         * big enough to hold the panel's preferred size.
         * Source: https://docs.oracle.com/javase/8/docs/api/java/awt/Window.html#pack--
         */
        frame.pack();
        // NEW (not taught): same as in MyProgram - fullscreen window.
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        /*
         * NEW (not taught): DISPOSE_ON_CLOSE means closing this window
         * just closes it - it does NOT shut down the whole program. This
         * matters because the arcade main menu is still running in the
         * background and we want to come back to it.
         * Source: https://docs.oracle.com/javase/tutorial/uiswing/components/frame.html
         */
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        /*
         * NEW (not taught): setLocationRelativeTo(null) centers the
         * window on the screen.
         */
        frame.setLocationRelativeTo(null);
        // NEW (not taught): setVisible(true) actually shows the window.
        frame.setVisible(true);
        // NEW (not taught): requestFocusInWindow makes our panel the
        // focused component, so it receives key-press events.
        game.requestFocusInWindow();
    }

    /**
     * Lets you run Pac-Man on its own (without the arcade menu) for
     * testing. Same as launch but uses EXIT_ON_CLOSE so closing the
     * window stops the program.
     * @param args command-line args (not used)
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man");
        PacMan game = new PacMan();
        frame.add(game);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // EXIT_ON_CLOSE because if you run this file by itself, closing
        // the window should shut everything down.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocusInWindow();
    }
}





