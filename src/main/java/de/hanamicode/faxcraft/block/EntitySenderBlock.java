package de.hanamicode.faxcraft.block;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import de.hanamicode.faxcraft.blockentity.EntitySenderBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
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

          ItemStack currentItemStack = blockEntity.getStack(i);

          blockEntity.setStack(i, playerActiveItem.copy());
          player.setStackInHand(hand, currentItemStack);
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

  public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
    if (!world.isClient) {
       boolean blockActive = (Boolean)state.get(ACTIVE);
       if (/*blockActive && */world.isReceivingRedstonePower(pos)) {
        world.getBlockTickScheduler().schedule(pos, this, 1);
       }
    }
  }

  public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    try {
      if (!(/*(Boolean)state.get(ACTIVE) && */world.isReceivingRedstonePower(pos))) return;

      List<Entity> entitiesAffected = world.getEntitiesByClass(Entity.class, new Box(pos.add(0, 1, 0)),
        (e) -> {
          return e instanceof LivingEntity || e instanceof ItemEntity;
        }
      );

      if (entitiesAffected.size() == 0) return;

      
      EntitySenderBlockEntity blockEntity = (EntitySenderBlockEntity) world.getBlockEntity(pos);
      if (blockEntity == null) return;

      ItemStack compassStack = blockEntity.getItems().get(0);
      if (compassStack.isEmpty() || compassStack.getTag() == null) return;

      CompoundTag compassCompTag = compassStack.getTag();
      if (compassCompTag == null) return;

      CompoundTag lodestonePosTag = compassStack.getSubTag("LodestonePos");
      if (lodestonePosTag == null) return;

      if (!world.getRegistryKey().equals(CompassItem.getLodestoneDimension(compassCompTag).get())) return; // Dimensionssprung

      BlockPos lodestonePos = NbtHelper.toBlockPos(lodestonePosTag);

      if (!world.getBlockState(lodestonePos).getBlock().equals(Blocks.LODESTONE)) return; // Lodestone abgebaut

      BlockPos lodestonePosCentered = lodestonePos.mutableCopy().add(0.5, 0, 0.5);


      entitiesAffected.sort((e1, e2) -> {
        if (e1 instanceof PlayerEntity && !(e2 instanceof PlayerEntity)) return -1;
        if (!(e2 instanceof PlayerEntity) && e1 instanceof PlayerEntity) return 1;

        if (e1 instanceof LivingEntity && !(e2 instanceof LivingEntity)) return -1;
        if (!(e2 instanceof LivingEntity) && e1 instanceof LivingEntity) return 1;

        return lodestonePosCentered.getSquaredDistance(e1.getPos(), true) < lodestonePosCentered.getSquaredDistance(e2.getPos(), true) ? -1 : 1;
      });

      entitiesAffected.get(0).teleport(lodestonePos.getX() + 0.5f, lodestonePos.getY() + 1, lodestonePos.getZ() + 0.5f);

      world.setBlockState(pos, (BlockState)state.cycle(ACTIVE), 2);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
