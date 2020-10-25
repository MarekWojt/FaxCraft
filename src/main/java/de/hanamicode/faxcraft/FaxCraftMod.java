package de.hanamicode.faxcraft;

import de.hanamicode.faxcraft.block.EntitySenderBlock;
import de.hanamicode.faxcraft.blockentity.EntitySenderBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FaxCraftMod implements ModInitializer {
	public static final EntitySenderBlock ENTITY_SENDER_BLOCK = new EntitySenderBlock();
	public static BlockEntityType<EntitySenderBlockEntity> ENTITY_SENDER_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		Registry.register(Registry.BLOCK, new Identifier("faxcraft", "entity_sender"), ENTITY_SENDER_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("faxcraft", "entity_sender"), new BlockItem(ENTITY_SENDER_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
		ENTITY_SENDER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "faxcraft:entity_sender", BlockEntityType.Builder.create(EntitySenderBlockEntity::new, ENTITY_SENDER_BLOCK).build(null));
	}

}
