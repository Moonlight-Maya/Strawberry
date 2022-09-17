package io.github.moonlight_maya.limits_strawberries.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moonlight_maya.limits_strawberries.client.screens.JournalScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;

public class SortButtonWidget<T> extends TexturedButtonWidget {

	private int index = 0;
	private final ArrayList<SortOption<T>> options;
	private final Runnable onClick;
	private final int baseU, baseV;

	public SortButtonWidget(int x, int y, int width, int height, int u, int v, ArrayList<SortOption<T>> options, Runnable onClick) {
		//Constructor: x, y, width, height, u, v, hoveredVOffset, texture, texWidth, texHeight, onClick
		super(x, y, width, height, u, v, height, JournalScreen.TEXTURE, JournalScreen.TEX_WIDTH, JournalScreen.TEX_HEIGHT, SortButtonWidget::pressAction);
		this.options = options;
		baseU = u;
		baseV = v;
		this.onClick = onClick;
	}

	private static void pressAction(ButtonWidget buttonWidget) {
		SortButtonWidget<?> w = (SortButtonWidget<?>) buttonWidget; //Cast
		w.index = (w.index + 1) % w.options.size();
		w.onClick.run();
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int u = baseU + width * index;
		int v = baseV + (hovered ? height : 0);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, JournalScreen.TEXTURE);
		RenderSystem.enableDepthTest();
		drawTexture(matrices, this.x, this.y, u, v, this.width, this.height, JournalScreen.TEX_WIDTH, JournalScreen.TEX_HEIGHT);
	}

	public Text getTooltip() {
		return hovered ? options.get(index).tooltip : null;
	}

	public Comparator<T> getCompareFunc() {
		return options.get(index).comparator;
	}

	public static class SortOption<T> {
		public final Comparator<T> comparator;
		public final Text tooltip;
		public SortOption(Comparator<T> comparator, Text tooltip) {
			this.comparator = comparator;
			this.tooltip = tooltip;
		}
	}
}
