# 1.3.2-1.21.5-fabric - 01/06/25

* Worlds generated from 1.2.x can now be loaded.

# 1.3.1.-1.21.5-fabric - 30/05/25

* Fix bug where world would corrupt after saving.

# 1.3.0-1.21.5-fabric - 28/05/05

* Added support for modded dimensions.

# 1.2.2-1.21.5-fabric - 25/05/25

* Minor changes which should improve compatibility with other mods.

# 1.2.1-1.21.5-fabric - 13/05/25

* Correct title of **Select Biomes** menu: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/9)
* Remove debug print because I'm very silly and left it in before release.
* Fixed bug where item icon background textures were not rendering.

# 1.2.0-1.21.5-fabric - 11/05/25

* Implemented checkerboard biome generation with
  customization: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/7)
* Improve autocomplete menu behaviour.
* Fixed bug where chunk generator would attempt to place blocks too high and too low in The End and The
  Nether: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/6)
* Fixed bug where not all blocks that need tile entities have them after
  generation: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/8)

# 1.1.1-1.21.5-fabric - 06/05/25

* Fixed bug where all dimensions would have the biome `minecraft:the_void` if you click the "Done" on the SkyGrid
  customization menu.

# 1.1.0-1.21.5-fabric - 05/05/25

* Adding a new block, item or entity to the lists in their respective menus will auto-scroll you to the bottom.
* Added the ability to click on and off the text box to show and hide the autocomplete suggestions in the **Customize
  Blocks**, **Customize Spawners** and **Customize Loot** menus.
* Increased all block and loot weightings while maintaining their individual chances.
* Items in the **Customize Loot** menu will now have an initial weight of **50**.
* Change the biome for The Overworld from `minecraft:the_void` to `minecraft:plains`  so mobs will naturally spawn.
* Change the biome for The Nether from `minecraft:the_void` to `minecraft:nether_wastes` so mobs will naturally spawn.
* Fixed bug where mobs would not naturally spawn in overworld or nether
  dimensions: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/5)
* Fixed missing period in mod description.
* Fixed grammatical errors in README.

# 1.0.1-1.21.5-fabric - 14/04/25

* Fixed bug where cancel button would preserve changes made to config

# 1.0.0-1.21.5-fabric - 07/04/25

* Ported to 1.21.5
* Added 1.21.5 blocks to the **Modern** preset

# 1.0.0-1.21.4-fabric - 07/04/25 - Initial Release

* Added SkyGrid world preset with:
    * Customizable block weightings
    * Customizable chest loot
    * Customizable spawner entities
    * User savable custom presets
* Added 'Classic' Preset
* Added 'Modern' Preset
* Added 'Boom Preset'