package net.just_s.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.JSMT;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {
    @Shadow @Final public MinecraftServer server;

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow protected abstract int calculateSpawnOffsetMultiplier(int horizontalSpawnArea);

    @Inject(method = "moveToSpawn", at = @At("HEAD"), cancellable = true)
    private void jsmt$moveToSpawn(ServerWorld world, CallbackInfo ci) {
        BlockPos worldSpawnPos = world.getSpawnPos();
        int radius = Math.max(0, server.getSpawnRadius(world));
        // if spawn radius is 0 or world is not Overworld or Server standart gamemode is Adventure - place player at exact location
        if (
                radius == 0 ||
                !world.getDimension().hasSkyLight() ||
                world.getServer().getSaveProperties().getGameMode() == GameMode.ADVENTURE
        ) {
            this.refreshPositionAndAngles(worldSpawnPos, 0.0F, 0.0F);

            if (!world.isSpaceEmpty(this)) {
                JSMT.LOGGER.warn("Could not respawn player on exact location.");
            }

            while(!world.isSpaceEmpty(this) && this.getY() < (double)(world.getTopY() - 1)) {
                this.setPosition(this.getX(), this.getY() + 1.0D, this.getZ());
            }
            ci.cancel();
        }

        // check if we are inside worldborder
        int worldBorderRadius = MathHelper.floor(
                world.getWorldBorder().getDistanceInsideBorder(
                        (double) worldSpawnPos.getX(),
                        (double) worldSpawnPos.getZ()
                )
        );
        if (worldBorderRadius < radius) {
            radius = worldBorderRadius;
        }
        if (worldBorderRadius <= 1) {
            radius = 1;
        }

        // initialize calculation variables
        long diameter = (radius * 2L + 1);
        long m = diameter * diameter;
        int area = m > 2147483647L ? 2147483647 : (int) m;
        int n = calculateSpawnOffsetMultiplier(area);
        int shift = Random.create().nextInt(area);

        for (int point = 0; point < area; ++point) {
            int indexInsideArea = (shift + n * point) % area;
            int x = indexInsideArea % (radius * 2 + 1);
            int z = indexInsideArea / (radius * 2 + 1);
            BlockPos spawnBlockPos = SpawnLocator.findOverworldSpawn(
                    world,
                    worldSpawnPos.getX() + x - radius,
                    worldSpawnPos.getZ() + z - radius,
                    JSMT.getConfigShape()
            );
            if (spawnBlockPos != null) {
                this.refreshPositionAndAngles(spawnBlockPos, 0.0F, 0.0F);
                if (world.isSpaceEmpty(this)) {
                    ci.cancel();
                }
            }
        }

        // if we could not find place to respawn, let vanilla code do that for us.
        JSMT.LOGGER.warn("Could not respawn player in specified area.");
    }
}
