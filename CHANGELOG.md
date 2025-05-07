* Fixed bug where not all blocks that need tile entities have them after
  generation: [Issue](https://github.com/Spacerulerwill/SkyGrid-Reloaded/issues/8)

# 1.1.0-1.21.1 - 06/05/25

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

# 1.0.1-1.21.1 - 14/04/25

* Backported from **1.0.0-1.21.4** to **1.0.0-1.21.1**
* Fixed bug where cancel button would preserve changes made to config

# 1.0.0-1.21.4 - 07/04/25 - Initial Release

* Added SkyGrid world preset with:
    * Customizable block weightings
    * Customizable chest loot
    * Customizable spawner entities
    * User savable custom presets
* Added 'Classic' Preset
* Added 'Modern' Preset
* Added 'Boom Preset'