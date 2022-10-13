package io.github.moonlight_maya.limits_strawberries.mixin;

import io.github.moonlight_maya.limits_strawberries.StrawberryMod;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

	@Shadow
	public abstract DataCommandStorage getDataCommandStorage();

	@Inject(method = "save", at = @At("HEAD"))
	public void saveBerries(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
		StrawberryMod.LOGGER.info("Saving berry information...");
		getDataCommandStorage().set(StrawberryMod.PERSISTENT_STORAGE, StrawberryMod.SERVER_BERRIES.serialize());
		StrawberryMod.LOGGER.info("Berry information saved.");
	}
}
