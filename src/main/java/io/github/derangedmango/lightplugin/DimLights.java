package io.github.derangedmango.lightplugin;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Wool;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatColor;

public class DimLights extends BukkitRunnable {
	private final JavaPlugin plugin;
	private final Player player;
	private final int normalRate;
	private final int colorRate;
	private LocalConnection con;
	private int lastLightLevel;
	private int lastHue;
	private int lastSat;
	private boolean paused;
	private String[] conInfo;
	private boolean inTheEnd;
	private int buttonPromptCounter;

    public DimLights(JavaPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        con = null;
        lastLightLevel = -1;
        lastHue = -1;
        lastSat = -1;
        paused = false;
        conInfo = null;
        normalRate = 3;
        colorRate = 7;
        inTheEnd = false;
        buttonPromptCounter = 0;
    }

    @Override
    public void run() {
    	if(player.isOnline()) {
    		if(conInfo == null) {
    			conInfo = getLightIP(player.getName());
    		}
	    	
	    	String lightIP = conInfo[0];
	    	String lightGroup = conInfo[1];
	    	String username = conInfo[2];
	    	
	    	if(lightIP != null) {
	    		// plugin.getLogger().info("Nano time before: " + System.nanoTime());
	    		// plugin.getLogger().info("light IP not null: " + lightIP);
	    		// plugin.getLogger().info("hue username: " + username);
	    		if(!paused) setLightLevel(getPlayerLightLevel(), lightIP, lightGroup, username);
	    		// plugin.getLogger().info("Nano time after: " + System.nanoTime());
	    	} else {
	    		this.cancel();
	    	}
    	} else {
    		this.cancel();
    	}
    }
    
    public void pause() { paused = true; }
    
    public void resume() {
        lastHue = -1;
    	paused = false;
    }
    
    public String getPlayerName() {
    	return player.getName();
    }
    
    public String[] getConInfo() {
    	return conInfo;
    }
    
    private byte getPlayerLightLevel() {
		return player.getLocation().getBlock().getLightLevel();
    }

    private void setLightLevel(byte level, String ip, String group, String usr) {
    	byte lightLevel = level;
    	String lightIP = ip;
    	String lightGroup = group;
    	String username = usr;
    	// String name = player.getName();
    	
    	int[] hueLevels = new int[] {22,37,52,67,82,97,112,127,142,157,172,187,202,217,232,247};
    	
    	// plugin.getLogger().info("Player: " + name + ", Light Level: " + lightLevel + ", Light IP: " + lightIP);
    	
    	if(con != null && !con.toString().equalsIgnoreCase("Not Connected")) {
    		if(con.toString().equalsIgnoreCase("Press Button")) {
    			if(buttonPromptCounter == 0 || buttonPromptCounter == 300) {
    				player.sendMessage(ChatColor.DARK_AQUA + "Please press the link button on your Hue hub.");
    			} else if(buttonPromptCounter > 600) {
    				player.sendMessage(ChatColor.RED + "Link button not pressed - registration attempt aborted!");
    				player.sendMessage(ChatColor.DARK_AQUA + "Disconnect and reconnect to the server to re-initiate attempt.");
    				this.cancel();
    			}
    			
    			if(con.registerUser()) {
    				conInfo = getLightIP(player.getName());
    				player.sendMessage(ChatColor.DARK_AQUA + "Connection Registered!");
    			} else {
    				buttonPromptCounter++;
    			}
    		} else if(con.toString().equalsIgnoreCase("Ready")) {
    			// plugin.getLogger().info("connection ready");
    			int[] color = getDomColor();
    			
    			if(inTheEnd) lightLevel = 15;
    			
    			if(lightLevel != lastLightLevel || color[0] != lastHue || color[1] != lastSat) {
    				// plugin.getLogger().info("difference found");
    				if(color[0] != lastHue || color[1] != lastSat) {
    					con.dim(hueLevels[lightLevel], color[0], color[1], colorRate);
    				} else {
    					con.dim(hueLevels[lightLevel], color[0], color[1], normalRate);
    				}
    				
    				lastLightLevel = lightLevel;
    				lastHue = color[0];
    				lastSat = color[1];
    			}
    		} else if(con.toString().equalsIgnoreCase("Invalid IP")) {
    			player.sendMessage(ChatColor.RED + "Error: The IP you registered is not valid.");
    			this.cancel();
    		}
    	} else {
    		con = new LocalConnection(plugin, player, lightIP, lightGroup, username);
    	}
    }
    
