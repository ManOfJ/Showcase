package com.manofj.minecraft.moj_showcase.capability

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable

import com.manofj.minecraft.moj_showcase.tileentity.ShowcaseBase


class DisplayItemContainerProvider( showcase: ShowcaseBase )
  extends ICapabilitySerializable[ NBTTagCompound ]
{
  import com.manofj.minecraft.moj_showcase.Showcase.{ displayItemContainerCap => CAPABILITY }


  private[ this ] val displayItem = new DisplayItemContainerImpl( showcase )


  override def getCapability[ T ]( capability: Capability[ T ], facing: EnumFacing ): T =
    if ( hasCapability( capability, facing ) ) CAPABILITY.cast[ T ]( displayItem ) else null.asInstanceOf[ T ]

  override def hasCapability( capability: Capability[ _ ], facing: EnumFacing ): Boolean =
    capability == CAPABILITY

  override def deserializeNBT( nbt: NBTTagCompound ): Unit =
    CAPABILITY.getStorage.readNBT( CAPABILITY, displayItem, null, nbt )

  override def serializeNBT(): NBTTagCompound =
    CAPABILITY.getStorage.writeNBT( CAPABILITY, displayItem, null ).asInstanceOf[ NBTTagCompound ]

}
