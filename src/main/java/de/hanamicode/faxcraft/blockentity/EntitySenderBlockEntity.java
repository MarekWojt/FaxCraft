package de.hanamicode.faxcraft.blockentity;

import de.hanamicode.faxcraft.FaxCraftMod;
import de.hanamicode.faxcraft.interfaces.ImplementedInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

public class EntitySenderBlockEntity extends BlockEntity implements ImplementedInventory {
  // ImplementedInventory
  private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);

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

  @Override
  public void fromTag(BlockState state, CompoundTag tag) {
    super.fromTag(state, tag);
    Inventories.fromTag(tag, items);
  }

  @Override
  public CompoundTag toTag(CompoundTag tag) {
    Inventories.toTag(tag, items);
    return super.toTag(tag);
  }
}