    private String[] getLightIP(String name) {
    	String[] result = new String[3];
    	boolean match = false;
    	
    	try(Scanner scanner = new Scanner(new File(plugin.getDataFolder(), "player_data.txt"))) {
					
			while (scanner.hasNextLine()  && !match) {
				String line = scanner.nextLine();
				
				if(line.substring(0, line.indexOf(":")).equalsIgnoreCase(name)) {
					match = true;
					
					String[] arr = line.split(":");
					result[0] = arr[1];
					result[1] = arr[2];
					result[2] = arr[3];
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	return result;
    }
    
    private int[] getDomColor() {
    	String[] blockTypes = new String[75];
		int[] blockCounts = new int[75];

		int px = player.getLocation().getBlock().getX();
		int py = player.getLocation().getBlock().getY();
		int pz = player.getLocation().getBlock().getZ();
		
		int lastIndex = -1;
		boolean match = false;
		
		Location loc;
		String blockName;
		World world = player.getWorld();
		
		if(world.getEnvironment().toString().equalsIgnoreCase("NETHER")) {
			inTheEnd = false;
			return new int[] {67,247};
    	} else if(world.getEnvironment().toString().equalsIgnoreCase("THE_END")) {
    		inTheEnd = true;
    		return new int[] {47416,214};
    	} else {
			for(int x = px - 2; x <= px + 2; x++) {
				for(int y = py - 1; y <= py + 1; y++) {
					for(int z = pz - 2; z <= pz + 2; z++) {
						loc = new Location(world, x, y, z);
						blockName = consolidateBlockNames(loc.getBlock());
	
						if(!blockName.equalsIgnoreCase("AIR")) {
							if(lastIndex > -1) {
								int i;
		
								for(i = 0; i <= lastIndex; i++) {
									if(blockName.equals(blockTypes[i])) {
										match = true;
										break;
									}
								}
		
								if(match) {
									blockCounts[i]++;
									match = false;
								} else {
									blockTypes[++lastIndex] = blockName;
									blockCounts[lastIndex] = 1;
								}
							} else {
								blockTypes[++lastIndex] = blockName;
								blockCounts[lastIndex] = 1;
							}
						}
					}
				}
			}
		}

		int currMax = 0;
		String domBlock = "";

		for(int i = 0; i <= lastIndex; i++) {
			// plugin.getLogger().info("NAME: " + blockTypes[i] + ", COUNT: " + blockCounts[i]);
			
			if(blockCounts[i] > currMax) {
				currMax = blockCounts[i];
				domBlock = blockTypes[i];
			}
		}
		
		inTheEnd = false;
		return getColorFromBlock(domBlock, blockTypes, blockCounts, lastIndex);
	}
    
    private String consolidateBlockNames(Block block) {
    	String name = block.getType().toString();
    	
    	if(name.contains("WATER")) {
			return "WATER";
		} else if(name.equalsIgnoreCase("LONG_GRASS") || name.equalsIgnoreCase("LEAVES") || name.equalsIgnoreCase("LEAVES_2")) {
			return "GRASS";
		} else if((name.contains("STONE") && !name.contains("SAND") && !name.contains("RED")
				&& !name.contains("END")) || name.equalsIgnoreCase("GRAVEL")  || name.contains("ORE")) {
			return "STONE";
		} else if(name.contains("LAVA")) {
			return "LAVA";
		} else if(name.contains("SAND")) {
			return "SAND";
		} else if(name.equalsIgnoreCase("WOOL")) {
			Wool wool = (Wool) block.getState().getData();
			return wool.getColor().toString().toUpperCase() + "_WOOL";
		} else {
			return name;
		}
    }
    
    private int[] getColorFromBlock(String domBlock, String[] blockTypes, int[] blockCounts, int lastIndex) {
    	if(domBlock.equalsIgnoreCase("GRASS")) {
			for(int i = 0; i <= lastIndex; i++) {
				if(blockTypes[i].equalsIgnoreCase("SNOW") && blockCounts[i] >= 10) {
					return new int[] {41513,108};
				}
			}
			
			Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
			// plugin.getLogger().info("biome name: " + biome.name());
			
			if(biome.name().contains("EXTREME_HILLS")) {
				return new int[] {35017,124};
			} else if(biome.name().equalsIgnoreCase("ROOFED_FOREST")) {
				return new int[] {26986,103};
			} else if(biome.name().equalsIgnoreCase("TAIGA_HILLS")) {
				return new int[] {37580,149};
			} else if(biome.name().equalsIgnoreCase("SWAMPLAND")) {
				return new int[] {15762,253};
			} else if(biome.name().contains("JUNGLE")) {
				return new int[] {26191,164};
			} else if(biome.name().contains("BADLANDS") || biome.name().contains("MESA")) {
				return new int[] {33664,32};
			}
			
			return new int[] {30539,85};
		} else if(domBlock.equalsIgnoreCase("WATER")) {
			return new int[] {45016,253};
		} else if(domBlock.equalsIgnoreCase("DIRT")) {
			Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
			// plugin.getLogger().info(biome.name());
			
			if(biome.name().contains("BADLANDS") || biome.name().contains("MESA")) {
				return new int[] {558,69};
			}
			
			for(int i = 0; i <= lastIndex; i++) {
				if(blockTypes[i].equalsIgnoreCase("GRASS")) {
					if(biome.name().contains("EXTREME_HILLS")) {
						return new int[] {35017,124};
					} else if(biome.name().equalsIgnoreCase("ROOFED_FOREST")) {
						return new int[] {26986,103};
					} else if(biome.name().equalsIgnoreCase("TAIGA_HILLS")) {
						return new int[] {37580,149};
					} else if(biome.name().equalsIgnoreCase("SWAMPLAND")) {
						return new int[] {15762,253};
					} else if(biome.name().contains("JUNGLE")) {
						return new int[] {26191,164};
					} else if(biome.name().contains("BADLANDS") || biome.name().contains("MESA")) {
						return new int[] {33664,32};
					}
					
					return new int[] {30539,85};
				}
			}
			
			return new int[] {5852,154};
		} else if(domBlock.equalsIgnoreCase("STONE")) {
			return new int[] {44559,129};
		} else if(domBlock.equalsIgnoreCase("LAVA")) {
			return new int[] {2732,217};
		} else if(domBlock.contains("SAND")) {
			Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
			// plugin.getLogger().info(biome.name());
			
			if(biome.name().contains("BADLANDS") || biome.name().contains("MESA")) {
				return new int[] {558,69};
			}
			
			return new int[] {43307,33};
		} else if(domBlock.equalsIgnoreCase("SNOW")) {
			return new int[] {41513,108};
		} else if(domBlock.equalsIgnoreCase("MYCEL")) {
			return new int[] {46711,143};
		} else if(domBlock.equalsIgnoreCase("STAINED_CLAY") || domBlock.equalsIgnoreCase("HARD_CLAY")) {
			Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
			// plugin.getLogger().info(biome.name());
			
			if(biome.name().contains("BADLANDS") || biome.name().contains("MESA")) {
				return new int[] {558,69};
			}
			
			return new int[] {44379,71};
		} else {
			return new int[] {44379,71};
		}
    }
}
