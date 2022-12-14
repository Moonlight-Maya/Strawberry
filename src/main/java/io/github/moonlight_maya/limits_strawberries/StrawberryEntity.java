package io.github.moonlight_maya.limits_strawberries;

import io.github.moonlight_maya.limits_strawberries.client.screens.BerryEditScreen;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import javax.annotation.Nullable;

public class StrawberryEntity extends Entity {

	public static final float WIDTH = 0.75f;
	public static final float HEIGHT = 0.9f;

	private boolean ticked;
	private String placerName;

	public StrawberryEntity(EntityType<? extends Entity> entityType, World world) {
		super(entityType, world);
		setInvulnerable(true);
	}

	public void setPlacer(@Nullable PlayerEntity placer) {
		placerName = placer == null ? null : placer.getEntityName();
	}

	@Override
	public void tick() {
		super.tick();
		if (!world.isClient) {
			if (!ticked) {
				//Only attempt to add berry on first tick server side
				StrawberryMod.SERVER_BERRIES.addBerryIfNeeded(getUuid());
				StrawberryMod.SERVER_BERRIES.updateBerry(getUuid(), null, null, null, placerName, null);
				ticked = true;
			}
			world.getOtherEntities(this, getBoundingBox(), e -> e instanceof ServerPlayerEntity).stream().map(e -> (ServerPlayerEntity) e).forEach(player -> {
				boolean newBerry = StrawberryMod.SERVER_BERRIES.collect(getUuid(), player.getUuid());
				if (newBerry) {
					player.sendMessage(Text.translatable("limits_strawberries.entity.berry_collect_notif"), true);
					boolean fullGroup = StrawberryMod.SERVER_BERRIES.hasPlayerCompleted(StrawberryMod.SERVER_BERRIES.berryInfo.get(getUuid()).group, player.getUuid());
					Identifier soundToPlay = fullGroup ? StrawberryMod.GROUP_FINISH_SOUND_ID : StrawberryMod.COLLECT_SOUND_ID;
					player.networkHandler.sendPacket(new PlaySoundIdS2CPacket(
							soundToPlay, SoundCategory.NEUTRAL, player.getPos(), 1f, 1f, player.world.getRandom().nextLong()
					));
				}
			});
		}
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!player.isCreative() || !player.getStackInHand(hand).isOf(StrawberryMod.ITEM))
			return super.interact(player, hand);
		if (player instanceof ServerPlayerEntity serverPlayerEntity) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(getId());
			ServerPlayNetworking.send(serverPlayerEntity, StrawberryMod.S2C_EDIT_SCREEN_PACKET_ID, buf);
		}

		return ActionResult.success(player.world.isClient);
	}

	@Override
	public boolean handleAttack(Entity attacker) {
		boolean success = attacker instanceof PlayerEntity player && player.isCreative() && player.getMainHandStack().getItem() instanceof SwordItem;
		if (success && !attacker.world.isClient) {
			playSound(SoundEvents.BLOCK_HONEY_BLOCK_BREAK);
			StrawberryMod.LOGGER.info("Berry {} destroyed by attack from player {} (UUID {}).",
				getUuid().toString(),
				attacker.getEntityName(),
				attacker.getUuid()
			);
			kill();
		}
		return success;
	}

	@Override
	public void kill() {
		StrawberryMod.SERVER_BERRIES.deleteBerry(getUuid());
		super.kill();
	}

	@Override
	public boolean collides() {
		return true;
	}

	@Override
	public PistonBehavior getPistonBehavior() {
		return PistonBehavior.IGNORE;
	}

	@Override
	protected void initDataTracker() {

	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {

	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {

	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}
}
