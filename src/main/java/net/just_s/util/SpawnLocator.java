package net.just_s.util;

import net.just_s.JSMT;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class SpawnLocator {
    @Nullable
    public static BlockPos findOverworldSpawn(ServerWorld world, int x, int z, Shape shape) {
        // Starting on top of our shape
        int i = world.getSpawnPos().getY() + world.getServer().getSpawnRadius(world);
        // If starting point is already too low - skip (please don't respawn people in the void)
        if (i < world.getBottomY()) return null;

        // Simple iteration from top to bottom,
        // We are trying to find a position inside our shape
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int k = i + 1; k >= world.getBottomY(); --k) {
            mutable.set(x, k, z);
            BlockState blockState = world.getBlockState(mutable);
            if (!blockState.getFluidState().isEmpty()) {
                break;
            }

            // Check if:
            // 1. Position is inside the shape
            // 2. Block is full and safe to be spawned on
            if (isInArea(world, mutable.up(), shape) && Block.isFaceFullSquare(blockState.getCollisionShape(world, mutable), Direction.UP)) {
                return mutable.up().toImmutable();
            }
        }

        return null;
    }

    /**
     * Checks whether provided position is inside specified shape with center on world's spawn
     * */
    private static boolean isInArea(ServerWorld world, BlockPos pos, Shape shape) {
        BlockPos center = world.getSpawnPos();
        int radius = world.getServer().getSpawnRadius(world);

        switch (shape) {
            case VANILLA -> {
                return MathHelper.abs(center.getX() - pos.getX()) < radius && MathHelper.abs(center.getZ() - pos.getZ()) < radius;
            }
            case BALL -> {
                return pos.isWithinDistance(center, radius);
            }
            case CUBE ->  {
                Box box = new Box(
                        center.add(radius+1, radius+1, radius+1),
                        center.add(-radius, -radius, -radius)
                );
                return box.contains(pos.toCenterPos());
            }
        }
        return false;
    }
}
