package net.waveyjuri.lcmods.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
import net.waveyjuri.lcmods.client.zoom.Zoom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Zoom-Eingriff: In {@code GameRenderer.extractOptions()} wird der FOV-Wert der
 * Optionen (Options.fov().get()) in den Render-State geschrieben – daraus baut
 * 26.3 die Welt-Projektion. Beim Zoomen teilen wir genau diesen Wert.
 * Der fov-Lesezugriff ist der 16. OptionInstance.get() in der Methode (ordinal 15).
 */
@Mixin(GameRenderer.class)
public class GameRendererZoomMixin {

	@ModifyExpressionValue(
		method = "extractOptions",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
			ordinal = 15
		)
	)
	private Object lcmods$zoomWorldFov(Object original) {
		if (!Zoom.isZooming()) {
			return original;
		}
		int fov = (Integer) original;
		int zoomed = (int) Math.max(1.0, Math.round(fov / Zoom.factor()));
		return Integer.valueOf(zoomed);
	}
}
