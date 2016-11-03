package com.manofj.minecraft.moj_showcase.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IContainerListener
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack


class ShowcaseContainer( playerInventory: IInventory, showcase: IInventory, owner: EntityPlayer )
  extends Container
{

  private[ this ] final val MAX_COLUMN = 4
  private[ this ] final val MAX_ROW    = 3

  private[ this ] var rows    = 0
  private[ this ] var columns = 0

  private[ this ] var settings = ( 0 until showcase.getFieldCount ).map( showcase.getField )


  {
    rows    = ( showcase.getSizeInventory / MAX_COLUMN ) min MAX_ROW max 1
    columns = showcase.getSizeInventory % MAX_COLUMN

    showcase.openInventory( owner )

    for { row    <- 0 until rows
          column <- 0 until columns
    } addSlotToContainer( new Slot( showcase, row * rows + column, 8 + column * 18, 18 + row * 18 ) )

    for { row    <- 0 until 3
          column <- 0 until 9
    } addSlotToContainer( new Slot( playerInventory, row * 9 + 9 + column, 8 + column * 18, 84 + row * 18 ) )

    ( 0 until 9 ).foreach( i => addSlotToContainer( new Slot( playerInventory, i, 8 + i * 18, 142 ) ) )

  }


  def getShowcase: IInventory = showcase


  override def addListener( listener: IContainerListener ): Unit = {
    super.addListener( listener )
    listener.sendAllWindowProperties( this, showcase )
  }

  override def detectAndSendChanges(): Unit = {
    import scala.collection.convert.WrapAsScala.asScalaIterator


    super.detectAndSendChanges()

    val current = ( 0 until showcase.getFieldCount ).map( showcase.getField )

    current
      .zipWithIndex
      .filter( x => x._1 != settings( x._2 ) )
      .foreach { case ( v, i ) =>
        listeners.iterator.foreach( _.sendProgressBarUpdate( this, i, v ) )
      }

    settings = current
  }

  override def updateProgressBar( id: Int, data: Int ): Unit = { showcase.setField( id, data ) }


  override def canInteractWith( playerIn: EntityPlayer ): Boolean = showcase.isUseableByPlayer( playerIn )

  override def transferStackInSlot( playerIn: EntityPlayer, index: Int ): ItemStack =
    Option( inventorySlots.get( index ) ) match {
      case Some( slot ) if slot.getHasStack =>
        val stack = slot.getStack
        val item  = stack.copy()

        val showcaseEndIndex = rows * columns
        val ( start, end, reverse ) =
          if ( index < showcaseEndIndex ) {
            ( showcaseEndIndex, inventorySlots.size, true )
          }
          else {
            ( 0, showcaseEndIndex, false )
          }

        if ( !mergeItemStack( stack, start, end, reverse ) ) {
          null
        }
        else {
          if ( stack.stackSize <= 0 ) {
            slot.putStack( null )
          }
          else {
            slot.onSlotChanged()
          }
          item
        }

      case _ =>
        null
    }

  override def onContainerClosed( playerIn: EntityPlayer ): Unit = {
    super.onContainerClosed( playerIn )
    showcase.closeInventory( playerIn )
  }

}
