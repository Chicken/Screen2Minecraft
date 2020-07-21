# Screen2Minecraft

Used for screensharing to minecraft server.

This is combination of a Node.JS server and a spigot plugin.

The node server takes a screencapture and sends it to a minecraft server with a net socket. Then the plugin takes the raw image buffer, turns it into minecraft blocks and places them at world spawn.
