package fr.Boulldogo.WatchLogs.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import fr.Boulldogo.WatchLogs.Main;
import fr.Boulldogo.WatchLogs.Listener.ToolListener;
import fr.Boulldogo.WatchLogs.Utils.DatabaseManager;
import fr.Boulldogo.WatchLogs.Utils.PlayerSession;

public class MainCommand implements CommandExecutor, TabCompleter{
    
    public final Main plugin;
    public DatabaseManager databaseManager;
    
    public MainCommand(Main plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        if(!(sender instanceof Player)) {
            plugin.getLogger().info("Only online players can use WatchLogs (/wl) commands!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if(args.length < 1) {
            player.sendMessage(translateString(plugin.getLang().getString("messages.unknown-subcommand")));
            return true;
        }
        
        String subCommand = args[0];
        
        if(!isValidSubcommand(subCommand)) {
            player.sendMessage(translateString(plugin.getLang().getString("messages.unknown-subcommand")));
            return true;
        }
        
        if(subCommand.equals("help")) {
            if(!player.hasPermission("watchlogs.help")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.have-not-permission")));
                return true;
            }
            
            List<String> helpList = plugin.getLang().getStringList("messages.help-message");
            for (String help : helpList) {
                player.sendMessage(translateString(help));
            }
        } else if(subCommand.equals("reload")) {
            if(!player.hasPermission("watchlogs.reload")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.have-not-permission")));
                return true;
            }
            
            plugin.reloadConfig();
            player.sendMessage(ChatColor.GREEN + "Configuration of WatchLogs plugin reloaded with success !");
        } else if(subCommand.equals("tool")) {
            if(!player.hasPermission("watchlogs.tool")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.have-not-permission")));
                return true;
            }
            
            if(databaseManager.playerExists(player.getName()) && databaseManager.isToolEnabled(player.getName())) {
                databaseManager.setToolEnabled(player.getName(), false);
                ItemStack logBlock = createLogBlock();
                PlayerInventory inventory = player.getInventory();
                ItemStack[] contents = inventory.getContents();
                for (int i = 0; i < contents.length; i++) {
                    if(contents[i] != null && contents[i].isSimilar(logBlock)) {
                        inventory.setItem(i, new ItemStack(Material.AIR)); 
                    }
                }
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.tool-correctly-removed")));
                return true;
            } else {    
                databaseManager.checkOrCreatePlayer(player.getName());
                if(player.getInventory().getItemInHand().getType() == Material.AIR) {
                    databaseManager.setToolEnabled(player.getName(), true);
                    player.getInventory().setItemInHand(createLogBlock());
                    player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.tool-correctly-given")));
                    return true;
                } else {
                    player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.you-have-item-in-hand")));
                    return true;
                }
            }
        } else if(subCommand.equals("giveitem")) {
            if(!player.hasPermission("watchlogs.giveitem")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.have-not-permission")));
                return true;
            }
            
            if(args.length < 2) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.usage-giveitem")));
                return true;
            }
            
            if(!plugin.getConfig().getBoolean("use-item-reborn-system")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.module-item-reborn-disable")));
                return true;
            }
            
            String i = args[1];
            if(!isInteger(i)) {
            	player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.id-number-positive")));
            	return true;
            }
            
            int id = Integer.parseInt(i);
            
            if(!databaseManager.itemIdExists(id)) {
            	player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.id-not-exists-for-item")));
            	return true;
            }
            
            if(player.getInventory().getItemInHand().getType() == Material.AIR) {
            	ItemStack stack = databaseManager.getItemById(id);
            	player.getInventory().setItemInHand(stack);
            	 player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.item-correctly-given")));
                return true;
            } else {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.you-have-item-in-hand")));
                return true;
            }  
            
        } else if(subCommand.equals("gdeath")) {
            if(!player.hasPermission("watchlogs.givedeath")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.have-not-permission")));
                return true;
            }
            
            if(args.length < 2) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.usage-giveitem")));
                return true;
            }
            
            if(!plugin.getConfig().getBoolean("use-item-reborn-system")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.module-item-reborn-disable")));
                return true;
            }
            
            String i = args[1];
            if(!isInteger(i)) {
            	player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.id-number-positive")));
            	return true;
            }
            
            int id = Integer.parseInt(i);
            
            if(id <= 0) {
            	player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.id-number-positive")));
            	return true;
            }
            
            if(plugin.getConfig().getBoolean("cancel-if-inventory-is-not-empty")) {
                if (player.getInventory().firstEmpty() == -1) {
                	player.sendMessage(prefix + plugin.getLang().getString("messages.inventory-is-not-empty"));
                	return true;
                }
            }
            
            regenerateItemsForDeath(player, id);
        } else if(subCommand.equals("database")) {
            if(!player.hasPermission("watchlogs.database")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.have-not-permission")));
                return true;
            }
            
            player.sendMessage(prefix + databaseManager.getDatabaseInfo());
        } else if(subCommand.equals("page")) {
            if(!player.hasPermission("watchlogs.page")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.have-not-permission")));
                return true;
            }
            
            if(args.length < 2) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.usage-page")));
                return true;
            }
            try {
                int page = Integer.parseInt(args[1]);
                if(page < 1) {
                    player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.page-number-positive")));
                    return true;
                }
                PlayerSession session = plugin.getPlayerSession(player);
                if(session.isToolLog()) {
                    ToolListener toolListener = new ToolListener(plugin, databaseManager);
                    session.setCurrentPage(page);
                    toolListener.showLogs(player, page);
                } else {
                    session.setCurrentPage(page);
                    this.showPage(session.getCurrentLogs(), page, player);
                }
            } catch (NumberFormatException e) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.invalid-page-number")));
            }
        } else if(subCommand.equals("tpto")) {
            if(!player.hasPermission("watchlogs.tpto")) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.have-not-permission")));
                return true;
            }
            
            if(args.length < 2) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.usage-tpto")));
                return true;
            }
            
            String idS = args[1];
            int id = Integer.parseInt(idS);
            if(!isInteger(idS) || id < 1) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.id-number-positive")));
                return true;
            }
            int maxid = databaseManager.getLastLogId();
            
            if(id > maxid) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.given-id-highest")));
                return true;    
            }
            
            String location = databaseManager.getLocationOfId(id);
            String world = databaseManager.getWorldOfId(id);
            
            if(Bukkit.getWorld(world) == null) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.world-not-exist")));
                return true;
            }
            
            World wrd = Bukkit.getWorld(world);
            
            String[] parts = location.split("/");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            
            Location loc = new Location(wrd, x, y, z);
            player.teleport(loc);
            player.sendMessage(prefix + String.format(translateString(plugin.getLang().getString("messages.correctly-teleported")), id));
            
        } else if(subCommand.equals("search")) {
            if(!player.hasPermission("watchlogs.search")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.have-not-permission")));
                return true;
            }

            if(args.length < 2) {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.usage-search")));
                return true;
            }

            String settings = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            String[] arguments = settings.split("\\s+");

            String playerSearch = "undefined";
            String worldSearch = "undefined";
            String actionSearch = "undefined";
            int radiusSearch = -1;
            String filterSearch = "undefined";
            String timeFilter = "undefined";
            boolean useTimestamp = false;
            boolean useRayon = false;

            for (String argument : arguments) {
                if(argument.startsWith("p:")) {
                    playerSearch = argument.substring(2);
                } else if(argument.startsWith("w:")) {
                    worldSearch = argument.substring(2);
                } else if(argument.startsWith("a:")) {
                    actionSearch = argument.substring(2);
                } else if(argument.startsWith("r:")) {
                    try {
                        radiusSearch = Integer.parseInt(argument.substring(2));
                        if(radiusSearch > plugin.getConfig().getInt("max-radius-research")) {
                            player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.radius-too-large")));
                            return true;
                        }
                        useRayon = true;
                    } catch (NumberFormatException e) {
                        player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.invalid-radius")));
                        return true;
                    }
                } else if(argument.startsWith("f:")) {
                    filterSearch = argument.substring(2);
                } else if(argument.startsWith("t:")) {
                    timeFilter = argument.substring(2);
                    useTimestamp = true;
                    if(timeFilter.isEmpty()) {
                        player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.invalid-time-format")));
                        return true;
                    }
                } else {
                    player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.invalid-argument")) + argument);
                    return true;
                }
            }

            Location location = player.getLocation();
            PlayerSession session = plugin.getPlayerSession(player);
            session.setToolLog(false);
            session.setCurrentPage(1);
            List<String> searchResult = databaseManager.getLogs(
                worldSearch, playerSearch, useRayon,
                location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                radiusSearch, actionSearch, filterSearch, timeFilter, useTimestamp
            );
            session.setCurrentLogs(searchResult);

            showPage(searchResult, 1, player);
            return true;
        }
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(!(sender instanceof Player)) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if(args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "tool", "database", "page", "tpto", "reload", "search", "giveitem", "gdeath");
            String currentArg = args[0].toLowerCase();
            completions = subCommands.stream()
                .filter(subCommand -> subCommand.startsWith(currentArg))
                .sorted((a, b) -> {
                    if(a.startsWith(currentArg) && !b.startsWith(currentArg)) {
                        return -1;
                    } else if(!a.startsWith(currentArg) && b.startsWith(currentArg)) {
                        return 1;
                    }
                    return a.compareTo(b);
                })
                .collect(Collectors.toList());
        } else if(args.length > 1) {
            String subCommand = args[0].toLowerCase();
            if(subCommand.equals("search")) {
                List<String> usedParams = Arrays.asList(args).subList(1, args.length - 1);

                List<String> searchParams = new ArrayList<>(Arrays.asList("p:", "w:", "a:", "r:", "f:", "t:"));
                searchParams.removeIf(param -> usedParams.stream().anyMatch(arg -> arg.startsWith(param)));

                String currentArg = args[args.length - 1].toLowerCase();

                if(currentArg.startsWith("a:")) {
                    List<String> actions = Arrays.asList(
                        "join", "leave", "teleport", "block-place", "block-break", "container-open", "container-transaction",
                        "item-drop", "item-pickup", "item-break", "player-death", "player-death-loot", "commands", "send-message",
                        "interact-item", "interact-block", "interact-entity", "block-explosion"
                    );
                    String actionArg = currentArg.substring(2);
                    completions = actions.stream()
                        .filter(action -> action.startsWith(actionArg))
                        .map(action -> "a:" + action)
                        .collect(Collectors.toList());
                } else if(currentArg.startsWith("p:")) {
                    String playerArg = currentArg.substring(2);
                    completions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(playerArg))
                        .map(name -> "p:" + name)
                        .collect(Collectors.toList());
                } else if(currentArg.startsWith("r:")) {
                    try {
                        int maxRadius = plugin.getConfig().getInt("max-radius-research");
                        completions = Arrays.stream(new int[maxRadius - 1])
                            .mapToObj(i -> "r:" + (i + 1))
                            .filter(r -> r.startsWith(currentArg))
                            .collect(Collectors.toList());
                    } catch (NumberFormatException e) {
                        completions.add("r:1");
                    }
                } else if(currentArg.startsWith("w:")) {
                    String worldArg = currentArg.substring(2);
                    completions = Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> name.toLowerCase().startsWith(worldArg))
                        .map(name -> "w:" + name)
                        .collect(Collectors.toList());
                } else {
                    completions = searchParams.stream()
                        .filter(param -> param.startsWith(currentArg))
                        .sorted((a, b) -> {
                            if(a.startsWith(currentArg) && !b.startsWith(currentArg)) {
                                return -1;
                            } else if(!a.startsWith(currentArg) && b.startsWith(currentArg)) {
                                return 1;
                            }
                            return a.compareTo(b);
                        })
                        .collect(Collectors.toList());
                }
            }
        }

        return completions;
    }

    
    public void showPage(List<String> searchResult, int page, Player player) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        if(searchResult.isEmpty()) {
            player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.no-matching-entries")));
        } else {
            int entries = plugin.getConfig().getInt("max-entries");
            int totalPage = (searchResult.size() + (entries - 1)) / entries;
            if(page <= totalPage) {
                player.sendMessage(""); 
                player.sendMessage(prefix + String.format(translateString(plugin.getLang().getString("messages.logs-found")), page, totalPage)); 
                player.sendMessage("");
                int startIndex = (page - 1) * entries;
                int endIndex = Math.min(startIndex + entries, searchResult.size()); 
                for (int i = startIndex; i < endIndex; i++) {
                    player.sendMessage(searchResult.get(i));
                }
                if(page < totalPage) {
                    player.sendMessage(translateString(plugin.getLang().getString("messages.next-page")) + (page + 1));
                }
            } else {
                player.sendMessage(prefix + translateString(plugin.getLang().getString("messages.page-number-exceeds")));
            }
        }
    }
    
    public ItemStack createLogBlock() {
        String id = plugin.getConfig().getString("block-tool.id");

        Material material = Material.getMaterial(id);
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(translateString(plugin.getConfig().getString("block-tool.name")));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public void regenerateItemsForDeath(Player player, int deathId) {
    	String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        List<ItemStack> items = databaseManager.getItemsByDeathId(deathId);
        if (items.isEmpty()) {
            player.sendMessage(prefix + plugin.getLang().getString("messages.no-death-given-with-this-id"));
            return;
        }

        for (ItemStack item : items) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.sendMessage(prefix + plugin.getLang().getString("messages.advertisment-item-drop-in-ground"));
            } else {
                player.getInventory().addItem(item);
            }
        }

        player.sendMessage(prefix + plugin.getLang().getString("messages.item-death-correctly-given"));
    }

    public boolean isValidSubcommand(String s) {
        return s.equals("help")
            || s.equals("tool")
            || s.equals("database")
            || s.equals("page")
            || s.equals("tpto")
            || s.equals("reload")
            || s.equals("gdeath")
            || s.equals("search")
            || s.equals("giveitem")
            || s.equals("reloadconfig");
    }

    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
