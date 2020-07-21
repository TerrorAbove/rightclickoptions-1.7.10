package com.terrorAndBlue.rightClickOptions.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.reflect.Field;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.trade.TradeContainer;
import com.terrorAndBlue.rightClickOptions.trade.TradeInventory;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession.Status;
import com.terrorAndBlue.rightClickOptions.util.Util;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int id, final EntityPlayer player, World world, int x, int y, int z)
	{
		if (id == TradeGUI.GUI_ID)
		{
			for(TradeSession sesh : RightClickOptions.tradeSessions)
			{
				if(sesh.getStatus() == Status.INCOMPLETE)
				{
					if(sesh.getP0().getEntityId() == player.getEntityId())
					{
						return new TradeContainer(player.inventory, sesh.getTradeInv0());
					}
					
					if(sesh.getP1().getEntityId() == player.getEntityId())
					{
						return new TradeContainer(player.inventory, sesh.getTradeInv1());
					}
				}
			}
			System.err.println("ERROR! Could not retrieve the correct TradeSession for getServerGuiElement!");
		}
		
		if (id == RightClickGUI.GUI_ID)
		{
			return new Container()
			{
				@Override
				public boolean canInteractWith(EntityPlayer p)
				{
					return p.getEntityId() == player.getEntityId();
				}
			};
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		if(id == RightClickGUI.GUI_ID)
			return new RightClickGUI(player);

		if(id == TradeGUI.GUI_ID)
		{
			TradeInventory tradeInv = new TradeInventory(player);
			return new TradeGUI(tradeInv);
		}

		return null;
	}
}
