package net.waveyjuri.lcmods.home;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Ein gespeicherter Home-Punkt: Name, Dimension, Position und Blickrichtung.
 */
public record Home(String name, ResourceKey<Level> dimension,
                   double x, double y, double z, float yaw, float pitch) {

	public static final Codec<Home> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(Home::name),
		ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Home::dimension),
		Codec.DOUBLE.fieldOf("x").forGetter(Home::x),
		Codec.DOUBLE.fieldOf("y").forGetter(Home::y),
		Codec.DOUBLE.fieldOf("z").forGetter(Home::z),
		Codec.FLOAT.fieldOf("yaw").forGetter(Home::yaw),
		Codec.FLOAT.fieldOf("pitch").forGetter(Home::pitch)
	).apply(instance, Home::new));
}
