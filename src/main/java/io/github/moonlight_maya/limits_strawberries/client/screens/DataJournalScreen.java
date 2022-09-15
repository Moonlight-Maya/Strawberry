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
	private int hoveredEntry = -1;
	private TexturedButtonWidget leftButton, rightButton;
	protected DataJournalScreen(Text text) {
		super(text);
		entries = createEntries();
		numPages = (int) Math.ceil(entries.length * 1d / ENTRIES_PER_PAGE) + 1;
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

		hoveredEntry = -1;
		if (mouseY > anchorY() + HEADER_SPACE && mouseY < anchorY() + HEADER_SPACE + ENTRY_HEIGHT * ENTRIES_PER_PAGE) {
			//In correct Y range...
			int entryIndexOnPage = (mouseY - anchorY() - HEADER_SPACE) / ENTRY_HEIGHT;
			if (currentPage > 0 && mouseX > anchorX() + LEFT_MARGIN && mouseX < anchorX() + SCREEN_WIDTH / 2 - RIGHT_MARGIN) {
				//We may be hovering an entry on the left page.
				hoveredEntry = (currentPage - 1) * ENTRIES_PER_PAGE + entryIndexOnPage;
			} else if (mouseX > anchorX() + SCREEN_WIDTH / 2 + LEFT_MARGIN && mouseX < anchorX() + SCREEN_WIDTH - RIGHT_MARGIN) {
				//We may be hovering an entry on the right page.
				hoveredEntry = currentPage * ENTRIES_PER_PAGE + entryIndexOnPage;
			}
		}

		renderPage(matrices, currentPage);
		renderPage(matrices, currentPage + 1);

		if (hoveredEntry > -1 && hoveredEntry < entries.length) {
			//Render hover text
			List<OrderedText> hoverText = entries[hoveredEntry].getHoverText();

			renderOrderedTooltip(matrices, hoverText, mouseX, mouseY);
			hoveredEntry = -1;
		}

		super.render(matrices, mouseX, mouseY, delta);
	}

	private void renderPage(MatrixStack matrices, int index) {
		if (index == 0) {
			return;
		}

		boolean left = index % 2 == 0;
		int x = anchorX() + LEFT_MARGIN;
		if (!left) x += SCREEN_WIDTH / 2;
		int y = anchorY() + HEADER_SPACE;

		int firstEntry = (index-1) * ENTRIES_PER_PAGE;
		for (int i = firstEntry; i < firstEntry + ENTRIES_PER_PAGE && i < entries.length; i++) {
			entries[i].render(matrices, x, y, getZOffset(), i == hoveredEntry);
			y += ENTRY_HEIGHT;
		}
	}

	protected interface Entry {
		List<OrderedText> getHoverText();
		void render(MatrixStack matrices, int x, int y, int z, boolean hovered);
	}
}
