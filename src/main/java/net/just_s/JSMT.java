package net.just_s;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.just_s.util.Shape;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSMT implements ModInitializer {
	public static final String MODID = "jsmt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final GameRules.Key<EnumRule<Shape>> SPAWN_SHAPE =
			GameRuleRegistry.register(
					"spawnShape",
					GameRules.Category.SPAWNING,
					GameRuleFactory.createEnumRule(Shape.VANILLA)
			);

	@Override
	public void onInitialize() {
	}
}