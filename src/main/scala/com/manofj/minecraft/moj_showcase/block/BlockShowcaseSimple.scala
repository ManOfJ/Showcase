package com.manofj.minecraft.moj_showcase.block

import java.util.Random

import net.minecraft.block.Block
import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

import com.manofj.minecraft.moj_showcase.Showcase
import com.manofj.minecraft.moj_showcase.tileentity.TileEntityShowcaseSimple


class BlockShowcaseSimple
  extends BlockContainer( Material.GLASS )
{

  {
    useNeighborBrightness = true
    setLightOpacity( 0 )
    translucent = false
    setUnlocalizedName( Showcase.languageKey( "showcase" ) )
    setCreativeTab( CreativeTabs.DECORATIONS )
  }


  private[ this ] def updateShowcasePowered( worldIn: World, pos: BlockPos, blockIn: Block ): Unit =
    if ( !worldIn.isRemote ) {
      worldIn.getTileEntity( pos ) match {
        case showcase: TileEntityShowcaseSimple =>
          showcase.updateRedstonePower( worldIn.isBlockIndirectlyGettingPowered( pos ) )

        case _ =>
      }
    }


  override def isFullBlock( state: IBlockState ): Boolean = false
  override def isOpaqueCube( state: IBlockState ): Boolean = false
  override def getRenderType( state: IBlockState ): EnumBlockRenderType = EnumBlockRenderType.MODEL
  override def getBlockLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT
  override def shouldSideBeRendered( blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing ): Boolean = true

  override def quantityDropped( state: IBlockState, fortune: Int, random: Random ): Int = 0
  override def canSilkHarvest( world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer ): Boolean = true

  override def createNewTileEntity( worldIn: World, meta: Int ): TileEntity = new TileEntityShowcaseSimple


  override def onBlockAdded( worldIn: World, pos: BlockPos, state: IBlockState ): Unit =
    updateShowcasePowered( worldIn, pos, state.getBlock )

  override def neighborChanged( state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block ): Unit =
    updateShowcasePowered( worldIn, pos, blockIn )


  override def onBlockPlacedBy( worldIn: World,
                                pos:     BlockPos,
                                state:   IBlockState,
                                placer:  EntityLivingBase,
                                stack:   ItemStack ): Unit =
    if ( !worldIn.isRemote ) {
      worldIn.getTileEntity( pos ) match {
        case showcase: TileEntityShowcaseSimple => showcase.setOwnerId( placer )
        case _ =>
      }
    }

  override def breakBlock( worldIn: World, pos: BlockPos, state: IBlockState ): Unit = {
    worldIn.getTileEntity( pos ) match {
      case inventory: TileEntityShowcaseSimple =>
        InventoryHelper.dropInventoryItems( worldIn, pos, inventory )
        worldIn.updateComparatorOutputLevel( pos, this )

      case _ =>
    }

    super.breakBlock( worldIn, pos, state )
  }

  override def onBlockActivated( worldIn:  World,
                                 pos:      BlockPos,
                                 state:    IBlockState,
                                 playerIn: EntityPlayer,
                                 hand:     EnumHand,
                                 heldItem: ItemStack,
                                 side:     EnumFacing,
                                 hitX:     Float,
                                 hitY:     Float,
                                 hitZ:     Float ): Boolean =
    if ( worldIn.isRemote ) {
      true
    }
    else {
      worldIn.getTileEntity( pos ) match {
        case showcase: TileEntityShowcaseSimple =>
          showcase.getOwnerId match {
            case Some( id ) if id == playerIn.getUniqueID =>
              playerIn.openGui( Showcase, 0, worldIn, pos.getX, pos.getY, pos.getZ )

            case None =>
              showcase.setOwnerId( playerIn )
              playerIn.openGui( Showcase, 0, worldIn, pos.getX, pos.getY, pos.getZ )

            case _ =>
              playerIn.addChatComponentMessage( new TextComponentTranslation( "moj_showcase.chat.showcase_permission_denied" ) )

          }

          true

        case _ =>
          false

      }
    }

}
