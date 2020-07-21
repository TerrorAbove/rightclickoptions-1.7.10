package com.terrorAndBlue.rightClickOptions.packet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.trade.TradeContainer;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession.Status;
import com.terrorAndBlue.rightClickOptions.util.Util;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class ClientTradeUpdateResponsePacket implements IMessage
{
	private boolean itemInHand;
	
	public ClientTradeUpdateResponsePacket(boolean itemInHand)
	{
		this.itemInHand = itemInHand;
	}
	
	public ClientTradeUpdateResponsePacket() {}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.itemInHand = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(itemInHand);
	}
	
	public static class Handler implements IMessageHandler<ClientTradeUpdateResponsePacket, IMessage>
	{
		@Override
		public IMessage onMessage(ClientTradeUpdateResponsePacket packet, MessageContext ctx)
		{
			EntityPlayer thePlayer = ctx.getServerHandler().playerEntity;
			
			Iterator<TradeSession> iter = RightClickOptions.tradeSessions.iterator();
			
			while(iter.hasNext())
			{
				TradeSession sesh = iter.next();

				if(sesh.getP0() == thePlayer)
				{
					//not currently in use; may be removed
					
					//sesh.setP0_itemInHand(packet.itemInHand);
					break;
				}
				else if(sesh.getP1() == thePlayer)
				{
					//sesh.setP1_itemInHand(packet.itemInHand);
					break;
				}
			}
			return null;
		}
	}
}
