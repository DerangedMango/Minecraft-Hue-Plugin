package io.github.derangedmango.minecrafthueplugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftHuePlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		generateDataFiles();
		
		File config = new File(getDataFolder(), "config.txt");
		BlockConfig[] blockConfigArr;
		int entryCounter = 0;

		try(Scanner scanner = new Scanner(config)) {
			while(scanner.hasNextLine()) {
				scanner.nextLine();
				entryCounter++;
			}

			entryCounter -= 3;
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}

		int runRate = 4;
		int colorRate = 7;
		int normalRate = 3;

		blockConfigArr = new BlockConfig[entryCounter];
		int[] alphaArr = new int[26];
		
		for(int i = 0; i < alphaArr.length; i++) {
			alphaArr[i] = blockConfigArr.length;
		}

		try(Scanner scanner = new Scanner(config)) {
			int counter = 0;
			int index = 0;
			int lastChar = -1;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if(counter > 2) {
					String name = line.substring(0, line.indexOf("#")).toUpperCase();
					String biome = line.substring(line.indexOf("#") + 1, line.indexOf(":")).toUpperCase();
					int hue = Integer.valueOf(line.substring(line.indexOf(":") + 1, line.indexOf(",")));
					int sat = Integer.valueOf(line.substring(line.indexOf(",") + 1));

					int currChar = line.toUpperCase().charAt(0);
					currChar -= 65;

					if(currChar != lastChar) alphaArr[currChar] = index;

					blockConfigArr[index++] = new BlockConfig(name, biome, hue, sat);
					lastChar = currChar;
				} else if(counter == 0) {
					runRate = Integer.valueOf(line.substring(line.indexOf(":") + 1));
					counter++;
				} else if(counter == 1) {
					colorRate = Integer.valueOf(line.substring(line.indexOf(":") + 1));
					counter++;
				} else if(counter == 2) {
					normalRate = Integer.valueOf(line.substring(line.indexOf(":") + 1));
					counter++;
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
		
		TaskList list = new TaskList();
		new EventListener(this, list, runRate, colorRate, normalRate, blockConfigArr, alphaArr);
		
		this.getCommand("registerLightIP").setExecutor(new LightCommandExecutor(this, list, runRate, colorRate, normalRate, blockConfigArr, alphaArr));
		this.getCommand("deregisterLightIP").setExecutor(new LightCommandExecutor(this, list, runRate, colorRate, normalRate, blockConfigArr, alphaArr));
	}
	
	private void generateDataFiles() {
		File df = getDataFolder();
		File file = new File(df, "player_data.txt");
		File config = new File(df, "config.txt");
		
		if (!df.exists()) { df.mkdir(); }
		
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		if(!config.exists()) {
			try {
				config.createNewFile();

				try(FileWriter fw = new FileWriter(config.getPath(), false);
					BufferedWriter bw = new BufferedWriter(fw);
			    	PrintWriter out = new PrintWriter(bw)) {

					out.println("Run Rate:4");
					out.println("Color Transition Rate:7");
					out.println("Normal Transition Rate:3");
					out.println("BLUE_WOOL#DEFAULT:46014,254");
					out.println("BROWN_WOOL#DEFAULT:7194,231");
					out.println("CYAN_WOOL#DEFAULT:41385,233");
					out.println("DIRT#BADLANDS:558,69");
					out.println("DIRT#MESA:558,69");
					out.println("DIRT#DEFAULT:5224,56");
					out.println("GRASS#EXTREME_HILLS:35017,124");
					out.println("GRASS#ROOFED_FOREST:26986,103");
					out.println("GRASS#TAIGA_HILLS:37580,149");
					out.println("GRASS#SWAMPLAND:15762,253");
					out.println("GRASS#JUNGLE:26191,164");
					out.println("GRASS#BADLANDS:33664,32");
					out.println("GRASS#MESA:33664,32");
					out.println("GRASS#DEFAULT:30539,85");
					out.println("GREEN_WOOL#DEFAULT:20252,202");
					out.println("HARDENED_CLAY#BADLANDS:558,69");
					out.println("HARDENED_CLAY#MESA:558,69");
					out.println("HARDENED_CLAY#DEFAULT:44379,71");
					out.println("LAVA#DEFAULT:2732,217");
					out.println("LIGHT_BLUE_WOOL#DEFAULT:42733,223");
					out.println("LIME_WOOL#DEFAULT:16993,246");
					out.println("MAGENTA_WOOL#DEFAULT:50685,223");
					out.println("MYCEL#DEFAULT:46711,143");
					out.println("NETHERRACK#DEFAULT:65295,254");
					out.println("ORANGE_WOOL#DEFAULT:6375,254");
					out.println("PINK_WOOL#DEFAULT:53899,149");
					out.println("PURPLE_WOOL#DEFAULT:47509,214");
					out.println("RED_WOOL#DEFAULT:452,203");
					out.println("SAND#BADLANDS:558,69");
					out.println("SAND#MESA:558,69");
					out.println("SAND#DEFAULT:43307,33");
					out.println("SNOW#DEFAULT:41513,108");
					out.println("STAINED_CLAY#BADLANDS:558,69");
					out.println("STAINED_CLAY#MESA:558,69");
					out.println("STAINED_CLAY#DEFAULT:44379,71");
					out.println("STONE#DEFAULT:44559,129");
					out.println("WATER#DEFAULT:45016,253");
					out.println("WHITE_WOOL#DEFAULT:41200,96");
					out.println("YELLOW_WOOL#DEFAULT:10782,254");

				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
