package net.waveyjuri.lcmods.client.waypoint;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.waveyjuri.lcmods.home.Home;
import net.waveyjuri.lcmods.home.HomeState;

/**
 * Waypoint-Kompass oben mittig: Pfeil, Name und Distanz zum naechsten Home
 * in der aktuellen Dimension. Liest im Singleplayer direkt vom integrierten
 * Server (kein Netzwerk-Sync noetig). Ersetzt das waypoint-Datapack.
 */
public class WaypointHud implements HudElement {
	private static boolean enabled = true;

	private static final int PAD = 4;
	private static final int MARGIN_TOP = 4;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int BG_COLOR = 0x80000000;

	private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

	public static boolean toggle() {
		enabled = !enabled;
		return enabled;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker delta) {
		if (!enabled) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;

		IntegratedServer server = mc.getSingleplayerServer();
		if (server == null) return; // nur im Singleplayer

		ResourceKey<Level> dimKey = mc.level.dimension();
		ServerLevel serverLevel = server.getLevel(dimKey);
		if (serverLevel == null) return;
		HomeState state = HomeState.getIfPresent(serverLevel);
		if (state == null) return;

		double px = mc.player.getX();
		double pz = mc.player.getZ();

		Home nearest = null;
		double best = Double.MAX_VALUE;
		for (Home h : state.all()) {
			if (!h.dimension().equals(dimKey)) continue;
			double dx = h.x() - px;
			double dz = h.z() - pz;
			double d2 = dx * dx + dz * dz;
			if (d2 < best) {
				best = d2;
				nearest = h;
			}
		}
		if (nearest == null) return;

		double dx = nearest.x() - px;
		double dz = nearest.z() - pz;
		double dist = Math.sqrt(best);

		double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
		double rel = wrapDegrees(targetYaw - mc.player.getYRot());
		String text = arrow(rel) + "  " + nearest.name() + "  " + Math.round(dist) + " m";

		Font font = mc.font;
		int boxW = font.width(text) + PAD * 2;
		int boxH = font.lineHeight + PAD * 2;
		int x = (g.guiWidth() - boxW) / 2;
		int y = MARGIN_TOP;

		g.fill(x, y, x + boxW, y + boxH, BG_COLOR);
		g.text(font, text, x + PAD, y + PAD, TEXT_COLOR);
	}

	private static double wrapDegrees(double deg) {
		double d = deg % 360.0;
		if (d >= 180.0) d -= 360.0;
		if (d < -180.0) d += 360.0;
		return d;
	}

	private static String arrow(double rel) {
		int idx = (int) Math.floor((rel + 22.5 + 360.0) / 45.0) % 8;
		return ARROWS[idx];
	}
}
