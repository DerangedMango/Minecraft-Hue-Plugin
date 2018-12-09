package io.github.derangedmango.minecrafthueplugin;

public class BlockConfig {
	private String name;
	private String biome;
	private int hue;
	private int sat;

	public BlockConfig(String name, String biome, int hue, int sat) {
		this.name = name;
		this.biome = biome;
		this.hue = hue;
		this.sat = sat;
	}

	public String getName() { return name; }
	public String getBiome() { return biome; }
	public int getHue() { return hue; }
	public int getSat() { return sat; }
    public String toString() { return "Name: " + name + ", Biome: " + biome + ", Hue: " + hue + ", Sat: " + sat; }
}
