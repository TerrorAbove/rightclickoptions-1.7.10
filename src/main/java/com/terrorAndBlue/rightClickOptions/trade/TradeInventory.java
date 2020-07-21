package com.terrorAndBlue.rightClickOptions.trade;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class TradeInventory implements IInventory
{
	public static final String TAG_NAME = "RCOTradeInventory";
	
	private static final int TRADE_INVENTORY_SIZE = 18;
	
	private ItemStack[] inv;
	private EntityPlayer player;
	
	public TradeSession session;//this should only be used SERVER SIDE!

	public TradeInventory(EntityPlayer player)
	{
		this.player = player;
		inv = new ItemStack[TRADE_INVENTORY_SIZE];
	}
	
	public EntityPlayer getPlayer()
	{
		return player;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack != null)
		{
			if (stack.stackSize <= amount)
			{
				setInventorySlotContents(slot, null);
			}
			else
			{
				stack = stack.splitStack(amount);
				if (stack.stackSize == 0)
				{
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}

	@Override
	public String getInventoryName()
	{
		return "rightClickOptions.trade";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public int getSizeInventory()
	{
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inv[slot];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack != null)
		{
			setInventorySlotContents(slot, null);
		}
		return stack;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return this.player == player;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if(slot < inv.length)
		{
			inv[slot] = stack;
			if (stack != null && stack.stackSize > getInventoryStackLimit())
			{
				stack.stackSize = getInventoryStackLimit();
			}
		}
	}

	@Override
	public void markDirty()
	{
		for (int i = 0; i < getSizeInventory(); ++i)
		{
			if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
				inv[i] = null;
			}
		}
	}
	
	public void clearItems()
	{
		inv = new ItemStack[TRADE_INVENTORY_SIZE];
	}
	
	public boolean isEmpty()
	{
		for(int i = 0; i < inv.length; i++)
			if(inv[i] != null && inv[i].stackSize > 0)
				return false;
		
		return true;
	}
	
	private boolean isOtherSideEmpty()
	{
		for(int i = 9; i < inv.length; i++)
			if(inv[i] != null && inv[i].stackSize > 0)
				return false;
		
		return true;
	}
	
	public int getFreeSpaceInMainInventory()
	{
		InventoryPlayer invP = player.inventory;
		
		int freeSpaces = invP.getItemStack() == null ? 0 : -1;//effectively subtracts 1 if player is holding something
		
		for(int i = 0; i < invP.mainInventory.length; i++)
			if(invP.mainInventory[i] == null)
				freeSpaces++;
		
		return freeSpaces;
	}
	
	public boolean hasRoom()//checks whether the player has enough free space to accept a trade
	{
		if(isOtherSideEmpty())
			return true;
		
		InventoryPlayer invP = player.inventory;
		
		int freeSpaces = getFreeSpaceInMainInventory();
		
		if(freeSpaces >= 9)
			return true;
		
		ArrayList<ItemStack> toMerge = new ArrayList<ItemStack>();
		
		for(int i = 9; i < getSizeInventory(); i++)
		{
			ItemStack p = inv[i];
			
			if(p != null && p.stackSize > 0)
			{
				boolean found = false;
				for(ItemStack s : toMerge)
				{
					if(TradeContainer.equal(s, p))
					{
						s.stackSize += p.stackSize;
						found = true;
						break;
					}
				}
				
				if(!found)
				{
					toMerge.add(p.copy());
				}
			}
		}
		
		ArrayList<ItemStack> extraFullStacks = new ArrayList<ItemStack>();
		ArrayList<ItemStack> partialStacks = new ArrayList<ItemStack>();
		
		Iterator<ItemStack> iter = toMerge.iterator();
		while(iter.hasNext())
		{
			ItemStack s = iter.next();
			
			int max = s.getMaxStackSize();
			
			double r = (double)s.stackSize / max;
			int a = (int)r;
			int b = s.stackSize % max;
			
			if(r > 1)//if we have strictly more than 1 stack of given item type
			{
				for(int i = 1; i < a; i++)//for each extra full stack...
				{
					extraFullStacks.add(new ItemStack(s.getItem(), max));//add a full stack
				}
				
				if(b > 0)
					partialStacks.add(new ItemStack(s.getItem(), b));//add a partial stack at the end
				
				s.stackSize = max;
			}
			else if(r < 1)//if we have less than a full stack of the given item type
			{
				partialStacks.add(s.copy());//we'll call it a "partial stack"
				iter.remove();//and remove it from our main list
			}
		}
		
		toMerge.addAll(extraFullStacks);
		
		iter = partialStacks.iterator();
		while(iter.hasNext())
		{
			ItemStack p = iter.next();
			
			for(int i = 0; i < invP.mainInventory.length; i++)
			{
				if(TradeContainer.equal(invP.mainInventory[i], p) && invP.mainInventory[i].stackSize + p.stackSize <= p.getMaxStackSize())
				{
					iter.remove();//remove since we need one less space (merge possible)
					break;
				}
			}
		}
		
		toMerge.addAll(partialStacks);

		return toMerge.size() <= freeSpaces;
	}
	
	public void writeToNBT(NBTTagCompound compound)
	{
		NBTTagList items = new NBTTagList();

		for (int i = 0; i < getSizeInventory(); ++i)
		{
			if (getStackInSlot(i) != null)
			{
				NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot", (byte) i);
				getStackInSlot(i).writeToNBT(item);
				items.appendTag(item);
			}
		}
		
		// We're storing our items in a custom tag list using our 'tagName' from above
		// to prevent potential conflicts
		compound.setTag(TAG_NAME, items);
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		if(compound.hasKey(TAG_NAME))
		{
			NBTTagList items = compound.getTagList(TAG_NAME, compound.getId());
			
			ItemStack[] temp = new ItemStack[TRADE_INVENTORY_SIZE];

			for (int i = 0; i < items.tagCount(); ++i)
			{
				NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
				byte slot = item.getByte("Slot");

				if (slot >= 0 && slot < getSizeInventory())
				{
					temp[slot] = ItemStack.loadItemStackFromNBT(item);
				}
			}
			
			inv = temp;
		}
	}
}
