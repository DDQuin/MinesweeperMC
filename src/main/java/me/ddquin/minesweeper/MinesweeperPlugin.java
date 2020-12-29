package me.ddquin.minesweeper;

import de.themoep.inventorygui.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MinesweeperPlugin extends JavaPlugin {

    private HashMap<UUID, Board> playerGame;
    private List<InventoryGui> openedGUIS;

    public void onEnable() {
        playerGame = new HashMap<>();
        openedGUIS = new ArrayList<>();
        getCommand("minesweeper").setExecutor(new MinesweeperCommands(this));
    }

    public void sendHelp(Player p) {
        p.sendMessage(ChatColor.GREEN + "/ms play [number] - Continue a Minesweeper game if currently in one or start a Minesweeper game with the given number of mines, if blank it will default to 5.");
        p.sendMessage(ChatColor.GREEN + "/ms quit - Quit the current minesweeper game.");
        p.sendMessage(ChatColor.GREEN + "/ms help - Show this help message");
        p.sendMessage(ChatColor.GREEN + "Plugin created by DDQuin");
    }


    public void playGame(Player p, String[] args) {

        if (playerGame.get(p.getUniqueId()) == null) {
            int mines = 5;
            if (args.length > 1) {
                try {
                    mines = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Please enter a valid integer for the number of mines");
                    return;
                }
            }
            if (mines < 0 || mines >= 54) {
                p.sendMessage(ChatColor.RED + "The number of mines must be between 0 and 53 (inclusive)");
                return;
            }
            p.sendMessage(ChatColor.GREEN + "Starting the game with the default number of 5 mines.");
            Board board = new Board(9, 6, mines);
            playerGame.put(p.getUniqueId(), board);
            showMineweeperGUI(p, board);
        } else {
            showMineweeperGUI(p, playerGame.get(p.getUniqueId()));
        }
    }

    public void quitGame(Player p) {
        if (playerGame.get(p.getUniqueId()) != null) {
            playerGame.remove(p.getUniqueId());
            p.sendMessage(ChatColor.GREEN + "You have quit your current game");
        } else {
            p.sendMessage(ChatColor.GREEN + "You are currently not in a game");
        }
    }

    private int getXfromIndex(int i) {
        return i % 9;
    }

    private int getYfromIndex(int i) {
        return i/9;
    }

    public void showMineweeperGUI(Player p, Board board) {
        String[] guiSetup = {
                "ttttttttt",
                "ttttttttt",
                "ttttttttt",
                "ttttttttt",
                "ttttttttt",
                "ttttttttt"

        };
        InventoryGui gui = new InventoryGui(this, null, "Minesweeper Flags Left: " + board.getFlags(), guiSetup);
        GuiElementGroup group = new GuiElementGroup('t');
        for (int i = 0; i < 54; i++) {
            StaticGuiElement s = new StaticGuiElement('t', getItemStack(board, getXfromIndex(i), getYfromIndex(i)),
                    click -> {
                        if (click.getType().isRightClick()) {
                            Board.GameStatus status = board.flagTile(getXfromIndex(click.getSlot()), getYfromIndex(click.getSlot()));
                            playTileSound(p, status);
                            click.getGui().close();
                            openedGUIS.remove(click.getGui());
                            showMineweeperGUI(p, board);
                            return true;
                        } else {
                            Board.GameStatus status = board.sweepTile(getXfromIndex(click.getSlot()), getYfromIndex(click.getSlot()));
                            playTileSound(p, status);
                            if (status == Board.GameStatus.LOST) {
                                p.sendMessage(ChatColor.RED + "You lost! do ms quit and then ms play to play again");
                                board.makeAllVisible();
                            }
                            if (status == Board.GameStatus.WON) {
                                p.sendMessage(ChatColor.GREEN + "You won! do ms quit and then ms play to play again");
                                board.makeAllVisible();
                            }
                            click.getGui().close();
                            openedGUIS.remove(click.getGui());
                            showMineweeperGUI(p, board);
                            return true;
                        }
                    });

            group.addElement(new DynamicGuiElement('t', (viewer) -> s));
        }
        gui.addElement(group);
        gui.show(p);
        openedGUIS.add(gui);
    }

    private void playTileSound(Player p, Board.GameStatus status) {
        switch (status) {
            case HIT_ONE:
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
                break;
            case HIT_LOTS:
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                break;
            case LOST:
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                break;
            case WON:
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);
                Bukkit.getScheduler().runTaskLater(this, () -> p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1), 20);
                break;
            case FLAG:
                p.playSound(p.getLocation(), Sound.BLOCK_STEM_PLACE, 1, 1);
                break;
            case NOTHING:
                break;
        }
    }

    public ItemStack getItemStack(Board board, int x, int y) {
        Tile tile = board.getTiles()[y][x];
        if (tile.isFlagged()) {
            ItemStack item = new ItemStack(Material.SPRUCE_SIGN);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Flagged Tile");
            item.setItemMeta(meta);
            return item;
        }
        if (tile.isHidden()) {
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Hidden Tile");
            item.setItemMeta(meta);
            return item;
        }
        if (tile.isMine()) {
            ItemStack item = new ItemStack(Material.TNT);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Mine!");
            item.setItemMeta(meta);
            return item;
        }
        if (tile.getMinesAdjacent() == 0) {
            return new ItemStack(Material.AIR);
        }
        Material mat = null;
        if (tile.getMinesAdjacent() == 1) {
            mat = Material.BLUE_STAINED_GLASS_PANE;
        } else if (tile.getMinesAdjacent() == 2) {
            mat = Material.LIME_STAINED_GLASS_PANE;
        } else if (tile.getMinesAdjacent() == 3) {
            mat = Material.RED_STAINED_GLASS_PANE;
        } else if (tile.getMinesAdjacent() == 4) {
            mat = Material.CYAN_STAINED_GLASS_PANE;
        } else if (tile.getMinesAdjacent() == 5) {
            mat = Material.ORANGE_STAINED_GLASS_PANE;
        } else if (tile.getMinesAdjacent() == 6) {
            mat = Material.MAGENTA_STAINED_GLASS_PANE;
        } else if (tile.getMinesAdjacent() == 7) {
            mat = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        } else if (tile.getMinesAdjacent() == 8) {
            mat = Material.YELLOW_STAINED_GLASS_PANE;
        }
        ItemStack item = new ItemStack(mat, tile.getMinesAdjacent());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(tile.getMinesAdjacent() + " mines adjacent");
        item.setItemMeta(meta);
        return item;
    }



    public void onDisable() {
        openedGUIS.forEach(gui -> gui.close());
        //Make sure all guis are shut down when server reloads/restarts so players dont get to dupe items
    }
}
