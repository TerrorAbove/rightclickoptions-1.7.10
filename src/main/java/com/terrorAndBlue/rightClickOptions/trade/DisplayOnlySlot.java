package com.terrorAndBlue.rightClickOptions.trade;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class DisplayOnlySlot extends Slot implements Highlightable
{
	public Highlight highlight;
	
	public DisplayOnlySlot(IInventory inv, int slotNum, int x, int y)
	{
		super(inv, slotNum, x, y);
		highlight = Highlight.NONE;
	}
	
	@Override
	public boolean canTakeStack(EntityPlayer player)
	{
		return false;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return false;
	}

	@Override
	public Highlight getHighlight()
	{
		return highlight;
	}
}
