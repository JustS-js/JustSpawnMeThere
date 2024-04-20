package net.just_s;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.just_s.util.Shape;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSMT implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("jsmt");
	public static MinecraftServer MS;

	public static Shape getConfigShape() {
		if (MS == null) return Shape.BOX;
		return Shape.SPHERE;
	}

	@Override
	public void onInitializeServer() {
		// get Server Instance after it started
		ServerLifecycleEvents.SERVER_STARTED.register(
				(server) -> MS = server
		);
	}
}