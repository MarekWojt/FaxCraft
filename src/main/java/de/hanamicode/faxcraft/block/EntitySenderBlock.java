package de.hanamicode.faxcraft.block;

import de.hanamicode.faxcraft.blockentity.EntitySenderBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EntitySenderBlock extends Block implements BlockEntityProvider {
  public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
  public EntitySenderBlock() {
    super(FabricBlockSettings.of(Material.STONE));
    setDefaultState(getStateManager().getDefaultState().with(ACTIVE, false));
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(ACTIVE);
  }
  
  @Override
  public BlockEntity createBlockEntity(BlockView blockView) {
    return new EntitySenderBlockEntity();
  }

  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    try {
      Inventory blockEntity = (Inventory) world.getBlockEntity(pos);
      if (blockEntity == null) return ActionResult.PASS;

      ItemStack playerActiveItem = player.getStackInHand(hand);
      if (!playerActiveItem.isEmpty()) {
        // Item in Hand, reinlegen
        
        for (int i = 0; i < blockEntity.size(); i++) {
          if (!blockEntity.isValid(i, playerActiveItem)) continue;

          blockEntity.setStack(i, playerActiveItem.copy());
          playerActiveItem.setCount(0);

          return ActionResult.SUCCESS;
        }
      } else {
        // Kein Item in Hand, rausnehmen
        if (playerActiveItem.isEmpty()) {
          for (int i = 0; i < blockEntity.size(); i++) {
            if (blockEntity.getStack(i).isEmpty()) continue;
            player.setStackInHand(hand, blockEntity.getStack(i));
            blockEntity.setStack(i, ItemStack.EMPTY);


            return ActionResult.SUCCESS;
          }
        }
      }

      return ActionResult.PASS;
    } catch (Exception e) {
      player.sendMessage(new LiteralText(e.getMessage()), false);
      return ActionResult.FAIL;
    }
  }

  @Override
  public void onSteppedOn(World world, BlockPos pos, Entity entity) {
    try {
      EntitySenderBlockEntity blockEntity = (EntitySenderBlockEntity) world.getBlockEntity(pos);
      if (blockEntity == null) return;

      if (!(entity instanceof LivingEntity || entity instanceof ItemEntity)) return;

      ItemStack compassStack = blockEntity.getItems().get(0);
      if (compassStack.isEmpty() || compassStack.getTag() == null) return;

      if (entity instanceof PlayerEntity) ((PlayerEntity) entity).sendMessage(new LiteralText(compassStack.getTag().asString()), false);

      CompoundTag lodestonePosTag = compassStack.getSubTag("LodestonePos");
      CompoundTag lodestoneDimensionCompTag = compassStack.getTag();
      if (lodestonePosTag == null || lodestoneDimensionCompTag == null) return;
      Tag lodestoneDimensionTag = lodestoneDimensionCompTag.get("LodestoneDimension");
      if (lodestoneDimensionTag == null) return;

      // TODO Dimension Check implementieren
      /*
      if (entity instanceof PlayerEntity) ((PlayerEntity) entity).sendMessage(new LiteralText(world.getDimension() + "?=" + lodestoneDimensionTag.asString()), false);
      if (world.getDimension().toString() != lodestoneDimensionTag.toString()) return; // Dimensionssprung
      */

      BlockPos lodestonePos = NbtHelper.toBlockPos(lodestonePosTag);

      entity.teleport(lodestonePos.getX() + 0.5f, lodestonePos.getY() + 1, lodestonePos.getZ() + 0.5f);

    } catch (Exception e) {
      if (entity instanceof PlayerEntity) ((PlayerEntity) entity).sendMessage(new LiteralText(e.getMessage()), false);
    }
  }
}
