# 1.3.0-1.21.1-fabric - 29/05/25

* Added support for modded dimensions.

# 1.2.2-1.21.1-fabric 25/05/25

* Minor changes which should improve compatibility with other mods.

# 1.2.1-1.21.1-fabric - 13/05/25

* Correct title of **Select Biomes** menu: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/9)

# 1.2.0-1.21.1-fabric - 11/05/25

* Implemented checkerboard biome generation with
  customization: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/7)
* Improve autocomplete menu behaviour.
* Fixed bug where chunk generator would attempt to place blocks too high and too low in The End and The
  Nether: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/6)
* Fixed bug where not all blocks that need tile entities have them after
  generation: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/8)

# 1.1.0-1.21.1-fabric - 06/05/25

* Adding a new block, item or entity to the lists in their respective menus will auto-scroll you to the bottom.
* Added the ability to click on and off the text box to show and hide the autocomplete suggestions in the **Customize
  Blocks**, **Customize Spawners** and **Customize Loot** menus.
* Increased all block and loot weightings while maintaining their individual chances.
* Items in the **Customize Loot** menu will now have an initial weight of **50**.
* Change the biome for The Overworld from `minecraft:the_void` to `minecraft:plains`  so mobs will naturally spawn.
* Change the biome for The Nether from `minecraft:the_void` to `minecraft:nether_wastes` so mobs will naturally spawn.
* Fixed bug where all dimensions would have the biome `minecraft:the_void` if you click the "Done" on the SkyGrid
  customization menu.
* Fixed bug where mobs would not naturally spawn in overworld or nether
  dimensions: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/5)
* Fixed missing period in mod description.
* Fixed grammatical errors in README.

# 1.0.1-1.21.1-fabric - 14/04/25

* Backported from **1.0.0-1.21.4** to **1.0.0-1.21.1**
* Fixed bug where cancel button would preserve changes made to config

# 1.0.0-1.21.4-fabric - 07/04/25 - Initial Release

* Added SkyGrid world preset with:
    * Customizable block weightings
    * Customizable chest loot
    * Customizable spawner entities
    * User savable custom presets
* Added 'Classic' Preset
* Added 'Modern' Preset
* Added 'Boom Preset'