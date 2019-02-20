package io.github.derangedmango.minecrafthueplugin;

import java.util.concurrent.TimeUnit;
import java.util.Random;

public class FireThread extends Thread {
	private LocalConnection con;
	private volatile boolean active;
	
	public FireThread(LocalConnection c, boolean a) {
		con = c;
		active = a;
	}
	
	public void deactivate() { active = false; }
	
	public void run() {
		Random ran = new Random();
		int[] breathe = new int[] {157, 172, 187, 202, 217, 232};
		int[] hues = new int[] {7278, 7147, 4046, 8669, 5090};
		int[] sats = new int[] {254, 217, 225, 254, 215};
		int[] bris = new int[] {247, 247, 247, 247, 148};
		int i = -1;
		
		while(active) {
			if(i == -1) {
				if(active) con.dim(247, 5349, 254, 10);
				
				try {
					TimeUnit.MILLISECONDS.sleep(Long.valueOf(1500));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				if(active) con.dim(247, 5349, 254, 3);
				
				try {
					TimeUnit.MILLISECONDS.sleep(Long.valueOf(290));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			
			for(int j = breathe.length-1; j >= 0; j--) {
				i = ran.nextInt(100);
				
				if(i < 15) {
					i = ran.nextInt(hues.length);
					
					if(active) {
						con.dim(bris[i], hues[i], sats[i], 1);
						
						try {
							TimeUnit.MILLISECONDS.sleep(Long.valueOf(90));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				if(active) con.dim(breathe[j], 5349, 254, 3);
				
				try {
					TimeUnit.MILLISECONDS.sleep(Long.valueOf(290));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			for(int j = 0; j < breathe.length; j++) {
				i = ran.nextInt(100);
				
				if(i < 15) {
					i = ran.nextInt(hues.length);
					
					if(active) {
						con.dim(bris[i], hues[i], sats[i], 1);
						
						try {
							TimeUnit.MILLISECONDS.sleep(Long.valueOf(90));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				if(active) con.dim(breathe[j], 5349, 254, 3);
				
				try {
					TimeUnit.MILLISECONDS.sleep(Long.valueOf(290));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
