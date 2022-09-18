package io.github.moonlight_maya.limits_strawberries;

import io.github.moonlight_maya.limits_strawberries.data.BerryMap;
import io.github.moonlight_maya.limits_strawberries.data.ServerBerryMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrawberryMod implements ModInitializer {

	public static final String MODID = "limits_strawberries";
	public static ServerBerryMap SERVER_BERRIES;
	public static final Identifier S2C_ADD_PACKET_ID = new Identifier(MODID, "s2c_add");
	public static final Identifier S2C_DELETE_PACKET_ID = new Identifier(MODID, "s2c_delete");
	public static final Identifier S2C_COLLECT_PACKET_ID = new Identifier(MODID, "s2c_collect");
	public static final Identifier S2C_SYNC_PACKET_ID = new Identifier(MODID, "s2c_sync");
	public static final Identifier S2C_UPDATE_PACKET_ID = new Identifier(MODID, "s2c_update");
	public static final Identifier S2C_RESET_PACKET_ID = new Identifier(MODID, "s2c_reset");
	public static final Identifier S2C_EDIT_SCREEN_PACKET_ID = new Identifier(MODID, "s2c_edit_screen");

	public static final Identifier C2S_UPDATE_PACKET_ID = new Identifier(MODID, "c2s_update");

	public static final Identifier PERSISTENT_STORAGE = new Identifier(MODID, "persistent");
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final Identifier COLLECT_SOUND_ID = new Identifier(MODID, "berry_collect");
	public static final SoundEvent COLLECT_SOUND = new SoundEvent(COLLECT_SOUND_ID);

	public static final Identifier GROUP_FINISH_SOUND_ID = new Identifier(MODID, "group_finish");
	public static final SoundEvent GROUP_FINISH_SOUND = new SoundEvent(GROUP_FINISH_SOUND_ID);

	public static final EntityType<StrawberryEntity> STRAWBERRY = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier(MODID, "strawberry"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, StrawberryEntity::new).dimensions(EntityDimensions.fixed(StrawberryEntity.WIDTH, StrawberryEntity.HEIGHT)).build()
	);

	public static final StrawberryItem ITEM = new StrawberryItem(new QuiltItemSettings().group(ItemGroup.MISC));

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.ITEM, new Identifier(MODID, "strawberry"), ITEM);
		Registry.register(Registry.SOUND_EVENT, COLLECT_SOUND_ID, COLLECT_SOUND);
		Registry.register(Registry.SOUND_EVENT, GROUP_FINISH_SOUND_ID, GROUP_FINISH_SOUND);

		ServerPlayNetworking.registerGlobalReceiver(C2S_UPDATE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			if (!player.isCreative()) {
				player.sendSystemMessage(Text.translatable("limits_strawberries.entity.berry_interact_gamemode_fail"));
				return;
			}
			int entityId = buf.readInt();
			Entity entity = player.world.getEntityById(entityId);
			if (entity instanceof StrawberryEntity strabby) {
				byte flags = buf.readByte();
				String name = null, clue = null, desc = null, group = null;
				if ((flags & 1) > 0)
					name = buf.readString(BerryMap.NAME_MAX);
				if ((flags & 2) > 0)
					clue = buf.readString(BerryMap.CLUE_MAX);
				if ((flags & 4) > 0)
					desc = buf.readString(BerryMap.DESC_MAX);
				if ((flags & 16) > 0)
					group = buf.readString(BerryMap.GROUP_MAX);

				SERVER_BERRIES.updateBerry(strabby.getUuid(), name, clue, desc, null, group);
				LOGGER.info("Update made to berry {}, by {} (UUID {}). Name = {}, Clue = {}, Desc = {}, Group = {}",
						entity.getUuid().toString(),
						player.getEntityName(),
						player.getUuid().toString(),
						name,
						clue,
						desc,
						group
				);
			}
		});

		BerryCommands.init();

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
