package io.github.moonlight_maya.limits_strawberries;

import io.github.moonlight_maya.limits_strawberries.data.ServerBerryMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;

public class StrawberryMod implements ModInitializer {

	public static final String MODID = "limits_strawberries";
	public static ServerBerryMap SERVER_BERRIES;
	public static final Identifier ADD_PACKET_ID = new Identifier(MODID, "add");
	public static final Identifier DELETE_PACKET_ID = new Identifier(MODID, "delete");
	public static final Identifier NAME_PACKET_ID = new Identifier(MODID, "name");
	public static final Identifier CLUE_PACKET_ID = new Identifier(MODID, "clue");
	public static final Identifier COLLECT_PACKET_ID = new Identifier(MODID, "collect");
	public static final Identifier SYNC_PACKET_ID = new Identifier(MODID, "sync");

	public static final Identifier PERSISTENT_STORAGE = new Identifier(MODID, "persistent");

	public static final EntityType<StrawberryEntity> STRAWBERRY = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier(MODID, "strawberry"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, StrawberryEntity::new).dimensions(EntityDimensions.fixed(0.75f, 0.9f)).build()
	);

	@Override
	public void onInitialize(ModContainer mod) {
		ServerLifecycleEvents.READY.register(server -> {
			NbtCompound saveData = server.getDataCommandStorage().get(PERSISTENT_STORAGE);
			if (saveData.isEmpty()) saveData = null;
			SERVER_BERRIES = new ServerBerryMap(server, saveData);
		});
		ServerLifecycleEvents.STOPPING.register(server -> {
			NbtCompound saveData = SERVER_BERRIES.serialize();
			server.getDataCommandStorage().set(PERSISTENT_STORAGE, saveData);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			SERVER_BERRIES.syncTo(handler.getPlayer());
		});
	}
}
