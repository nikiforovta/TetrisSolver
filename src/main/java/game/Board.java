package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.LocalDateTime;

public class Board extends JPanel implements ActionListener {
    public static final int BOARD_WIDTH = 10; //Ширина игрового поля
    public static final int BOARD_HEIGHT = 22; //Высота игрового поля
    private static final int INITIAL_DELAY = 1000; //Задержка срабатывания таймеров при инициализации, а также коэффициент, участвующий в увеличении начисляемых очков при увеличении скорости игры
    static boolean isSolverOn = false;
    static Shape nextPiece;
    private static Next next;
    private static boolean isFallingFinished = false;
    public Timer timer; //Таймер, отвечающий за падение фигур на игровом поле
    private Timer timerStats; //Таймер, отвечающий за отображение времени игры
    public boolean isStarted = false;
    public boolean isPaused = false;
    public int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    public int scoreInt = 0;
    private JLabel score;
    private JLabel lines;
    private JLabel time;
    private LocalDateTime startTime;
    private Shape curPiece;
    private Tetrominoe[][] board;

    public Tetrominoe[][] getBoard() {
        return board;
    }

    public Shape getCurPiece() {
        return curPiece;
    }

    public Shape getNextPiece() {
        return nextPiece;
    }

    public Board(Tetris parent) {
        setFocusable(true);
        setPreferredSize(new Dimension(260, 460));
        next = new Next();

        curPiece = new Shape();
        nextPiece = new Shape();
        nextPiece();

        score = parent.getScore();
        lines = parent.getLines();
        time = parent.getTime();

        timerStats = new Timer(INITIAL_DELAY, e -> {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(startTime, now);
            time.setText(format(duration));
        });

        timer = new Timer(INITIAL_DELAY, this);
        board = new Tetrominoe[BOARD_WIDTH][BOARD_HEIGHT];
        clearBoard();
        addKeyListener(new TetrisAdapter());
    }

    static Next getNext() {
        return next;
    }

    private String format(Duration duration) {
        long hours = duration.toHours();
        long mins = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusMinutes(mins).toMillis() / INITIAL_DELAY;
        return String.format("%02dh %02dm %02ds", hours, mins, seconds);
    }

    private int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    private int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    public Tetrominoe shapeAt(int x, int y) {
        return board[x][y];
    }

    private void clearBoard() {
        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                board[x][y] = Tetrominoe.NoShape;
            }
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY + curPiece.y(i);
            board[x][y] = curPiece.getShape();
        }
        removeFullLines();
        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void nextPiece() {
        nextPiece.setRandomShape();
    }

    public void newPiece() {
        curPiece = nextPiece;
        nextPiece = new Shape();
        nextPiece();
        curX = BOARD_WIDTH / 2 - 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        if (!tryMove(curPiece, curX, curY - 1)) {
            curPiece.setShape(Tetrominoe.NoShape);
            nextPiece.setShape(Tetrominoe.NoShape);
            timer.stop();
            timerStats.stop();
            isStarted = false;
        }
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1))
            pieceDropped();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape) {
        g.setColor(shape.color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Dimension size = getSize();
        if (!isPaused && isStarted && curPiece.getShape() != Tetrominoe.NoShape)
            next.repaint();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Tetrominoe.NoShape) {
                    drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
                }
            }
        }

        if (curPiece.getShape() != Tetrominoe.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY + curPiece.y(i);
                drawSquare(g, x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(), curPiece.getShape());
            }
        }
    }

    void start() {
        if (isPaused)
            return;
        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        scoreInt = 0;
        clearBoard();
        newPiece();
        timer.start();
        startTime = LocalDateTime.now();
        timerStats.start();
        score.setText("");
        lines.setText("");
    }

    private void pause() {
        if (!isStarted)
            return;
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            timerStats.stop();
        } else {
            timer.start();
            timerStats.start();
            score.setText(String.valueOf(scoreInt));
        }
        repaint();
    }

    public boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY + newPiece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
                return false;
            if (shapeAt(x, y) != Tetrominoe.NoShape)
                return false;
        }
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    public void removeFullLines() {
        int numFullLines = 0;
        final int LINES_1 = 100;
        final int LINES_2 = 300;
        final int LINES_3 = 700;
        final int LINES_4 = 1500;
        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                numFullLines++;
                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j) {
                        board[j][k] = shapeAt(j, k + 1);
                    }
                }
            }
        }
        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            switch (numFullLines) {
                case 4:
                    scoreInt += LINES_4 * (INITIAL_DELAY / timer.getDelay());
                    break;
                case 3:
                    scoreInt += LINES_3 * (INITIAL_DELAY / timer.getDelay());
                    break;
                case 2:
                    scoreInt += LINES_2 * (INITIAL_DELAY / timer.getDelay());
                    break;
                case 1:
                    scoreInt += LINES_1 * (INITIAL_DELAY / timer.getDelay());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + numFullLines);
            }
            lines.setText(String.valueOf(numLinesRemoved));
            score.setText(String.valueOf(scoreInt));
            isFallingFinished = true;
            curPiece.setShape(Tetrominoe.NoShape);
            repaint();
        }
    }

    public void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1))
                break;
            --newY;
        }
        pieceDropped();
    }

    class TetrisAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent k) {
            int keyCode = k.getKeyCode();
            if ((!isStarted || curPiece.getShape() == Tetrominoe.NoShape) && (keyCode == 'r' || keyCode == 'R')) {
                start();
                dropDown();
            }
            if (keyCode == 'p' || keyCode == 'P') {
                pause();
            }
            if (isPaused || !isStarted || curPiece.getShape() == Tetrominoe.NoShape || Board.isSolverOn) {
                return;
            }
            if (!(!isStarted || curPiece.getShape() == Tetrominoe.NoShape))
                switch (keyCode) {
                    case KeyEvent.VK_LEFT:
                        tryMove(curPiece, curX - 1, curY);
                        break;
                    case KeyEvent.VK_RIGHT:
                        tryMove(curPiece, curX + 1, curY);
                        break;
                    case KeyEvent.VK_DOWN:
                        oneLineDown();
                        break;
                    case KeyEvent.VK_UP:
                        tryMove(curPiece.rotateLeft(), curX, curY);
                        break;
                    case KeyEvent.VK_SPACE:
                        dropDown();
                        break;
                }
        }
    }
}