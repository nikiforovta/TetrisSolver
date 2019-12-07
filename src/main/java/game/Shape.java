package game;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Shape {
    private Tetrominoe pieceShape;
    private int[][] coords;
    private LinkedList<Tetrominoe> bag = new LinkedList<>(); //Список фигур, необходимый для улучшенного случайного подбора следующей фигуры


    public Shape() {
        coords = new int[4][2];
        setShape(Tetrominoe.NoShape);
    }

    private void setX(int index, int x) {
        coords[index][0] = x;
    }

    private void setY(int index, int y) {
        coords[index][1] = y;
    }

    public int x(int index) {
        return coords[index][0];
    }

    public int y(int index) {
        return coords[index][1];
    }

    public Tetrominoe getShape() {
        return pieceShape;
    }

    public void setShape(Tetrominoe shape) {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(shape.coords[i], 0, coords[i], 0, 2);
        }
        pieceShape = shape;
    }

    /**
     * Метод, выбирающий случайную фигуру из "мешка" фигур
     */
    void setRandomShape() {
        if (bag.size() == 0) {
            bag = new LinkedList<>(Arrays.asList(Tetrominoe.values()));
            bag.remove(0);
        }
        Collections.shuffle(bag);
        setShape(bag.removeFirst());
    }

    int minY() {
        int m = coords[0][1];
        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }
        return m;
    }

    /**
     * Метод поворота фигуры на 90 градусов против часовой стрелки
     */
    public Shape rotateLeft() {
        if (pieceShape == Tetrominoe.O)
            return this;
        Shape result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; i++) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }
        return result;
    }
}