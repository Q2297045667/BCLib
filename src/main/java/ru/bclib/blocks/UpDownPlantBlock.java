package ru.bclib.blocks;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.bclib.api.tag.NamedMineableTags;
import ru.bclib.api.tag.TagAPI.TagLocation;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.interfaces.TagProvider;

import java.util.List;

public abstract class UpDownPlantBlock extends BaseBlockNotFull implements RenderLayerProvider, TagProvider {
	private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);
	
	public UpDownPlantBlock() {
		this(FabricBlockSettings
			.of(Material.PLANT)
			.breakByHand(true)
			.sound(SoundType.GRASS)
			.noCollission()
		);
	}
	
	public UpDownPlantBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}
	
	protected abstract boolean isTerrain(BlockState state);
	
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
		return SHAPE;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState down = world.getBlockState(pos.below());
		BlockState up = world.getBlockState(pos.above());
		return (isTerrain(down) || down.getBlock() == this) && (isSupport(up, world, pos) || up.getBlock() == this);
	}
	
	protected boolean isSupport(BlockState state, LevelReader world, BlockPos pos) {
		return canSupportCenter(world, pos.above(), Direction.UP);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BlockState updateShape(BlockState state, Direction facing, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if (!canSurvive(state, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		else {
			return state;
		}
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ItemStack tool = builder.getParameter(LootContextParams.TOOL);
		if (tool != null && FabricToolTags.SHEARS.contains(tool.getItem()) || EnchantmentHelper.getItemEnchantmentLevel(
			Enchantments.SILK_TOUCH,
			tool
		) > 0) {
			return Lists.newArrayList(new ItemStack(this));
		}
		else {
			return Lists.newArrayList();
		}
	}
	
	@Override
	public BCLRenderLayer getRenderLayer() {
		return BCLRenderLayer.CUTOUT;
	}
	
	@Override
	public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
		super.playerDestroy(world, player, pos, state, blockEntity, stack);
		world.neighborChanged(pos, Blocks.AIR, pos.below());
	}
	
	@Override
	public void addTags(List<TagLocation<Block>> blockTags, List<TagLocation<Item>> itemTags) {
		blockTags.add(NamedMineableTags.SHEARS);
		blockTags.add(NamedMineableTags.HOE);
	}
}
