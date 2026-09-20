package net.waveyjuri.lcmods.client.coords;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Koordinaten-HUD unten links: XYZ, Blickrichtung und die echte
 * Werkzeug-Haltbarkeit (aus dem ItemStack, keine hartcodierte Tabelle mehr).
 * Ersetzt das coords-Datapack samt seiner per-Tick-Actionbar-Schleife.
 */
public class CoordsHud implements HudElement {
	private static boolean enabled = true;

	private static final int PAD = 4;
	private static final int MARGIN = 4;
	private static final int LINE_GAP = 2;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int BG_COLOR = 0x80000000;

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

		Font font = mc.font;
		BlockPos pos = mc.player.blockPosition();
		Direction dir = mc.player.getDirection();

		String[] lines;
		ItemStack tool = mc.player.getMainHandItem();
		if (tool.isDamageableItem()) {
			int left = tool.getMaxDamage() - tool.getDamageValue();
			lines = new String[] {
				"XYZ  " + pos.getX() + "  " + pos.getY() + "  " + pos.getZ(),
				"Blick  " + facing(dir),
				"Haltbarkeit  " + left + " / " + tool.getMaxDamage()
			};
		} else {
			lines = new String[] {
				"XYZ  " + pos.getX() + "  " + pos.getY() + "  " + pos.getZ(),
				"Blick  " + facing(dir)
			};
		}

		int textWidth = 0;
		for (String l : lines) {
			textWidth = Math.max(textWidth, font.width(l));
		}
		int lineH = font.lineHeight;
		int boxW = textWidth + PAD * 2;
		int boxH = lines.length * lineH + (lines.length - 1) * LINE_GAP + PAD * 2;

		int x = MARGIN;
		int y = g.guiHeight() - MARGIN - boxH;

		g.fill(x, y, x + boxW, y + boxH, BG_COLOR);
		int ty = y + PAD;
		for (String l : lines) {
			g.text(font, l, x + PAD, ty, TEXT_COLOR);
			ty += lineH + LINE_GAP;
		}
	}

	private static String facing(Direction dir) {
		return switch (dir) {
			case NORTH -> "Norden (-Z)";
			case SOUTH -> "Süden (+Z)";
			case WEST -> "Westen (-X)";
			case EAST -> "Osten (+X)";
			default -> dir.getName();
		};
	}
}
