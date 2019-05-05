# Minecraft Hue
A Minecraft plugin that syncs the game with your Hue smart lights.


## Description
This is my first Minecraft plugin. It will sync your Philips Hue smart lights completely with Minecraft, making them react to changes in light, color, and biome in real-time. The lights will also respond to lightning and player damage events. 

## How To Use
The plugin will work on a Spigot server running MC 1.12.2 (this is the only version I’ve tested on so far). However, it’s not doing anything crazy, so I suspect it will work on any modern Minecraft version.

The plugin adds two commands:

**/registerLightIP [Hue Hub Local IP] [Light Group Name]**

**/deregisterLightIP**

You can find the local IP of your Hue Bridge/Hub easily using their mobile app. You do not need to provide a light group, but the plugin will connect to all of your Hue lights by default if you don’t. The group name can have spaces but it currently only accepts one group.

## Download
The plugin jar file can be downloaded [here](https://github.com/DerangedMango/Minecraft-Hue-Plugin/releases).  
Don't know how to install a plugin file? Follow these [instructions](https://bukkit.gamepedia.com/Installing_Plugins).

## Donate
Although I am sharing this with the Minecraft community as a gift, a lot of time and love went into it. Please consider donating if you are enjoying it and are able. [![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.me/derangedmango/5)
