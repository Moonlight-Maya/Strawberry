package io.github.moonlight_maya.limits_strawberries;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class StrawberryEntity extends Entity {

	public StrawberryEntity(EntityType<? extends Entity> entityType, World world) {
		super(entityType, world);
		if (!world.isClient)
			StrawberryMod.SERVER_BERRIES.addBerryIfNeeded(getUuid());
	}

	@Override
	public void tick() {
		super.tick();
		if (!world.isClient)
			world.getOtherEntities(this, getBoundingBox(), e -> e instanceof ServerPlayerEntity).stream().map(e -> (ServerPlayerEntity) e).forEach(p -> {
				boolean newBerry = StrawberryMod.SERVER_BERRIES.collect(getUuid(), p.getUuid());
				if (newBerry) p.sendMessage(Text.literal("Collected a berry!"), true);
			});
	}

	@Override
	public void kill() {
		StrawberryMod.SERVER_BERRIES.deleteBerry(getUuid());
		super.kill();
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
