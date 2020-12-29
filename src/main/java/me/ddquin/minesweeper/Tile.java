package me.ddquin.minesweeper;

public class Tile {

    private int minesAdjacent;
    private boolean isMine;
    private boolean isHidden;
    private boolean isFlagged;
    private int x;
    private int y;

    public Tile(int x, int y) {
        minesAdjacent = 0;
        isMine = false;
        isHidden = true;
        isFlagged = false;
        this.x = x;
        this.y = y;
    }

    public void increaseMinesAdjacent() {
        minesAdjacent++;
    }

    public int getMinesAdjacent() {
        return minesAdjacent;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isMine() {
        return isMine;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void reset() {
        minesAdjacent = 0;
        isMine = false;
        isHidden = true;
        isFlagged = false;
    }

    public boolean setIsFlagged(boolean b) {
        isFlagged = b;
        return b;
    }

    public void setIsMine() {
        isMine = true;
    }

    public void removeMine() { isMine = false; }

    public void setHidden(boolean b) {
        isHidden = b;
    }

}
