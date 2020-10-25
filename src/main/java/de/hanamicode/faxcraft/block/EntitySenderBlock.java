package de.hanamicode.faxcraft.block;

import de.hanamicode.faxcraft.blockentity.EntitySenderBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
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
      EntitySenderBlockEntity blockEntity = (EntitySenderBlockEntity)world.getBlockEntity(pos);
      if (blockEntity == null) return ActionResult.SUCCESS;

      ItemStack compassStack = blockEntity.getItems().get(0);
      if (compassStack.getTag() != null) {
        BlockPos lodestonePos = NbtHelper.toBlockPos(compassStack.getSubTag("LodestonePos"));
        player.sendMessage(new LiteralText(lodestonePos.toString()), false);
        player.teleport(lodestonePos.getX() + 0.5f, lodestonePos.getY() + 1, lodestonePos.getZ() + 0.5f, true);
      }
      return ActionResult.SUCCESS;
    } catch (Exception e) {
      player.sendMessage(new LiteralText(e.getMessage()), false);
      return ActionResult.FAIL;
    }
  }
}
