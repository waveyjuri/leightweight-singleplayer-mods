package net.waveyjuri.lcmods.client.zoom;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.waveyjuri.lcmods.LcMods;

/**
 * OptiFine-artiger Zoom. Standardtaste C, im Steuerungsmenü frei belegbar
 * (Kategorie "Low Cortisol Mods"). Der eigentliche FOV-Eingriff passiert im
 * GameRendererZoomMixin, der diesen Zustand abfragt.
 */
public final class Zoom {
	/** Eigene Tastenkategorie, damit der Zoom im Steuerungsmenü gruppiert erscheint. */
	public static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath(LcMods.MOD_ID, "main"));

	/** Zoom-Taste, Standard: C. */
	public static final KeyMapping ZOOM_KEY =
		new KeyMapping("key.lcmods.zoom", InputConstants.KEY_C, CATEGORY);

	/** Zoom-Stärke: FOV wird durch diesen Faktor geteilt. */
	private static final double FACTOR = 4.0;

	private Zoom() {}

	public static void register() {
		KeyMappingHelper.registerKeyMapping(ZOOM_KEY);
	}

	public static boolean isZooming() {
		return ZOOM_KEY.isDown();
	}

	public static double factor() {
		return FACTOR;
	}
}
