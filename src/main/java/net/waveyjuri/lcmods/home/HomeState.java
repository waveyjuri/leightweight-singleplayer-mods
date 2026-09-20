package net.waveyjuri.lcmods.home;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistente, weltweite Home-Verwaltung (Mojang: SavedData, frueher
 * "PersistentState"). In 26.3 Codec-basiert. Wird immer im Overworld-Storage
 * abgelegt, damit Homes dimensionsuebergreifend geteilt werden.
 */
public class HomeState extends SavedData {
	private final Map<String, Home> homes;

	public static final Codec<HomeState> CODEC = Codec
		.unboundedMap(Codec.STRING, Home.CODEC)
		.xmap(HomeState::new, state -> state.homes);

	private static final SavedDataType<HomeState> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath("lcmods", "homes"),
		HomeState::new,
		CODEC,
		DataFixTypes.LEVEL
	);

	public HomeState() {
		this.homes = new ConcurrentHashMap<>();
	}

	public HomeState(Map<String, Home> source) {
		this.homes = new ConcurrentHashMap<>(source);
	}

	/** Liefert (und erzeugt bei Bedarf) den Speicher. Nur vom Server-Thread aufrufen. */
	public static HomeState get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	/** Liefert den Speicher ohne ihn zu erzeugen (thread-sicher fuer Lesezugriff, z. B. Client-HUD). */
	public static HomeState getIfPresent(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().get(TYPE);
	}

	public void setHome(Home home) {
		homes.put(home.name(), home);
		setDirty();
	}

	public boolean removeHome(String name) {
		boolean removed = homes.remove(name) != null;
		if (removed) setDirty();
		return removed;
	}

	public Home get(String name) {
		return homes.get(name);
	}

	public Collection<Home> all() {
		return homes.values();
	}

	public List<String> names() {
		return List.copyOf(homes.keySet());
	}
}
