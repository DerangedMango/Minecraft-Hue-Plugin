package io.github.derangedmango.lightplugin;

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class LightPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		generateDataFile();
		
		TaskList list = new TaskList();
		new EventListener(this, list);
		
		this.getCommand("registerLightIP").setExecutor(new LightCommandExecutor(this, list));
		this.getCommand("deregisterLightIP").setExecutor(new LightCommandExecutor(this, list));
	}
	
	private void generateDataFile() {
		File df = getDataFolder();
		File file = new File(df, "player_data.txt");
		
		if (!df.exists()) { df.mkdir(); }
		
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
