# Just Spawn Me There - Mod for FabricMC

## About

This mod rewrites logic behind vanilla respawning system, so players would not spawn on top of buildings.

## How to Use
To set world spawn for everyone, use

```/setworldspawn [pos] <angle>```

Where:
* `pos` - required argument of 3 coordinates. This would be the center of spawn region.
* `angle` - not required float argument. Determines in which direction you will be looking upon respawn.

To modify spawn radius, use

```/gamerule spawnRadius [value]```

Where:
* `value` - required argument of non-negative integer. If `value` is set to `0`, players will respawn at exact location specified by "/setworldspawn" command.

And finally, to select the shape of your spawn region, use

```/gamerule spawnShape [value]```
* `value` - required string argument with the specified shape. You can choose between `CUBE`, `BALL` and `VANILLA` - this would determine the shape of spawn region where players might respawn. Initially it is set to `VANILLA` - that means, respawn system will work as it does in vanilla Minecraft.

If server could not find a safe respawn location in specified spawn region, it will use vanilla behaviour for this attempt.
Unsuccessful attempts will be logged in the server console.
## License

This mod is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
