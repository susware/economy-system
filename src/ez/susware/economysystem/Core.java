package ez.susware.economysystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener, CommandExecutor {

	public void onEnable() {
		this.getConfig().addDefault("players", 0);
		this.getConfig().options().copyDefaults(true);
		saveConfig();
		
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
		saveConfig();
	}
	
	Core pl;
	FileConfiguration config = this.getConfig();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		String path = "players." + e.getPlayer().getName().toLowerCase();
		if(!e.getPlayer().hasPlayedBefore()) {
			config.set(path, 100);
		} else {
			if(config.contains(path)) {
				return;
			} else {
				config.set(path, 0);
			}
		}
	}
	
	public static boolean isInt(String s) {
	    try {
	        Integer.parseInt(s);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("bal")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				String path = "players." + p.getName().toLowerCase();
				int mane = config.getInt("players." + p.getName().toLowerCase());
				if(args.length > 1) {
					p.sendMessage("§cUsage: /bal (player)");
					return true;
				} else {
					if(args.length == 0) {
						if(config.contains(path)) {
							p.sendMessage("§8" + p.getName() + "§7's balance: §c" + mane + "$");
							return true;
						} else {
							p.sendMessage("§cYou're not in the database yet");
							return true;
						}
					}
					if(args.length == 1) {
						if(config.contains("players." + args[0].toLowerCase())) {
							p.sendMessage("§8" + args[0] + "§7's balance: §c" + config.getInt("players." + args[0].toLowerCase()) + "$");
							return true;
						} else {
							p.sendMessage("§cThis player is not in the database yet");
							return true;
						}
					}
				}
			} else {
				if(args.length != 1) {
					sender.sendMessage("wtf");
					return true;
				} else {
					if(config.contains("players." + args[0].toLowerCase())) {
						sender.sendMessage("§8" + args[0] + "§7's balance: §c" + config.getInt("players." + args[0].toLowerCase()) + "$");
						return true;
					} else {
						sender.sendMessage("§cThis player is not in the database yet");
						return true;
					}
				}
			}
		}
		if(cmd.getName().equalsIgnoreCase("pay")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length != 2) {
					p.sendMessage("§cUsage: /pay (player) (amount)");
					return true;
				} else {
					Player t = Bukkit.getPlayer(args[0]);
					if(t == p) {
						p.sendMessage("§cYou can not send money to yourself");
						return true;
					} else {
						if(t == null) {
							p.sendMessage("§cThis player is currently offline");
							return true;
						} else {
							if(isInt(args[1])) {
								if(args[1].startsWith("0")) {
									p.sendMessage("§cUse integers");
									return true;
								} else {
									String path = "players." + p.getName().toLowerCase();
									String patht = "players." + t.getName().toLowerCase();
									int mane = config.getInt("players." + p.getName().toLowerCase());
									if(Float.valueOf(args[1]) > mane) {
										p.sendMessage("§cThis amount of money is bigger than your balance");
										return true;
									} else {
										int mona = config.getInt("players." + p.getName().toLowerCase());
										int monat = config.getInt("players." + t.getName().toLowerCase());
										config.set(path, mona - Float.valueOf(args[1]));
										config.set(patht, monat + Float.valueOf(args[1]));
										t.sendMessage("§aYou received §7" + args[1] + "$ §afrom §7" + p.getName());
										p.sendMessage("§aSuccessfully sent §7" + args[1] + "$ §ato §7" + t.getName());
									}
								}
 							} else {
								p.sendMessage("§cUsage: /pay (player) (amount)");
								return true;
							}
						}
					}
				}
			} else {
				sender.sendMessage("wtf");
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("eco")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(p.hasPermission("economy.eco")) {
					if(args.length < 2 || args.length > 3) {
						p.sendMessage("§cUsage: /eco (rs|set|add|remove) (player) ([amount])");
						return true;
					} else {
						if(args.length == 2) {
							if(args[0].equalsIgnoreCase("rs") || args[0].equalsIgnoreCase("reset")) {
								String path = "players." + args[1].toLowerCase();
								if(config.contains(path)) {
									config.set(path, 0);
									p.sendMessage("§aSuccesfully reset §7" + args[1] + "§a's balance");
								} else {
									p.sendMessage("§cThis player is not in the database");
									return true;
								}
							}
						} else if(args.length == 3) {
							if(args[0].equalsIgnoreCase("set")) {
								String path = "players." + args[1].toLowerCase();
								if(config.contains(path)) {
									if(isInt(args[2])) {
										config.set(path, Float.valueOf(args[2]));
										p.sendMessage("§aSuccessfully set §7" + args[1] + "§a's balance to §7" + args[2] + "$");
									} else {
										p.sendMessage("§cUsage: /eco (rs|set|add|remove) (player) ([amount])");
										return true;
									}
								} else {
									p.sendMessage("§cThis player is not in the database");
									return true;
								}
							} else if(args[0].equalsIgnoreCase("add")) {
								String path = "players." + args[1].toLowerCase();
								int mona = config.getInt("players." + args[1].toLowerCase());
								if(config.contains(path)) {
									if(isInt(args[2])) {
										p.sendMessage("§aSuccessfully added §7" + args[2] + "$ §ato §7" + args[1] + "§a's §abalance");
										config.set(path, mona + Float.valueOf(args[2]));
									} else {
										p.sendMessage("§cUsage: /eco (rs|set|add|remove) (player) ([amount])");
										return true;
									}
								} else {
									p.sendMessage("§cThis player is not in the database");
									return true;
								}
							} else if(args[0].equalsIgnoreCase("remove")) {
								String path = "players." + args[1].toLowerCase();
								int mona = config.getInt("players." + args[1].toLowerCase());
								if(config.contains(path)) {
									if(isInt(args[2])) {
										p.sendMessage("§aSuccessfully removed §7" + args[2] + "$ §afrom §7" + args[1] + "§a's §abalance");
										config.set(path, mona - Float.valueOf(args[2]));
									} else {
										p.sendMessage("§cUsage: /eco (rs|set|add|remove) (player) ([amount])");
										return true;
									}
								} else {
									p.sendMessage("§cThis player is not in the database");
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
}
