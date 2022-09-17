package io.github.moonlight_maya.limits_strawberries;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class StrawberryItem extends Item {

	private static final int MAX_USE_TIME = 32;

	public StrawberryItem(Settings settings) {
		super(settings);
	}

	@Override
	public boolean isFood() {
		return super.isFood();
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		if (player != null && !player.getAbilities().allowModifyWorld)
			return ActionResult.PASS;

		World world = context.getWorld();
		if (!world.isClient) {
			Vec3d snappedToGrid = context.getHitPos().multiply(2);
			snappedToGrid = new Vec3d(Math.round(snappedToGrid.x), Math.round(snappedToGrid.y), Math.round(snappedToGrid.z)).multiply(0.5);
			Vec3f dirVec = context.getSide().getUnitVector();
			dirVec.scale(0.5f);
			snappedToGrid = snappedToGrid.add(new Vec3d(dirVec)).add(0, -StrawberryEntity.HEIGHT / 2, 0);

			//snappedToGrid now contains the eventual position of the berry upon placement.
			StrawberryEntity entity = StrawberryMod.STRAWBERRY.create(world);
			if (entity != null) {
				entity.setPos(snappedToGrid.x, snappedToGrid.y, snappedToGrid.z);
				entity.refreshPositionAndAngles(snappedToGrid.x, snappedToGrid.y, snappedToGrid.z, 0, 0);
				entity.setPlacer(player);
				world.spawnEntity(entity);
				entity.emitGameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
				entity.playSound(SoundEvents.BLOCK_HONEY_BLOCK_BREAK);

				String playerName = player == null ? "Unknown Player" : player.getEntityName();
				String playerUUID = player == null ? "N/A" : player.getUuid().toString();
				StrawberryMod.LOGGER.info("Berry {} was placed at ({}, {}, {}) by player {} (UUID {}).",
						entity.getUuid(),
						snappedToGrid.x,
						snappedToGrid.y,
						snappedToGrid.z,
						playerName,
						playerUUID
				);

				return ActionResult.CONSUME;
			}
			return ActionResult.FAIL;
		} else {
			return ActionResult.SUCCESS;
		}
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		PlayerEntity player = user instanceof PlayerEntity playerTemp ? playerTemp : null;
		if (!world.isClient && player != null) {
			NbtCompound tag = stack.getNbt();
			if (tag != null) {
				if (tag.containsUuid("berryId")) {
					UUID berryUUID = tag.getUuid("berryId");
					UUID playerUUID = player.getUuid();
					boolean newBerry = StrawberryMod.SERVER_BERRIES.collect(berryUUID, playerUUID);
					if (newBerry) {
						player.sendMessage(Text.translatable("limits_strawberries.entity.berry_collect_notif"), true);
					}
				}
			}
			if (!player.getAbilities().creativeMode) {
				stack.decrement(1);
			}
		}
		if (player == null || !player.getAbilities().creativeMode)
			stack.decrement(1);
		user.emitGameEvent(GameEvent.EAT);
		return stack;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		NbtCompound tag = stack.getNbt();
		if (tag != null && tag.getBoolean("edible"))
			return MAX_USE_TIME;
		return 0;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		NbtCompound tag = stack.getNbt();
		if (tag != null && tag.getBoolean("edible"))
			return UseAction.EAT;
		return UseAction.NONE;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		NbtCompound tag = stack.getNbt();
		if (tag != null && tag.getBoolean("edible") && tag.containsUuid("berryId")) {
			MutableText edibleNotif = Text.translatable("limits_strawberries.item.edible").setStyle(Style.EMPTY.withColor(Formatting.AQUA));
			tooltip.add(edibleNotif);
		}
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		NbtCompound tag = user.getStackInHand(hand).getNbt();
		if (tag != null && tag.getBoolean("edible"))
			return ItemUsage.consumeHeldItem(world, user, hand);
		return super.use(world, user, hand);
	}
}
