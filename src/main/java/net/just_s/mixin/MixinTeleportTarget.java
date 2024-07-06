package net.just_s.mixin;

import net.just_s.JSMT;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeleportTarget.class)
public abstract class MixinTeleportTarget {

    @Shadow @Final private ServerWorld world;

    @Shadow public abstract ServerWorld world();

    @Shadow
    private static Vec3d getWorldSpawnPos(ServerWorld world, Entity entity) {
        return null;
    }

    @Inject(method = "missingSpawnBlock", at = @At("HEAD"), cancellable = true)
    private static void jsmt$modifySpawnAngle(ServerWorld world, Entity entity, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        cir.setReturnValue(
                new TeleportTarget(world, getWorldSpawnPos(world, entity), Vec3d.ZERO, world.getSpawnAngle(), 0.0F, true, postDimensionTransition)
        );
    }
}
