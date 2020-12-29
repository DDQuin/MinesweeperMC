package me.ddquin.minesweeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {

    private Tile[][] tiles;
    boolean isFirstTurn;
    private int width;
    private int height;
    private int mines;
    private int flags;

    public Board(int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.mines = mines;
        this.flags = mines;
        isFirstTurn = true;
        if (width < 0 || height < 0 || mines < 0) throw new IndexOutOfBoundsException();
        tiles = new Tile[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //Intialise all tiles to a blank hidden tile
                tiles[y][x] = new Tile(x,y);
            }
        }
        populateMines(null);

    }

    public enum GameStatus {
        WON,
        LOST,
        HIT_ONE,
        NOTHING,
        FLAG,
        HIT_LOTS
    }

    public void refreshTiles() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[y][x].reset();
            }
        }
    }



    public List<Tile> tilesToList() {
        List<Tile> list = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                list.add(tiles[y][x]);
            }
        }
        return list;
    }

    private void populateMines(Tile avoidTile) {
        Random rand = new Random();
        int minesPlaced = 0;
        List<Tile> freeTiles = tilesToList();
        //if there is a tile to avoid placing a mine in then remove the mine and remove it from free tiles
        if (avoidTile != null) {
            avoidTile.removeMine();
            freeTiles.remove(avoidTile);
        }
        while (minesPlaced < mines) {
            int randomIndex = rand.nextInt(freeTiles.size());
            Tile tileToPlaceMine = freeTiles.get(randomIndex);
            tileToPlaceMine.setIsMine();
            minesPlaced++;
            for (Tile tileAdjacent: getAdjacentTiles(tileToPlaceMine)) {
                tileAdjacent.increaseMinesAdjacent();
            }
            freeTiles.remove(randomIndex);
        }
    }

    public GameStatus flagTile(int x, int y) {
        if (coordOutOfBounds(x, y)) throw new IndexOutOfBoundsException();
        Tile tileToFlag = tiles[y][x];
        //If the tile is hidden (no point flagging a visible tile) and it is not flagged
        //and there is at least one flag left then flag the tile and decrease number of flags left
        if (tileToFlag.isHidden() && !tileToFlag.isFlagged() && flags > 0) {
            tileToFlag.setIsFlagged(true);
            flags--;
            return GameStatus.FLAG;
        }
        // Else if the tile is hidden and it is already flagged then unflag it and increment number of flags left.
        else if (tileToFlag.isHidden() && tileToFlag.isFlagged()) {
            tileToFlag.setIsFlagged(false);
            flags++;
            return GameStatus.FLAG;
        }
        return GameStatus.NOTHING;
    }

    public GameStatus sweepTile(int x, int y) {
        if (coordOutOfBounds(x, y)) throw new IndexOutOfBoundsException();
        Tile tileToSweep = tiles[y][x];
        if (!tileToSweep.isHidden()) return GameStatus.NOTHING;
        if (tileToSweep.isFlagged()) {
            tileToSweep.setIsFlagged(false);
            flags++;
        }
        //if Sweeped mine tile first turn then repopulate mines avoiding to place the mine in that tile
        if (tileToSweep.isMine() && isFirstTurn) {
            refreshTiles();
            populateMines(tileToSweep);
        }
        tileToSweep.setHidden(false);
        isFirstTurn = false;
        //if its a mine or a tile with adjacent mines dont reveal any other tiles;
        if (tileToSweep.isMine() || tileToSweep.getMinesAdjacent() > 0) {
            if (tileToSweep.isMine()) return GameStatus.LOST;
            if (hasWon()) return GameStatus.WON;
            return GameStatus.HIT_ONE;
        }
        //Since its an empty tile reveal all adjacent empty tiles
        for (Tile tileAdjacent: getAdjacentTiles(tileToSweep)) {
            revealOthers(tileAdjacent);
        }
        // check after revealing all other tiles if won game
        if (hasWon()) return GameStatus.WON;
        return GameStatus.HIT_LOTS;

    }

    private void revealOthers(Tile tile) {
        //Base case, tile is already visible no need to reveal
        if (!tile.isHidden()) {
            return;
        }
        //If flagged remove flag and add to number of flags and make visible
        if (tile.isFlagged()) {
            tile.setIsFlagged(false);
            flags++;
        }
        tile.setHidden(false);
        //If the tile has adjacent mines then don't reveal any others.
        if (tile.getMinesAdjacent() > 0) {
            return;
        }
        for (Tile tileAdjacent: getAdjacentTiles(tile)) {
            revealOthers(tileAdjacent);
        }

    }

    private List<Tile> getAdjacentTiles(Tile curTile) {
        int x = curTile.getX();
        int y = curTile.getY();
        if (coordOutOfBounds(x, y)) throw new IndexOutOfBoundsException();
        List<Tile> adjTiles = new ArrayList<>();
        for (int xDiff = -1; xDiff < 2; xDiff++ ) {
            for (int yDiff = -1; yDiff < 2; yDiff++) {
                int curX = x + xDiff;
                int curY = y + yDiff;
                // if the coordinate is not out of bounds and the curX and curY aren't the starting tile then add
                if (!(coordOutOfBounds(curX,curY) || (curX == x && curY == y))) {
                    adjTiles.add(tiles[curY][curX]);
                }
            }
        }
        return adjTiles;
    }


    public boolean coordOutOfBounds(int x, int y) {
        if (x < 0 || x > width - 1 || y < 0 || y > height - 1  ) {
            return true;
        }
        return false;
    }

    public void makeAllVisible() {
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                Tile tile = tiles[row][column];
                tile.setHidden(false);
                tile.setIsFlagged(false);
            }
        }
    }

    private boolean hasWon() {
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                Tile tile = tiles[row][column];
                if (tile.isHidden() && !tile.isMine()) {
                    return false;
                }
            }
        }
        return true;
    }


    public Tile[][] getTiles() {
        return tiles;
    }

    public int getFlags() {
        return flags;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
