# Vanilla Pings

A Minecraft mod to easily ping entities and locations like in most popular shooter games. The pings show up using armor stands and have **glowing** on them. This makes it possible for the mod to be server-side only. Using it on the client will **add a hotkey**.
<img src="https://cdn.modrinth.com/data/P6Y8Vr1q/images/3217cf61ecadc4927ce902033af7a80c5f4c551c.png" alt="drawing" width="700"/>
<img src="https://cdn.modrinth.com/data/P6Y8Vr1q/images/daafdfde862a189de9eec894368119d77f837f33.gif" alt="drawing" width="700"/>

It's possible to customize the language used for messages (see: [custom messages](#custom-messages)).

The mod uses the game rules `sendCommandFeedback` and `logAdminCommands` for command feedback and logging (just like vanilla commands) on all [admin commands](admin-commands-only-for-operators-work-from-the-console).

## Usage
The mod is required on the server. But not mandatory for clients. However, using it on the client adds a customizable hotkey for pinging. **Admin commands** behave like vanilla operator commands and **can only be run by operators**.

### Client-side Hotkey (Optional)
Using it on the client adds a customizable hotkey for pinging.
* **Default Key:** By default, the hotkey to ping is **Z** on US Keyboards.
* **Configure:** Change at any time in the standard Minecraft controls menu (`Options...` > `Controls...` > `Key Binds...`) under the **"Vanilla Pings"** category.


### Commands

#### Public commands *(can be run by everyone)*
* `/ping` to ping the block or any entity in front of you
#### Admin commands *(only for operators, work from the console)*
* `/vanillapings reload` to reload all [settings](#settings-file).
* `/vanillapings removeOld` to remove pings that didn't disappear on their own ([ping-remove-old (settings)](#settings-file)).
* `/vanillapings language <name>` to set your desired language ([lang (settings)](#settings-file) more info [here](#creating-your-own-language-file)).
* `/vanillapings sound (<true|false>)` to disable/enable playing sounds when a ping appears ([ping-sound (settings)](#settings-file)).
* `/vanillapings cooldown <ticks>` to set the cooldown which a player needs to wait for between each ping ([ping-cooldown (settings)](#settings-file)).
* `/vanillapings item <item>` to set the item used for the ping ([ping-item (settings)](#settings-file)).
* `/vanillapings item count <true|false>` to disable/enable displaying the amount of items in a ping ([ping-item-count (settings)](#settings-file)).
* `/vanillapings item count range <value>` to set the range in which items will be counted ([ping-item-count-range (settings)](#settings-file)).
* `/vanillapings glowing (<true|false>)` to disable/enable the glowing effect pings have ([ping-glowing (settings)](#settings-file)).
* `/vanillapings glowing flash (<true|false>)` to disable/enable the flashing effect pings have before disappearing ([ping-glowing-flash (settings)](#settings-file)).
* `/vanillapings range <value>` to set the maximum range a player can ping from their position ([ping-max-range (settings)](#settings-file)).
* `/vanillapings range chat <value>` to set the maximum range a player needs to be near a ping to see chat messages ([ping-chat-message-range (settings)](#settings-file)).
* `/vanillapings range direction <value>` to set the maximum range a player needs to be near a ping to see direction messages in the action bar ([ping-direction-message-range (settings)](#settings-file)).

## Configuration
### Settings file
For general settings use the **vanillapings.properties** file found in the ``config/vanillapings`` folder of your server or minecraft folder. Settings should be configured on the **server side** or on your **local client** if you play single player or share your world via open to lan.
```properties
#vanillapings.properties
#Mon May 13 11:39:38 CEST 2024
lang=en_us                             # Language used for ping chat messages
ping-chat-message-range=160.0          # Range a player needs to be near a ping to see chat messages (-1=no limit, 0=disabled)
ping-cooldown=5                        # Server side cooldown for pings sent by players in game ticks. Players ping speed using the hotkey is limited to 5 ticks per ping so that's also the default. (Note: "/ping" can be used to ping faster if the cooldown is set lower)
ping-direction-message-range=160.0     # Range a player needs to be near a ping to see direction messages in the action bar (-1=no limit, 0=disabled)
ping-glowing=true                      # If pings should be highlighted with glowing.
ping-glowing-flash=true                # Start flashing the ping (only if ping-glowing is enabled) when it's about to disappear.
ping-item=minecraft\:red_stained_glass # The type of item that's used for a ping. (empty=default, nonexistent item=default)
ping-item-count=true                   # Show the count of items near an item ping
ping-item-count-range=1.0              # Range for counting items near an item ping
ping-max-range=500.0                   # Max range a player can ping. (Note: horizontal range is still limited to 256 due to that also being the max render distance) (-1=max-range, 0=disabled)
ping-remove-old=true                   # If old pings should be removed automatically every 20s. Old pings are created when a ping hasn't deleted itself due to the world unloading.
ping-sound=true                        # If a sound should be played when a ping appears
```
*You may delete the file to regenerate the default configuration upon a restart. A reload only reconstructs current values into the file (This behaviour will likely change in future updates).*

### Custom messages
When a player pings an entity the server sends a message with information about the ping in the chat. The language of that message (and other messages) can be configured in the [vanillapings.properties](#settings-file) file.
*By default, only en_us and de_de are available as I only know these languages.*

#### Creating your own language file
* Copy the **en_us.json** out of the GitHub repo found in the resource folder into your local ``config/vanillapings/lang`` folder. *You might have to create it if it doesn't exist.*
* Fill out the json in your language and select the language in the config **lang** field. E.g. if your file is named **fr_fr.json** you have to specify **fr_fr** as the **lang** in the **vanillapings.properties** config file.
* Restart your server or use ``/vanillapings reload`` to reload the configuration.

You may also select your custom language using the new ``/vanillapings language`` command.

*In spectator mode you can see the invisible armor stand wearing the highlighted block (in case you're curious).*
