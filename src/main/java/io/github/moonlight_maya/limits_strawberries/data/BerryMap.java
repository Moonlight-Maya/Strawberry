package io.github.moonlight_maya.limits_strawberries.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

import javax.annotation.Nullable;
import java.util.*;

//Used on the client, extended for server, so it syncs.
public class BerryMap {

	public static final int NAME_MAX = 30;
	public static final int CLUE_MAX = 100;
	public static final int DESC_MAX = 500;
	public static final int PLACER_MAX = 30;
	public static final int GROUP_MAX = 30;

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
					berryData.contains("placer") ? berryData.getString("placer") : null,
					berryData.contains("group") ? berryData.getString("group") : null
			);
			berryData.getList("collectors", NbtElement.INT_ARRAY_TYPE).stream().map(NbtHelper::toUuid).forEach(p -> collect(berryUUID, p));
		}

		NbtCompound virtual = compound.getCompound("virtual");
		for (String key : virtual.getKeys())
			virtualBerries.put(key, virtual.getUuid(key));
	}

	public final Map<UUID, Berry> berryInfo = new HashMap<>();
	public final Map<UUID, Set<UUID>> collectorInfo = new HashMap<>(); //Map of player UUIDs to which berries they collected
	public final Map<String, Set<UUID>> groups = new HashMap<>();
	public final BiMap<String, UUID> virtualBerries = HashBiMap.create(); //Map of string ids in chat, to virtual berry UUIDs, and back

	//Returns true if the added berry was new, and false if it was already in the map.
	public boolean addBerryIfNeeded(UUID berryUUID) {
		return berryInfo.putIfAbsent(berryUUID, new Berry()) == null;
	}

	//Returns true if the berry existed and was deleted successfully.
	public boolean deleteBerry(UUID berryUUID) {
		berryInfo.get(berryUUID).collectors.forEach(p -> {
			collectorInfo.get(p).remove(berryUUID);
			if (collectorInfo.get(p).isEmpty())
				collectorInfo.remove(p);
		});
		String group = berryInfo.get(berryUUID).group;
		if (group != null) {
			groups.get(group).remove(berryUUID);
			if (groups.get(group).isEmpty())
				groups.remove(group);
		}
		return berryInfo.remove(berryUUID) != null;
	}

	public void resetPlayer(UUID playerUUID) {
		berryInfo.values().forEach(berry -> berry.collectors.remove(playerUUID));
		collectorInfo.remove(playerUUID);
	}

	//Returns error feedback, this should be called only by commands
	//Returns null on success.
	public Text createVirtualBerry(String key) {
		if (virtualBerries.containsKey(key))
			return Text.translatable("limits_strawberries.command.berry_create_already_exists", key);
		UUID berryUUID;
		do berryUUID = UUID.randomUUID(); while (!addBerryIfNeeded(berryUUID));
		virtualBerries.put(key, berryUUID);
		return null;
	}

	//Bit flags.
	//1 bit means name changed,
	//2 bit means clue changed,
	//4 bit means desc changed,
	//8 bit means placer changed.
	public byte updateBerry(UUID berryUUID, @Nullable String name, @Nullable String clue, @Nullable String desc, @Nullable String placer, @Nullable String group) {
		if (!berryInfo.containsKey(berryUUID))
			return 0;
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

		if (group != null && !Objects.equals(berry.group, group)) {
			result |= 16;
			if (berry.group != null && groups.containsKey(berry.group)) {
				groups.get(berry.group).remove(berryUUID);
				if (groups.get(berry.group).isEmpty())
					groups.remove(berry.group);
			}
			berry.group = group;
			groups.computeIfAbsent(group, s -> new HashSet<>()).add(berryUUID);
		}

		return result;
	}

	//Returns true if the berry wasn't already collected by that player.
	//Returns false if the berry uuid given does not exist.
	public boolean collect(UUID berryUUID, UUID playerUUID) {
		if (!berryInfo.containsKey(berryUUID))
			return false;
		collectorInfo.computeIfAbsent(playerUUID, p -> new HashSet<>()).add(berryUUID);
		return berryInfo.get(berryUUID).collectors.add(playerUUID);
	}

	public boolean hasPlayerCollected(UUID berryUUID, UUID playerUUID) {
		return collectorInfo.computeIfAbsent(playerUUID, p -> new HashSet<>()).contains(berryUUID);
	}

	public boolean hasPlayerCompleted(String group, UUID playerUUID) {
		if (group == null)
			return false;
		if (!groups.containsKey(group))
			return true;
		if (!collectorInfo.containsKey(playerUUID))
			return false;
		Set<UUID> collected = collectorInfo.get(playerUUID);
		for (UUID berryUUID : groups.get(group))
			if (!collected.contains(berryUUID))
				return false;
		return true;
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
			if (entry.getValue().group != null)
				berryData.putString("group", entry.getValue().group);
			NbtList collectors = new NbtList();
			entry.getValue().collectors.stream().map(NbtHelper::fromUuid).forEach(collectors::add);
			berryData.put("collectors", collectors);
			data.put(entry.getKey().toString(), berryData);
		}
		result.put("data", data);

		NbtCompound virtual = new NbtCompound();
		for (Map.Entry<String, UUID> entry : virtualBerries.entrySet())
			virtual.putUuid(entry.getKey(), entry.getValue());
		result.put("virtual", virtual);

		return result;
	}

	public static class Berry {
		public String name, clue, desc, placer, group;
		public final Set<UUID> collectors = new HashSet<>();
	}

}
