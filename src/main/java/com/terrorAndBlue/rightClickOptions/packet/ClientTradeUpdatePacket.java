package com.terrorAndBlue.rightClickOptions.packet;

import io.netty.buffer.ByteBuf;

import com.terrorAndBlue.rightClickOptions.gui.TradeGUI;
import com.terrorAndBlue.rightClickOptions.trade.TradeContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class ClientTradeUpdatePacket implements IMessage
{
	private NBTTagCompound tagCompound;
	
	public ClientTradeUpdatePacket() {}
	
	public ClientTradeUpdatePacket(NBTTagCompound tagCompound)
	{
		this.tagCompound = tagCompound;
	}

	public static class Handler implements IMessageHandler<ClientTradeUpdatePacket, ClientTradeUpdateResponsePacket>
	{
		@Override
		public ClientTradeUpdateResponsePacket onMessage(ClientTradeUpdatePacket packet, MessageContext ctx)
		{
			if(ctx.side.isClient())
			{
				boolean containerOpen = Minecraft.getMinecraft().thePlayer.openContainer instanceof TradeContainer;
				boolean guiOpen = Minecraft.getMinecraft().currentScreen instanceof TradeGUI;
				
				if(containerOpen)
				{
					TradeContainer tc = (TradeContainer)Minecraft.getMinecraft().thePlayer.openContainer;
					tc.getTradeInventory().readFromNBT(packet.tagCompound);
				}
				if(guiOpen)
				{
					TradeGUI gui = (TradeGUI)Minecraft.getMinecraft().currentScreen;
					gui.readFromNBT(packet.tagCompound);
				}
				
				/*boolean handEmpty = true;
				
				try
				{
					handEmpty = Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null;
				}
				catch(Exception ex) {}
				
				return new ClientTradeUpdateResponsePacket(!handEmpty);*/
			}
			return null;
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		tagCompound = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, tagCompound);
	}
}
