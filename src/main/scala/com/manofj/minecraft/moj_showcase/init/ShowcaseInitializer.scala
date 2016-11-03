package com.manofj.minecraft.moj_showcase.init

import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.registry.GameRegistry

import com.manofj.minecraft.moj_showcase.Showcase
import com.manofj.minecraft.moj_showcase.ShowcaseGuiHandler
import com.manofj.minecraft.moj_showcase.capability.DisplayItemContainer
import com.manofj.minecraft.moj_showcase.capability.DisplayItemContainerImpl
import com.manofj.minecraft.moj_showcase.capability.DisplayItemContainerStorage
import com.manofj.minecraft.moj_showcase.renderer.TileEntityShowcaseSimpleRenderer
import com.manofj.minecraft.moj_showcase.tileentity.TileEntityShowcaseSimple


trait ShowcaseInitializer {
  def preInit( event: FMLPreInitializationEvent ): Unit
  def init( event: FMLInitializationEvent ): Unit
}

class ShowcaseCommonInitializer
  extends ShowcaseInitializer
{
  override def preInit( event: FMLPreInitializationEvent ): Unit = {
    ShowcaseBlocks.preInit()
    NetworkRegistry.INSTANCE.registerGuiHandler( Showcase, ShowcaseGuiHandler )
    GameRegistry.registerTileEntity( classOf[ TileEntityShowcaseSimple ], Showcase.languageKey( "Showcase" ) )
    CapabilityManager.INSTANCE.register( classOf[ DisplayItemContainer ], DisplayItemContainerStorage, classOf[ DisplayItemContainerImpl ] )
  }

  override def init( event: FMLInitializationEvent ): Unit = {
    ShowcaseBlocks.init()
  }
}

class ShowcaseClientInitializer
  extends ShowcaseCommonInitializer
{
  override def preInit( event: FMLPreInitializationEvent ): Unit = {
    super.preInit( event )
    ShowcaseBlocks.preInitClient()
    ClientRegistry.bindTileEntitySpecialRenderer( classOf[ TileEntityShowcaseSimple ], TileEntityShowcaseSimpleRenderer )
  }
}
