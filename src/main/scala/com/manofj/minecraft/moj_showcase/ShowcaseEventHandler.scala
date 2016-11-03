package com.manofj.minecraft.moj_showcase

import net.minecraft.tileentity.TileEntity

import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import com.manofj.minecraft.moj_showcase.capability.DisplayItemContainerProvider
import com.manofj.minecraft.moj_showcase.tileentity.ShowcaseBase


@Mod.EventBusSubscriber
object ShowcaseEventHandler {

  @SubscribeEvent
  def attachCapabilities( event: AttachCapabilitiesEvent[ TileEntity ] ): Unit =
    event.getObject match {
      case showcase: ShowcaseBase =>
        event.addCapability( Showcase.resourceLocation( "DisplayItem" ), new DisplayItemContainerProvider( showcase ) )

      case _ =>
    }

}
