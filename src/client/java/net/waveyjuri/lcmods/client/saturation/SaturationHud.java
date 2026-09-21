package net.waveyjuri.lcmods.client.saturation;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

/**
 * Sättigungsbalken direkt über dem Hungerbalken (wie AppleSkin & Co.).
 * Sättigung ist ein versteckter Wert, wird im Singleplayer aber über den
 * Gesundheits-Packet zum Client synchronisiert und kann daher hier gelesen werden.
 *
 * Positionen sind aus dem 26.3-Bytecode von {@code net.minecraft.client.gui.Hud#extractFood}
 * verifiziert: Nahrungsleiste rechtsbündig bei {@code guiWidth/2 + 91}, Oberkante
 * {@code guiHeight - 39}, 81 px breit. Der Balken sitzt rechtsbündig darüber.
 */
public class SaturationHud implements HudElement {
	private static boolean enabled = true;

	// Layout der Vanilla-Nahrungsleiste (aus dem Bytecode verifiziert)
	private static final int RIGHT_OFFSET = 91; // Mitte + 91 = rechter Rand
	private static final int BAR_WIDTH = 81;     // 10 Icons je ~8 px
	private static final int ROW_TOP_OFFSET = 39; // guiHeight - 39 = Oberkante der Icons

	private static final int BAR_HEIGHT = 2;
	private static final int GAP = 1; // Abstand zwischen Balken und Icons

	private static final int TRACK_COLOR = 0x60000000; // dezente dunkle Schiene
	private static final int FILL_COLOR = 0xFFFFB300;  // Sättigungs-Gold

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
		Player player = mc.player;
		if (player == null || mc.level == null) return;

		// Gleiche Bedingungen wie die Vanilla-Nahrungsleiste
		// (F1/HUD-ausgeblendet wird von der HUD-Render-Ebene selbst abgefangen):
		if (player.isCreative() || player.isSpectator()) return; // kein Hungerbalken
		if (player.getVehicle() instanceof LivingEntity) return; // Reittier-Gesundheit ersetzt Nahrung

		FoodData food = player.getFoodData();
		float saturation = food.getSaturationLevel();
		if (saturation <= 0.0f) return; // nichts zu zeigen

		float frac = Math.min(saturation / 20.0f, 1.0f);
		int filled = Math.round(BAR_WIDTH * frac);

		int centerX = g.guiWidth() / 2;
		int right = centerX + RIGHT_OFFSET;
		int left = right - BAR_WIDTH;

		int top = g.guiHeight() - ROW_TOP_OFFSET;
		int y2 = top - GAP;              // Unterkante des Balkens (knapp über den Icons)
		int y1 = y2 - BAR_HEIGHT;        // Oberkante

		// Schiene (volle Breite) + rechtsbündige Füllung, deckungsgleich mit den Keulen
		g.fill(left, y1, right, y2, TRACK_COLOR);
		g.fill(right - filled, y1, right, y2, FILL_COLOR);
	}
}
