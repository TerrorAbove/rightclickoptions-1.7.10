package com.terrorAndBlue.rightClickOptions.trade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.gui.TradeGUI;
import com.terrorAndBlue.rightClickOptions.packet.GuiActionPacket;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession.Status;
import com.terrorAndBlue.rightClickOptions.util.Util;
import com.terrorAndBlue.rightClickOptions.packet.ClientTradeUpdatePacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;

public class TradeSession
{
	private TradeInventory inv0;
	private TradeInventory inv1;
	
	private EntityPlayer player_0;
	private EntityPlayer player_1;
	
	private int sessionInitStatus;
	
	private long timeStarted;
	
	private Status status;
	
	private Timer timer;
	
	private boolean p0_accepted;
	private boolean p1_accepted;
	private boolean last_p0_accepted;
	private boolean last_p1_accepted;
	
	private boolean needsOfferSlotUpdate;
	
	private boolean sessionCheckRunning;
	
	private boolean roomForTrade;//this is true only if BOTH players have adequate room
	
	private int numberOfTrades;
	
	/**
	 * Instantiates and "starts" a trade session between two players.
	 */
	public TradeSession(TradeInventory a, TradeInventory b)
	{
		assert a != null && b != null && a.getPlayer() != null && b.getPlayer() != null;
		
		inv0 = a;
		inv1 = b;
		
		player_0 = a.getPlayer();
		player_1 = b.getPlayer();
		
		p0_accepted = false;
		p1_accepted = false;
		
		timeStarted = System.currentTimeMillis();
		
		status = Status.INCOMPLETE;
		
		last_p0_accepted = false;
		last_p1_accepted = false;
		
		needsOfferSlotUpdate = false;
		
		sessionInitStatus = 0;
		sessionCheckRunning = false;
		
		numberOfTrades = 0;
		
		roomForTrade = true;
		
		timer = new Timer();
	}
	
