package io.github.moonlight_maya.limits_strawberries.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.*;

//Used on the client, extended for server, so it syncs.
public class BerryMap {
	public void loadFrom(NbtCompound compound) {
		berryInfo.clear();
		collectorInfo.clear();
		NbtCompound data = compound.getCompound("data");
		for (String uuid : data.getKeys()) {
			UUID berryUUID = UUID.fromString(uuid);
			NbtCompound berryData = data.getCompound(uuid);
			addBerryIfNeeded(berryUUID);
			updateName(berryUUID, berryData.getString("name"));
			updateClue(berryUUID, berryData.getString("clue"));
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

	//Returns true if the name is different from the old name, false otherwise.
	public boolean updateName(UUID berryUUID, String name) {
		addBerryIfNeeded(berryUUID);
		Berry berry = berryInfo.get(berryUUID);
		String oldName = berry.name;
		berry.name = name;
		return !name.equals(oldName);
	}

	//Returns true if the clue is different from the old clue, false otherwise.
	public boolean updateClue(UUID berryUUID, String clue) {
		addBerryIfNeeded(berryUUID);
		Berry berry = berryInfo.get(berryUUID);
		String oldClue = berry.clue;
		berry.clue = clue;
		return !clue.equals(oldClue);
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
			berryData.putString("name", entry.getValue().name);
			berryData.putString("clue", entry.getValue().clue);
			NbtList collectors = new NbtList();
			entry.getValue().collectors.stream().map(UUID::toString).map(NbtString::of).forEach(collectors::add);
			berryData.put("collectors", collectors);
			data.put(entry.getKey().toString(), berryData);
		}
		result.put("data", data);
		return result;
	}

	public static class Berry {
		public String name = "", clue = "";
		public final Set<UUID> collectors = new HashSet<>();
	}

}
