package game;

import java.awt.*;

public enum Tetrominoe {
    NoShape(new int[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}}, new Color(0, 0, 0)), //добавить в отчет описание фигур
    S(new int[][]{{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}, new Color(204, 102, 102)),
    Z(new int[][]{{0, -1}, {0, 0}, {1, 0}, {1, 1}}, new Color(102, 204, 102)),
    I(new int[][]{{0, -1}, {0, 0}, {0, 1}, {0, 2}}, new Color(102, 102, 204)),
    T(new int[][]{{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, new Color(204, 204, 102)),
    O(new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}}, new Color(204, 102, 204)),
    J(new int[][]{{-1, -1}, {0, -1}, {0, 0}, {0, 1}}, new Color(102, 204, 204)),
    L(new int[][]{{1, -1}, {0, -1}, {0, 0}, {0, 1}}, new Color(218, 170, 0));

    public int[][] coords;
    public Color color;

    Tetrominoe(int[][] coords, Color c) {
        this.coords = coords;
        color = c;
    }
}