package io.github.moonlight_maya.limits_strawberries;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.moonlight_maya.limits_strawberries.data.BerryMap;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BerryCommands {

	private static int berryReset(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
			if (!lastCommands.getOrDefault(player.getUuid(), "").equalsIgnoreCase("berry reset")) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.reset_warning"), false);
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.confirm_prompt", Text.translatable("limits_strawberries.command.reset_command")), false);
				return -1;
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

	private static int berryCreate(CommandContext<ServerCommandSource> context) {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			Text feedback = StrawberryMod.SERVER_BERRIES.createVirtualBerry(key);
			boolean success = feedback == null;
			if (success) {
				feedback = Text.translatable("limits_strawberries.command.berry_create_success", key);
				StrawberryMod.LOGGER.info("Virtual berry with key {} and uuid {} created by player {} (UUID {}).",
						key,
						StrawberryMod.SERVER_BERRIES.virtualBerries.get(key),
						context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getEntityName(),
						context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getUuid()
				);
			}
			context.getSource().sendFeedback(feedback, false);
			return success ? 0 : -1;
		}
		return -1;
	}

	private static int berryDelete(CommandContext<ServerCommandSource> context) {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			UUID berryUUID = StrawberryMod.SERVER_BERRIES.virtualBerries.get(key);
			if (berryUUID == null) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_invalid_key", key), false);
				return -1;
			}
			boolean success = StrawberryMod.SERVER_BERRIES.deleteBerry(berryUUID);
			if (success) {
				StrawberryMod.SERVER_BERRIES.virtualBerries.remove(key);
				StrawberryMod.LOGGER.info("Virtual berry with key {} deleted by player {} (UUID {}).",
						key,
						context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getEntityName(),
						context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getUuid()
				);
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.berry_delete_success", key), false);
				return 0;
			}
			context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.berry_delete_failure_not_exist", key), false);
			return -1;
		}
		return -1;
	}

	private static int berryList(CommandContext<ServerCommandSource> context) {
		if (context.getSource().hasPermissionLevel(2)) {
			Text result;
			Set<String> keys = StrawberryMod.SERVER_BERRIES.virtualBerries.keySet();
			if (keys.isEmpty()) {
				result = Text.translatable("limits_strawberries.command.berry_list_none");
			} else {
				StringBuilder builder = new StringBuilder();
				for (String key : keys) {
					builder.append("\"");
					builder.append(key);
					builder.append("\", ");
				}
				builder.delete(builder.length() - 2, builder.length()); //remove trailing comma and space
				result = Text.translatable("limits_strawberries.command.berry_list_result", builder.toString());
			}
			context.getSource().sendFeedback(result, false);
			return 0;
		}
		return -1;
	}

	private static int berryGrant(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			UUID berryUUID = StrawberryMod.SERVER_BERRIES.virtualBerries.get(key);
			if (berryUUID == null) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_invalid_key", key), false);
				return -1;
			}
			Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
			int num = (int) targets.stream().filter(playerEntity -> StrawberryMod.SERVER_BERRIES.collect(berryUUID, playerEntity.getUuid())).count();
			context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.berry_grant_success", num), false);
			return 0;
		}
		return -1;
	}

	private static int berryGive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			UUID berryUUID = StrawberryMod.SERVER_BERRIES.virtualBerries.get(key);
			if (berryUUID == null) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_invalid_key", key), false);
				return -1;
			}
			Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");

			ItemStack stack = StrawberryMod.ITEM.getDefaultStack();
			NbtCompound tag = stack.getOrCreateNbt();
			tag.putBoolean("edible", true);
			tag.putUuid("berryId", berryUUID);
			for (ServerPlayerEntity player : targets) {
				//Inside of this for loop taken from net.minecraft.server.command.GiveCommand

				ItemStack itemStack = stack.copy();
				boolean bl = player.getInventory().insertStack(itemStack);
				ItemEntity itemEntity;
				if (bl && itemStack.isEmpty()) {
					itemStack.setCount(1);
					itemEntity = player.dropItem(itemStack, false);
					if (itemEntity != null) {
						itemEntity.setDespawnImmediately();
					}

					player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
					player.currentScreenHandler.sendContentUpdates();
				} else {
					itemEntity = player.dropItem(itemStack, false);
					if (itemEntity != null) {
						itemEntity.resetPickupDelay();
						itemEntity.setOwner(player.getUuid());
					}
				}
			}
			return 0;
		}
		return -1;
	}

	private static int berryUpdateName(CommandContext<ServerCommandSource> context) {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			UUID berryUUID = StrawberryMod.SERVER_BERRIES.virtualBerries.get(key);
			if (berryUUID == null) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_invalid_key", key), false);
				return -1;
			}
			String name = StringArgumentType.getString(context, "name");
			if (name.length() > BerryMap.NAME_MAX) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_too_long", BerryMap.NAME_MAX), false);
				return -1;
			}
			StrawberryMod.LOGGER.info("Virtual berry with key {} and uuid {} had its name set to \"{}\" by player {} (UUID {})",
					key,
					StrawberryMod.SERVER_BERRIES.virtualBerries.get(key),
					name,
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getEntityName(),
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getUuid()
			);
			StrawberryMod.SERVER_BERRIES.updateBerry(berryUUID, name, null, null, null, null);
			return 0;
		}
		return -1;
	}

	private static int berryUpdateClue(CommandContext<ServerCommandSource> context) {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			UUID berryUUID = StrawberryMod.SERVER_BERRIES.virtualBerries.get(key);
			if (berryUUID == null) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_invalid_key", key), false);
				return -1;
			}
			String clue = StringArgumentType.getString(context, "clue");
			if (clue.length() > BerryMap.CLUE_MAX) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_too_long", BerryMap.CLUE_MAX), false);
				return -1;
			}
			StrawberryMod.LOGGER.info("Virtual berry with key {} and uuid {} had its clue set to \"{}\" by player {} (UUID {})",
					key,
					StrawberryMod.SERVER_BERRIES.virtualBerries.get(key),
					clue,
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getEntityName(),
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getUuid()
			);
			StrawberryMod.SERVER_BERRIES.updateBerry(berryUUID, null, clue, null, null, null);
			return 0;
		}
		return -1;
	}

	private static int berryUpdateDesc(CommandContext<ServerCommandSource> context) {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			UUID berryUUID = StrawberryMod.SERVER_BERRIES.virtualBerries.get(key);
			if (berryUUID == null) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_invalid_key", key), false);
				return -1;
			}
			String desc = StringArgumentType.getString(context, "desc");
			if (desc.length() > BerryMap.DESC_MAX) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_too_long", BerryMap.DESC_MAX), false);
				return -1;
			}
			StrawberryMod.LOGGER.info("Virtual berry with key {} and uuid {} had its description set to \"{}\" by player {} (UUID {})",
					key,
					StrawberryMod.SERVER_BERRIES.virtualBerries.get(key),
					desc,
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getEntityName(),
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getUuid()
			);
			StrawberryMod.SERVER_BERRIES.updateBerry(berryUUID, null, null, desc, null, null);
			return 0;
		}
		return -1;
	}

	private static int berryUpdatePlacer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			UUID berryUUID = StrawberryMod.SERVER_BERRIES.virtualBerries.get(key);
			if (berryUUID == null) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_invalid_key", key), false);
				return -1;
			}
			String placer = StringArgumentType.getString(context, "placer");
			if (placer.length() > BerryMap.PLACER_MAX) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_too_long", BerryMap.PLACER_MAX), false);
				return -1;
			}
			StrawberryMod.LOGGER.info("Virtual berry with key {} and uuid {} had its placer set to \"{}\" by player {} (UUID {})",
					key,
					StrawberryMod.SERVER_BERRIES.virtualBerries.get(key),
					placer,
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getEntityName(),
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getUuid()
			);
			StrawberryMod.SERVER_BERRIES.updateBerry(berryUUID, null, null, null, placer, null);
			return 0;
		}
		return -1;
	}

	private static int berryUpdateGroup(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		if (context.getSource().hasPermissionLevel(2)) {
			String key = StringArgumentType.getString(context, "key");
			UUID berryUUID = StrawberryMod.SERVER_BERRIES.virtualBerries.get(key);
			if (berryUUID == null) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_invalid_key", key), false);
				return -1;
			}
			String group = StringArgumentType.getString(context, "group");
			if (group.length() > BerryMap.GROUP_MAX) {
				context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_too_long", BerryMap.GROUP_MAX), false);
				return -1;
			}
			StrawberryMod.LOGGER.info("Virtual berry with key {} and uuid {} had its group set to \"{}\" by player {} (UUID {})",
					key,
					StrawberryMod.SERVER_BERRIES.virtualBerries.get(key),
					group,
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getEntityName(),
					context.getSource().getEntity() == null || !(context.getSource().getEntity() instanceof PlayerEntity) ? "N/A" : context.getSource().getEntity().getUuid()
			);
			StrawberryMod.SERVER_BERRIES.updateBerry(berryUUID, null, null, null, null, group);
			return 0;
		}
		return -1;
	}

	public static void init() {
		BerryKeyInstructionProvider keySuggester = new BerryKeyInstructionProvider();

		LiteralArgumentBuilder<ServerCommandSource> root = literal("berry")
				.then(literal("reset")
						.executes((c) -> unwrapAndExecute(c, BerryCommands::berryReset))
						.then(literal("players")
								.requires(source -> source.hasPermissionLevel(3))
								.then(argument("targets", EntityArgumentType.players())
										.executes((c) -> unwrapAndExecute(c, BerryCommands::berryResetPlayers))
								)
						)
				)
				.then(literal("create")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("key", StringArgumentType.string())
								.executes((c) -> unwrapAndExecute(c, BerryCommands::berryCreate))
						)
				)
				.then(literal("delete")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("key", StringArgumentType.string())
								.suggests(keySuggester)
								.executes((c) -> unwrapAndExecute(c, BerryCommands::berryDelete))
						)
				).then(literal("list")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(BerryCommands::berryList)
				).then(literal("grant")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("targets", EntityArgumentType.players())
								.then(argument("key", StringArgumentType.string())
										.suggests(keySuggester)
										.executes((c) -> unwrapAndExecute(c, BerryCommands::berryGrant))
								)
						)
				).then(literal("give")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("targets", EntityArgumentType.players())
								.then(argument("key", StringArgumentType.string())
										.suggests(keySuggester)
										.executes((c) -> unwrapAndExecute(c, BerryCommands::berryGive))
								)
						)
				).then(literal("update")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("key", StringArgumentType.string())
								.suggests(keySuggester)
								.then(literal("name")
										.then(argument("name", StringArgumentType.greedyString())
												.executes((c) -> unwrapAndExecute(c, BerryCommands::berryUpdateName))
										)
								)
								.then(literal("clue")
										.then(argument("clue", StringArgumentType.greedyString())
												.executes((c) -> unwrapAndExecute(c, BerryCommands::berryUpdateClue))
										)
								)
								.then(literal("desc")
										.then(argument("desc", StringArgumentType.greedyString())
												.executes((c) -> unwrapAndExecute(c, BerryCommands::berryUpdateDesc))
										)
								)
								.then(literal("placer")
										.then(argument("placer", StringArgumentType.greedyString())
												.executes((c) -> unwrapAndExecute(c, BerryCommands::berryUpdatePlacer))
										)
								)
								.then(literal("group")
										.then(argument("group", StringArgumentType.greedyString())
												.executes((c) -> unwrapAndExecute(c, BerryCommands::berryUpdateGroup))
										)
								)
						)
				);

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> dispatcher.register(root));
	}

	private static class BerryKeyInstructionProvider implements SuggestionProvider<ServerCommandSource> {
		@Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
			for (String key : StrawberryMod.SERVER_BERRIES.virtualBerries.keySet())
				builder.suggest(key);
			return builder.buildFuture();
		}
	}

	@FunctionalInterface
	public interface CommandBiFunction<T1, T2, R> {
		R apply(T1 t1, T2 t2) throws CommandSyntaxException;
	}

	@FunctionalInterface
	public interface CommandFunction<T1, R> {
		R apply(T1 t1) throws CommandSyntaxException;
	}

	private static final Map<UUID, String> lastCommands = new HashMap<>();

	private static int unwrapAndExecute(CommandContext<ServerCommandSource> context, CommandBiFunction<CommandContext<ServerCommandSource>, ServerPlayerEntity, Integer> executeFunction, boolean requirePlayer) throws CommandSyntaxException {
		// This is a weird "unwrapping" function that helps both validate players and save previous commands.
		// The pattern can be expanded to remove context from downstream methods entirely (it's messy though)

		ServerPlayerEntity player = context.getSource().method_44023(); // GetPlayerOrNull (c'mon QM)

		if (requirePlayer && player == null) {
			context.getSource().sendFeedback(Text.translatable("limits_strawberries.command.failure_non_player"), false);
			return 0;
		}

		int result = executeFunction.apply(context, player);

		if (player != null) {
			lastCommands.put(player.getUuid(), context.getInput()); // Save this command (for confirmations)
		}

		return result;
	}

	private static int unwrapAndExecute(CommandContext<ServerCommandSource> context, CommandBiFunction<CommandContext<ServerCommandSource>, ServerPlayerEntity, Integer> executeFunction) throws CommandSyntaxException  {
		return unwrapAndExecute(context, executeFunction, true);
	}

	private static int unwrapAndExecute(CommandContext<ServerCommandSource> context, CommandFunction<CommandContext<ServerCommandSource>, Integer> executeFunction) throws CommandSyntaxException  {
		return unwrapAndExecute(context, (c, p) -> executeFunction.apply(c), false);
	}

}
