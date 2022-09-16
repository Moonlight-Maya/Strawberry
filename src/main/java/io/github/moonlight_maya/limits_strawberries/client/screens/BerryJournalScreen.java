package io.github.moonlight_maya.limits_strawberries.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.moonlight_maya.limits_strawberries.client.StrawberryModClient;
import io.github.moonlight_maya.limits_strawberries.client.widget.SortButtonWidget;
import io.github.moonlight_maya.limits_strawberries.client.widget.ToggleTexturedButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BerryJournalScreen extends DataJournalScreen<BerryJournalScreen.BerryEntry> {

	protected BerryJournalScreen() {
		super(Text.literal("Berries"));
	}

	private static final ArrayList<SortButtonWidget.SortOption<BerryEntry>> SORT_OPTIONS = new ArrayList<>() {{
		add(new SortButtonWidget.SortOption<>(Comparator.comparing(entry -> entry.name), Text.literal("Name")));
		add(new SortButtonWidget.SortOption<>(Comparator.comparing(entry -> !entry.collected), Text.literal("You Collected")));
		add(new SortButtonWidget.SortOption<>(Comparator.comparing(entry -> -entry.collectedBy), Text.literal("Most Collected")));
	}};
	private static final int BUTTON_WIDTH = 15;
	private static final int BUTTON_HEIGHT = 15;
	private static final int SORT_BUTTON_U = SCREEN_WIDTH + MainJournalScreen.BUTTON_WIDTH + BerryEntry.ICON_SIZE;
	private static final int SORT_BUTTON_V = DataJournalScreen.PAGE_BUTTON_HEIGHT * 2;
	private static final int REVERSE_BUTTON_U = SORT_BUTTON_U;
	private static final int REVERSE_BUTTON_V = SORT_BUTTON_V + 2 * BUTTON_HEIGHT;

	private SortButtonWidget<BerryEntry> sortWidget;
	private ToggleTexturedButtonWidget reverseWidget;

	private BerryEntry rarestBerry = null;
	private int berriesCollected;
	private int totalBerries;
	private int leaderboardPosition;
	@Override
	protected void init() {
		super.init();
		Runnable reSort = () -> reSort(sortWidget.getCompareFunc(), reverseWidget.getEnabled());
		sortWidget = addDrawableChild(new SortButtonWidget<>(anchorX() + LEFT_MARGIN, 0, BUTTON_WIDTH, BUTTON_HEIGHT, SORT_BUTTON_U, SORT_BUTTON_V, SORT_OPTIONS, reSort));
		reverseWidget = addDrawableChild(new ToggleTexturedButtonWidget(anchorX() + LEFT_MARGIN, 0, BUTTON_WIDTH, BUTTON_HEIGHT, REVERSE_BUTTON_U, REVERSE_BUTTON_V, Text.literal("Reverse Sort"), reSort));
		reSort.run();

		int rarity = Integer.MAX_VALUE;
		for (BerryEntry entry : baseEntries) {
			totalBerries++;
			if (entry.collected) {
				berriesCollected++;
				if (entry.collectedBy < rarity) {
					rarity = entry.collectedBy;
					rarestBerry = entry;
				}
			}
		}
		leaderboardPosition = (int) StrawberryModClient.CLIENT_BERRIES.collectorInfo.values().stream().filter(s -> s.size() > berriesCollected).count() + 1;
	}

	@Override
	protected BerryEntry[] createEntries() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null)
			return new BerryEntry[0];
		return StrawberryModClient.CLIENT_BERRIES.berryInfo.values().stream().map(berry -> {
			BerryEntry entry = new BerryEntry();
			entry.name = berry.name;
			entry.name = "Berry " + (int) Math.floor(Math.random() * 1000000);
			entry.placer = "Command";
			entry.desc = "This is a desc. It has many, many, many, many, many, many, many, many, many, many, many, many, many, many, many, many, many, many, many, many, lines.";
			entry.clue = berry.clue;
			entry.clue = "Bruh berries are covering the entire platform";
			entry.collected = berry.collectors.contains(player.getUuid());
			entry.collectedBy = berry.collectors.size();
			return entry;
		}).toList().toArray(new BerryEntry[0]);
	}

	private static final int BODY_1_Y = HEADER_SPACE + 15;

	@Override
	protected void renderPageZero(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		//Draw title
		Text title = Text.literal("My Strawberries").setStyle(Style.EMPTY.withUnderline(true));
		textRenderer.draw(matrices, title, anchorX() + SCREEN_WIDTH / 4 - textRenderer.getWidth(title) / 2, anchorY() + HEADER_SPACE, 0);

		StringVisitable body1 = StringVisitable.plain("A place for you to track your berry progress and search for new ones!");
		List<OrderedText> lines = textRenderer.wrapLines(body1, SCREEN_WIDTH / 2 - LEFT_MARGIN - RIGHT_MARGIN);
		int x = anchorX() + LEFT_MARGIN;
		int y = anchorY() + BODY_1_Y;
		for (OrderedText line : lines) {
			textRenderer.draw(matrices, line, x, y, 0);
			y += LINE_SPACING;
		}
		y += LINE_SPACING;
		y = renderSorting(matrices, x, y, mouseX, mouseY, delta);
		y = renderStats(matrices, y, mouseX, mouseY);
		Text tooltip = sortWidget.getTooltip();
		if (tooltip != null) renderTooltip(matrices, tooltip, mouseX, mouseY);
		tooltip = reverseWidget.getTooltip();
		if (tooltip != null) renderTooltip(matrices, tooltip, mouseX, mouseY);

	}

	//Returns new y
	private int renderSorting(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		Text options = Text.literal("Sorting Options: ");
		int optionWidth = textRenderer.getWidth(options);
		int spacing = 5;
		int textOffset = (BUTTON_HEIGHT - textRenderer.fontHeight) / 2;
		textRenderer.draw(matrices, options, x, y + textOffset, 0);
		x += optionWidth;
		x += spacing;
		sortWidget.setPos(x, y);
		x += BUTTON_WIDTH;
		x += spacing;
		reverseWidget.setPos(x, y);
		y += BUTTON_HEIGHT;
		return y + 6;
	}

	private static final int PROGRESS_BAR_WIDTH = 120;
	private static final int PROGRESS_BAR_HEIGHT = 20;
	private static final int PROGRESS_BAR_FRAME_U = 0;
	private static final int PROGRESS_BAR_FRAME_V = SCREEN_HEIGHT;
	private static final int PROGRESS_BAR_U = PROGRESS_BAR_WIDTH;
	private static final int PROGRESS_BAR_V = SCREEN_HEIGHT;
	private static final int PROGRESS_BAR_X = (SCREEN_WIDTH / 2 - PROGRESS_BAR_WIDTH) / 2;

	//Return new y
	private int renderStats(MatrixStack matrices, int y, int mouseX, int mouseY) {

		Text stats = Text.literal("Stats: ");
		int x = anchorX() + LEFT_MARGIN;
		textRenderer.draw(matrices, stats, x, y, 0);
		y += textRenderer.fontHeight + 4;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, JournalScreen.TEXTURE);
		RenderSystem.enableDepthTest();
		int fillWidth = (int) ((double) berriesCollected / totalBerries * PROGRESS_BAR_WIDTH);
		drawTexture(matrices, anchorX() + PROGRESS_BAR_X, y, PROGRESS_BAR_U, PROGRESS_BAR_V, fillWidth, PROGRESS_BAR_HEIGHT, JournalScreen.TEX_WIDTH, JournalScreen.TEX_HEIGHT);
		drawTexture(matrices, anchorX() + PROGRESS_BAR_X, y, PROGRESS_BAR_FRAME_U, PROGRESS_BAR_FRAME_V, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT, JournalScreen.TEX_WIDTH, JournalScreen.TEX_HEIGHT);

		y += (PROGRESS_BAR_HEIGHT - textRenderer.fontHeight) / 2 + 1;
		Text fraction = Text.literal(berriesCollected + " / " + totalBerries);
		x = anchorX() + SCREEN_WIDTH / 4 - textRenderer.getWidth(fraction) / 2;
		textRenderer.draw(matrices, fraction, x, y, 0);
		y += PROGRESS_BAR_HEIGHT - (PROGRESS_BAR_HEIGHT - textRenderer.fontHeight) / 2 - 1;

		y += 6;
		x = anchorX() + LEFT_MARGIN;
		MutableText rarest = Text.literal("Rarest: ");
		if (rarestBerry == null)
			rarest.append(Text.literal("N/A").setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
		else
			rarest.append(Text.literal(rarestBerry.name).setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
		textRenderer.draw(matrices, rarest, x, y, 0);
		y += textRenderer.fontHeight;

		y += 6;
		x = anchorX() + LEFT_MARGIN;
		MutableText place = Text.literal("Leaderboard: ");
		place.append(Text.literal("#" + leaderboardPosition).setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
		textRenderer.draw(matrices, place, x, y, 0);
		y += textRenderer.fontHeight;

		return y + 5;
	}

	@Override
	protected void nextPage() {
		super.nextPage();
		sortWidget.visible = false;
		reverseWidget.visible = false;
	}

	@Override
	protected void prevPage() {
		super.prevPage();
		if (currentPage == 0) {
			sortWidget.visible = true;
			reverseWidget.visible = true;
		}
	}



	public class BerryEntry implements Entry {
		private boolean collected;
		private String name, clue, desc, placer;
		private int collectedBy;

		private boolean clueRevealed;

		private static final int HORIZ_SPACING = 5;
		public static final int ICON_SIZE = 11;
		private static final int TEXT_Y = (ENTRY_HEIGHT - LINE_SPACING) / 2;
		private static final int TEXT_WIDTH = ENTRY_WIDTH - HORIZ_SPACING * 4 - ICON_SIZE * 2;

		private static final int CLUE_ICON_X = HORIZ_SPACING + TEXT_WIDTH + HORIZ_SPACING;
		private static final int COLLECT_ICON_X = CLUE_ICON_X + ICON_SIZE + HORIZ_SPACING;
		private static final int ICON_Y = (ENTRY_HEIGHT - ICON_SIZE) / 2;

		private static final int CHECK_ICON_U = SCREEN_WIDTH + MainJournalScreen.BUTTON_WIDTH;
		private static final int CHECK_ICON_V = 0;

		private static final int CROSS_ICON_U = CHECK_ICON_U;
		private static final int CROSS_ICON_V = CHECK_ICON_V + ICON_SIZE;

		private static final int CLUE_ICON_U = CHECK_ICON_U;
		private static final int CLUE_ICON_V = CROSS_ICON_V + ICON_SIZE;

		private List<OrderedText> cachedHoverText = null;
		public List<OrderedText> getHoverText() {
			if (cachedHoverText == null) {
				cachedHoverText = new ArrayList<>();
				//Name text
				cachedHoverText.add(Text.literal(name).setStyle(Style.EMPTY.withBold(true).withUnderline(false).withColor(Formatting.GOLD)).asOrderedText());
				//Placed by
				cachedHoverText.add(Text.literal("Placed by ").setStyle(Style.EMPTY.withColor(Formatting.BLUE)).append(Text.literal(placer).setStyle(Style.EMPTY.withColor(Formatting.AQUA))).asOrderedText());
				//Description
				cachedHoverText.addAll(textRenderer.wrapLines(StringVisitable.plain(desc), ENTRY_WIDTH));
				//Num collected by
				String playerCount = collectedBy + " Player" + (collectedBy == 1 ? "" : "s");
				cachedHoverText.add(Text.literal("Collected by ").setStyle(Style.EMPTY.withColor(Formatting.BLUE)).append(Text.literal(playerCount).setStyle(Style.EMPTY.withColor(Formatting.AQUA))).asOrderedText());
			}
			return cachedHoverText;
		}

		private List<OrderedText> cachedClueText = null;
		public List<OrderedText> getClueText() {
			if (cachedClueText == null)
				cachedClueText = textRenderer.wrapLines(StringVisitable.plain(clue), ENTRY_WIDTH);
			return cachedClueText;
		}

		private static final List<OrderedText> CLUE_HOVER = new ArrayList<>() {{add(Text.literal("Click for clue").asOrderedText());}};

		@Override
		public List<OrderedText> render(MatrixStack matrices, int x, int y, int z, int mouseX, int mouseY, boolean mouseDown) {
			String trimmedName = textRenderer.trimToWidth(name, TEXT_WIDTH);
			if (trimmedName.length() < name.length())
				trimmedName = trimmedName.substring(0, trimmedName.length() - 3) + "...";
			int relX = mouseX - x;
			int relY = mouseY - y;
			boolean hoveringClue = relX >= CLUE_ICON_X && relX < CLUE_ICON_X + ICON_SIZE && relY >= ICON_Y && relY < ICON_Y + ICON_SIZE;
			boolean hoveringMain = !hoveringClue && relX >= 0 && relX < CLUE_ICON_X - HORIZ_SPACING && relY >= 0 && relY < ENTRY_HEIGHT;
			if (hoveringMain)
				textRenderer.draw(matrices, Text.literal(trimmedName).setStyle(Style.EMPTY.withUnderline(true)), x + HORIZ_SPACING, y + TEXT_Y, 0);
			else
				textRenderer.draw(matrices, trimmedName, x + HORIZ_SPACING, y + TEXT_Y, 0);

			if (hoveringClue && mouseDown)
				clueRevealed = true;

			int u = collected ? CHECK_ICON_U : CROSS_ICON_U;
			int v = collected ? CHECK_ICON_V : CROSS_ICON_V;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, TEXTURE);
			//Collection icon
			DrawableHelper.drawTexture(matrices, x + COLLECT_ICON_X, y + ICON_Y, z, u, v, ICON_SIZE, ICON_SIZE, TEX_WIDTH, TEX_HEIGHT);

			u = CLUE_ICON_U;
			v = CLUE_ICON_V + (hoveringClue ? ICON_SIZE : 0);
			//Clue icon
			DrawableHelper.drawTexture(matrices, x + CLUE_ICON_X, y + ICON_Y, z, u, v, ICON_SIZE, ICON_SIZE, TEX_WIDTH, TEX_HEIGHT);

			if (hoveringClue)
				if (clueRevealed)
					return getClueText();
				else
					return CLUE_HOVER;

			if (hoveringMain)
				return getHoverText();

			return null;
		}
	}

}
