package com.manofj.minecraft.moj_showcase.renderer

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity

import net.minecraftforge.fml.client.FMLClientHandler

import com.manofj.minecraft.moj_showcase.capability.{ DisplayItemSetting => Setting }
import com.manofj.minecraft.moj_showcase.tileentity.ShowcaseBase


trait TileEntityShowcaseRenderer[ A <: TileEntity with ShowcaseBase ]
  extends TileEntitySpecialRenderer[ A ]
{
  protected final val minecraft = FMLClientHandler.instance.getClient


  def renderDisplayItems( showcase: A, x: Double, y: Double, z: Double ): Unit = {
    ( 0 until showcase.getSizeInventory )
      .map( i => i -> Option( showcase.getStackInSlot( i ) ) )
      .withFilter( _._2.isDefined )
      .foreach { case ( i, Some( item ) ) =>

        GlStateManager.pushMatrix()
        GlStateManager.disableLighting()

        val pos = showcase.getItemBasePos( i )
        GlStateManager.translate( x + pos.xCoord, y + pos.yCoord, z + pos.zCoord )

        val scale = showcase.getFieldFloat( i, Setting.SCALE )
        GlStateManager.scale( scale, scale, scale )

        GlStateManager.rotate( showcase.getFieldFloat( i, Setting.DEFAULT_ROTATION_PITCH ), 1F, 0F, 0F )
        GlStateManager.rotate( showcase.getFieldFloat( i, Setting.DEFAULT_ROTATION_YAW ), 0F, 1F, 0F )
        GlStateManager.rotate( showcase.getFieldFloat( i, Setting.DEFAULT_ROTATION_ROLL ), 0F, 0F, 1F )

        GlStateManager.rotate( showcase.getFieldFloat( i, Setting.ROTATION_PITCH ), 1F, 0F, 0F )
        GlStateManager.rotate( showcase.getFieldFloat( i, Setting.ROTATION_YAW ), 0F, 1F, 0F )
        GlStateManager.rotate( showcase.getFieldFloat( i, Setting.ROTATION_ROLL ), 0F, 0F, 1F )

        GlStateManager.pushAttrib()
        RenderHelper.enableStandardItemLighting()
        minecraft.getRenderItem.renderItem( item, TransformType.FIXED )
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popAttrib()
        GlStateManager.enableLighting()
        GlStateManager.popMatrix()

      }
  }


  override def renderTileEntityAt( showcase:     A,
                                   x:            Double,
                                   y:            Double,
                                   z:            Double,
                                   partialTicks: Float,
                                   destroyStage: Int ): Unit =
  {
    renderDisplayItems( showcase, x, y, z )
  }

}
