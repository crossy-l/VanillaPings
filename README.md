# Vanilla Pings

A Minecraft mod to easily ping entities and locations. The pings show up using armor stands and have glowing on them. This makes it possible for the mod to be server side only.

It's possible to customize the language used for the ping messages see [custom messages](#custom-messages).

### Usage

The mod is required on the server. But not mandatory on clients. However, using it on the client adds a customizable hotkey for pinging.

#### Commands
* Use "/ping" to ping the block you are currently looking at

## Custom Messages
When a player pings an entity the server sends a message with information about the ping. The language of that message can be configured in the config of the mod found in the "config/vanillapings" folder of your server.

By default only en_us and de_de are available since I only know these languages.

If you want to create your own language file follow these steps. 

* Copy the en_us.json out of the github repo found in the resource folder into the "config/vanillapings/lang" folder. (You might have to create it if it doesn't exist)
* Fill out the json in your language and select the language in the config lang field. E.g. if your file is named fr_fr.json you have to specify fr_fr as the lang in the vanillapings.properties config file.
