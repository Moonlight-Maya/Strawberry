package io.github.moonlight_maya.limits_strawberries;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import java.util.HashMap;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BerryCommands {

	private static final HashMap<UUID, Integer> resetRequesters = new HashMap<>();

	private static int berryResetRequest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
		int code = (int) (Math.random() * 1000);
		resetRequesters.put(player.getUuid(), code);
		context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.reset_confirmation", code), false);
		return 0;
	}

	private static int berryResetConfirm(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
		int submittedCode = IntegerArgumentType.getInteger(context, "code");
		Integer requiredCode = resetRequesters.remove(player.getUuid());
		if (requiredCode == null) {
			context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.reset_confirm_before_request"), false);
			return -1;
		} else if (requiredCode != submittedCode) {
			context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.reset_canceled"), false);
			return 0;
		} else {
			StrawberryMod.SERVER_BERRIES.resetPlayer(player.getUuid());
			context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.reset_confirmed"), false);
			return 0;
		}
	}

	private static int berryResetPlayers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		if (context.getSource().hasPermissionLevel(3)) {
			EntityArgumentType.getPlayers(context, "targets").forEach(playerEntity -> StrawberryMod.SERVER_BERRIES.resetPlayer(playerEntity.getUuid()));
			return 0;
		}
		return -1;
	}

	public static void init() {
		LiteralArgumentBuilder<ServerCommandSource> root = literal("berry")
				.then(literal("reset")
						.executes(BerryCommands::berryResetRequest)
						.then(literal("confirm")
								.then(argument("code", IntegerArgumentType.integer())
										.executes(BerryCommands::berryResetConfirm)
								)
						)
						.then(literal("players")
								.then(argument("targets", EntityArgumentType.players())
										.requires(source -> source.hasPermissionLevel(3))
										.executes(BerryCommands::berryResetPlayers)
								)
						)
				);

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> dispatcher.register(root));
	}

}
