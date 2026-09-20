package net.waveyjuri.lcmods.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import net.waveyjuri.lcmods.client.zoom.Zoom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Zoom-Eingriff an der richtigen Stelle: {@code Camera.calculateFov(float)}
 * liefert den Welt-FOV, der ins Feld {@code fov} und damit in die
 * Welt-Projektion (setupPerspective) fliesst. Die Hand nutzt separat
 * {@code calculateHudFov} und bleibt unveraendert. Beim Zoomen teilen wir
 * den Welt-FOV durch den Zoom-Faktor.
 */
@Mixin(Camera.class)
public class CameraZoomMixin {

	@ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
	private float lcmods$zoomFov(float fov) {
		if (!Zoom.isZooming()) {
			return fov;
		}
		return (float) (fov / Zoom.factor());
	}
}
