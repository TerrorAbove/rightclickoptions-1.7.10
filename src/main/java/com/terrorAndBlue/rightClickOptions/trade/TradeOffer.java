package com.terrorAndBlue.rightClickOptions.trade;

import net.minecraft.entity.player.EntityPlayer;

public class TradeOffer
{
	private EntityPlayer target;
	private long time;
	
	public TradeOffer(EntityPlayer target)
	{
		this.target = target;
	}
	
	public TradeOffer start()
	{
		this.time = System.currentTimeMillis() + 30000;
		return this;
	}
	
	public EntityPlayer getPlayer()
	{
		return target;
	}
	
	public boolean isActive()
	{
		return time >= System.currentTimeMillis();
	}
}
