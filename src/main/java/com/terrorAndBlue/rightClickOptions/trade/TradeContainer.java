package com.terrorAndBlue.rightClickOptions.trade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.gui.TradeGUI;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;

public class TradeContainer extends Container
{
	private TradeInventory tradeInv;
	private InventoryPlayer playerInv;
	
	public TradeContainer(InventoryPlayer playerInventory, TradeInventory tradeInventory)
	{
		tradeInv = tradeInventory;
		playerInv = playerInventory;
		
		addUpperLeftSlots(tradeInventory);
		
		addUpperRightSlots(tradeInventory);
		
		bindPlayerInventory(playerInventory);
	}
	
	public TradeInventory getTradeInventory()
	{
		return tradeInv;
	}
	
	private void addUpperLeftSlots(TradeInventory inv)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				addSlotToContainer(new TradeOfferSlot(inv, j + i * 3, 8 + j * 18, 17 + i * 18));
			}
		}
	}
	
	private void addUpperRightSlots(TradeInventory inv)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				addSlotToContainer(new DisplayOnlySlot(inv, 9 + j + i * 3, 116 + j * 18, 17 + i * 18));
			}
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return tradeInv.isUseableByPlayer(player);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new PlayerInventorySlot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new PlayerInventorySlot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}
	
	/**
	 * only called by TradeSession upon both players clicking accept
	 */
	public void doTradeAcceptedSwap()
	{
		for(int i = 0; i < 9; i++)
		{
			ItemStack temp = tradeInv.getStackInSlot(i+9);
			ItemStack temp2 = tradeInv.getStackInSlot(i);
			
			if(temp != null)
				temp = temp.copy();
			if(temp2 != null)
				temp2 = temp2.copy();
			
			tradeInv.setInventorySlotContents(i, temp);
			tradeInv.setInventorySlotContents(i+9, temp2);
		}
	}
	
	@Override
	public void onContainerClosed(EntityPlayer p)
	{
		for(int i = 0; i < 9; i++)
			if(tradeInv.getStackInSlot(i) != null)
				mergeStack(playerInv, true, (Slot)inventorySlots.get(i), inventorySlots, true);
		
		InventoryPlayer inventory = p.inventory;
		Container c = p.inventoryContainer;

        if (inventory.getItemStack() != null)
        {
        	for(Object o : c.inventorySlots)
        	{
        		if(o instanceof Slot)
        		{
        			Slot slot = (Slot)o;
        			
        			if(!slot.getHasStack())
        			{
        				slot.putStack(inventory.getItemStack());
        				inventory.setItemStack(null);
        				
        				//TODO testing client sync
        				
        				c.detectAndSendChanges();
        				
        				break;
        			}
        		}
        	}
        }
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
	{
		return handleShiftClick(this, player, slotIndex);
	}

	/**
	 * <p>Implementation for shift-clicking in Containers. This is a drop-in replacement you can call from the
	 * {@link Container#transferStackInSlot(EntityPlayer, int)} method in your Container.</p>
	 * <ul>
	 * <li>When the slot to be moved is not in the inventory of the player it will be moved there as known
	 * from vanilla inventories.</li>
	 * <li>When the slot to be moved is in the inventory of the player it will be moved to the first available Slot
	 * that accepts it.</li>
	 * </ul>
	 * <p>This behavior can be overridden by implementing {@link de.take_weiland.mods.commons.inv.SpecialShiftClick} on
	 * your Container.</p>
	 *
	 * @param container the Container
	 * @param player    the player performing the shift-click
	 * @param slotIndex the slot being shift-clicked
	 * @return null if no transfer is possible
	 */
	public static ItemStack handleShiftClick(Container container, EntityPlayer player, int slotIndex)
	{
		@SuppressWarnings("unchecked")
		List<Slot> slots = container.inventorySlots;
		Slot sourceSlot = slots.get(slotIndex);
		ItemStack inputStack = sourceSlot.getStack();
		if (inputStack == null) {
			return null;
		}

		boolean sourceIsPlayer = sourceSlot.inventory == player.inventory;

		ItemStack copy = inputStack.copy();

		if (sourceIsPlayer)
		{
			// transfer to any inventory
			if (!mergeStack(player.inventory, false, sourceSlot, slots, false)) {
				return null;
			} else {
				return copy;
			}
		} else {
			// transfer to player inventory
			// this is heuristic, but should do fine. if it doesn't the only "issue" is that vanilla behavior is not matched 100%
			//boolean isMachineOutput = !sourceSlot.isItemValid(inputStack);
			if (!mergeStack(player.inventory, true, sourceSlot, slots, false)) {
				return null;
			} else {
				return copy;
			}
		}
	}

	// returns true if it has found a target
	private static boolean mergeStack(InventoryPlayer playerInv, boolean mergeIntoPlayer, Slot sourceSlot, List<Slot> slots, boolean reverse)
	{
		ItemStack sourceStack = sourceSlot.getStack();

		int originalSize = sourceStack.stackSize;

		int len = slots.size();
		int idx;

		// first pass, try to merge with existing stacks
		// can skip this if stack is not stackable at all
		if (sourceStack.isStackable()) {
			idx = reverse ? len - 1 : 0;

			while (sourceStack.stackSize > 0 && (reverse ? idx >= 0 : idx < len)) {
				Slot targetSlot = slots.get(idx);
				if ((targetSlot.inventory == playerInv) == mergeIntoPlayer) {
					ItemStack target = targetSlot.getStack();
					if (equal(sourceStack, target)) { // also checks target != null, because stack is never null
						int targetMax = Math.min(targetSlot.getSlotStackLimit(), target.getMaxStackSize());
						int toTransfer = Math.min(sourceStack.stackSize, targetMax - target.stackSize);
						if (toTransfer > 0) {
							target.stackSize += toTransfer;
							sourceStack.stackSize -= toTransfer;
							
							targetSlot.putStack(new ItemStack(target.getItem(), target.stackSize));
							
							if(sourceStack.stackSize > 0)
								sourceSlot.putStack(new ItemStack(sourceStack.getItem(), sourceStack.stackSize));
							else
								sourceSlot.putStack(null);
						}
					}
				}

				if (reverse) {
					idx--;
				} else {
					idx++;
				}
			}
			if (sourceStack.stackSize == 0) {
				sourceSlot.putStack(null);
				return true;
			}
		}

		// 2nd pass: try to put anything remaining into a free slot
		idx = reverse ? len - 1 : 0;
		while (reverse ? idx >= 0 : idx < len) {
			Slot targetSlot = slots.get(idx);
			if ((targetSlot.inventory == playerInv) == mergeIntoPlayer
					&& !targetSlot.getHasStack() && targetSlot.isItemValid(sourceStack)) {
				targetSlot.putStack(sourceStack);
				sourceSlot.putStack(null);
				return true;
			}

			if (reverse) {
				idx--;
			} else {
				idx++;
			}
		}

		// we had success in merging only a partial stack
		if (sourceStack.stackSize != originalSize) {
			sourceSlot.onSlotChanged();
			return true;
		}
		return false;
	}

	/**
	 * <p>Determine if the given ItemStacks are equal.</p>
	 * <p>This method checks the Item, damage value and NBT data of the stack, it does not check stack sizes.</p>
	 *
	 * @param a an ItemStack
	 * @param b an ItemStack
	 * @return true if the ItemStacks are equal
	 */
	public static boolean equal(ItemStack a, ItemStack b)
	{
		return a == b || (a != null && b != null && equalsImpl(a, b));
	}

	private static boolean equalsImpl(ItemStack a, ItemStack b)
	{
		//TODO testing
		
		return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
	}
}
