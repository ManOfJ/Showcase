package com.manofj.minecraft.moj_showcase.network

import io.netty.buffer.ByteBuf

import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

import com.manofj.commons.minecraftforge.network.MessageHandler

import com.manofj.minecraft.moj_showcase.inventory.ShowcaseContainer


object UpdateSetting
  extends MessageHandler[ UpdateSetting, IMessage ]
{

  override def onMessage( message: UpdateSetting, ctx: MessageContext ): IMessage = {
    val player = ctx.getServerHandler.playerEntity

    player.openContainer match {
      case showcase: ShowcaseContainer =>
        message.settings.foreach { case ( k, v ) =>
          showcase.getShowcase.setField( k, math.round( v * 10000F ) )
        }

      case _ =>
    }

    null
  }

}

class UpdateSetting
  extends IMessage
{
  var settings = Map.empty[ Int, Float ]


  def this( settings: Map[ Int, Float ] ) = {
    this()
    this.settings = settings
  }


  override def fromBytes( buf: ByteBuf ): Unit = {
    settings = Map.empty[ Int, Float ]
    ( 0 until buf.readInt() ).foreach { i =>
      settings += buf.readInt() -> buf.readFloat()
    }
  }

  override def toBytes( buf: ByteBuf ): Unit = {
    buf.writeInt( settings.size )
    settings.foreach { case ( k, v ) =>
      buf.writeInt( k )
      buf.writeFloat( v )
    }
  }

}