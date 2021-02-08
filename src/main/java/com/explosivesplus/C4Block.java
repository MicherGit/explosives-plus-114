package com.explosivesplus;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.AbstractState;
import net.minecraft.state.property.Properties;
import net.minecraft.state.StateManager;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.Hand;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.state.property.Property;
import net.minecraft.state.property.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class C4Block extends Block {
    public static final BooleanProperty UNSTABLE;

    public C4Block(final Settings settings) {
        super(settings);
    }

    @Override
    public void onBlockAdded(final BlockState state, final World world, final BlockPos pos, final BlockState oldState, final boolean moved) {
        if (oldState.getBlock() == state.getBlock()) {
            return;
        }
        if (world.isReceivingRedstonePower(pos)) {
            primeTnt(world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void neighborUpdate(final BlockState state, final World world, final BlockPos pos, final Block block, final BlockPos neighborPos, final boolean moved) {
        if (world.isReceivingRedstonePower(pos)) {
            primeTnt(world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void onBreak(final World world, final BlockPos pos, final BlockState state, final PlayerEntity player) {
        if (!world.isClient() && !player.isCreative() && state.<Boolean>get((Property<Boolean>)C4Block.UNSTABLE)) {
            primeTnt(world, pos);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onDestroyedByExplosion(final World world, final BlockPos pos, final Explosion explosion) {
        if (world.isClient) {
            return;
        }
        final TntEntity lv = new TntEntity(world, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f, explosion.getCausingEntity());
        lv.setFuse((short)(world.random.nextInt(lv.getFuseTimer() / 4) + lv.getFuseTimer() / 8));
        world.spawnEntity(lv);
    }

    public static void primeTnt(final World world, final BlockPos pos) {
        primeTnt(world, pos, null);
    }

    private static void primeTnt(final World world, final BlockPos pos, @Nullable final LivingEntity igniter) {
        if (world.isClient) {
            return;
        }
        final TntEntity lv = new TntEntity(world, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f, igniter);
        world.spawnEntity(lv);
        world.playSound(null, lv.x, lv.y, lv.z, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public boolean activate(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockHitResult hit) {
        final ItemStack lv = player.getStackInHand(hand);
        final Item lv2 = lv.getItem();
        if (lv2 == Items.FLINT_AND_STEEL || lv2 == Items.FIRE_CHARGE) {
            primeTnt(world, pos, player);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            if (lv2 == Items.FLINT_AND_STEEL) {
                lv.<PlayerEntity>damage(1, player, arg2 -> arg2.sendToolBreakStatus(hand));
            }
            else {
                lv.decrement(1);
            }
            return true;
        }
        return super.activate(state, world, pos, player, hand, hit);
    }

    @Override
    public void onProjectileHit(final World world, final BlockState state, final BlockHitResult hitResult, final Entity entity) {
        if (!world.isClient && entity instanceof ProjectileEntity) {
            final ProjectileEntity lv = (ProjectileEntity)entity;
            final Entity lv2 = lv.getOwner();
            if (lv.isOnFire()) {
                final BlockPos lv3 = hitResult.getBlockPos();
                primeTnt(world, lv3, (lv2 instanceof LivingEntity) ? ((LivingEntity)lv2) : null);
                world.removeBlock(lv3, false);
            }
        }
    }

    @Override
    public boolean shouldDropItemsOnExplosion(final Explosion explosion) {
        return false;
    }

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        builder.add(C4Block.UNSTABLE);
    }

    static {
        UNSTABLE = Properties.UNSTABLE;
    }
}