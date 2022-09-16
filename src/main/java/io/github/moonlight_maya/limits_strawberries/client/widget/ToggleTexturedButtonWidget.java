package io.github.moonlight_maya.limits_strawberries.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moonlight_maya.limits_strawberries.client.screens.JournalScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ToggleTexturedButtonWidget extends TexturedButtonWidget {

	private final int baseU, baseV;
	private boolean enabled;
	private final Text hoverTooltip;
	private final Runnable onClick;

	public ToggleTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Text hoverTooltip, Runnable onClick) {
		//Constructor: x, y, width, height, u, v, hoveredVOffset, texture, texWidth, texHeight, onClick
		super(x, y, width, height, u, v, height, JournalScreen.TEXTURE, JournalScreen.TEX_WIDTH, JournalScreen.TEX_HEIGHT, ToggleTexturedButtonWidget::pressAction);
		baseU = u;
		baseV = v;
		this.hoverTooltip = hoverTooltip;
		this.onClick = onClick;
	}

	private static void pressAction(ButtonWidget button) {
		ToggleTexturedButtonWidget w = (ToggleTexturedButtonWidget) button;
		w.enabled = !w.enabled;
		w.onClick.run();
	}

	public boolean getEnabled() {
		return enabled;
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int u = baseU + (enabled ? width : 0);
		int v = baseV + (hovered ? height : 0);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, JournalScreen.TEXTURE);
		RenderSystem.enableDepthTest();
		drawTexture(matrices, this.x, this.y, u, v, this.width, this.height, JournalScreen.TEX_WIDTH, JournalScreen.TEX_HEIGHT);
	}

	public Text getTooltip() {
		return hovered ? hoverTooltip : null;
	}
}
