package io.github.moonlight_maya.limits_strawberries.client.screens;

import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public abstract class DataJournalScreen<T extends DataJournalScreen.Entry> extends JournalScreen {

	protected static final int LEFT_MARGIN = 20;
	protected static final int RIGHT_MARGIN = 20;
	protected static final int HEADER_SPACE = 15;
	protected static final int FOOTER_SPACE = 15;

	protected static final int ENTRY_WIDTH = (SCREEN_WIDTH / 2) - LEFT_MARGIN - RIGHT_MARGIN;
	protected static final int ENTRY_HEIGHT = 15;
	protected static final int ENTRIES_PER_PAGE = (SCREEN_HEIGHT - HEADER_SPACE - FOOTER_SPACE) / ENTRY_HEIGHT;


	private static final int PAGE_BUTTON_WIDTH = 22;
	private static final int PAGE_BUTTON_HEIGHT = 11;
	private static final int LEFT_BUTTON_X = 5;
	private static final int RIGHT_BUTTON_X = SCREEN_WIDTH - RIGHT_MARGIN - PAGE_BUTTON_WIDTH;
	private static final int PAGE_BUTTON_Y = SCREEN_HEIGHT - 2 - PAGE_BUTTON_HEIGHT;
	private static final int LEFT_BUTTON_U = SCREEN_WIDTH + MainJournalScreen.BUTTON_WIDTH + BerryJournalScreen.BerryEntry.ICON_SIZE;
	private static final int RIGHT_BUTTON_U = LEFT_BUTTON_U + PAGE_BUTTON_WIDTH;
	private static final int PAGE_BUTTON_V = 0;

	private final T[] entries;
	private int currentPage = 0;
	private int numPages;
	private List<OrderedText> tooltip;
	private TexturedButtonWidget leftButton, rightButton;
	protected boolean mouseDown;
	protected DataJournalScreen(Text text) {
		super(text);
		entries = createEntries();
		numPages = (int) Math.ceil(entries.length * 1d / ENTRIES_PER_PAGE) + 1;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		mouseDown = true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		mouseDown = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void init() {
		super.init();
		//Constructor after studying:
		//x, y, width, height, u, v, hoveredVOffset, texture, texWidth, texHeight, onClick
		leftButton = addDrawableChild(new TexturedButtonWidget(anchorX() + LEFT_BUTTON_X, anchorY() + PAGE_BUTTON_Y, PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT, LEFT_BUTTON_U, PAGE_BUTTON_V, PAGE_BUTTON_HEIGHT, TEXTURE, TEX_WIDTH, TEX_HEIGHT, button -> {
			currentPage -= 2;
			leftButton.visible = currentPage > 0;
			rightButton.visible = numPages - currentPage > 2;
		}));
		rightButton = addDrawableChild(new TexturedButtonWidget(anchorX() + RIGHT_BUTTON_X, anchorY() + PAGE_BUTTON_Y, PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT, RIGHT_BUTTON_U, PAGE_BUTTON_V, PAGE_BUTTON_HEIGHT, TEXTURE, TEX_WIDTH, TEX_HEIGHT, button -> {
			currentPage += 2;
			leftButton.visible = currentPage > 0;
			rightButton.visible = numPages - currentPage > 2;
		}));
		leftButton.visible = currentPage > 0;
		rightButton.visible = numPages - currentPage > 2;
	}

	protected abstract T[] createEntries();

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderJournalBackground(matrices);

		//Render and assign tooltip
		tooltip = null;
		renderPage(matrices, mouseX, mouseY, currentPage);
		renderPage(matrices, mouseX, mouseY, currentPage + 1);

		//Render tooltip
		if (tooltip != null)
			renderOrderedTooltip(matrices, tooltip, mouseX, mouseY);

		super.render(matrices, mouseX, mouseY, delta);
	}

	private void renderPage(MatrixStack matrices, int mouseX, int mouseY, int index) {
		if (index == 0) {
			return;
		}

		boolean left = index % 2 == 0;
		int x = anchorX() + LEFT_MARGIN;
		if (!left) x += SCREEN_WIDTH / 2;
		int y = anchorY() + HEADER_SPACE;

		int firstEntry = (index-1) * ENTRIES_PER_PAGE;
		for (int i = firstEntry; i < firstEntry + ENTRIES_PER_PAGE && i < entries.length; i++) {
			List<OrderedText> tooltipResult = entries[i].render(matrices, x, y, getZOffset(), mouseX, mouseY, mouseDown);
			if (tooltipResult != null)
				tooltip = tooltipResult;
			y += ENTRY_HEIGHT;
		}
	}

	protected interface Entry {
		List<OrderedText> render(MatrixStack matrices, int x, int y, int z, int mouseX, int mouseY, boolean mouseDown);
	}
}
