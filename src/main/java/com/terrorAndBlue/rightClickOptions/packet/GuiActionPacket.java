package com.terrorAndBlue.rightClickOptions.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ChatComponentText;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.follow.FollowSession;
import com.terrorAndBlue.rightClickOptions.gui.RightClickGUI;
import com.terrorAndBlue.rightClickOptions.gui.TradeGUI;
import com.terrorAndBlue.rightClickOptions.trade.DisplayOnlySlot;
import com.terrorAndBlue.rightClickOptions.trade.Highlight;
import com.terrorAndBlue.rightClickOptions.trade.TradeContainer;
import com.terrorAndBlue.rightClickOptions.trade.TradeOffer;
import com.terrorAndBlue.rightClickOptions.trade.TradeOfferSlot;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession;
import com.terrorAndBlue.rightClickOptions.trade.TradeSession.Status;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class GuiActionPacket implements IMessage
{
	private int buttonId;
	private int gui_Id;
	
	private String args;//extra arguments depending on the situation
	
	public GuiActionPacket(int gui_Id, int buttonId)
	{
		this.gui_Id = gui_Id;
		this.buttonId = buttonId;
		this.args = "";
	}
	
	public GuiActionPacket(int gui_Id, int buttonId, String args)
	{
		this(gui_Id, buttonId);
		
		if(args != null)
			this.args = args;
	}
	
	public GuiActionPacket() {}
	
	public static class Handler implements IMessageHandler<GuiActionPacket, IMessage>
	{
		@Override
		public IMessage onMessage(GuiActionPacket packet, MessageContext ctx)
		{
			EntityPlayer thePlayer = ctx.getServerHandler().playerEntity;
			Entity targ = thePlayer.worldObj.getEntityByID(RightClickOptions.interactTargets.get(thePlayer.getEntityId()));
			
			if(targ instanceof EntityPlayer)
			{
				EntityPlayer interactTarget = (EntityPlayer)targ;
				switch(packet.gui_Id)
				{
				case RightClickGUI.GUI_ID:
					if(packet.buttonId == RightClickOptions.GUI_CLOSED_ID)
					{
						//something here...?
						return null;
					}
					
					thePlayer.closeScreen();

					switch(packet.buttonId)
					{
					case 0://trade
						interactTarget.addChatMessage(new ChatComponentText("\u00A75"+thePlayer.getDisplayName()+" would like to trade with you."));
						interactTarget.addChatMessage(new ChatComponentText("\u00A75"+"Right click the player to accept."));
						interactTarget.addChatMessage(new ChatComponentText("\u00A75"+"(30 seconds left)"));
						thePlayer.addChatMessage(new ChatComponentText("\u00A76"+"You've sent "+interactTarget.getDisplayName()+" a trade offer."));
						RightClickOptions.tradeOffers.put(new TradeOffer(interactTarget).start(), thePlayer);
						break;
					case 1://follow
						FollowSession.followSessions.add(new FollowSession(thePlayer, interactTarget));
						break;
					case 2://request assist
						break;
					case 3://mute (higher permissions only)
						break;
					case 4://kick (higher permissions only)
						break;
					case 5://ban (higher permissions only)
						break;
					}

					break;
				case TradeGUI.GUI_ID:
					switch(packet.buttonId)
					{
					case RightClickOptions.GUI_CLOSED_ID:
						if(packet.args.equals("pressed-x"))
						{
							for(TradeSession sesh : RightClickOptions.tradeSessions)
							{
								if(sesh.getP0().getEntityId() == thePlayer.getEntityId() || sesh.getP1().getEntityId() == thePlayer.getEntityId())
								{
									if((sesh.isP0Accepting() && !sesh.isP1Accepting()) || (!sesh.isP0Accepting() && sesh.isP1Accepting()))
									{
										sesh.setP0Accepted(false);
										sesh.setP1Accepted(false);
									}
									else if(!sesh.isP0Accepting() && !sesh.isP1Accepting())
									{
										thePlayer.closeScreen();
									}
									break;
								}
							}
						}
						break;
					case TradeGUI.CONFIRM_TRADE_ID:
						for(TradeSession sesh : RightClickOptions.tradeSessions)
						{
							if(sesh.getP0().getEntityId() == thePlayer.getEntityId())
							{
								sesh.setP0Accepted(!sesh.isP0Accepting());
								break;
							}
							if(sesh.getP1().getEntityId() == thePlayer.getEntityId())
							{
								sesh.setP1Accepted(!sesh.isP1Accepting());
								break;
							}
						}
						break;
					case TradeGUI.REQUEST_CHANGE_ID:
						try
						{
							String[] s = packet.args.split(",");
							
							int index = Integer.parseInt(s[0]);
							int type = Integer.parseInt(s[1]);

							for(TradeSession sesh : RightClickOptions.tradeSessions)
							{
								if(sesh.getP0().getEntityId() == thePlayer.getEntityId())
								{
									if(thePlayer.openContainer instanceof TradeContainer)
									{
										TradeContainer c0 = (TradeContainer)thePlayer.openContainer;
										
										Slot slot = c0.getSlot(9+index);//this slot is for the requester's view, i.e. so they know their click did something lol
										if(slot instanceof DisplayOnlySlot)
										{
											DisplayOnlySlot dos = (DisplayOnlySlot)slot;
											
											if(dos.getHasStack())
											{
												if(type == 0)//left click
													dos.highlight = dos.highlight == Highlight.GREEN ? Highlight.NONE : Highlight.GREEN;//want more of this item
												else if(type == 1)//right click
													dos.highlight = dos.highlight == Highlight.RED ? Highlight.NONE : Highlight.RED;//want less of this item
											}
											else
											{
												dos.highlight = dos.highlight == Highlight.BLUE ? Highlight.NONE : Highlight.BLUE;//want something here...?
											}
										}
									}
									if(sesh.getP1().openContainer instanceof TradeContainer)
									{
										TradeContainer c1 = (TradeContainer)sesh.getP1().openContainer;
										
										Slot slot = c1.getSlot(index);//this slot is for the other player's view
										if(slot instanceof TradeOfferSlot)
										{
											TradeOfferSlot tos = (TradeOfferSlot)slot;
											
											if(tos.getHasStack())
											{
												if(type == 0)//left click
													tos.highlight = tos.highlight == Highlight.GREEN ? Highlight.NONE : Highlight.GREEN;//want more of this item
												else if(type == 1)//right click
													tos.highlight = tos.highlight == Highlight.RED ? Highlight.NONE : Highlight.RED;//want less of this item
											}
											else
											{
												tos.highlight = tos.highlight == Highlight.BLUE ? Highlight.NONE : Highlight.BLUE;//want something here...?
											}
										}
									}
									break;
								}
								if(sesh.getP1().getEntityId() == thePlayer.getEntityId())
								{
									if(thePlayer.openContainer instanceof TradeContainer)
									{
										TradeContainer c0 = (TradeContainer)thePlayer.openContainer;
										
										Slot slot = c0.getSlot(9+index);//this slot is for the requester's view, i.e. so they know their click did something lol
										if(slot instanceof DisplayOnlySlot)
										{
											DisplayOnlySlot dos = (DisplayOnlySlot)slot;
											
											if(dos.getHasStack())
											{
												if(type == 0)//left click
													dos.highlight = dos.highlight == Highlight.GREEN ? Highlight.NONE : Highlight.GREEN;//want more of this item
												else if(type == 1)//right click
													dos.highlight = dos.highlight == Highlight.RED ? Highlight.NONE : Highlight.RED;//want less of this item
											}
											else
											{
												dos.highlight = dos.highlight == Highlight.BLUE ? Highlight.NONE : Highlight.BLUE;//want something here...?
											}
										}
									}
									if(sesh.getP0().openContainer instanceof TradeContainer)
									{
										TradeContainer c1 = (TradeContainer)sesh.getP0().openContainer;
										
										Slot slot = c1.getSlot(index);//this slot is for the other player's view
										if(slot instanceof TradeOfferSlot)
										{
											TradeOfferSlot tos = (TradeOfferSlot)slot;
											
											if(tos.getHasStack())
											{
												if(type == 0)//left click
													tos.highlight = tos.highlight == Highlight.GREEN ? Highlight.NONE : Highlight.GREEN;//want more of this item
												else if(type == 1)//right click
													tos.highlight = tos.highlight == Highlight.RED ? Highlight.NONE : Highlight.RED;//want less of this item
											}
											else
											{
												tos.highlight = tos.highlight == Highlight.BLUE ? Highlight.NONE : Highlight.BLUE;//want something here...?
											}
										}
									}
									break;
								}
							}
						}
						catch(Exception ex){}
						
						break;
					case TradeGUI.TRADE_INIT_ID:
						for(TradeSession sesh : RightClickOptions.tradeSessions)
						{
							if(sesh.getP0().getEntityId() == thePlayer.getEntityId())
							{
								int status = sesh.getSessionInitStatus();
								
								sesh.setSessionInitStatus(++status);
								
								if(status >= 2)
								{
									sesh.beginTask(250, 500);
								}
								
								break;
							}
							if(sesh.getP1().getEntityId() == thePlayer.getEntityId())
							{
								int status = sesh.getSessionInitStatus();
								
								sesh.setSessionInitStatus(++status);
								
								if(status >= 2)
								{
									sesh.beginTask(250, 500);
								}
								
								break;
							}
						}
						break;
					}
					break;
				}
			}
			return null;
		}
		
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		buttonId = buf.readInt();
		gui_Id = buf.readInt();
		args = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(buttonId);
		buf.writeInt(gui_Id);
		ByteBufUtils.writeUTF8String(buf, args);
	}
}
