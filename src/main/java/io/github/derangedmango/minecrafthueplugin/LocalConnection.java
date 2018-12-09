package io.github.derangedmango.minecrafthueplugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LocalConnection {
	private int status;
	private final Plugin plugin;
	private Player player;
	private String ip;
	private String group;
	private String username;
	private String[] lightIDs;
	
	public LocalConnection(Plugin plugin, Player player, String ip, String group, String username) {
		this.plugin = plugin;
		this.ip = ip;
		this.group = group;
		this.username = username;
		this.player = player;
		status = 0;
		
		initializeConnection();
	}

	public boolean registerUser() {
		if(status == 2) {
			username = doPost("http://" + ip + "/api");
			
			if(username.contains("\"username\":")) {
				username = username.substring(username.indexOf("username\":\"") + 11, username.lastIndexOf("\""));
				logUsername();
				
				if(group.equals("ALL")) {
					String s = doGet("http://" + ip + "/api/" + username + "/lights");
					int lightCount = (StringUtils.countMatches(s, "{") - 1) / 8;
					
					lightIDs = new String[lightCount];
					
					for(int i = 0; i < lightIDs.length; i++) {
						lightIDs[i] = String.valueOf(i + 1);
					}
				} else {
					String s = doGet("http://" + ip + "/api/" + username + "/groups");
					
					if(s.contains(group)) {
						int startIndex = s.indexOf(group) + group.length();
						String str = s.substring(s.indexOf("[", startIndex) + 1, s.indexOf("]", startIndex));
						lightIDs = str.replace("\"", "").replace(" ", "").split(",");
					}
				}
				
				status = 3;
				return true;
			} else if(username.contains("link button not pressed")) {
				// plugin.getLogger().info("button has not yet been pressed!");
			} else {
				plugin.getLogger().info("something went wrong!");
			}
		}
		
		return false;
	}

	public void dim(int bri, int hue, int sat, int rate) {
		StringBuilder result = new StringBuilder();
		
		for(int i = 0; i < lightIDs.length; i++) {
			try {
				URL url = new URL("http://" + ip + "/api/" + username + "/lights/" + lightIDs[i] + "/state");
			    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    	conn.setRequestMethod("PUT");
		    	conn.setDoOutput(true);
		    	
		    	byte[] out = ("{\"on\":true,\"bri\":" + bri + ",\"hue\":" + hue + ",\"sat\":" + sat + 
		    			",\"transitiontime\":" + rate + "}").getBytes(StandardCharsets.UTF_8);
		    	int length = out.length;
	
		    	conn.setFixedLengthStreamingMode(length);
		    	conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		    	conn.connect();
		    	
		    	try(OutputStream os = conn.getOutputStream()) {
		    	    os.write(out);
		    	}
		    	
		    	BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    	String line;
		    	
		    	while ((line = rd.readLine()) != null) {
			    	result.append(line);
			    }
		    	
		    	// plugin.getLogger().info(result.toString());
	
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initializeConnection() {
		status = 1;
		
		String getResponse = doGet("http://" + ip + "/api/" + username);

		if(getResponse.contains("unauthorized user")) {
			if(doPost("http://" + ip + "/api").contains("link button not pressed")) {
				status = 2;
			}
		} else if(getResponse.contains("\"lights\":")) {
			if(group.equals("ALL")) {
				String s = doGet("http://" + ip + "/api/" + username + "/lights");
				int lightCount = (StringUtils.countMatches(s, "{") - 1) / 8;
				
				lightIDs = new String[lightCount];
				
				for(int i = 0; i < lightIDs.length; i++) {
					lightIDs[i] = String.valueOf(i + 1);
				}
			} else {
				String s = doGet("http://" + ip + "/api/" + username + "/groups");
				
				if(s.contains(group)) {
					int startIndex = s.indexOf(group) + group.length();
					String str = s.substring(s.indexOf("[", startIndex) + 1, s.indexOf("]", startIndex));
					lightIDs = str.replace("\"", "").replace(" ", "").split(",");
				}
			}
				
			status = 3;
		} else {
			status = 4;
		}
	}

	private String doGet(String urlString) {
		StringBuilder result = new StringBuilder();
		
		try {
		    URL url = new URL(urlString);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setConnectTimeout(1500);
		    conn.setRequestMethod("GET");
		    
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    
		    while ((line = rd.readLine()) != null) {
		    	result.append(line);
		    }
		    
		    rd.close();
		    
		    // plugin.getLogger().info(result.toString());
		    
		} catch(SocketTimeoutException e) {
			plugin.getLogger().info("Connection to " + player.getName() + "'s hub timed out, likely bad IP!");
		}  catch(IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}

	private String doPost(String urlString) {
		StringBuilder result = new StringBuilder();

		try {
			URL url = new URL(urlString);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    	conn.setRequestMethod("POST");
	    	conn.setDoOutput(true);
	    	
	    	byte[] out = "{\"devicetype\":\"my_hue_app#mc-lighting\"}".getBytes(StandardCharsets.UTF_8);
	    	int length = out.length;

	    	conn.setFixedLengthStreamingMode(length);
	    	conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	    	conn.connect();
	    	
	    	try(OutputStream os = conn.getOutputStream()) {
	    	    os.write(out);
	    	}
	    	
	    	BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    	String line;
	    	
	    	while ((line = rd.readLine()) != null) {
		    	result.append(line);
		    }
	    	
	    	// plugin.getLogger().info(result.toString());

		} catch(IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}
	
	private void logUsername() {
		File file = new File(plugin.getDataFolder(), "player_data.txt");
		boolean match = false;
		String outString = "";
		
		try(Scanner scanner = new Scanner(file)) {
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if(line.substring(0, line.indexOf(":")).equalsIgnoreCase(player.getName())) {
					match = true;
					outString = outString + line.substring(0, line.indexOf("$")) + username + "~";
				} else {
					outString = outString + line + "~";
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
		
		if(match) {
			try(FileWriter fw = new FileWriter(file.getPath(), false);
				BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw)) {
				
				String seg = "";
				
				for(int x = 0; x < outString.length(); x++) {
					if(String.valueOf(outString.charAt(x)).equals("~")) {
						out.println(seg);
						seg = "";
					} else {
						seg = seg + outString.charAt(x);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String toString() {
		String output;
		
		switch(status) {
			case 0:
				output = "Not Connected";
				break;
			case 1:
				output = "Initializing";
				break;
			case 2:
				output = "Press Button";
				break;
			case 3:
				output = "Ready";
				break;
			case 4:
				output = "Invalid IP";
				break;
			default:
				output = "Not Connected";
				break;
		}
		
		return output;
	}
}
