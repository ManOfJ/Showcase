package com.manofj.minecraft.moj_showcase.tileentity

import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Vec3d

import com.manofj.minecraft.moj_showcase.Showcase
import com.manofj.minecraft.moj_showcase.capability.DisplayItemContainer
import com.manofj.minecraft.moj_showcase.capability.{ DisplayItemSetting => Setting }


trait ShowcaseBase
  extends IInventory
{
  protected def displayItems: DisplayItemContainer


  def defaultSettings: Map[ Setting, Float ]

  def getItemBasePos( index: Int ): Vec3d


  def getFieldName( setting: Setting ): String = Showcase.languageKey( s"gui.showcase.setting_${ setting.getName }" )

  def getFieldName( id: Int ): String = getFieldName( Setting.byIndex( id ) )

  def getFieldId( index: Int, setting: Setting ): Int = ( index * Setting.size ) + setting.getIndex

  def getFieldId( index: Int, setting: Int ): Int = getFieldId( index, Setting.byIndex( setting ) )

  def getFieldFloat( index: Int, setting: Setting ): Float = displayItems.getSetting( index, setting )

  def getFieldFloat( id: Int ): Float = displayItems.getSetting( id )

  def setFieldFloat( index: Int, setting: Setting, value: Float ): Unit = displayItems.setSetting( index, setting, value )

  def setFieldFloat( id: Int, value: Float ): Unit = displayItems.setSetting( id, value )


  override def getInventoryStackLimit: Int = 1

  override def getStackInSlot( index: Int ): ItemStack = displayItems.getStackInSlot( index )

  override def setInventorySlotContents( index: Int, stack: ItemStack ): Unit = displayItems.setStackInSlot( index, stack )

  override def removeStackFromSlot( index: Int ): ItemStack = displayItems.removeStackFromSlot( index )

  override def decrStackSize( index: Int, count: Int ): ItemStack = displayItems.extractItem( index, count, false )

  override def clear(): Unit = displayItems.clear()


  override def getFieldCount: Int = displayItems.settingCount

  override def getField( id: Int ): Int = ( getFieldFloat( id ) * 10000F ).toInt

  override def setField( id: Int, value: Int ): Unit = setFieldFloat( id, value / 10000F )

}