	public TradeSession beginTask(long startDelay, long interval)
	{
		if(!sessionCheckRunning)
		{
			timer.scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					if(status != Status.INCOMPLETE)
					{
						this.cancel();
						return;
					}
					
					NBTTagCompound comp0 = new NBTTagCompound();
					NBTTagCompound comp1 = new NBTTagCompound();
					
					boolean p1Online = Util.isPlayerValid(player_0);
					boolean p2Online = Util.isPlayerValid(player_1);
					
					boolean bothOnline = p1Online && p2Online;
					
					if(bothOnline && player_0.openContainer instanceof TradeContainer && player_1.openContainer instanceof TradeContainer)
					{
						TradeContainer c0 = (TradeContainer)player_0.openContainer;
						TradeContainer c1 = (TradeContainer)player_1.openContainer;

						if(p0_accepted && p1_accepted)
						{
							p0_accepted = false;
							p1_accepted = false;

							if(roomForTrade && player_0.inventory.getItemStack() == null && player_1.inventory.getItemStack() == null)
							{
								c0.doTradeAcceptedSwap();
								c1.doTradeAcceptedSwap();

								needsOfferSlotUpdate = true;

								if(numberOfTrades < Integer.MAX_VALUE)//we don't want this to go negative
									numberOfTrades++;//not that that would ever happen lol
							}
						}

						if(needsOfferSlotUpdate)
						{
							inv0.writeToNBT(comp0);
							inv1.writeToNBT(comp1);

							needsOfferSlotUpdate = false;
						}
						
						byte[] p0_highlight_data = new byte[18];
						byte[] p1_highlight_data = new byte[18];
						
						for(int i = 0; i < p0_highlight_data.length; i++)
						{
							Slot s0 = c0.getSlot(i);
							Slot s1 = c1.getSlot(i);
							
							if(s0 instanceof Highlightable && s1 instanceof Highlightable)
							{
								p0_highlight_data[i] = (byte) ((Highlightable)s0).getHighlight().ordinal();
								p1_highlight_data[i] = (byte) ((Highlightable)s1).getHighlight().ordinal();
							}
						}
						
						comp0.setByteArray("highlightData", p0_highlight_data);
						comp1.setByteArray("highlightData", p1_highlight_data);

						comp0.setBoolean("roomForTrade", roomForTrade);
						comp1.setBoolean("roomForTrade", roomForTrade);

						int p0_space = inv0.getFreeSpaceInMainInventory();
						int p1_space = inv1.getFreeSpaceInMainInventory();
						
						comp0.setInteger("otherPlayerFreeSlots", p1_space);
						comp1.setInteger("otherPlayerFreeSlots", p0_space);

						updateInfoString(comp0, comp1);
						
						RightClickOptions.wrapper.sendTo(new ClientTradeUpdatePacket(comp0), (EntityPlayerMP)player_0);
						RightClickOptions.wrapper.sendTo(new ClientTradeUpdatePacket(comp1), (EntityPlayerMP)player_1);
					}
					else
					{
						if(status == Status.INCOMPLETE)
						{
							if(numberOfTrades == 0)
							{
								setStatus(Status.CANCELED);
								
								if(p1Online)
								{
									player_0.addChatComponentMessage(new ChatComponentText("\u00A74"+"Trade canceled."));
								}
								if(p2Online)
								{
									player_1.addChatComponentMessage(new ChatComponentText("\u00A74"+"Trade canceled."));
								}
							}
							else
							{
								setStatus(Status.COMPLETE);
							}
						}
						/*else if(status == Status.ACCEPTED)
						{
							if(p1Online)
							{
								player_0.addChatComponentMessage(new ChatComponentText("\u00A7a"+"Trade accepted!"));
							}
							if(p2Online)
							{
								player_1.addChatComponentMessage(new ChatComponentText("\u00A7a"+"Trade accepted!"));
							}
						}*/
						
						player_0.closeScreen();
						player_1.closeScreen();
						
						removeSession();
					}
				}
			}, startDelay, interval);
		}
		
		sessionCheckRunning = true;
		
		return this;
	}
	
	public void setStatus(Status status)
	{
		this.status = status;
	}
	
	public EntityPlayer getP0()
	{
		return player_0;
	}
	
	public EntityPlayer getP1()
	{
		return player_1;
	}
	
	public int getSessionInitStatus()
	{
		return sessionInitStatus;
	}
	
	public void setSessionInitStatus(int status)
	{
		sessionInitStatus = status;
	}
	
	public TradeInventory getTradeInv0()
	{
		return inv0;
	}
	
	public TradeInventory getTradeInv1()
	{
		return inv1;
	}
	
	public Status getStatus()
	{
		return status;
	}
	
	public long getTimeStarted()
	{
		return timeStarted;
	}
	
	public void setP0Accepted(boolean accepted)
	{
		p0_accepted = accepted;
	}
	
	public void setP1Accepted(boolean accepted)
	{
		p1_accepted = accepted;
	}
	
	public boolean isP0Accepting()
	{
		return p0_accepted;
	}
	
	public boolean isP1Accepting()
	{
		return p1_accepted;
	}
	
	public void setNeedsOfferSlotUpdate()
	{
		needsOfferSlotUpdate = true;
	}
	
	public int getNumberOfTrades()
	{
		return numberOfTrades;
	}
	
	public void setRoomForTrade(boolean b)
	{
		roomForTrade = b;
	}
	
	public boolean roomForTrade()
	{
		return roomForTrade;
	}
	
	private void removeSession()
	{
		RightClickOptions.tradeSessions.remove(this);
	}
	
	public void updateInfoString(NBTTagCompound c0, NBTTagCompound c1)
	{
		try
		{
			String toSet0 = "";
			String toSet1 = "";
			
			if(p0_accepted)
			{
				if(p1_accepted)
				{
					toSet0 = "Trade Confirmed!";
					toSet1 = "Trade Confirmed!";
				}
				else
				{
					toSet0 = "Waiting for the other player to accept...";
					toSet1 = "The other player has accepted the trade.";
				}
			}
			else
			{
				if(p1_accepted)
				{
					toSet1 = "Waiting for the other player to accept...";
					toSet0 = "The other player has accepted the trade.";
				}
			}
			
			if((last_p0_accepted != p0_accepted) || (last_p1_accepted != p1_accepted))
			{
				c0.setString(TradeInventory.TAG_NAME + "_infoString", toSet0);
				c1.setString(TradeInventory.TAG_NAME + "_infoString", toSet1);
			}
			
			last_p0_accepted = p0_accepted;
			last_p1_accepted = p1_accepted;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static enum Status
	{
		INCOMPLETE,
		CANCELED,
		COMPLETE
	}
}
