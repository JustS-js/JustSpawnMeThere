package net.just_s.util;

import net.just_s.JSMT;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class SpawnLocator {
    @Nullable
    public static BlockPos findOverworldSpawn(ServerWorld world, int x, int z, Shape shape) {
        int i = world.getSpawnPos().getY() + JSMT.MS.getSpawnRadius(world);
        if (i < world.getBottomY()) return null;

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int k = i + 1; k >= world.getBottomY(); --k) {
            mutable.set(x, k, z);
            BlockState blockState = world.getBlockState(mutable);
            if (!blockState.getFluidState().isEmpty()) {
                break;
            }

            if (isInArea(world, mutable.up(), shape) && Block.isFaceFullSquare(blockState.getCollisionShape(world, mutable), Direction.UP)) {
                return mutable.up().toImmutable();
            }
        }

        return null;
    }

    private static boolean isInArea(ServerWorld world, BlockPos pos, Shape shape) {
        BlockPos spawnPos = world.getSpawnPos();
        int radius = JSMT.MS.getSpawnRadius(world);

        switch (shape) {
            case SPHERE -> {
                return pos.isWithinDistance(spawnPos, radius);
            }
            case BOX ->  {
                Box box = new Box(
                        spawnPos.add(radius+1, radius+1, radius+1),
                        spawnPos.add(-radius, -radius, -radius)
                );
                return box.contains(pos.toCenterPos());
            }
        }
        return false;
    }
}
