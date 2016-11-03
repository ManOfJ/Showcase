package com.manofj.minecraft.moj_showcase

import scala.annotation.meta.setter

import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.relauncher.Side

import com.manofj.commons.minecraftforge.base.MinecraftForgeMod
import com.manofj.commons.minecraftforge.i18n.I18nSupportMod
import com.manofj.commons.minecraftforge.network.SimpleNetworkMod
import com.manofj.commons.minecraftforge.resource.ResourceLocationMakerMod

import com.manofj.minecraft.moj_showcase.capability.DisplayItemContainer
import com.manofj.minecraft.moj_showcase.init.ShowcaseInitializer
import com.manofj.minecraft.moj_showcase.network.UpdateSetting


@Mod( modid       = Showcase.modId,
      name        = Showcase.modName,
      version     = Showcase.modVersion,
      modLanguage = Showcase.modLanguage )
object Showcase
  extends MinecraftForgeMod
  with    ResourceLocationMakerMod
  with    SimpleNetworkMod
  with    I18nSupportMod
{

  override final val modId      = "moj_showcase"
  override final val modName    = "Showcase"
  override final val modVersion = "@version@"


  @SidedProxy( modId = Showcase.modId,
               serverSide = "com.manofj.minecraft.moj_showcase.init.ShowcaseCommonInitializer",
               clientSide = "com.manofj.minecraft.moj_showcase.init.ShowcaseClientInitializer" )
  var initializer: ShowcaseInitializer = null

  @( CapabilityInject @ setter )( classOf[ DisplayItemContainer ] )
  var displayItemContainerCap: Capability[ DisplayItemContainer ] = null


  @Mod.EventHandler
  def preInit( event: FMLPreInitializationEvent ): Unit = {
    initializer.preInit( event )
    registerMessage( UpdateSetting, Side.SERVER )
  }

  @Mod.EventHandler def init( event: FMLInitializationEvent ): Unit = initializer.init( event )

}
