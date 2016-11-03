package com.manofj.minecraft.moj_showcase.capability

import com.manofj.minecraft.moj_showcase.tileentity.ShowcaseBase


class DisplayItemContainerImpl( size: Int )
  extends DisplayItemContainer( size )
{
  private[ this ] var showcaseOpt = Option.empty[ ShowcaseBase ]


  def this() = { this( 1 ) }

  def this( showcase: ShowcaseBase ) = {
    this( showcase.getSizeInventory )
    showcaseOpt = Option( showcase )
    settings.foreach( _ ++= showcase.defaultSettings )
  }


  override def defaultSettings: Map[ DisplayItemSetting, Float ] = showcaseOpt.map( _.defaultSettings ).getOrElse( Map.empty )

  override def onContentsChanged( slot: Int ): Unit = showcaseOpt.foreach( _.markDirty() )

}
