package net.waveyjuri.lcmods.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.waveyjuri.lcmods.LcMods;
import net.waveyjuri.lcmods.client.coords.CoordsHud;
import net.waveyjuri.lcmods.client.waypoint.WaypointHud;

public class LcModsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// HUD-Elemente hinter die Vanilla-Overlays haengen (unter Chat etc.)
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(LcMods.MOD_ID, "coords"), new CoordsHud());
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(LcMods.MOD_ID, "waypoint"), new WaypointHud());

		// Client-Commands zum Ein-/Ausblenden der HUDs
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("coords").executes(ctx -> {
				boolean on = CoordsHud.toggle();
				ctx.getSource().sendFeedback(Component.literal("Koordinaten-HUD " + (on ? "an" : "aus")));
				return 1;
			}));
			dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("waypoint").executes(ctx -> {
				boolean on = WaypointHud.toggle();
				ctx.getSource().sendFeedback(Component.literal("Waypoint-HUD " + (on ? "an" : "aus")));
				return 1;
			}));
		});

		LcMods.LOGGER.info("Low Cortisol Mods (client) geladen");
	}
}
