package com.manofj.minecraft.moj_showcase.tileentity

import java.util.UUID

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTUtil
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.math.Vec3d

import net.minecraftforge.common.util.Constants.NBT

import com.manofj.minecraft.moj_showcase.Showcase
import com.manofj.minecraft.moj_showcase.capability.DisplayItemContainer
import com.manofj.minecraft.moj_showcase.capability.{ DisplayItemSetting => Setting }


object TileEntityShowcaseSimple {

  final val DEFAULT_SETTINGS =
    Map( Setting.SCALE                  -> 0.9375F,
         Setting.DEFAULT_ROTATION_PITCH -> 0F,
         Setting.DEFAULT_ROTATION_YAW   -> 0F,
         Setting.DEFAULT_ROTATION_ROLL  -> 0F,
         Setting.ROTATION_PITCH         -> 0F,
         Setting.ROTATION_YAW           -> 0F,
         Setting.ROTATION_ROLL          -> 0F,
         Setting.STEP_ROTATION_PITCH    -> 0F,
         Setting.STEP_ROTATION_YAW      -> 1F,
         Setting.STEP_ROTATION_ROLL     -> 0F )

}

class TileEntityShowcaseSimple
  extends TileEntity
  with    ITickable
  with    ShowcaseBase
{
  private[ this ] var ownerIdOpt = Option.empty[ UUID ]

  private[ this ] var busy = false

  private[ this ] var redstonePower = 0

  private[ this ] var displayItemsObj: DisplayItemContainer = null


  def setOwnerId( owner: EntityLivingBase ): Unit = ownerIdOpt = Option( owner ).map( _.getUniqueID )

  def getOwnerId: Option[ UUID ] = ownerIdOpt

  def isOwner( entity: EntityLivingBase ): Boolean = ownerIdOpt.exists( Option( entity ).map( _.getUniqueID ).contains )


  def updateRedstonePower( redstonePower: Int ): Unit = {
    this.redstonePower = ( redstonePower min 15 ) max 0
    worldObj.addBlockEvent( pos, getBlockType, 0, this.redstonePower )
  }


  override protected def displayItems: DisplayItemContainer = displayItemsObj


  override def defaultSettings: Map[ Setting, Float ] = TileEntityShowcaseSimple.DEFAULT_SETTINGS

  override def getItemBasePos( id: Int ): Vec3d = new Vec3d( 0.5D, 0.5D, 0.5D )

  override def onLoad(): Unit = {
    displayItemsObj = getCapability( Showcase.displayItemContainerCap, null )
  }

  override def update(): Unit = {
    if ( !busy && redstonePower > 0 ) {
      val power = redstonePower / 15F

      Seq( Setting.ROTATION_PITCH -> Setting.STEP_ROTATION_PITCH,
           Setting.ROTATION_YAW   -> Setting.STEP_ROTATION_YAW,
           Setting.ROTATION_ROLL  -> Setting.STEP_ROTATION_ROLL )
        .foreach { case ( axis, step ) =>
          val axisVal = displayItems.getSetting( 0, axis )
          val stepVal = displayItems.getSetting( 0, step )
          val tmp     = axisVal + ( power * stepVal )

          displayItems.setSetting( 0, axis, if ( tmp < 0F ) tmp + 360F else tmp % 360F )
        }

    }
  }

  override def receiveClientEvent( id: Int, `type`: Int ): Boolean =
    id match {
      case 0 =>
        this.redstonePower = `type`
        true

      case 1 =>
        this.busy = `type` == 1
        true

      case _ =>
        super.receiveClientEvent( id, `type` )
    }

  override def getUpdateTag: NBTTagCompound = writeToNBT( new NBTTagCompound )

  override def readFromNBT( compound: NBTTagCompound ): Unit = {
    super.readFromNBT( compound )
    if ( compound.hasKey( "OwnerId", NBT.TAG_COMPOUND ) ) {
      val tag = compound.getCompoundTag( "OwnerId" )

      ownerIdOpt = Option( NBTUtil.getUUIDFromTag( tag ) )
    }
    redstonePower = compound.getInteger( "Powered" )
  }

  override def writeToNBT( compound: NBTTagCompound ): NBTTagCompound = {
    super.writeToNBT( compound )
    ownerIdOpt.map( NBTUtil.createUUIDTag ).foreach( compound.setTag( "OwnerId", _ ) )
    compound.setInteger( "Powered", redstonePower )
    compound
  }

  override def getSizeInventory: Int = 1

  override def getInventoryStackLimit: Int = 1

  override def openInventory( player: EntityPlayer ): Unit =
    if ( isOwner( player ) ) {
      busy = true
      worldObj.addBlockEvent( pos, getBlockType, 1, 1 )
    }

  override def closeInventory( player: EntityPlayer ): Unit =
    if ( isOwner( player ) ) {
      busy = false
      worldObj.addBlockEvent( pos, getBlockType, 1, 0 )
    }

  override def isUseableByPlayer( player: EntityPlayer ): Boolean = isOwner( player )

  override def isItemValidForSlot( index: Int, stack: ItemStack ): Boolean = true

  override def getName: String = Showcase.languageKey( "inventory.showcase_simple.name" )

  override def hasCustomName: Boolean = false

}
