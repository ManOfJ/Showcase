package com.manofj.minecraft.moj_showcase.capability

import scala.collection.mutable.{ HashMap => MutableMap }

import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList

import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.items.ItemStackHandler

import com.manofj.minecraft.moj_showcase.capability.{ DisplayItemSetting => Setting }


abstract class DisplayItemContainer( size: Int )
  extends ItemStackHandler( size )
{
  private[ this ] def cleanSettings = Array.fill( getSlots )( MutableMap( defaultSettings.toSeq: _* ) )


  protected var settings: Array[ MutableMap[ Setting, Float ] ] = Array.fill( size )( MutableMap.empty )


  def defaultSettings: Map[ Setting, Float ]


  def settingCount: Int = getSlots * Setting.size

  def getSetting( i: Int, setting: Setting ): Float = settings( i )( setting )

  def setSetting( i: Int, setting: Setting, value: Float ): Unit = settings( i )( setting ) = value

  def getSetting( i: Int ): Float = getSetting( i / Setting.size, Setting.byIndex( i ) )

  def setSetting( i: Int, value: Float ): Unit = setSetting( i / Setting.size, Setting.byIndex( i ), value )

  def removeStackFromSlot( slot: Int ): ItemStack = {
    validateSlotIndex( slot )

    val item = ItemStackHelper.getAndRemove( stacks, slot )
    onContentsChanged( slot )

    item
  }

  def clear(): Unit = super.setSize( getSlots )


  override def getStackLimit( slot: Int, stack: ItemStack ): Int = 1

  override def setSize( size: Int ): Unit = {
    super.setSize( size )
    settings = cleanSettings
  }

  override def deserializeNBT( nbt: NBTTagCompound ): Unit = {
    super.deserializeNBT( nbt )

    settings = cleanSettings
    if ( nbt.hasKey( "Settings", NBT.TAG_LIST ) ) {
      val list = nbt.getTagList( "Settings", NBT.TAG_COMPOUND )

      ( 0 until list.tagCount ).foreach { i =>
        val tag   = list.getCompoundTagAt( i )
        val index = tag.getInteger( "Index" ) & 0x7FFFFFFF
        val map   = settings( index )

        map.keys.foreach { key =>
          map( key ) = tag.getFloat( s"Value_${ key.getIndex }" )
      } }
    }
  }

  override def serializeNBT(): NBTTagCompound = {
    val compound = super.serializeNBT()

    compound.setTag( "Settings",
      settings
        .zipWithIndex
        .foldLeft( new NBTTagList ) { case ( list, ( data, i ) ) =>
          val tag = new NBTTagCompound

          tag.setInteger( "Index", i )
          data.foreach { case ( k, v ) =>
            tag.setFloat( s"Value_${ k.getIndex }", v )
          }
          list.appendTag( tag )

          list
    } )

    compound
  }
}
