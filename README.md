# Vanilla Pings

A Minecraft mod to easily ping entities and locations. The pings show up using armor stands and have glowing on them. This makes it possible for the mod to be server side only.

It's possible to customize the language used for the ping messages see [custom messages](#custom-messages).

The mod uses the game rules `sendCommandFeedback` and `logAdminCommands` for command feedback and logging like vanilla commands. Right now only on the `/vanillapings reload` command.

The mod is still in early development so please post any issues you may have on the issues page.

### Usage
The mod is required on the server. But not mandatory on clients. However, using it on the client adds a customizable hotkey for pinging.

#### Commands
* ``/ping`` to ping the block you are currently looking at
* ``/vanillapings reload`` to reload all [settings](#settings-file). *Gets broadcast to other admins and logged in the console.*

## Configuration
### Settings file
For general settings there is the **vanillapings.properties** file found in the ``config/vanillapings`` folder of your server or minecraft folder. Settings should be configured on the server side or for singe player on your local client.
```properties
#vanillapings.properties
#Mon Jun 19 20:34:38 CEST 2023
lang=en_us                 # language used for ping chat messages
ping-cooldown=5            # server side cooldown for pings sent by players in game ticks. Players ping speed using the hotkey is limited to 5 ticks per ping so that's also the default. (Note: "/ping" can be used to ping faster if the cooldown is set lower)
ping-max-range=500.0       # max range a player can ping (Note: horizontal range is still limited to 256 due to that also being the max render distance)
ping-item-count=true       # show the count of items near an item ping
ping-item-count-range=1.0  # range for counting items near an item ping
```
*You may delete the file to regenerate the default configuration upon a restart. A reload only reconstructs current values into the file (I will propably change this later).*

### Custom messages
When a player pings an entity the server sends a message with information about the ping. The language of that message can be configured in the [vanillapings.properties](#settings-file) file.
*By default, only **en_us** and **de_de** are available since I only know these languages.*

### Creating your own language file
* Copy the **en_us.json** out of the GitHub repo found in the resource folder into your local ``config/vanillapings/lang`` folder. *You might have to create it if it doesn't exist.*
* Fill out the json in your language and select the language in the config lang field. E.g. if your file is named **fr_fr.json** you have to specify **fr_fr** as the **lang** in the **vanillapings.properties** config file.
* Restart your server or use ``/vanillapings reload`` to reload the configuration.


## Troubleshooting
### Ping signals sticking around indefinitely
*This is no longer relevant in versions >= 1.4 since then old pings are automatically removed.*

Normally all pings despawn after some time. In some cases however for example when the server is extremely laggy or crashes while pings are present the automatic ping deletion might not work. You might see floating pings that can't be interacted with in the world and wonder how to remove them.
Since they're just armor stands with a glass head one can delete them with a simple command:

``/kill @e[type=minecraft:armor_stand, distance=0..5]``

This will **delete all armor stands within a 5 block radius** so just get close to them or modify the command to remove them.

Limiting the distance is recommended since ``/kill @e[type=minecraft:armor_stand]`` will **delete all** armor stands from your current world. **You propably don't want this!**

*In spectator mode you can actually see the invisible armor stand wearing the highlighted block for yourself.*