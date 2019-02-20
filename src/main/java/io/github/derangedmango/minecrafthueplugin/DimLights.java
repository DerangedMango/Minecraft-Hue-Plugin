package io.github.derangedmango.minecrafthueplugin;

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
	private boolean nearFire;
	private FireThread fireThread;
	private int buttonPromptCounter;
	private BlockConfig[] blockConfigArr;
	private int[] alphaArr;

    public DimLights(JavaPlugin plugin, Player player, int c, int n, BlockConfig[] bc, int[] a) {
        this.plugin = plugin;
        this.player = player;
        con = null;
        lastLightLevel = -1;
        lastHue = -1;
        lastSat = -1;
        paused = false;
        conInfo = null;
        normalRate = n;
        colorRate = c;
        inTheEnd = false;
        nearFire = false;
        fireThread = null;
        buttonPromptCounter = 0;
        blockConfigArr = bc;
        alphaArr = a;
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
    
    public void deactivateFire() {
    	if(fireThread != null) {
    		fireThread.deactivate();
    		fireThread = null;
    		lastHue = -1;
    	}
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
    			
    			if(nearFire) {
    				if(fireThread == null) {
    					fireThread = new FireThread(con, true);
    					fireThread.start();
    				}
    			} else {
    				this.deactivateFire();
    				
	    			if(lightLevel != lastLightLevel || color[0] != lastHue || color[1] != lastSat) {
	    				if(color[0] != lastHue || color[1] != lastSat) {
	    					con.dim(hueLevels[lightLevel], color[0], color[1], colorRate);
	    				} else {
	    					con.dim(hueLevels[lightLevel], color[0], color[1], normalRate);
	    				}
	    				
	    				lastLightLevel = lightLevel;
	    				lastHue = color[0];
	    				lastSat = color[1];
	    			}
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
					
					String[] arr = line.substring(line.indexOf(":") + 1).split(",");
					result[0] = arr[0];
					result[1] = arr[1];
					result[2] = arr[2];
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
		
		if(world.getEnvironment().toString().equalsIgnoreCase("THE_END")) {
    		inTheEnd = true;
    		return new int[] {47416,214};
    	} else {
			for(int x = px - 2; x <= px + 2; x++) {
				for(int y = py - 1; y <= py + 1; y++) {
					for(int z = pz - 2; z <= pz + 2; z++) {
						loc = new Location(world, x, y, z);
						blockName = consolidateBlockNames(loc.getBlock());
						
						if(blockName.equalsIgnoreCase("FIRE")) {
							nearFire = true;
							return new int[] {3332, 254};
						}
						
						nearFire = false;
	
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
		String domBlock = "VOID";

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
    	int alphaIndex = domBlock.toUpperCase().charAt(0);
    	alphaIndex -= 65;

    	Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

    	for(int i = alphaArr[alphaIndex]; i < blockConfigArr.length; i++) {
    		if(domBlock.equalsIgnoreCase(blockConfigArr[i].getName())) {
    			if(blockConfigArr[i].getBiome().equalsIgnoreCase(biome.toString())) {
    				// plugin.getLogger().info("Found perfect match: " + domBlock + ", " + biome.toString());
    				return new int[] {blockConfigArr[i].getHue(), blockConfigArr[i].getSat()};
    			} else if(blockConfigArr[i].getBiome().equalsIgnoreCase("DEFAULT")) {
    				// plugin.getLogger().info("Found default match: " + domBlock + ", default setting");
    				return new int[] {blockConfigArr[i].getHue(), blockConfigArr[i].getSat()};
    			}
    		}
    	}

    	// plugin.getLogger().info("No config set up for this block");
    	return new int[] {44379, 71};
    }
}
