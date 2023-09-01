# Vanilla Pings

A Minecraft mod to easily ping entities and locations. The pings show up using armor stands and have **glowing** on them. This makes it possible for the mod to be **server-side** only.

It's possible to customize the language used for in messages see [custom messages](#custom-messages).

The mod uses the game rules `sendCommandFeedback` and `logAdminCommands` for command feedback and logging like vanilla commands. Right now only on the `/vanillapings reload` command.

## Usage
The mod is required on the server. But not mandatory for clients. However, using it on the client adds a customizable hotkey for pinging. **Admin commands** behave like vanilla operator commands and **can only be run by operators**.

#### Public commands *(can be run by everyone)*
* `/ping` to ping the block or any entity in front of you
#### Admin commands *(only for operators, work from the console)*
* `/vanillapings reload` to reload all [settings](#settings-file).
* `/vanillapings removeOld` to remove pings that didn't disappear on their own. Also see [ping-remove-old (settings)](#settings-file).

## Configuration
### Settings file
For general settings use the **vanillapings.properties** file found in the ``config/vanillapings`` folder of your server or minecraft folder. Settings should be configured on the **server side** or on your **local client** if you play single player or share your world via open to lan.
```properties
#vanillapings.properties
#Fri Sep 01 17:59:44 CEST 2023
lang=en_us                 # Language used for ping chat messages
ping-cooldown=5            # Server side cooldown for pings sent by players in game ticks. Players ping speed using the hotkey is limited to 5 ticks per ping so that's also the default. (Note: "/ping" can be used to ping faster if the cooldown is set lower)
ping-max-range=500.0       # Max range a player can ping (Note: horizontal range is still limited to 256 due to that also being the max render distance)
ping-item-count=true       # Show the count of items near an item ping
ping-item-count-range=1.0  # Range for counting items near an item ping
ping-remove-old=true       # If old pings should be removed automatically every 20s. Old pings are created when a ping hasn't deleted itself due to the world unloading.
```
*You may delete the file to regenerate the default configuration upon a restart. A reload only reconstructs current values into the file (This behaviour will likely change in future updates).*

### Custom messages
When a player pings an entity the server sends a message with information about the ping in the chat. The language of that message (and other messages) can be configured in the [vanillapings.properties](#settings-file) file.
*By default, only en_us and de_de are available as I only know these languages.*

#### Creating your own language file
* Copy the **en_us.json** out of the GitHub repo found in the resource folder into your local ``config/vanillapings/lang`` folder. *You might have to create it if it doesn't exist.*
* Fill out the json in your language and select the language in the config **lang** field. E.g. if your file is named **fr_fr.json** you have to specify **fr_fr** as the **lang** in the **vanillapings.properties** config file.
* Restart your server or use ``/vanillapings reload`` to reload the configuration.

*In spectator mode you can see the invisible armor stand wearing the highlighted block (in case your curious).*