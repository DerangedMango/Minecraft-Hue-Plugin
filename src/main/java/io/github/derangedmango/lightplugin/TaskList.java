package io.github.derangedmango.lightplugin;

import java.util.ArrayList;

public class TaskList {
	private ArrayList<DimLights> list;
	
	public TaskList() {
		list = new ArrayList<DimLights>();
	}
	
	public void add(DimLights task) {
		list.add(task);
	}
	
	public void pauseAll() {
		for(DimLights temp : list) {
			temp.pause();
		}
	}
	
	public void resumeAll() {
		for(DimLights temp : list) {
			temp.resume();
		}
	}
	
	public void pause(String name) {
		for(DimLights temp : list) {
			if(temp.getPlayerName().equals(name)) {
				temp.pause();
			}
		}
	}
	
	public void resume(String name) {
		for(DimLights temp : list) {
			if(temp.getPlayerName().equals(name)) {
				temp.resume();
			}
		}
	}
	
	public String[][] getAllConInfo() {
		String[][] out = new String[list.size()][2];
		int counter = 0;
		
		for(DimLights temp : list) {
			if(!temp.isCancelled()) out[counter++] = temp.getConInfo();
		}
		
		return out;
	}
	
	public String[] getConInfo(String name) {
		for(DimLights temp : list) {
			if(temp.getPlayerName().equals(name)) {
				return temp.getConInfo();
			}
		}
		
		return null;
	}
	
	public void cancel(String name) {
		for(DimLights temp : list) {
			if(temp.getPlayerName().equals(name)) {
				temp.cancel();
			}
		}
	}
	
	public void purge() {
		for(DimLights temp : list) {
			if(temp.isCancelled()) {
				list.remove(temp);
			}
		}
	}
}
