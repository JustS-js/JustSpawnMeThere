package net.just_s.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.command.GameRuleCommand;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.SERVER)
@Mixin(GameRuleCommand.class)
public class MixinGameRuleCommand {
}
