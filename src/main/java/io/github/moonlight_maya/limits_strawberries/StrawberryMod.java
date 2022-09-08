package io.github.moonlight_maya.limits_strawberries;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class StrawberryMod implements ModInitializer {

	public static final String MODID = "limits_strawberries";

	public static final EntityType<StrawberryEntity> STRAWBERRY = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier(MODID, "strawberry"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, StrawberryEntity::new).build()
	);

	@Override
	public void onInitialize(ModContainer mod) {




	}
}
