/**
 * PAC-MAN
 * Classic Pac-Man clone. Use the arrow keys to move Pac-Man around the maze
 * and eat every pellet to win. Avoid the 4 ghosts, you have 3 lives. The big
 * dots are power pellets that let you eat the ghosts back for a few seconds.
 * Your best score is saved to a file so it survives between runs and shows
 * up on the arcade hub. Press R on the game over screen to play again, or
 * use the Back to Menu button to return to the hub.
 *
 * Can run on its own (main method) or get launched from the splashscreen
 * with PacMan.launch().
 *
 * @author Aiden Ndreu
 * Created: May 29, 2026
 * Last Edited: June 12, 2026 (Aiden Ndreu)
 *
 * SOURCES:
 * * 1. swing gui (JFrame, JPanel)
 * * https://docs.oracle.com/javase/tutorial/uiswing/
 * * 2. drawing with Graphics (fillRect, fillOval, fillArc, drawString)
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
 * * 10. array initializer literals like new int[]{9, 9, 10, 10}
 * * https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html
 * * 11. java.util.Random (random ghost movement)
 * * https://docs.oracle.com/javase/8/docs/api/java/util/Random.html
 * * 12. private / protected / final access modifiers
 * * https://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html
 * * 13. writing my own constructor
 * * https://docs.oracle.com/javase/tutorial/java/javaOO/constructors.html
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;

public class PacMan extends JPanel implements ActionListener {

    // game settings
    private static final int TILE = 28; // pixel size of one maze tile, everything is drawn on this grid
    private static final int COLS = 19; // maze width in tiles
    private static final int ROWS = 21; // maze height in tiles
    private static final int TICK_MS = 150; // how often the game loop fires (ms), about 6-7 ticks a second
    private static final int POWER_TICKS = 40; // how long power mode lasts (40 ticks x 150ms = 6 seconds)
    private static final String SCORE_FILE = "pacman_highscore.txt"; // this is where the high score is saved

    // codes used inside the 2D maze grid
    private static final int EMPTY  = 0;
    private static final int WALL   = 1;
    private static final int PELLET = 2;
    private static final int POWER  = 3;

    // direction codes for pac-man and the ghosts
    private static final int DIR_NONE  = 0;
    private static final int DIR_UP    = 1;
    private static final int DIR_DOWN  = 2;
    private static final int DIR_LEFT  = 3;
    private static final int DIR_RIGHT = 4;

    // the maze layout (2D array). 1 = wall, 0 = pellet, 3 = power pellet,
    // 9 = empty path (ghost pen and the side tunnel)
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

    // maze the game actually plays on (gets refilled from the template every reset)
    private int[][] maze;

    // pac-man
    private int pacRow; // grid position
    private int pacCol;
    private int pacDir; // direction he's currently moving
    private int nextDir; // direction the player WANTS (might be blocked by a wall)

    // stats
    private int score; // current run's score
    private int highScore; // the saved best score
    private int lives; // lives left, game over at 0
    private int pelletsLeft; // win when this hits 0
    private int powerTicksLeft; // ticks of power mode left

    // the 4 ghosts as parallel arrays, index 0 is ghost 0 in all of them
    private int[] ghostRow;
    private int[] ghostCol;
    private int[] ghostDir;
    private Color[] ghostColor;
    private int[] ghostStartRow; // spawn spots for respawning
    private int[] ghostStartCol;

    // game state
    private boolean gameRunning; // false freezes the game loop
    private boolean gameWon;
    private boolean gameLost;

    private Timer gameTimer; // the game loop, fires every TICK_MS

    // the score file writer. opened once in the constructor because opening
    // is the only part that can fail, println/flush never do
    private PrintWriter scoreWriter;

    private java.util.Random rng; // for the random ghost movement

    /**
     * Constructor.
     * Sets up the panel, key listener, loads the saved high score, opens the
     * score file for saving, builds the starting maze and starts the game
     * loop.
     *
     * @author Aiden Ndreu
     * @throws Exception
     */
    public PacMan() throws Exception {
        setPreferredSize(new Dimension(COLS * TILE, ROWS * TILE + 60)); // maze + room for the hud
        setBackground(Color.BLACK);
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

        resetGame(); // puts everything in its starting spot

        // the game loop. calls actionPerformed every TICK_MS milliseconds
        gameTimer = new Timer(TICK_MS, this);
        gameTimer.start();
    } // PacMan ends

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
     * Returns how many lives are left
     *
     * @author Aiden Ndreu
     * @return lives remaining
     */
    public int getLives() {
        return lives;
    } // getLives ends

    /**
     * Returns how many pellets are still on the board
     *
     * @author Aiden Ndreu
     * @return pellets remaining
     */
    public int getPelletsLeft() {
        return pelletsLeft;
    } // getPelletsLeft ends

    /**
     * Returns true if the player ate every pellet
     *
     * @author Aiden Ndreu
     * @return true on a win
     */
    public boolean isGameWon() {
        return gameWon;
    } // isGameWon ends

    /**
     * Returns true if the player ran out of lives
     *
     * @author Aiden Ndreu
     * @return true on a loss
     */
    public boolean isGameLost() {
        return gameLost;
    } // isGameLost ends

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
     * Adds points to the score. Private since only this class should
     * change the score. Ignores negatives.
     *
     * @author Aiden Ndreu
     * @param points how many points to add
     */
    private void addScore(int points) {
        if (points > 0) {
            score = score + points;
        }
    } // addScore ends

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
     * Resets the maze, pac-man and the ghosts back to the starting state.
     * The saved high score is kept so the player has something to beat.
     *
     * @author Aiden Ndreu
     */
    private void resetGame() {
        // fresh run
        score = 0;
        lives = 3;
        powerTicksLeft = 0;
        gameRunning = true;
        gameWon = false;
        gameLost = false;

        // rebuild the live maze from the template and count the pellets
        maze = new int[ROWS][COLS];
        pelletsLeft = 0;
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

        // pac-man starts in the lower middle
        pacRow = 16;
        pacCol = 9;
        pacDir = DIR_NONE;
        nextDir = DIR_NONE;

        // ghosts start in the pen in the middle
        ghostRow = new int[]{ 9, 9, 10, 10 };
        ghostCol = new int[]{ 8, 10, 8,  10 };
        ghostStartRow = new int[]{ 9, 9, 10, 10 };
        ghostStartCol = new int[]{ 8, 10, 8,  10 };
        ghostDir = new int[]{ DIR_UP, DIR_UP, DIR_UP, DIR_UP };

        ghostColor = new Color[]{
                new Color(255, 0, 0),     // red
                new Color(255, 184, 222), // pink
                new Color(0, 255, 255),   // cyan
                new Color(255, 184, 82)   // orange
        };
    } // resetGame ends

    /**
     * Takes a key press and stores it as the next direction pac-man should
     * try to turn. R on the win/lose screen restarts the game, anything
     * else just gets ignored.
     *
     * @author Aiden Ndreu
     * @param keyCode the key code from the KeyEvent
     */
    private void handleKey(int keyCode) {
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
        else if (keyCode == KeyEvent.VK_R && (gameWon || gameLost)) {
            // R restarts, but only on the end screen
            resetGame();
            gameTimer.start();
        }
    } // handleKey ends

    /**
     * The game loop. Moves pac-man, eats pellets, moves the ghosts, checks
     * collisions and the win condition, then repaints. Runs every tick.
     *
     * @author Aiden Ndreu
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameRunning) {
            return;
        }

        // try to apply the direction the player asked for. uses the
        // 1-parameter overload of canMove that checks from pac-man's spot
        if (nextDir != DIR_NONE && canMove(nextDir)) {
            pacDir = nextDir;
        }

        // move pac-man
        if (pacDir != DIR_NONE && canMove(pacDir)) {
            int[] next = step(pacRow, pacCol, pacDir);
            pacRow = next[0];
            pacCol = next[1];
        }

        // eat whatever is under pac-man
        if (maze[pacRow][pacCol] == PELLET) {
            maze[pacRow][pacCol] = EMPTY;
            addScore(10);
            pelletsLeft = pelletsLeft - 1;
        }
        else if (maze[pacRow][pacCol] == POWER) {
            maze[pacRow][pacCol] = EMPTY;
            addScore(50);
            pelletsLeft = pelletsLeft - 1;
            powerTicksLeft = POWER_TICKS; // power mode on
        }

        moveGhosts();
        checkGhostCollisions();

        // count down power mode
        if (powerTicksLeft > 0) {
            powerTicksLeft = powerTicksLeft - 1;
        }

        // win check, save the record if this run beat it
        if (pelletsLeft <= 0) {
            gameWon = true;
            gameRunning = false;
            gameTimer.stop();
            setHighScore(score);
            saveHighScore();
        }

        repaint(); // redraw the frame
    } // actionPerformed ends

    /**
     * Checks if whatever is at (row, col) can move one step in a direction
     * without hitting a wall.
     *
     * @author Aiden Ndreu
     * @param row the current row
     * @param col the current column
     * @param dir the direction to try
     * @return true if the next tile that way isn't a wall
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
    } // canMove(3 params) ends

    /**
     * Overload of canMove that checks from pac-man's own position so the
     * game loop doesn't have to pass his row and column every time.
     *
     * @author Aiden Ndreu
     * @param dir the direction to try from pac-man's square
     * @return true if pac-man can move that way
     */
    private boolean canMove(int dir) {
        return canMove(pacRow, pacCol, dir);
    } // canMove(1 param) ends

    /**
     * Returns the (row, col) you'd be at after one step in a direction.
     * Handles the side tunnel on row 10 by wrapping around the screen.
     *
     * @author Aiden Ndreu
     * @param row the starting row
     * @param col the starting column
     * @param dir the direction
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
        // the wrap-around tunnel
        if (row == 10 && nc < 0) {
            nc = COLS - 1;
        }
        else if (row == 10 && nc >= COLS) {
            nc = 0;
        }
        return new int[]{ nr, nc };
    } // step ends

    /**
     * Moves every ghost one step. Ghosts keep going straight until they hit
     * a wall, then pick a new random legal direction. They also have a 25%
     * chance per tick to turn at an intersection so they don't get stuck in
     * loops.
     *
     * @author Aiden Ndreu
     */
    private void moveGhosts() {
        for (int i = 0; i < ghostRow.length; i++) {
            int dir = ghostDir[i];
            int row = ghostRow[i];
            int col = ghostCol[i];

            if (!canMove(row, col, dir)) {
                // blocked, pick a new way
                dir = pickGhostDir(row, col, ghostDir[i]);
                ghostDir[i] = dir;
            }
            else {
                // 25% chance to turn anyway (nextInt(4) is 0,1,2 or 3)
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
    } // moveGhosts ends

    /**
     * Picks a random direction the ghost CAN move, preferring not to do a
     * 180 so the ghosts feel intentional instead of jittery.
     *
     * @author Aiden Ndreu
     * @param row the ghost's row
     * @param col the ghost's column
     * @param currentDir the direction it was just moving
     * @return one of the DIR codes, or DIR_NONE if completely stuck
     */
    private int pickGhostDir(int row, int col, int currentDir) {
        int opposite = oppositeDir(currentDir);
        int[] options = new int[4];
        int count = 0;
        // collect every legal direction that isn't a 180 turn
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
            return opposite; // dead end, turning around is the only option
        }
        return DIR_NONE;
    } // pickGhostDir ends

    /**
     * Returns the opposite of a direction
     *
     * @author Aiden Ndreu
     * @param dir a direction code
     * @return the 180 degree turn of it
     */
    private int oppositeDir(int dir) {
        if (dir == DIR_UP)    return DIR_DOWN;
        if (dir == DIR_DOWN)  return DIR_UP;
        if (dir == DIR_LEFT)  return DIR_RIGHT;
        if (dir == DIR_RIGHT) return DIR_LEFT;
        return DIR_NONE;
    } // oppositeDir ends

    /**
     * Checks every ghost for a collision with pac-man. In power mode the
     * ghost gets eaten (+200, back to the pen), otherwise pac-man loses a
     * life. On the last life the game ends and the record gets saved.
     *
     * @author Aiden Ndreu
     */
    private void checkGhostCollisions() {
        for (int i = 0; i < ghostRow.length; i++) {
            if (ghostRow[i] == pacRow && ghostCol[i] == pacCol) {
                if (powerTicksLeft > 0) {
                    // pac-man eats the ghost
                    addScore(200);
                    ghostRow[i] = ghostStartRow[i];
                    ghostCol[i] = ghostStartCol[i];
                    ghostDir[i] = DIR_UP;
                }
                else {
                    // ghost eats pac-man
                    lives = lives - 1;
                    if (lives <= 0) {
                        gameLost = true;
                        gameRunning = false;
                        gameTimer.stop();
                        setHighScore(score); // in case this run still beat the record
                        saveHighScore();
                    }
                    else {
                        // respawn everyone
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
    } // checkGhostCollisions ends

    /**
     * Method is called every time repaint(); is called
     * Draws the maze, pellets, ghosts, pac-man, the hud, the instructions
     * and the game over screen.
     *
     * @author Aiden Ndreu
     * @param g the Graphics object swing gives us to draw with
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // clears the panel to the background color

        // the maze
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = c * TILE;
                int y = r * TILE;
                int tile = maze[r][c];

                if (tile == WALL) {
                    g.setColor(new Color(33, 33, 222));
                    g.fillRect(x + 2, y + 2, TILE - 4, TILE - 4);
                }
                else if (tile == PELLET) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + TILE/2 - 2, y + TILE/2 - 2, 4, 4); // small dot
                }
                else if (tile == POWER) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + TILE/2 - 6, y + TILE/2 - 6, 12, 12); // big dot
                }
            }
        }

        // the ghosts
        for (int i = 0; i < ghostRow.length; i++) {
            int gx = ghostCol[i] * TILE;
            int gy = ghostRow[i] * TILE;
            if (powerTicksLeft > 0) {
                // power mode: ghosts turn dark blue and flash white when
                // it's about to run out
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
            // eyes, only when the ghost is normal
            if (powerTicksLeft <= 0) {
                g.setColor(Color.WHITE);
                g.fillOval(gx + 7, gy + 9, 5, 5);
                g.fillOval(gx + TILE - 12, gy + 9, 5, 5);
            }
        }

        // pac-man. fillArc draws a pie slice, 300 of 360 degrees leaves a
        // 60 degree gap for the mouth, angled by his facing direction
        int px = pacCol * TILE;
        int py = pacRow * TILE;
        g.setColor(Color.YELLOW);
        g.fillArc(px + 3, py + 3, TILE - 6, TILE - 6, pacMouthStart(),
                pacMouthSweep());

        // hud
        int hudY = ROWS * TILE + 20;
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("SCORE: " + score, 12, hudY);
        g.drawString("BEST: " + highScore, 170, hudY);
        g.drawString("LIVES: " + lives, 330, hudY);
        if (powerTicksLeft > 0) {
            g.setColor(Color.YELLOW);
            g.drawString("POWER!", 450, hudY);
        }

        // shows the controls until the player actually starts moving
        if (pacDir == DIR_NONE && !gameWon && !gameLost && lives == 3
                && score == 0) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString("Arrow keys to move. Eat every dot, avoid the ghosts!",
                    75, 10 * TILE + 8);
        }

        // game over screen with your score, the saved best, and how to retry
        if (gameWon || gameLost) {
            g.setColor(Color.BLACK);
            g.fillRect(40, ROWS * TILE / 2 - 50, COLS * TILE - 80, 110);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            if (gameWon) {
                g.drawString("YOU WIN! Score: " + score, 100,
                        ROWS * TILE / 2);
            }
            else {
                g.drawString("GAME OVER. Score: " + score, 90,
                        ROWS * TILE / 2);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("Best: " + highScore + "   Press R to play again",
                    110, ROWS * TILE / 2 + 35);
        }
    } // paintComponent ends

    /**
     * Picks the start angle for pac-man's mouth based on his direction
     *
     * @author Aiden Ndreu
     * @return an angle in degrees for fillArc
     */
    private int pacMouthStart() {
        if (pacDir == DIR_RIGHT) return 30;
        if (pacDir == DIR_UP)    return 120;
        if (pacDir == DIR_LEFT)  return 210;
        if (pacDir == DIR_DOWN)  return 300;
        return 30;
    } // pacMouthStart ends

    /**
     * Returns the sweep angle of pac-man's mouth (300 of 360 degrees,
     * leaving the 60 degree mouth gap)
     *
     * @author Aiden Ndreu
     * @return the sweep angle in degrees
     */
    private int pacMouthSweep() {
        return 300;
    } // pacMouthSweep ends

    /**
     * Creates the game window. The arcade hub calls this from its Pac-Man
     * button. Throws Exception because building the game opens the score
     * file.
     *
     * @author Aiden Ndreu
     * @throws Exception
     */
    public static void launch() throws Exception {
        JFrame frame = new JFrame("Pac-Man");
        PacMan game = new PacMan();

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
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // fullscreen
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
} // PacMan class ends