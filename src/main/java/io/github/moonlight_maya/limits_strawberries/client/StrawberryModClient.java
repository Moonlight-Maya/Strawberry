package io.github.moonlight_maya.limits_strawberries.client;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.moonlight_maya.limits_strawberries.StrawberryEntity;
import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import io.github.moonlight_maya.limits_strawberries.client.screens.BerryEditScreen;
import io.github.moonlight_maya.limits_strawberries.client.screens.BerryJournalScreen;
import io.github.moonlight_maya.limits_strawberries.data.BerryMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.UUID;

public class StrawberryModClient implements ClientModInitializer {

	public static final EntityModelLayer MODEL_STRAWBERRY_LAYER = new EntityModelLayer(new Identifier(StrawberryMod.MODID, "strawberry"), "main");
	public static BerryMap CLIENT_BERRIES;
	private static NbtCompound IN_PROGRESS_BERRY_INFO;

	public static KeyBind keyBinding;

	@Override
	public void onInitializeClient(ModContainer mod) {

		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBind(
				"key." + StrawberryMod.MODID + ".journal",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_J,
				"category." + StrawberryMod.MODID + ".journal"
		));

		ClientTickEvents.END.register(client -> {
			while (keyBinding.wasPressed())
				if (client.currentScreen == null)
					client.setScreen(new BerryJournalScreen());
			StrawberryEntityRenderer.CLIENT_TICKS++;
		});

		EntityRendererRegistry.register(StrawberryMod.STRAWBERRY, StrawberryEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(MODEL_STRAWBERRY_LAYER, StrawberryEntityRenderer::getTexturedModelData);

		CLIENT_BERRIES = new BerryMap();

		ClientPlayNetworking.registerGlobalReceiver(StrawberryMod.S2C_ADD_PACKET_ID, (client, handler, buf, responseSender) -> {
			CLIENT_BERRIES.addBerryIfNeeded(buf.readUuid());
		});

		ClientPlayNetworking.registerGlobalReceiver(StrawberryMod.S2C_DELETE_PACKET_ID, (client, handler, buf, responseSender) -> {
			CLIENT_BERRIES.deleteBerry(buf.readUuid());
		});

		ClientPlayNetworking.registerGlobalReceiver(StrawberryMod.S2C_RESET_PACKET_ID, (client, handler, buf, responseSender) -> {
			CLIENT_BERRIES.resetPlayer(buf.readUuid());
		});

		ClientPlayNetworking.registerGlobalReceiver(StrawberryMod.S2C_UPDATE_PACKET_ID, (client, handler, buf, responseSender) -> {
			UUID berryUUID = buf.readUuid();
			byte flags = buf.readByte();
			String name = null, clue = null, desc = null, placer = null, group = null;
			if ((flags & 1) > 0)
				name = buf.readString();
			if ((flags & 2) > 0)
				clue = buf.readString();
			if ((flags & 4) > 0)
				desc = buf.readString();
			if ((flags & 8) > 0)
				placer = buf.readString();
			if ((flags & 16) > 0)
				group = buf.readString();
			CLIENT_BERRIES.updateBerry(berryUUID, name, clue, desc, placer, group);
		});

		ClientPlayNetworking.registerGlobalReceiver(StrawberryMod.S2C_COLLECT_PACKET_ID, (client, handler, buf, responseSender) -> {
			CLIENT_BERRIES.collect(buf.readUuid(), buf.readUuid());
		});

		ClientPlayNetworking.registerGlobalReceiver(StrawberryMod.S2C_SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
			NbtCompound info = buf.readNbt();
			boolean complete = buf.readBoolean();
			if (info == null) return; //Don't know when this happens, but apparently it can, according to annotations

			//If this is the first packet in the chain:
			if (IN_PROGRESS_BERRY_INFO == null) {
				//If there is only one packet in the chain, load directly and end.
				if (complete) {
//					System.out.println("Received sync packet with " + info.getCompound("data").getSize() + " data and " + info.getCompound("virtual").getSize() + " virtual berries.");
					CLIENT_BERRIES.loadFrom(info);
					return;
				}
				//Otherwise, prepare the compound builder.
				IN_PROGRESS_BERRY_INFO = new NbtCompound();
				IN_PROGRESS_BERRY_INFO.put("data", new NbtCompound());
				IN_PROGRESS_BERRY_INFO.put("virtual", new NbtCompound());
			}

			//Read packet info, copy into our builder.
			NbtCompound newData = info.getCompound("data");
			NbtCompound localData = IN_PROGRESS_BERRY_INFO.getCompound("data");
			NbtCompound newVirtual = info.getCompound("virtual");
			NbtCompound localVirtual = IN_PROGRESS_BERRY_INFO.getCompound("virtual");
			for (String key : newData.getKeys())
				localData.put(key, newData.get(key));
			for (String key : newVirtual.getKeys())
				localVirtual.put(key, newVirtual.get(key));

			//If we're done, then load, and delete the builder.
			if (complete) {
				CLIENT_BERRIES.loadFrom(IN_PROGRESS_BERRY_INFO);
				IN_PROGRESS_BERRY_INFO = null;
			}
//			System.out.println("Received sync packet with " + newData.getSize() + " data and " + newVirtual.getSize() + " virtual berries.");
		});

		ClientPlayNetworking.registerGlobalReceiver(StrawberryMod.S2C_EDIT_SCREEN_PACKET_ID, (client, handler, buf, responseSender) -> {
			World world = MinecraftClient.getInstance().world;
			if (world != null) {
				Entity e = world.getEntityById(buf.readInt());
				if (e instanceof StrawberryEntity s)
					client.execute(() -> MinecraftClient.getInstance().setScreen(new BerryEditScreen(s)));
			}
		});

	}
}
