package net.waveyjuri.lcmods;

import net.fabricmc.api.ModInitializer;
import net.waveyjuri.lcmods.home.HomeCommands;
import net.waveyjuri.lcmods.veinminer.VeinMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LcMods implements ModInitializer {
	public static final String MOD_ID = "lcmods";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		VeinMiner.register();
		HomeCommands.register();
		LOGGER.info("Low Cortisol Mods (common) geladen");
	}
}
