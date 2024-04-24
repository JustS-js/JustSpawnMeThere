package net.just_s.mixin;

import com.mojang.authlib.GameProfile;
import net.just_s.JSMT;
import net.just_s.util.Shape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.just_s.util.SpawnLocator;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {
    @Shadow @Final public MinecraftServer server;

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow protected abstract int calculateSpawnOffsetMultiplier(int horizontalSpawnArea);

    @Unique private float respawnAngle;

    @Inject(method = "moveToSpawn", at = @At("HEAD"), cancellable = true)
    private void jsmt$moveToSpawn(ServerWorld world, CallbackInfo ci) {
        respawnAngle = world.getSpawnAngle();

        // If spawn region shape is not modified - skip custom logic altogether
        if (world.getGameRules().get(JSMT.SPAWN_SHAPE).get() == Shape.VANILLA) return;

        BlockPos worldSpawnPos = world.getSpawnPos();
        int radius = Math.max(0, server.getSpawnRadius(world));
        // if spawn radius is 0 or world is not Overworld or Server standard gamemode is Adventure
        // We should place player at exact location
        if (
                radius == 0 ||
                !world.getDimension().hasSkyLight() ||
                world.getServer().getSaveProperties().getGameMode() == GameMode.ADVENTURE
        ) {
            this.refreshPositionAndAngles(worldSpawnPos, respawnAngle, 0.0F);

            if (!world.isSpaceEmpty(this)) {
                JSMT.LOGGER.warn("Could not respawn player on exact location.");
            }

            while(!world.isSpaceEmpty(this) && this.getY() < (double)(world.getTopY() - 1)) {
                this.setPosition(this.getX(), this.getY() + 1.0D, this.getZ());
            }
            ci.cancel();
            return;
        }

        // We have to account for worldborder radius
        // (we do not want our players to suffocate or get stuck after respawning)
        int worldBorderRadius = MathHelper.floor(
                world.getWorldBorder().getDistanceInsideBorder(
                        worldSpawnPos.getX(),
                        worldSpawnPos.getZ()
                )
        );
        if (worldBorderRadius < radius) {
            radius = worldBorderRadius;
        }
        if (worldBorderRadius <= 1) {
            radius = 1;
        }

        // Initialize calculation variables
        // i actually just copied them from original code
        long diameter = (radius * 2L + 1);
        long m = diameter * diameter;
        int area = m > 2147483647L ? 2147483647 : (int) m;
        int n = calculateSpawnOffsetMultiplier(area);
        int shift = Random.create().nextInt(area);

        // Iterating through a "spawn square" as if it is an array of "points".
        // We shift randomly from the start of the array and just iterate
        // through area until we find good spawn location.
        for (int point = 0; point < area; ++point) {
            int indexInsideArea = (shift + n * point) % area;
            int x = indexInsideArea % (radius * 2 + 1);
            int z = indexInsideArea / (radius * 2 + 1);

            // Either returns safe spawn position or null
            BlockPos spawnBlockPos = SpawnLocator.findOverworldSpawn(
                    world,
                    worldSpawnPos.getX() + x - radius,
                    worldSpawnPos.getZ() + z - radius,
                    world.getGameRules().get(JSMT.SPAWN_SHAPE).get()
            );
            // If safe position found - spawn player and ci.cancel() further code
            if (spawnBlockPos != null) {
                this.refreshPositionAndAngles(spawnBlockPos, respawnAngle, 0.0F);
                if (world.isSpaceEmpty(this)) {
                    ci.cancel();
                    return;
                }
            }
        }

        // If we could not find place to respawn, let vanilla code do that for us.
        JSMT.LOGGER.warn(
                "Could not respawn player in specified area [SHAPE={}; CENTER=({}); RADIUS={}].",
                world.getGameRules().get(JSMT.SPAWN_SHAPE).get().name(),
                worldSpawnPos.toShortString(),
                server.getSpawnRadius(world)
        );
    }

    @ModifyArg(method = "moveToSpawn", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;refreshPositionAndAngles(Lnet/minecraft/util/math/BlockPos;FF)V"),
            index = 1
    )
    private float jsmt$modifySpawnAngle(float par2) {
        return respawnAngle;
    }
}
