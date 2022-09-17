package io.github.moonlight_maya.limits_strawberries.data;

import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
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
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.S2C_ADD_PACKET_ID, PacketByteBufs.create().writeUuid(berryUUID));
		return needsSync;
	}

	@Override
	public boolean deleteBerry(UUID berryUUID) {
		boolean needsSync = super.deleteBerry(berryUUID);
		if (needsSync)
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.S2C_DELETE_PACKET_ID, PacketByteBufs.create().writeUuid(berryUUID));
		return needsSync;
	}

	@Override
	public void resetPlayer(UUID playerUUID) {
		super.resetPlayer(playerUUID);
		ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.S2C_RESET_PACKET_ID, PacketByteBufs.create().writeUuid(playerUUID));
	}

	@Override
	public byte updateBerry(UUID berryUUID, @Nullable String name, @Nullable String clue, @Nullable String desc, @Nullable String placer) {
		byte flags = super.updateBerry(berryUUID, name, clue, desc, placer);
		if (flags > 0) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeUuid(berryUUID);
			buf.writeByte(flags);

			if ((flags & 1) > 0)
				buf.writeString(name);
			if ((flags & 2) > 0)
				buf.writeString(clue);
			if ((flags & 4) > 0)
				buf.writeString(desc);
			if ((flags & 8) > 0)
				buf.writeString(placer);
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.S2C_UPDATE_PACKET_ID, buf);
		}
		return flags;
	}

	@Override
	public boolean collect(UUID berryUUID, UUID playerUUID) {
		boolean needsSync = super.collect(berryUUID, playerUUID);
		if (needsSync)
			ServerPlayNetworking.send(PlayerLookup.all(server), StrawberryMod.S2C_COLLECT_PACKET_ID, PacketByteBufs.create().writeUuid(berryUUID).writeUuid(playerUUID));
		return needsSync;
	}

	public void syncTo(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, StrawberryMod.S2C_SYNC_PACKET_ID, PacketByteBufs.create().writeNbt(serialize()));
	}
}
