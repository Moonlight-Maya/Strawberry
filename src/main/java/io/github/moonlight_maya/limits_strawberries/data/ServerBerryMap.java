package io.github.moonlight_maya.limits_strawberries.data;

import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.UUID;

public class ServerBerryMap extends BerryMap {

	private final MinecraftServer server;

	public ServerBerryMap(MinecraftServer server, NbtCompound saveData) {
		this.server = server;
		if (saveData != null)
			loadFrom(saveData);
	}

	@Override
	public boolean addBerryIfNeeded(UUID berryUUID) {
		boolean needsSync = super.addBerryIfNeeded(berryUUID);
		if (needsSync)
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.ADD_PACKET_ID, PacketByteBufs.create().writeUuid(berryUUID));
		return needsSync;
	}

	@Override
	public boolean deleteBerry(UUID berryUUID) {
		boolean needsSync = super.deleteBerry(berryUUID);
		if (needsSync)
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.DELETE_PACKET_ID, PacketByteBufs.create().writeUuid(berryUUID));
		return needsSync;
	}

	@Override
	public boolean updateName(UUID berryUUID, String name) {
		boolean needsSync = super.updateName(berryUUID, name);
		if (needsSync)
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.NAME_PACKET_ID, PacketByteBufs.create().writeUuid(berryUUID).writeString(name));
		return needsSync;
	}

	@Override
	public boolean updateClue(UUID berryUUID, String clue) {
		boolean needsSync = super.updateClue(berryUUID, clue);
		if (needsSync)
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.CLUE_PACKET_ID, PacketByteBufs.create().writeUuid(berryUUID).writeString(clue));
		return needsSync;
	}

	@Override
	public boolean collect(UUID berryUUID, UUID playerUUID) {
		boolean needsSync = super.collect(berryUUID, playerUUID);
		if (needsSync)
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.COLLECT_PACKET_ID, PacketByteBufs.create().writeUuid(berryUUID).writeUuid(playerUUID));
		return needsSync;
	}

	public void syncTo(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, StrawberryMod.SYNC_PACKET_ID, PacketByteBufs.create().writeNbt(serialize()));
	}
}
