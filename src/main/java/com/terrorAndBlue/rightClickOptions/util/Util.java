package com.terrorAndBlue.rightClickOptions.util;

import net.minecraft.entity.player.EntityPlayer;

public class Util
{
	public static boolean isPlayerOnline(EntityPlayer player)
	{
		return player.worldObj != null && player.worldObj.getEntityByID(player.getEntityId()) == player;
	}
	
	public static boolean isPlayerValid(EntityPlayer player)
	{
		if(player == null)
			return false;
		if(player.isDead)
			return false;
		
		return isPlayerOnline(player);
	}
	
	public static String upper(String str)
	{
		String first = str.substring(0, 1);
		String after = str.substring(1);
		
		return first.toUpperCase() + after;
	}
}
