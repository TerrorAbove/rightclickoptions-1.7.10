package com.terrorAndBlue.rightClickOptions.trade;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession.Status;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class PlayerInventorySlot extends Slot
{
	public PlayerInventorySlot(IInventory inv, int slot_ID_within_Inv, int x_on_screen, int y_on_screen)
	{
		super(inv, slot_ID_within_Inv, x_on_screen, y_on_screen);
	}
	
	public void onSlotChanged()
	{
		if(inventory instanceof InventoryPlayer)
		{
			InventoryPlayer t = (InventoryPlayer)inventory;
			EntityPlayer p = t.player;
			if(p != null && p.worldObj != null && !p.worldObj.isRemote)//is server side
			{
				for(TradeSession sesh : RightClickOptions.tradeSessions)
				{
					if(sesh.getStatus() == Status.INCOMPLETE)
					{
						if(sesh.getP0().getEntityId() == p.getEntityId() || sesh.getP1().getEntityId() == p.getEntityId())
						{
							boolean check0 = sesh.getTradeInv0().hasRoom();
							boolean check1 = sesh.getTradeInv1().hasRoom();
							
							sesh.setRoomForTrade(check0 && check1);
							
							sesh.setNeedsOfferSlotUpdate();
							
							break;
						}
					}
				}
			}
		}
	}
}
