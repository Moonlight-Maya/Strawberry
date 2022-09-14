package io.github.moonlight_maya.limits_strawberries.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.moonlight_maya.limits_strawberries.StrawberryEntity;
import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class StrawberryEntityRenderer extends EntityRenderer<StrawberryEntity> {

	private static final Identifier TEXTURE_UNCOLLECTED = new Identifier(StrawberryMod.MODID, "textures/entity/strawberry/strawberry.png");
	private static final Identifier TEXTURE_COLLECTED = new Identifier(StrawberryMod.MODID, "textures/entity/strawberry/strawberry_collected.png");
	private final RenderLayer UNCOLLECTED_LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE_UNCOLLECTED);
	private final RenderLayer COLLECTED_LAYER = RenderLayer.getEntityTranslucent(TEXTURE_COLLECTED);

	private final ModelPart root;

	protected StrawberryEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.shadowRadius = 0.375f;
		this.root = context.getPart(StrawberryModClient.MODEL_STRAWBERRY_LAYER);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		modelData.getRoot()
		.addChild("berry", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-3, 5, -3, 6, 8, 6)
				.uv(0, 14).cuboid(-2, 3, -2, 4, 2, 4)
				, ModelTransform.pivot(0, 0, 0)
		).addChild("leaves", ModelPartBuilder.create()
				.uv(12, 14).cuboid(-3, 13, 0, 6, 3, 0)
				.uv(12, 8).cuboid(0, 13, -3, 0, 3, 6)
				, ModelTransform.of(0, 0, 0, 0, (float) Math.PI/4, 0)
		);
		return TexturedModelData.of(modelData, 32, 32);
	}

	@Override
	public void render(StrawberryEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		float time = entity.world.getTime() + tickDelta;
		root.yaw = (time / 5) % 360;
		root.pivotY = (float) (Math.sin(time * 2 * Math.PI / 40) * 1.5);

		boolean collected = isCollected(entity);
		VertexConsumer consumer = vertexConsumers.getBuffer(collected ? COLLECTED_LAYER : UNCOLLECTED_LAYER);
		root.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, collected ? 0.5f : 1f);
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}

	@Override
	public Identifier getTexture(StrawberryEntity entity) {
		return isCollected(entity) ? TEXTURE_COLLECTED : TEXTURE_UNCOLLECTED;
	}

	private static boolean isCollected(StrawberryEntity entity) {
		ClientPlayerEntity cpe = MinecraftClient.getInstance().player;
		if (cpe == null)
			return false;
		return StrawberryModClient.CLIENT_BERRIES.hasPlayerCollected(entity.getUuid(), cpe.getUuid());
	}
}
