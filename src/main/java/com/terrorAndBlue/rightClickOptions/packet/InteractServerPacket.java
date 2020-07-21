package com.terrorAndBlue.rightClickOptions.packet;

import java.lang.reflect.Field;
import java.util.Iterator;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.gui.RightClickGUI;
import com.terrorAndBlue.rightClickOptions.gui.TradeGUI;
import com.terrorAndBlue.rightClickOptions.trade.TradeInventory;
import com.terrorAndBlue.rightClickOptions.trade.TradeOffer;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class InteractServerPacket implements IMessage
{
	private int otherPlayerId;
	
	public InteractServerPacket(int otherPlayerId)
	{
		this.otherPlayerId = otherPlayerId;
	}
	
	public InteractServerPacket() {}
	
	public static class Handler implements IMessageHandler<InteractServerPacket, InteractServerResponsePacket>
	{
		@Override
		public InteractServerResponsePacket onMessage(InteractServerPacket packet, MessageContext ctx)
		{
			EntityPlayer thePlayer = ctx.getServerHandler().playerEntity;
			Entity other = thePlayer.worldObj.getEntityByID(packet.otherPlayerId);

			if(other instanceof EntityPlayer)
			{
				EntityPlayer otherPlayer = (EntityPlayer)other;
				RightClickOptions.interactTargets.put(thePlayer.getEntityId(), packet.otherPlayerId);

				Iterator<TradeOffer> iter = RightClickOptions.tradeOffers.keySet().iterator();

				TradeOffer first = null;

				while(iter.hasNext())
				{
					TradeOffer offer = iter.next();

					if(offer.getPlayer() == thePlayer && RightClickOptions.tradeOffers.get(offer) == otherPlayer && thePlayer.worldObj == otherPlayer.worldObj && offer.isActive())
					{
						if(first == null)
							first = offer;

						iter.remove();
					}
				}
				
				if(first == null)//open right click main gui
				{
					thePlayer.openGui(RightClickOptions.instance, RightClickGUI.GUI_ID, thePlayer.worldObj, (int)thePlayer.posX, (int)thePlayer.posY, (int)thePlayer.posZ);
					
					return new InteractServerResponsePacket(packet.otherPlayerId);
				}
				
				int p1X = (int)thePlayer.posX;
				int p1Y = (int)thePlayer.posY;
				int p1Z = (int)thePlayer.posZ;
				
				int p2X = (int)otherPlayer.posX;
				int p2Y = (int)otherPlayer.posY;
				int p2Z = (int)otherPlayer.posZ;
				
				TradeInventory tradeInv = new TradeInventory(thePlayer);
				TradeInventory tradeInv2 = new TradeInventory(otherPlayer);
				
				TradeSession theSesh = new TradeSession(tradeInv, tradeInv2);
				tradeInv.session = theSesh;
				tradeInv2.session = theSesh;
				
				RightClickOptions.tradeSessions.add(theSesh);
				thePlayer.openGui(RightClickOptions.instance, TradeGUI.GUI_ID, thePlayer.worldObj, p1X, p1Y + 1, p1Z);
				otherPlayer.openGui(RightClickOptions.instance, TradeGUI.GUI_ID, otherPlayer.worldObj, p2X, p2Y + 1, p2Z);
				
				RightClickOptions.wrapper.sendTo(new InteractServerResponsePacket(thePlayer.getEntityId()), (EntityPlayerMP) otherPlayer);
				return new InteractServerResponsePacket(packet.otherPlayerId);
			}
			return null;
		}
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		otherPlayerId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(otherPlayerId);
	}
}
