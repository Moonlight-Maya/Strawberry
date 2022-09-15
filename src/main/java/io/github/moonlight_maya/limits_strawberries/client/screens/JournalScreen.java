package io.github.moonlight_maya.limits_strawberries.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class JournalScreen extends Screen {

	public static final Identifier TEXTURE = new Identifier(StrawberryMod.MODID, "textures/gui/journal.png");
	public static final int TEX_WIDTH = 512;
	public static final int TEX_HEIGHT = 512;
	public static final int SCREEN_WIDTH = 320;
	public static final int SCREEN_HEIGHT = 240;
	public static final int LINE_SPACING = 9;


	protected JournalScreen(Text text) {
		super(text);
	}

	protected int anchorX() {
		return (width - SCREEN_WIDTH) / 2;
	}

	protected int anchorY() {
		return (height - SCREEN_HEIGHT) / 2;
	}

	protected void renderJournalBackground(MatrixStack matrices) {
		renderBackground(matrices);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		//The mappings are lying. textureHeight is the width, and textureWidth is the height.
		DrawableHelper.drawTexture(matrices, anchorX(), anchorY(), getZOffset(), 0f, 0f, SCREEN_WIDTH, SCREEN_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
	}
}
