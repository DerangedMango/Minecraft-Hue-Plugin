package io.github.derangedmango.lightplugin;

import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

public class EventListener implements Listener {
	private final LightPlugin plugin;
	private final int runRate;
	private final int colorRate;
	private final int normalRate;
	private final BlockConfig[] blockConfigArr;
	private final int[] alphaArr;
	private TaskList list;

    public EventListener(LightPlugin plugin, TaskList list, int r, int c, int n, BlockConfig[] bc, int[] a) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.list = list;
        runRate = Math.max(r, 2);
        colorRate = c;
        normalRate = n;
        blockConfigArr = bc;
        alphaArr = a;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    	DimLights task = new DimLights(plugin, event.getPlayer(), colorRate, normalRate, blockConfigArr, alphaArr);
    	task.runTaskTimer(plugin, 0, runRate);
        list.add(task);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
    	list.cancel(event.getPlayer().getName());
    	//list.purge();
    }
    
    @EventHandler
    public void onLightning(LightningStrikeEvent event) {
    	String[][] conArr = list.getAllConInfo();
    	
    	if(conArr[0][0] != null) {
    		list.pauseAll();
        	
        	for(int i = 0; i < conArr.length; i++) {
        		if(conArr[i][0] != null && !conArr[i][2].equals("$")) {
        			LocalConnection conn = new LocalConnection(plugin, null, conArr[i][0], conArr[i][1], conArr[i][2]);
        			conn.dim(254, 44773, 9, 0);
        			
        			try {
    					TimeUnit.MILLISECONDS.sleep(Long.valueOf(20));
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
        			
        			conn.dim(0, 44773, 9, 0);
        			
        			try {
    					TimeUnit.MILLISECONDS.sleep(Long.valueOf(20));
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
        			
        			conn.dim(254, 44773, 9, 0);
        			conn.dim(0, 44773, 9, 1);
        		}
        	}
        	
        	list.resumeAll();
    	}
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
    	Entity entity = event.getEntity();
    	
    	if(entity instanceof Player) {
    		Player player = (Player) entity;
    		String name = player.getName();
    		
        	String[] conInfo = list.getConInfo(name);
        	
        	if(conInfo[0] != null && !conInfo[2].equals("$")) {
        		list.pause(name);
        		
        		LocalConnection conn = new LocalConnection(plugin, null, conInfo[0], conInfo[1], conInfo[2]);
            	
            	conn.dim(254, 60088, 123, 0);
    			
    			try {
    				TimeUnit.MILLISECONDS.sleep(Long.valueOf(125));
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
            	
            	list.resume(name);
        	}
    	}
    }
}
