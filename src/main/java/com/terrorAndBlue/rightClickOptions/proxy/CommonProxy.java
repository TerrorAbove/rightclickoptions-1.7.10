package com.terrorAndBlue.rightClickOptions.proxy;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.gui.RightClickGUI;
import com.terrorAndBlue.rightClickOptions.gui.TradeGUI;
import com.terrorAndBlue.rightClickOptions.packet.GuiActionPacket;
import com.terrorAndBlue.rightClickOptions.packet.InteractServerPacket;
import com.terrorAndBlue.rightClickOptions.packet.ClientTradeUpdateResponsePacket;
import com.terrorAndBlue.rightClickOptions.trade.TradeContainer;
import com.terrorAndBlue.rightClickOptions.trade.TradeInventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy
{
	public void preInit()
	{
		
	}
}
