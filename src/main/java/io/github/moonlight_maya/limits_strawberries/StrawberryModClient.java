package io.github.moonlight_maya.limits_strawberries;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class StrawberryModClient implements ClientModInitializer {

	public static final EntityModelLayer MODEL_STRAWBERRY_LAYER = new EntityModelLayer(new Identifier(StrawberryMod.MODID, "strawberry"), "main");

	@Override
	public void onInitializeClient(ModContainer mod) {

		EntityRendererRegistry.register(StrawberryMod.STRAWBERRY, StrawberryEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(MODEL_STRAWBERRY_LAYER, StrawberryEntityRenderer::getTexturedModelData);
	}
}
