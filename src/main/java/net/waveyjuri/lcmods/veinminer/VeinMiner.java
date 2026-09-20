package net.waveyjuri.lcmods.veinminer;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * VeinCapitator: Beim Sneak-Abbau eines Stamms oder Erzes wird die gesamte
 * zusammenhaengende Ader in einem Zug abgebaut. Ersetzt das gleichnamige
 * Datapack (Predicate + Flood-Fill) durch eine event-getriebene Java-Loesung.
 */
public final class VeinMiner {
	/** Sicherheitslimit, damit eine einzelne Ader nie den Server blockiert. */
	private static final int MAX_BLOCKS = 128;

	/** Verhindert Rekursion, falls ein Abbau erneut ein Break-Event ausloest. */
	private static boolean active = false;

	private VeinMiner() {}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (active) return;
			if (!(world instanceof ServerLevel level)) return;
			if (!(player instanceof ServerPlayer serverPlayer)) return;
			if (!serverPlayer.isShiftKeyDown()) return;      // nur beim Sneaken
			if (serverPlayer.isCreative()) return;           // im Kreativ nicht noetig
			if (!isVeinMineable(state)) return;

			ItemStack tool = serverPlayer.getMainHandItem();
			if (!tool.isCorrectToolForDrops(state)) return;  // Axt fuer Holz, Spitzhacke fuer Erz

			mineVein(level, serverPlayer, tool, pos, state.getBlock());
		});
	}

	private static boolean isVeinMineable(BlockState state) {
		return state.is(BlockTags.LOGS) || state.is(ConventionalBlockTags.ORES);
	}

	private static void mineVein(ServerLevel level, ServerPlayer player, ItemStack tool,
	                             BlockPos origin, Block target) {
		active = true;
		try {
			Set<BlockPos> visited = new HashSet<>();
			ArrayDeque<BlockPos> queue = new ArrayDeque<>();
			visited.add(origin.immutable());
			queue.add(origin.immutable());

			int mined = 0;
			while (!queue.isEmpty() && mined < MAX_BLOCKS) {
				BlockPos current = queue.poll();

				// 26er-Nachbarschaft: erfasst auch diagonale Aeste/Adern
				for (int dx = -1; dx <= 1; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						for (int dz = -1; dz <= 1; dz++) {
							if (dx == 0 && dy == 0 && dz == 0) continue;
							BlockPos next = current.offset(dx, dy, dz);
							if (!visited.add(next.immutable())) continue;
							if (!level.getBlockState(next).is(target)) continue;
							queue.add(next.immutable());

							if (!hasDurabilityLeft(tool)) return;   // Werkzeug schonen
							breakBlock(level, player, tool, next);
							if (++mined >= MAX_BLOCKS) return;
						}
					}
				}
			}
		} finally {
			active = false;
		}
	}

	/** true, solange das Werkzeug den Abbau ueberlebt (bricht nie das Werkzeug). */
	private static boolean hasDurabilityLeft(ItemStack tool) {
		if (!tool.isDamageableItem()) return true;
		return tool.getMaxDamage() - tool.getDamageValue() > 1;
	}

	private static void breakBlock(ServerLevel level, ServerPlayer player, ItemStack tool, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		BlockEntity be = level.getBlockEntity(pos);
		// Break-Partikel + Sound wie beim normalen Abbau
		level.levelEvent(2001, pos, Block.getId(state));
		level.removeBlock(pos, false);
		// Drops inkl. Verzauberungen (Behutsamkeit/Gluecksbringer) aus dem Werkzeug
		Block.dropResources(state, level, pos, be, player, tool);
		if (tool.isDamageableItem()) {
			tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
		}
	}
}
