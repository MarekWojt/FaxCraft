package de.hanamicode.faxcraft.blockentity;

import de.hanamicode.faxcraft.FaxCraftMod;
import de.hanamicode.faxcraft.interfaces.ImplementedInventory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class EntitySenderBlockEntity extends BlockEntity implements ImplementedInventory {
  // ImplementedInventory
  private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);

  @Override
  public DefaultedList<ItemStack> getItems() {
    return items;
  }

  @Override
  public int getMaxCountPerStack() {
    return 1;
  }

  @Override
  public boolean isValid(int slot, ItemStack stack) {
    return slot == 1 || stack.getItem() instanceof CompassItem;
  }

  // Constructor
  public EntitySenderBlockEntity() {
    super(FaxCraftMod.ENTITY_SENDER_BLOCK_ENTITY);
  }
}
