package com.manofj.minecraft.moj_showcase.init

import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack

import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.registry.GameRegistry

import com.manofj.minecraft.moj_showcase.Showcase
import com.manofj.minecraft.moj_showcase.block.BlockShowcaseSimple


object ShowcaseBlocks {

  final val SHOWCASE = new BlockShowcaseSimple


  private[ this ] val SHOWCASE_ITEM = new ItemBlock( SHOWCASE )


  private[ init ] def preInitClient(): Unit = {
    val name = Showcase.resourceLocation( "showcase_simple" )
    val location = new ModelResourceLocation( name, "inventory" )

    ModelLoader.setCustomModelResourceLocation( SHOWCASE_ITEM, 0, location )
  }

  private[ init ] def preInit(): Unit = {
    GameRegistry.register( SHOWCASE, Showcase.resourceLocation( "showcase_simple" ) )
    GameRegistry.register( SHOWCASE_ITEM, Showcase.resourceLocation( "showcase_simple" ) )
  }

  private[ init ] def init(): Unit = {

    GameRegistry.addRecipe( new ItemStack( SHOWCASE ),
      "###",
      "#$#",
      "###",
      '#': Character, Blocks.GLASS,
      '$': Character, Items.REDSTONE
    )

  }

}
