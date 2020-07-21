package com.terrorAndBlue.rightClickOptions.packet;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class InteractServerResponsePacket implements IMessage
{
	private int other_player_id;
	
	public InteractServerResponsePacket() {}
	
	public InteractServerResponsePacket(int other_player_id)
	{
		this.other_player_id = other_player_id;
	}
	
	public static class Handler implements IMessageHandler<InteractServerResponsePacket, IMessage>
	{
		@Override
		public IMessage onMessage(InteractServerResponsePacket message, MessageContext ctx)
		{
			if(ctx.side == Side.CLIENT)
			{
				EntityPlayer p = Minecraft.getMinecraft().thePlayer;
				Entity other = p.worldObj.getEntityByID(message.other_player_id);
				
				if(other instanceof EntityPlayer)
				{
					RightClickOptions.interactTargets.put(p.getEntityId(), message.other_player_id);
				}
			}
			return null;
		}
	}
	
	@Override
	public void fromBytes(ByteBuf b)
	{
		other_player_id = b.readInt();
	}

	@Override
	public void toBytes(ByteBuf b)
	{
		b.writeInt(other_player_id);
	}
}
