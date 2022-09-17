package io.github.moonlight_maya.limits_strawberries.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import javax.annotation.Nullable;
import java.util.*;

//Used on the client, extended for server, so it syncs.
public class BerryMap {

	public static final int NAME_MAX = 30;
	public static final int CLUE_MAX = 100;
	public static final int DESC_MAX = 500;

	public void loadFrom(NbtCompound compound) {
		berryInfo.clear();
		collectorInfo.clear();
		NbtCompound data = compound.getCompound("data");
		for (String uuid : data.getKeys()) {
			UUID berryUUID = UUID.fromString(uuid);
			NbtCompound berryData = data.getCompound(uuid);
			addBerryIfNeeded(berryUUID);
			updateBerry(berryUUID,
					berryData.contains("name") ? berryData.getString("name") : null,
					berryData.contains("clue") ? berryData.getString("clue") : null,
					berryData.contains("desc") ? berryData.getString("desc") : null,
					berryData.contains("placer") ? berryData.getString("placer") : null
			);
			berryData.getList("collectors", NbtElement.STRING_TYPE).stream().map(NbtElement::asString).map(UUID::fromString).forEach(p -> collect(berryUUID, p));
		}
	}

	public final Map<UUID, Berry> berryInfo = new HashMap<>();
	public final Map<UUID, Set<UUID>> collectorInfo = new HashMap<>(); //Map of player UUIDs to which berries they collected

	//Returns true if the added berry was new, and false if it was already in the map.
	public boolean addBerryIfNeeded(UUID berryUUID) {
		return berryInfo.putIfAbsent(berryUUID, new Berry()) == null;
	}

	//Returns true if the berry existed and was deleted successfully.
	public boolean deleteBerry(UUID berryUUID) {
		berryInfo.get(berryUUID).collectors.forEach(p -> collectorInfo.get(p).remove(berryUUID));
		return berryInfo.remove(berryUUID) != null;
	}

	public void resetPlayer(UUID playerUUID) {
		berryInfo.values().forEach(berry -> berry.collectors.remove(playerUUID));
		collectorInfo.remove(playerUUID);
	}

	//Bit flags.
	//1 bit means name changed,
	//2 bit means clue changed,
	//4 bit means desc changed,
	//8 bit means placer changed.
	public byte updateBerry(UUID berryUUID, @Nullable String name, @Nullable String clue, @Nullable String desc, @Nullable String placer) {
		addBerryIfNeeded(berryUUID);
		Berry berry = berryInfo.get(berryUUID);
		byte result = 0;

		if (name != null && !Objects.equals(berry.name, name)) {
			result |= 1;
			berry.name = name;
		}

		if (clue != null && !Objects.equals(berry.clue, clue)) {
			result |= 2;
			berry.clue = clue;
		}

		if (desc != null && !Objects.equals(berry.desc, desc)) {
			result |= 4;
			berry.desc = desc;
		}

		if (placer != null && !Objects.equals(berry.placer, placer)) {
			result |= 8;
			berry.placer = placer;
		}

		return result;
	}

	//Returns true if the berry wasn't already collected by that player, false otherwise.
	public boolean collect(UUID berryUUID, UUID playerUUID) {
		addBerryIfNeeded(berryUUID);
		collectorInfo.computeIfAbsent(playerUUID, p -> new HashSet<>()).add(berryUUID);
		return berryInfo.get(berryUUID).collectors.add(playerUUID);
	}

	public boolean hasPlayerCollected(UUID berryUUID, UUID playerUUID) {
		return collectorInfo.computeIfAbsent(playerUUID, p -> new HashSet<>()).contains(berryUUID);
	}

	public NbtCompound serialize() {
		NbtCompound result = new NbtCompound();
		NbtCompound data = new NbtCompound();
		for (Map.Entry<UUID, Berry> entry : berryInfo.entrySet()) {
			NbtCompound berryData = new NbtCompound();
			if (entry.getValue().name != null)
				berryData.putString("name", entry.getValue().name);
			if (entry.getValue().clue != null)
				berryData.putString("clue", entry.getValue().clue);
			if (entry.getValue().desc != null)
				berryData.putString("desc", entry.getValue().desc);
			if (entry.getValue().placer != null)
				berryData.putString("placer", entry.getValue().placer);
			NbtList collectors = new NbtList();
			entry.getValue().collectors.stream().map(UUID::toString).map(NbtString::of).forEach(collectors::add);
			berryData.put("collectors", collectors);
			data.put(entry.getKey().toString(), berryData);
		}
		result.put("data", data);
		return result;
	}

	public static class Berry {
		public String name, clue, desc, placer;
		public final Set<UUID> collectors = new HashSet<>();
	}

}
