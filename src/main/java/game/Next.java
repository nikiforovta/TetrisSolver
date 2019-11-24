package game;

import javax.swing.*;
import java.awt.*;

import static game.Board.nextPiece;

public class Next extends JPanel {
    private Tetrominoe[][] boardNext;

    Next() {
        setPreferredSize(new Dimension(150, 180));
        boardNext = new Tetrominoe[5][5];
        clearBoard();
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape) {
        g.setColor(shape.color);
        g.fillRect(x + 1, y + 1, 28, 28);
    }

    private void clearBoard() {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                boardNext[x][y] = Tetrominoe.NoShape;
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - 150;
        if (nextPiece.getShape() != Tetrominoe.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = nextPiece.x(i);
                int y = nextPiece.y(i);
                drawSquare(g, (x + 1) * 30, boardTop + (y + 2) * 30, nextPiece.getShape());
            }
        }
    }
}