package io.github.moonlight_maya.limits_strawberries;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.UUID;

public class StrawberryItem extends Item {

	public StrawberryItem(Settings settings) {
		super(settings);
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
}
