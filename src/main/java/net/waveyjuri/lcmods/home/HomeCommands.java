package net.waveyjuri.lcmods.home;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Set;

/**
 * Commands fuer das Home/Fast-Travel-System. Ersetzt das homes-Datapack;
 * teleportiert nativ (kein Marker-/Macro-Umweg mehr noetig).
 */
public final class HomeCommands {
	private HomeCommands() {}

	private static final SuggestionProvider<CommandSourceStack> HOME_SUGGEST = (ctx, builder) -> {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player != null) {
			for (String name : HomeState.get(level(player)).names()) {
				builder.suggest(name);
			}
		}
		return builder.buildFuture();
	};

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("sethome")
				.executes(ctx -> setHome(ctx, "home"))
				.then(Commands.argument("name", StringArgumentType.word())
					.executes(ctx -> setHome(ctx, StringArgumentType.getString(ctx, "name")))));

			dispatcher.register(Commands.literal("home")
				.executes(ctx -> goHome(ctx, "home"))
				.then(Commands.argument("name", StringArgumentType.word()).suggests(HOME_SUGGEST)
					.executes(ctx -> goHome(ctx, StringArgumentType.getString(ctx, "name")))));

			dispatcher.register(Commands.literal("homes").executes(HomeCommands::listHomes));

			dispatcher.register(Commands.literal("delhome")
				.then(Commands.argument("name", StringArgumentType.word()).suggests(HOME_SUGGEST)
					.executes(ctx -> delHome(ctx, StringArgumentType.getString(ctx, "name")))));
		});
	}

	private static ServerLevel level(ServerPlayer player) {
		return (ServerLevel) player.level();
	}

	private static int setHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		ServerLevel serverLevel = level(player);
		Home home = new Home(name, serverLevel.dimension(),
			player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
		HomeState.get(serverLevel).setHome(home);
		ctx.getSource().sendSuccess(() -> Component.literal("Home '" + name + "' gesetzt.")
			.withStyle(ChatFormatting.GREEN), false);
		return 1;
	}

	private static int goHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		Home home = HomeState.get(level(player)).get(name);
		if (home == null) {
			ctx.getSource().sendFailure(Component.literal("Kein Home mit dem Namen '" + name + "'."));
			return 0;
		}
		ServerLevel target = level(player).getServer().getLevel(home.dimension());
		if (target == null) {
			ctx.getSource().sendFailure(Component.literal("Die Dimension dieses Homes existiert nicht mehr."));
			return 0;
		}
		player.teleportTo(target, home.x(), home.y(), home.z(), Set.of(), home.yaw(), home.pitch(), true);
		ctx.getSource().sendSuccess(() -> Component.literal("Teleportiert zu '" + name + "'.")
			.withStyle(ChatFormatting.AQUA), false);
		return 1;
	}

	private static int delHome(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		boolean removed = HomeState.get(level(player)).removeHome(name);
		if (removed) {
			ctx.getSource().sendSuccess(() -> Component.literal("Home '" + name + "' geloescht.")
				.withStyle(ChatFormatting.YELLOW), false);
			return 1;
		}
		ctx.getSource().sendFailure(Component.literal("Kein Home mit dem Namen '" + name + "'."));
		return 0;
	}

	private static int listHomes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		Collection<Home> homes = HomeState.get(level(player)).all();
		if (homes.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Component.literal("Noch keine Homes. Setze eins mit /sethome [Name].")
				.withStyle(ChatFormatting.GRAY), false);
			return 1;
		}
		ctx.getSource().sendSuccess(() -> Component.literal("Deine Homes:").withStyle(ChatFormatting.GOLD), false);
		for (Home home : homes) {
			ctx.getSource().sendSuccess(() -> renderLine(home), false);
		}
		return 1;
	}

	private static MutableComponent renderLine(Home home) {
		String dim = home.dimension().identifier().getPath();
		return Component.literal(" • ")
			.append(Component.literal(home.name()).withStyle(ChatFormatting.WHITE))
			.append(Component.literal("  (" + dim + ")").withStyle(ChatFormatting.DARK_GRAY))
			.append(Component.literal("  [TP]").withStyle(style -> style
				.withColor(ChatFormatting.GREEN).withBold(true)
				.withClickEvent(new ClickEvent.RunCommand("/home " + home.name()))))
			.append(Component.literal(" [X]").withStyle(style -> style
				.withColor(ChatFormatting.RED)
				.withClickEvent(new ClickEvent.RunCommand("/delhome " + home.name()))));
	}
}
