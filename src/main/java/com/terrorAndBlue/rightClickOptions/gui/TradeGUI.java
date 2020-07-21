package com.terrorAndBlue.rightClickOptions.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import org.lwjgl.opengl.GL11;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.packet.GuiActionPacket;
import com.terrorAndBlue.rightClickOptions.trade.Highlight;
import com.terrorAndBlue.rightClickOptions.trade.TradeContainer;
import com.terrorAndBlue.rightClickOptions.trade.TradeInventory;
import com.terrorAndBlue.rightClickOptions.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

public class TradeGUI extends GuiContainer
{
	public static final int GUI_ID = 1;
	public static final ResourceLocation TRADE_GUI = new ResourceLocation(RightClickOptions.MODID.toLowerCase() + ":textures/gui/trade_main_gui_extended.png");
	
	public static final int BACKGROUND_WIDTH = 176;
	public static final int BACKGROUND_HEIGHT = 173;//166 regular, 173 extended
	public static final int BUTTON_WIDTH = 23;
	public static final int BUTTON_HEIGHT = 17;
	
	public static final int SLOT_WIDTH = 16;
	public static final int SLOT_HEIGHT = 16;
	
	public static final int CONFIRM_TRADE_ID = 0x57;
	public static final int REQUEST_CHANGE_ID = 0x73;
	public static final int TRADE_INIT_ID = 0x94;
	
	
	private TradeInventory tradeInv;
	
	private boolean swapEnabled;//this determines if the swap button should be rendered "enabled"
	private boolean cancelEnabled;//this will pretty much always be true
	
	//below variables are updated from server continuously
	private String offerAcceptedInfoString;
	private boolean roomForTrade;
	private int otherPlayerFreeSlots;
	private byte[] highlightData;//TODO display highlight data
	
