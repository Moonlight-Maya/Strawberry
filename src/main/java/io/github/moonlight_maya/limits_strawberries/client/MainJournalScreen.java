package io.github.moonlight_maya.limits_strawberries.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class MainJournalScreen extends Screen {

	public static final Identifier TEXTURE = new Identifier(StrawberryMod.MODID, "textures/gui/journal.png");
	public static final int TEX_WIDTH = 512;
	public static final int TEX_HEIGHT = 512;
	public static final int SCREEN_WIDTH = 320;
	public static final int SCREEN_HEIGHT = 240;
	public static final int LINE_SPACING = 9;


	private static final int BUTTON_WIDTH = 120;
	private static final int BUTTON_HEIGHT = 90;

	private static final int HORIZ_PADDING = (SCREEN_WIDTH / 2 - BUTTON_WIDTH) / 2;
	private static final int VERT_PADDING = (SCREEN_HEIGHT - 2 * BUTTON_HEIGHT) / 3;
	private static final int BUTTON_X = SCREEN_WIDTH / 2 + HORIZ_PADDING;
	private static final int BUTTON1_Y = VERT_PADDING;
	private static final int BUTTON2_Y = BUTTON1_Y + BUTTON_HEIGHT + VERT_PADDING;

	private static final int BUTTON1_U = SCREEN_WIDTH;
	private static final int BUTTON1_V = 0;
	private static final int BUTTON2_U = SCREEN_WIDTH;
	private static final int BUTTON2_V = BUTTON_HEIGHT + BUTTON_HEIGHT;

	public MainJournalScreen() {
		super(Text.literal("Journal"));
	}

	@Override
	protected void init() {
		super.init();
		//Constructor after studying:
		//x, y, width, height, u, v, hoveredVOffset, texture, texWidth, texHeight, onClick
		addDrawableChild(new TexturedButtonWidget(BUTTON_X, BUTTON1_Y, BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON1_U, BUTTON1_V, BUTTON_HEIGHT, TEXTURE, TEX_WIDTH, TEX_HEIGHT, button -> {
			if (client != null) client.setScreen(new BerryJournalScreen());
		}));
		addDrawableChild(new TexturedButtonWidget(BUTTON_X, BUTTON2_Y, BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON2_U, BUTTON2_V, BUTTON_HEIGHT, TEXTURE, TEX_WIDTH, TEX_HEIGHT, button -> {
			if (client != null) client.setScreen(new PlayerJournalScreen());
		}));
	}

	public static void renderJournalBackground(Screen screen, MatrixStack matrices) {
		screen.renderBackground(matrices);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int x = (screen.width - SCREEN_WIDTH) / 2;
		int y = (screen.height - SCREEN_HEIGHT) / 2;
		//The mappings are lying. textureHeight is the width, and textureWidth is the height.
		DrawableHelper.drawTexture(matrices, x, y, screen.getZOffset(), 0f, 0f, SCREEN_WIDTH, SCREEN_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderJournalBackground(this, matrices);
		StringVisitable text = StringVisitable.plain("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus eleifend ligula sit amet lorem facilisis, vitae mattis enim consectetur. Fusce sit amet ex efficitur, sodales lacus eget, porttitor massa. Nunc a eros eget purus auctor ornare. Integer blandit metus sed orci auctor, egestas bibendum tellus pellentesque. Integer molestie felis justo, sed suscipit velit tristique et. Duis ultricies arcu ipsum, vel faucibus erat molestie vitae. Pellentesque quis ultricies mauris, et blandit sapien. Nam congue lorem lorem, ac fringilla ante volutpat sit amet. Ut dictum malesuada gravida. Aenean sed erat vitae orci bibendum elementum. Donec in elit eu neque commodo lacinia. Aenean sit amet mattis justo. Suspendisse porttitor tellus vel sodales euismod. Nullam aliquet ac odio vitae suscipit.");
		List<OrderedText> lines = textRenderer.wrapLines(text, SCREEN_WIDTH / 2 - HORIZ_PADDING * 2);
		int y = (height - SCREEN_HEIGHT) / 2 + VERT_PADDING;
		for (OrderedText line : lines) {
			textRenderer.draw(matrices, line, (width - SCREEN_WIDTH) / 2 + HORIZ_PADDING, y, 0);
			y += LINE_SPACING;
		}

		super.render(matrices, mouseX, mouseY, delta);
	}
}
