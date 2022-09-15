package io.github.moonlight_maya.limits_strawberries.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moonlight_maya.limits_strawberries.client.StrawberryModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class BerryJournalScreen extends DataJournalScreen<BerryJournalScreen.BerryEntry> {

	protected BerryJournalScreen() {
		super(Text.literal("Berries"));
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
			boolean hoveringMain = !hoveringClue && relX >= 0 && relX < ENTRY_WIDTH && relY >= 0 && relY < ENTRY_HEIGHT;
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
