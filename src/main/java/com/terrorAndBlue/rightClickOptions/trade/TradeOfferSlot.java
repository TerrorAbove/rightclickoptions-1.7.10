package com.terrorAndBlue.rightClickOptions.trade;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession.Status;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TradeOfferSlot extends Slot implements Highlightable
{
	public Highlight highlight;
	
	public TradeOfferSlot(IInventory inv, int slot_ID_within_Inv, int x_on_screen, int y_on_screen)
	{
		super(inv, slot_ID_within_Inv, x_on_screen, y_on_screen);
		this.highlight = Highlight.NONE;
	}
	
	public void onSlotChanged()
	{
		if(inventory instanceof TradeInventory)
		{
			TradeInventory t = (TradeInventory)inventory;
			EntityPlayer p = t.getPlayer();
			if(p != null && p.worldObj != null && !p.worldObj.isRemote)//is server side
			{
				for(TradeSession sesh : RightClickOptions.tradeSessions)
				{
					if(sesh.getStatus() == Status.INCOMPLETE)
					{
						if(sesh.getP0().getEntityId() == p.getEntityId())
						{
							sesh.getTradeInv1().setInventorySlotContents(slotNumber+9, getStack());
							
							sesh.setP0Accepted(false);//slots changed so cancel acceptance for both players
							sesh.setP1Accepted(false);
							
							highlight = Highlight.NONE;
							if(sesh.getP1().openContainer instanceof TradeContainer)
							{
								TradeContainer c1 = (TradeContainer)sesh.getP1().openContainer;
								
								Object o = c1.inventorySlots.get(slotNumber+9);
								if(o instanceof DisplayOnlySlot)
								{
									((DisplayOnlySlot)o).highlight = Highlight.NONE;
								}
							}
							
							boolean check0 = sesh.getTradeInv0().hasRoom();
							boolean check1 = sesh.getTradeInv1().hasRoom();
							
							sesh.setRoomForTrade(check0 && check1);
							
							sesh.setNeedsOfferSlotUpdate();
							
							break;
						}
						
						if(sesh.getP1().getEntityId() == p.getEntityId())
						{
							sesh.getTradeInv0().setInventorySlotContents(slotNumber+9, getStack());
							
							sesh.setP0Accepted(false);//slots changed so cancel acceptance for both players
							sesh.setP1Accepted(false);
							
							highlight = Highlight.NONE;
							if(sesh.getP0().openContainer instanceof TradeContainer)
							{
								TradeContainer c0 = (TradeContainer)sesh.getP0().openContainer;
								
								Object o = c0.inventorySlots.get(slotNumber+9);
								if(o instanceof DisplayOnlySlot)
								{
									((DisplayOnlySlot)o).highlight = Highlight.NONE;
								}
							}
							
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

	@Override
	public Highlight getHighlight()
	{
		return highlight;
	}
}
