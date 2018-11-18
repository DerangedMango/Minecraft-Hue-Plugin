package io.github.derangedmango.lightplugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.ChatColor;

public class LightCommandExecutor implements CommandExecutor {
	private final LightPlugin plugin;
	private TaskList list;

	public LightCommandExecutor(LightPlugin plugin, TaskList list) {
		this.plugin = plugin;
		this.list = list;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.getName().equalsIgnoreCase("console")) {
			if (cmd.getName().equalsIgnoreCase("registerLightIP")) {
				if(args.length == 0) {
					sender.sendMessage(ChatColor.RED + "Error: You must specify an IP to register.");
					return false;
				} else if(args.length == 1) {
					if(!addIP(sender.getName(), args[0], "ALL")) {
						sender.sendMessage(ChatColor.RED + "Error: Your username already has a registered IP.");
						return false;
					} else {
						DimLights task = new DimLights(plugin, plugin.getServer().getPlayer(sender.getName()));

						task.runTaskTimer(plugin, 0, 4);
				        list.add(task);
						
						return true;
					}
				} else {
					String lightGroup = "";
					
					for(int i = 1; i < args.length; i++) {
						lightGroup = lightGroup + args[i] + " ";
					}
					
					if(!addIP(sender.getName(), args[0], lightGroup.trim())) {
						sender.sendMessage(ChatColor.RED + "Error: Your username already has a registered IP.");
						return false;
					} else {
						DimLights task = new DimLights(plugin, plugin.getServer().getPlayer(sender.getName()));

						task.runTaskTimer(plugin, 0, 4);
				        list.add(task);
						
						return true;
					}
				}
			} else if(cmd.getName().equalsIgnoreCase("deregisterLightIP")) {
				if(!removeIP(sender.getName())) {
					sender.sendMessage(ChatColor.RED + "Error: Your username does not have a registered IP.");
					return false;
				} else {
					return true;
				}
			}
		}
		
		sender.sendMessage(ChatColor.RED + "Error: This command must be run by a user.");
		return false;
	}
	
	private boolean addIP(String name, String ip, String group) {
		File file = new File(plugin.getDataFolder(), "player_data.txt");
		boolean match = false;
		
		try(Scanner scanner = new Scanner(file)) {
			
			while (scanner.hasNextLine()  && !match) {
				String line = scanner.nextLine();
				
				if(line.substring(0, line.indexOf(":")).equalsIgnoreCase(name)) {
					match = true;
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    		return false;
    	}
		
		if(!match) {
			try(FileWriter fw = new FileWriter(file.getPath(), true);
				BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw)) {
			
			    out.println(name + ":" + ip + ":" + group + ":$");
			    
			    return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	private boolean removeIP(String name) {
		File file = new File(plugin.getDataFolder(), "player_data.txt");
		boolean match = false;
		String outString = "";
		
		try(Scanner scanner = new Scanner(file)) {
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if(line.substring(0, line.indexOf(":")).equalsIgnoreCase(name)) {
					match = true;
				} else {
					outString = outString + line + ",";
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    		return false;
    	}
		
		if(match) {
			list.cancel(name);
			
			try(FileWriter fw = new FileWriter(file.getPath(), false);
				BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw)) {
				
				String seg = "";
				
				for(int x = 0; x < outString.length(); x++) {
					if(String.valueOf(outString.charAt(x)).equals(",")) {
						out.println(seg);
						seg = "";
					} else {
						seg = seg + outString.charAt(x);
					}
				}
			    
			    return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
}
