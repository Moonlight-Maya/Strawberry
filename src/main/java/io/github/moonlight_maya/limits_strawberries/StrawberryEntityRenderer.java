package io.github.moonlight_maya.limits_strawberries;

import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class StrawberryEntityRenderer extends EntityRenderer<StrawberryEntity> {

	private static final Identifier TEXTURE = new Identifier(StrawberryMod.MODID, "textures/entity/strawberry/strawberry.png");
	private final RenderLayer RENDER_LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE);

	private ModelPart root;

	protected StrawberryEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.shadowRadius = 0.375f;
		this.root = context.getPart(StrawberryModClient.MODEL_STRAWBERRY_LAYER);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		modelData.getRoot()
		.addChild("berry", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-3, 4, -3, 6, 8, 6)
				.uv(0, 14).cuboid(-2, 2, -2, 4, 2, 4)
				, ModelTransform.pivot(0, 0, 0)
		).addChild("leaves", ModelPartBuilder.create()
				.uv(12, 14).cuboid(-3, 12, 0, 6, 3, 0)
				.uv(12, 8).cuboid(0, 12, -3, 0, 3, 6)
				, ModelTransform.of(0, 0, 0, 0, (float) Math.PI/4, 0)
		);
		return TexturedModelData.of(modelData, 32, 32);
	}

	@Override
	public void render(StrawberryEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		root.render(matrices, vertexConsumers.getBuffer(RENDER_LAYER), light, OverlayTexture.DEFAULT_UV);
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}

	@Override
	public Identifier getTexture(StrawberryEntity entity) {
		return TEXTURE;
	}
}