	public TradeGUI(TradeInventory tradeInv)
	{
		super(new TradeContainer(tradeInv.getPlayer().inventory, tradeInv));
		
		this.xSize = BACKGROUND_WIDTH;
		this.ySize = BACKGROUND_HEIGHT;
		
		this.tradeInv = tradeInv;
		this.swapEnabled = true;
		this.cancelEnabled = true;
		
		offerAcceptedInfoString = "";
		roomForTrade = true;
		otherPlayerFreeSlots = -1;
		highlightData = new byte[18];
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		RightClickOptions.wrapper.sendToServer(new GuiActionPacket(GUI_ID, TRADE_INIT_ID));
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		if(tradeInv != null)
		{
			boolean handEmpty = mc.thePlayer.inventory.getItemStack() == null;//true if no item in hand
			
			swapEnabled = /*!tradeInv.isEmpty() && */ roomForTrade && handEmpty;

			TextureManager tm = mc.getTextureManager();

			drawDefaultBackground();

			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

			tm.bindTexture(TRADE_GUI);

			this.guiLeft = (width - xSize) / 2;
			this.guiTop = (height - ySize) / 2;

			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

			int swapButtonTextureY = 46;//disabled

			if(swapEnabled)
			{
				Rectangle r = new Rectangle(guiLeft + 65, guiTop + 35, BUTTON_WIDTH, BUTTON_HEIGHT);
				swapButtonTextureY = r.contains(mouseX, mouseY) ? 23 : 0;//selected or enabled
			}
			
			//TODO add tooltip when hovering over disabled swap showing why it is disabled

			//paint "confirm trade" button
			drawTexturedModalRect(guiLeft + 65, guiTop + 35, BACKGROUND_WIDTH, swapButtonTextureY, BUTTON_WIDTH, BUTTON_HEIGHT);

			if(!swapEnabled)
				GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.5F);
			drawTexturedModalRect(guiLeft + 65, guiTop + 35, 199, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

			int cancelButtonTextureY = 46;//disabled

			if(this.cancelEnabled)
			{
				Rectangle r = new Rectangle(guiLeft + 88, guiTop + 35, BUTTON_WIDTH, BUTTON_HEIGHT);
				swapButtonTextureY = r.contains(mouseX, mouseY) ? 23 : 0;//selected or enabled
			}

			//paint "cancel trade" button
			drawTexturedModalRect(guiLeft + 88, guiTop + 35, BACKGROUND_WIDTH, swapButtonTextureY, BUTTON_WIDTH, BUTTON_HEIGHT);
			drawTexturedModalRect(guiLeft + 88, guiTop + 35, 199, 23, BUTTON_WIDTH, BUTTON_HEIGHT);
			
			long timeInSec = System.currentTimeMillis() / 100;
			double sinAdjusted = Math.sin(Math.PI*timeInSec/50);
			
			if(offerAcceptedInfoString != null && offerAcceptedInfoString.length() > 0)
			{
				drawCenteredString(fontRendererObj, offerAcceptedInfoString, guiLeft + xSize/2 + (int)(sinAdjusted * xSize / 2), guiTop/2, Color.WHITE.getRGB());
				drawCenteredString(fontRendererObj, offerAcceptedInfoString, guiLeft + xSize/2 + (int)(sinAdjusted * xSize / 2), (height + guiTop + ySize)/2, Color.WHITE.getRGB());
			}
			
			for(int i = 0; i < highlightData.length; i++)
			{
				Highlight h = Highlight.values()[highlightData[i]];
				
				Color c = null;
				
				if(h == Highlight.GREEN)
					c = Color.GREEN;
				else if(h == Highlight.RED)
					c = Color.RED;
				else if(h == Highlight.BLUE)
					c = Color.BLUE;
				
				if(c != null)
				{
					int j = i%9;
					
					int y0 = (j/3) * 18;//current y
					int x0 = (j%3) * 18;//current x
					
					int startX = i < 9 ? 7 : 115;//initial x to start with for first slot
					int startY = 16;//initial y to start with for first slot
					
					drawEmptySquare(guiLeft + startX + x0, guiTop + startY + y0, 17, c);
				}
			}
			
			try
			{
				//TODO NullPointer here sometimes, may need a better way of passing info to clients
				String otherPlayerName = ((EntityPlayer)mc.theWorld.getEntityByID(RightClickOptions.interactTargets.get(mc.thePlayer.getEntityId()))).getDisplayName();

				int oldHeight = fontRendererObj.FONT_HEIGHT;

				fontRendererObj.FONT_HEIGHT = 10;
				
				drawCenteredString(fontRendererObj, "Trading with " + otherPlayerName + ".", guiLeft + 85, guiTop + 5, Color.WHITE.getRGB());

				if(otherPlayerFreeSlots >= 0)
					drawCenteredString(fontRendererObj, otherPlayerName + " has " + otherPlayerFreeSlots + " free slot(s).", guiLeft + 85, guiTop + 161, Color.WHITE.getRGB());
				
				fontRendererObj.FONT_HEIGHT = 4;
				
				int halfAlphaWhite = new Color(255, 255, 255, 128).getRGB();

				double sizeFactor = 0.75;
				double reciprocal = 1 / sizeFactor;
				GL11.glScaled(sizeFactor, sizeFactor, sizeFactor);
				drawCenteredString(fontRendererObj, "Your Offer", (int)((guiLeft + 34) * reciprocal), (int)((guiTop + 71) * reciprocal), halfAlphaWhite);
				GL11.glScaled(reciprocal, reciprocal, reciprocal);

				sizeFactor = Math.max(0.5, 0.7 - (0.01 * otherPlayerName.length()));
				reciprocal = 1 / sizeFactor;
				GL11.glScaled(sizeFactor, sizeFactor, sizeFactor);
				drawCenteredString(fontRendererObj, otherPlayerName + "'s Offer", (int)((guiLeft + 142) * reciprocal), (int)((guiTop + 71) * reciprocal), halfAlphaWhite);
				GL11.glScaled(reciprocal, reciprocal, reciprocal);
				
				fontRendererObj.FONT_HEIGHT = oldHeight;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
	    	
			//we need manually take care of item renders in the upper right 9 "slots"
			/*for (int i = 0; i < 3; i++)
			{
				for (int j = 0; j < 3; j++)
				{
					ItemStack s = tradeInv.getStackInSlot(j + i * 3 + 9);
					if(s != null && s.stackSize > 0)
					{
						drawItemStack(s, guiLeft + 116 + j * 18, guiTop + 17 + i * 18);
					}
				}
			}*/

			//tool tip needs to be done in it's own loop
			/*for (int i = 0; i < 3; i++)
			{
				for (int j = 0; j < 3; j++)
				{
					ItemStack s = tradeInv.getStackInSlot(j + i * 3 + 9);
					if(s != null && s.stackSize > 0)
					{
						Rectangle r = new Rectangle(guiLeft + 116 + j * 18, guiTop + 17 + i * 18, SLOT_WIDTH, SLOT_HEIGHT);

						if(r.contains(mouseX, mouseY))
						{
							List l = s.getTooltip(tradeInv.getPlayer(), false);

							if(l != null)
							{
								try
								{
									String otherName = ((EntityPlayer)tradeInv.getPlayer().worldObj.getEntityByID(RightClickOptions.interactTargets.get(tradeInv.getPlayer().getEntityId()))).getDisplayName();
									l.add(0, "[ " + otherName + "'s Item ]");
								}
								catch(Exception ex) {}

								//this.drawHoveringText(l, mouseX, mouseY, fontRendererObj);
							}
						}
					}
				}
			}*/
		}
	}
	
	public void mouseClicked(int x, int y, int type)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				if(new Rectangle(guiLeft + 116 + j * 18, guiTop + 17 + i * 18, SLOT_WIDTH, SLOT_HEIGHT).contains(x, y))
				{
					RightClickOptions.wrapper.sendToServer(new GuiActionPacket(GUI_ID, REQUEST_CHANGE_ID, (3*i+j) + "," + type));
					return;
				}
			}
		}
		
		if(type == 0)//left click
    	{
			Rectangle swap = new Rectangle(guiLeft + 65, guiTop + 35, BUTTON_WIDTH, BUTTON_HEIGHT);
			Rectangle cancel = new Rectangle(guiLeft + 88, guiTop + 35, BUTTON_WIDTH, BUTTON_HEIGHT);
			
			if(swap.contains(x, y) && swapEnabled)
			{
				RightClickOptions.wrapper.sendToServer(new GuiActionPacket(GUI_ID, CONFIRM_TRADE_ID));
			}
			else if(cancel.contains(x, y) && cancelEnabled)
			{
				RightClickOptions.wrapper.sendToServer(new GuiActionPacket(GUI_ID, RightClickOptions.GUI_CLOSED_ID, "pressed-x"));
			}
			else
			{
				super.mouseClicked(x, y, type);
			}
    	}
		else if(type == 1)//right click
		{
			super.mouseClicked(x, y, type);
		}
	}
	
	@Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
	
	public void readFromNBT(NBTTagCompound compound)
	{
		if(compound.hasKey(TradeInventory.TAG_NAME + "_infoString"))
			offerAcceptedInfoString = compound.getString(TradeInventory.TAG_NAME + "_infoString");
		
		if(compound.hasKey("otherPlayerFreeSlots"))
			otherPlayerFreeSlots = compound.getInteger("otherPlayerFreeSlots");
		
		if(compound.hasKey("roomForTrade"))
			roomForTrade = compound.getBoolean("roomForTrade");
		
		if(compound.hasKey("highlightData"))
			highlightData = compound.getByteArray("highlightData");
	}
	
	protected void drawEmptySquare(int x, int y, int length, Color color)
	{
		int d = color.darker().getRGB();
		int c = color.getRGB();
		
		drawHorizontalLine(x, x+length, y, d);
		drawHorizontalLine(x, x+length, y+length, c);
		drawVerticalLine(x, y, y+length, d);
		drawVerticalLine(x+length, y, y+length, c);
	}
}
