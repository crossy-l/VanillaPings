# Vanilla Pings

A Minecraft mod to easily ping entities and locations. The pings show up using armor stands and have glowing on them. This makes it possible for the mod to be server side only.

It's possible to customize the language used for the ping messages see [custom messages](#custom-messages-only-required-on-the-server).

The mod uses the game rules `sendCommandFeedback` and `logAdminCommands` for command feedback and logging like vanilla commands. Right now only on the `/vanillapings reload` command.

The mod is still in early development so please post any issues you may have on the issues page.

### Usage
The mod is required on the server. But not mandatory on clients. However, using it on the client adds a customizable hotkey for pinging.

#### Commands
* ``/ping`` to ping the block you are currently looking at
* ``/vanillapings reload`` to reload all [settings](#settings-file). *Gets broadcasted to other admins and logged in the console.*

## Configuration
### Settings file
For general settings there is the **vanillapings.properties** file found in the ``config/vanillapings`` folder of your server or minecraft folder. Settings should be configured on the server side or for singeplayer on your local client.
```properties
#vanillapings.properties
#Mon Jun 19 20:34:38 CEST 2023
lang=en_us             # Specify the language used for ping chat messages
ping-max-range=500.0   # Specify the max range a player can ping
```
*You may delete the file to regenerate the default configuration upon a restart. A reload only reconstructs current values into the file (I will propably change this later).*

### Custom messages
When a player pings an entity the server sends a message with information about the ping. The language of that message can be configured in the [vanillapings.properties](#settings-file) file.
*By default only **en_us** and **de_de** are available since I only know these languages.*

### Creating your own language file
* Copy the **en_us.json** out of the github repo found in the resource folder into your local ``config/vanillapings/lang`` folder. *You might have to create it if it doesn't exist.*
* Fill out the json in your language and select the language in the config lang field. E.g. if your file is named **fr_fr.json** you have to specify **fr_fr** as the **lang** in the **vanillapings.properties** config file.
* Restart your server or use ``/vanillapings reload`` to reload the configuration.


## Troubleshooting
### Ping signals sticking around indefinitely
Normally all pings despawn after some time. In some cases however for example when the server is extremely laggy or crashes while pings are present the automatic ping deletion might not work. You might see floating pings that can't be interacted with in the world and wonder how to remove them.
Since they're just armor stands with a glass head one can delete them with a simple command:

``/kill @e[type=minecraft:armor_stand, distance=0..5]``

This will **delete all armor stands within a 5 block radius** so just get close to them or modify the command to remove them.

Limitting the distance is recommend since ``/kill @e[type=minecraft:armor_stand]`` will **delete all** armor stands from your current world. **You propably don't want this!**

*In spectator mode you can actually see the invisble armor stand wearing the highlighted block for yourself.*