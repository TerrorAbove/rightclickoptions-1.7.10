package com.terrorAndBlue.rightClickOptions.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.packet.GuiActionPacket;
import com.terrorAndBlue.rightClickOptions.trade.TradeOffer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

public class RightClickGUI extends GuiScreen
{
    public static final int GUI_ID = 0;
    
    public static final ResourceLocation MAIN_GUI = new ResourceLocation(RightClickOptions.MODID.toLowerCase() + ":textures/gui/RCO_main_gui.png");
    
    private static final int BACKGROUND_WIDTH = 72;
	private static final int BACKGROUND_HEIGHT = 120;
	private static final int BUTTON_WIDTH = 30;
	private static final int BUTTON_HEIGHT = 30;
    
	
    private EntityPlayer initiator;
    
    private GuiButton upperLeftButton;
    private GuiButton upperRightButton;
    private GuiButton middleLeftButton;
    private GuiButton middleRightButton;
    private GuiButton lowerLeftButton;
    private GuiButton lowerRightButton;
    
    public RightClickGUI(EntityPlayer initiator)
    {
    	this.initiator = initiator;
    }
    
    @Override
    public void initGui()
    {
    	int baseX = (width - BACKGROUND_WIDTH) / 2;
    	int baseY = (height - BACKGROUND_HEIGHT) / 2;
    	
    	//id, x, y, width, height, text
    	upperLeftButton = new GuiButton(0, baseX + 4, baseY + 19, 30, 30, "");
    	upperRightButton = new GuiButton(1, baseX + BUTTON_WIDTH + 7, baseY + 19, 30, 30, "");
    	middleLeftButton = new GuiButton(2, baseX + 4, baseY + BUTTON_HEIGHT + 22, 30, 30, "");
    	middleRightButton = new GuiButton(3, baseX + BUTTON_WIDTH + 7, baseY + BUTTON_HEIGHT + 22, 30, 30, "");
    	lowerLeftButton = new GuiButton(4, baseX + 4, baseY + BUTTON_HEIGHT * 2 + 25, 30, 30, "");
    	lowerRightButton = new GuiButton(5, baseX + BUTTON_WIDTH + 7, baseY + BUTTON_HEIGHT * 2 + 25, 30, 30, "");
    	
    	buttonList.add(upperLeftButton);
    	buttonList.add(upperRightButton);
    	buttonList.add(middleLeftButton);
    	buttonList.add(middleRightButton);
    	buttonList.add(lowerLeftButton);
    	buttonList.add(lowerRightButton);
    	
    	//TODO TEMPORARY!!, these are disabled until I fix/perfect them
    	upperRightButton.enabled = false;
    	middleLeftButton.enabled = false;
    	
    	
    	//these are for admins only
    	middleRightButton.enabled = false;
		lowerLeftButton.enabled = false;
		lowerRightButton.enabled = false;
    }
    
