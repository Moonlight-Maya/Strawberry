package io.github.moonlight_maya.limits_strawberries.client.screens;

import io.github.moonlight_maya.limits_strawberries.StrawberryEntity;
import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import io.github.moonlight_maya.limits_strawberries.client.StrawberryModClient;
import io.github.moonlight_maya.limits_strawberries.data.BerryMap;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.Objects;

public class BerryEditScreen extends Screen {

	private final StrawberryEntity entity;
	public BerryEditScreen(StrawberryEntity entity) {
		super(Text.translatable("limits_strawberries.gui.edit_berry_title"));
		this.entity = entity;
	}

	private TextFieldWidget nameField, clueField, descField;
	private ButtonWidget doneButton, cancelButton;

	private String prevName, prevClue, prevDesc;
	private String placer;

	@Override
	protected void init() {
		client.keyboard.setRepeatEvents(true);
		doneButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, ScreenTexts.DONE, (button) -> {
			this.commitAndClose();
		}));
		cancelButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, ScreenTexts.CANCEL, (button) -> {
			this.closeScreen();
		}));
		placer = StrawberryModClient.CLIENT_BERRIES.berryInfo.get(entity.getUuid()).placer;
		if (placer == null) placer = "Command";

		nameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, 50, 300, 20, Text.translatable("limits_strawberries.gui.name"));
		nameField.setMaxLength(BerryMap.NAME_MAX);
		prevName = StrawberryModClient.CLIENT_BERRIES.berryInfo.get(entity.getUuid()).name;
		if (prevName == null) prevName = "";
		nameField.setText(prevName);

		clueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, 100, 300, 20, Text.translatable("limits_strawberries.gui.clue"));
		clueField.setMaxLength(BerryMap.CLUE_MAX);
		prevClue = StrawberryModClient.CLIENT_BERRIES.berryInfo.get(entity.getUuid()).clue;
		if (prevClue == null) prevClue = "";
		clueField.setText(prevClue);

		descField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, 150, 300, 20, Text.translatable("limits_strawberries.gui.desc"));
		descField.setMaxLength(BerryMap.DESC_MAX);
		prevDesc = StrawberryModClient.CLIENT_BERRIES.berryInfo.get(entity.getUuid()).desc;
		if (prevDesc == null) prevDesc = "";
		descField.setText(prevDesc);

		addSelectableChild(nameField);
		addSelectableChild(clueField);
		addSelectableChild(descField);
		setInitialFocus(nameField);
		nameField.setTextFieldFocused(true);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		drawCenteredText(matrices, this.textRenderer, Text.translatable("limits_strawberries.gui.edit_berry_title"), this.width / 2, 20, 16777215);
		drawCenteredText(matrices, this.textRenderer, Text.translatable("limits_strawberries.gui.edit_berry_placed_by", placer), this.width / 2, 30, 16777215);
		drawTextWithShadow(matrices, this.textRenderer, Text.translatable("limits_strawberries.gui.name"), this.width / 2 - 150, 40, 10526880);
		nameField.render(matrices, mouseX, mouseY, delta);
		drawTextWithShadow(matrices, this.textRenderer, Text.translatable("limits_strawberries.gui.clue"), this.width / 2 - 150, 90, 10526880);
		clueField.render(matrices, mouseX, mouseY, delta);
		drawTextWithShadow(matrices, this.textRenderer, Text.translatable("limits_strawberries.gui.desc"), this.width / 2 - 150, 140, 10526880);
		descField.render(matrices, mouseX, mouseY, delta);

		super.render(matrices, mouseX, mouseY, delta);
	}

	private void commitAndClose() {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(entity.getId());

		boolean nameChange = !nameField.getText().equals(prevName);
		boolean clueChange = !clueField.getText().equals(prevClue);
		boolean descChange = !descField.getText().equals(prevDesc);

		int flags = 0;
		flags |= nameChange ? 1 : 0;
		flags |= clueChange ? 2 : 0;
		flags |= descChange ? 4 : 0;
		buf.writeByte(flags);
		if (nameChange)
			buf.writeString(nameField.getText(), BerryMap.NAME_MAX);
		if (clueChange)
			buf.writeString(clueField.getText(), BerryMap.CLUE_MAX);
		if (descChange)
			buf.writeString(descField.getText(), BerryMap.DESC_MAX);

		ClientPlayNetworking.send(StrawberryMod.C2S_UPDATE_PACKET_ID, buf);
		closeScreen();
	}
}
