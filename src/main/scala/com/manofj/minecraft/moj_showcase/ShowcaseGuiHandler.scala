package com.manofj.minecraft.moj_showcase

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

import net.minecraftforge.fml.common.network.IGuiHandler

import com.manofj.minecraft.moj_showcase.gui.GuiShowcase
import com.manofj.minecraft.moj_showcase.inventory.ShowcaseContainer
import com.manofj.minecraft.moj_showcase.tileentity.ShowcaseBase


object ShowcaseGuiHandler
  extends IGuiHandler
{

  override def getClientGuiElement( ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int ): AnyRef =
    ID match {
      case 0 =>
        world.getTileEntity( new BlockPos( x, y, z ) ) match {
          case showcase: ShowcaseBase =>
            new GuiShowcase( player.inventory, showcase, player )

          case _ =>
            null

        }
    }

  override def getServerGuiElement( ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int ): AnyRef =
    ID match {
      case 0 =>
        world.getTileEntity( new BlockPos( x, y, z ) ) match {
          case showcase: ShowcaseBase if showcase.isUseableByPlayer( player ) =>
            new ShowcaseContainer( player.inventory, showcase, player )

          case _ =>
            null

        }
    }

}
