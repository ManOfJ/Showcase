package com.manofj.minecraft.moj_showcase.capability

import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage


object DisplayItemContainerStorage
  extends IStorage[ DisplayItemContainer ]
{

  override def readNBT( capability: Capability[ DisplayItemContainer ],
                        instance:   DisplayItemContainer,
                        side:       EnumFacing,
                        nbt:        NBTBase ): Unit =
    nbt match {
      case compound: NBTTagCompound => instance.deserializeNBT( compound )
      case _ =>
    }

  override def writeNBT( capability: Capability[ DisplayItemContainer ],
                         instance:   DisplayItemContainer,
                         side:       EnumFacing ): NBTBase =
    instance.serializeNBT()

}