    @Override
    public void drawScreen(int x, int y, float f)
    {
    	drawDefaultBackground();
    	
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	mc.getTextureManager().bindTexture(MAIN_GUI);
    	
    	int posX = (width - BACKGROUND_WIDTH) / 2;
    	int posY = (height - BACKGROUND_HEIGHT) / 2;

    	drawTexturedModalRect(posX, posY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    	
    	int textPosX = width / 2;
    	int textPosY = (height - BACKGROUND_HEIGHT) / 2 + 5;
    	
    	Color textColor = Color.white;
    	Entity target = initiator.worldObj.getEntityByID(RightClickOptions.interactTargets.get(initiator.getEntityId()));
    	String otherUsername = "";
    	
    	try
    	{
        	otherUsername = ((EntityPlayer)target).getDisplayName();
        	
        	//TODO: check rights, if allowed, set mute/kick/ban true (default false)
    	}
    	catch(Exception ex)
    	{
    		textColor = Color.black;
    		otherUsername = "PLAYER NOT FOUND!";
    		
    		upperLeftButton.enabled = false;
        	upperRightButton.enabled = false;
        	middleLeftButton.enabled = false;
        	middleRightButton.enabled = false;
        	lowerLeftButton.enabled = false;
        	lowerRightButton.enabled = false;
    	}
    	
    	//adjust scale for text
    	//final double MAX_LENGTH = 11.0;
    	int length = otherUsername.length();
    	double sizeFactor = Math.pow(0.85, length) + 0.5;
    	double reciprocal = 1 / sizeFactor;
    	GL11.glScaled(sizeFactor, sizeFactor, sizeFactor);
    	
    	//draw other player's name
    	drawCenteredString(fontRendererObj, otherUsername, (int)(textPosX * reciprocal), (int)(textPosY * reciprocal) + length / 4, textColor.getRGB());
    	
    	GL11.glScaled(reciprocal, reciprocal, reciprocal);
    	mc.getTextureManager().bindTexture(MAIN_GUI);
    	
    	Point p = new Point(x, y);
    	
    	updateUpperLeftButtonGfx(p);
		updateUpperRightButtonGfx(p);
		updateMiddleLeftButtonGfx(p);
		updateMiddleRightButtonGfx(p);
		updateLowerLeftButtonGfx(p);
		updateLowerRightButtonGfx(p);
    }
    
    private void updateUpperLeftButtonGfx(Point cursorLoc)
    {
    	Rectangle rect = new Rectangle(upperLeftButton.xPosition, upperLeftButton.yPosition, upperLeftButton.width, upperLeftButton.height);
    	
    	int posX = (width - BACKGROUND_WIDTH) / 2;
    	int posY = (height - BACKGROUND_HEIGHT) / 2;
    	
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	if(!upperLeftButton.enabled)
    	{
    		drawTexturedModalRect(posX + 4, posY + 19, BACKGROUND_WIDTH, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
    	}
    	else if(!rect.contains(cursorLoc))//if mouse is not hovering
    		drawTexturedModalRect(posX + 4, posY + 19, BACKGROUND_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    	else
    		drawTexturedModalRect(posX + 4, posY + 19, BACKGROUND_WIDTH + BUTTON_WIDTH + 2, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    	
    	drawTexturedModalRect(posX + 4, posY + 19, 0, 128, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    private void updateUpperRightButtonGfx(Point cursorLoc)
    {
    	Rectangle rect = new Rectangle(upperRightButton.xPosition, upperRightButton.yPosition, upperRightButton.width, upperRightButton.height);
    	
    	int posX = (width - BACKGROUND_WIDTH) / 2;
    	int posY = (height - BACKGROUND_HEIGHT) / 2;
    	
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	if(!upperRightButton.enabled)
    	{
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + 19, BACKGROUND_WIDTH, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
    	}
    	else if(!rect.contains(cursorLoc))//if mouse is not hovering
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + 19, BACKGROUND_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    	else
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + 19, BACKGROUND_WIDTH + BUTTON_WIDTH + 2, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    	
    	drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + 19, 34, 128, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    private void updateMiddleLeftButtonGfx(Point cursorLoc)
    {
    	Rectangle rect = new Rectangle(middleLeftButton.xPosition, middleLeftButton.yPosition, middleLeftButton.width, middleLeftButton.height);
    	
    	int posX = (width - BACKGROUND_WIDTH) / 2;
    	int posY = (height - BACKGROUND_HEIGHT) / 2;
    	
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	if(!middleLeftButton.enabled)
    	{
    		drawTexturedModalRect(posX + 4, posY + BUTTON_HEIGHT + 22, BACKGROUND_WIDTH, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
    	}
    	else if(!rect.contains(cursorLoc))//if mouse is not hovering
    		drawTexturedModalRect(posX + 4, posY + BUTTON_HEIGHT + 22, BACKGROUND_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    	else
    		drawTexturedModalRect(posX + 4, posY + BUTTON_HEIGHT + 22, BACKGROUND_WIDTH + BUTTON_WIDTH + 2, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    	
    	drawTexturedModalRect(posX + 4, posY + BUTTON_HEIGHT + 22, 0, 160, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    private void updateMiddleRightButtonGfx(Point cursorLoc)
    {
    	Rectangle rect = new Rectangle(middleRightButton.xPosition, middleRightButton.yPosition, middleRightButton.width, middleRightButton.height);
    	
    	int posX = (width - BACKGROUND_WIDTH) / 2;
    	int posY = (height - BACKGROUND_HEIGHT) / 2;
    	
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	if(!middleRightButton.enabled)
    	{
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + BUTTON_HEIGHT + 22, BACKGROUND_WIDTH, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
    	}
    	else if(!rect.contains(cursorLoc))//if mouse is not hovering
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + BUTTON_HEIGHT + 22, BACKGROUND_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    	else
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + BUTTON_HEIGHT + 22, BACKGROUND_WIDTH + BUTTON_WIDTH + 2, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    	
    	drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + BUTTON_HEIGHT + 22, 32, 160, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    private void updateLowerLeftButtonGfx(Point cursorLoc)
    {
    	Rectangle rect = new Rectangle(lowerLeftButton.xPosition, lowerLeftButton.yPosition, lowerLeftButton.width, lowerLeftButton.height);
    	
    	int posX = (width - BACKGROUND_WIDTH) / 2;
    	int posY = (height - BACKGROUND_HEIGHT) / 2;
    	
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	if(!lowerLeftButton.enabled)
    	{
    		drawTexturedModalRect(posX + 4, posY + BUTTON_HEIGHT * 2 + 25, BACKGROUND_WIDTH, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
    	}
    	else if(!rect.contains(cursorLoc))//if mouse is not hovering
    		drawTexturedModalRect(posX + 4, posY + BUTTON_HEIGHT * 2 + 25, BACKGROUND_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    	else
    		drawTexturedModalRect(posX + 4, posY + BUTTON_HEIGHT * 2 + 25, BACKGROUND_WIDTH + BUTTON_WIDTH + 2, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    	
    	drawTexturedModalRect(posX + 4, posY + BUTTON_HEIGHT * 2 + 25, 0, 192, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    private void updateLowerRightButtonGfx(Point cursorLoc)
    {
    	Rectangle rect = new Rectangle(lowerRightButton.xPosition, lowerRightButton.yPosition, lowerRightButton.width, lowerRightButton.height);
    	
    	int posX = (width - BACKGROUND_WIDTH) / 2;
    	int posY = (height - BACKGROUND_HEIGHT) / 2;
    	
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	if(!lowerRightButton.enabled)
    	{
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + BUTTON_HEIGHT * 2 + 25, BACKGROUND_WIDTH, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
    	}
    	else if(!rect.contains(cursorLoc))//if mouse is not hovering
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + BUTTON_HEIGHT * 2 + 25, BACKGROUND_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    	else
    		drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + BUTTON_HEIGHT * 2 + 25, BACKGROUND_WIDTH + BUTTON_WIDTH + 2, BUTTON_HEIGHT + 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    	
    	drawTexturedModalRect(posX + BUTTON_WIDTH + 7, posY + BUTTON_HEIGHT * 2 + 25, 32, 192, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    @Override
    public void actionPerformed(GuiButton button)
    {
    	if(button != null && button.enabled)
    	{
    		RightClickOptions.wrapper.sendToServer(new GuiActionPacket(GUI_ID, button.id));
    	}
    }
    
    @Override
    public void mouseClicked(int x, int y, int type)
    {
    	if(type == 0)//left click
    	{
    		Point pointClicked = new Point(x, y);
        	
        	Rectangle upperLeft = new Rectangle(upperLeftButton.xPosition, upperLeftButton.yPosition, upperLeftButton.width, upperLeftButton.height);
        	Rectangle upperRight = new Rectangle(upperRightButton.xPosition, upperRightButton.yPosition, upperRightButton.width, upperRightButton.height);
        	Rectangle middleLeft = new Rectangle(middleLeftButton.xPosition, middleLeftButton.yPosition, middleLeftButton.width, middleLeftButton.height);
        	Rectangle middleRight = new Rectangle(middleRightButton.xPosition, middleRightButton.yPosition, middleRightButton.width, middleRightButton.height);
        	Rectangle lowerLeft = new Rectangle(lowerLeftButton.xPosition, lowerLeftButton.yPosition, lowerLeftButton.width, lowerLeftButton.height);
        	Rectangle lowerRight = new Rectangle(lowerRightButton.xPosition, lowerRightButton.yPosition, lowerRightButton.width, lowerRightButton.height);
        	
        	/*
        	 * have to explicitly call this because actionPerformed does not get called
        	 * by the button listener when this method is overridden (for whatever reason)
        	 */
        	
        	if(upperLeft.contains(pointClicked))
        		actionPerformed(upperLeftButton);
        	else if(upperRight.contains(pointClicked))
        		actionPerformed(upperRightButton);
        	else if(middleLeft.contains(pointClicked))
        		actionPerformed(middleLeftButton);
        	else if(middleRight.contains(pointClicked))
        		actionPerformed(middleRightButton);
        	else if(lowerLeft.contains(pointClicked))
        		actionPerformed(lowerLeftButton);
        	else if(lowerRight.contains(pointClicked))
        		actionPerformed(lowerRightButton);
    	}
    	else if(type == 1)//right click
    	{
    		initiator.closeScreen();
    	}
    }
    
    @Override
	public void onGuiClosed()
	{
		RightClickOptions.wrapper.sendToServer(new GuiActionPacket(GUI_ID, RightClickOptions.GUI_CLOSED_ID));
	}
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}