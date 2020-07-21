package com.terrorAndBlue.rightClickOptions.event;

import java.util.ArrayList;

import com.terrorAndBlue.rightClickOptions.RightClickOptions;
import com.terrorAndBlue.rightClickOptions.follow.FollowSession;
import com.terrorAndBlue.rightClickOptions.packet.InteractServerPacket;
import com.terrorAndBlue.rightClickOptions.trade.TradeContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class VanillaEventHandler
{
	@SubscribeEvent
	public void handleUpdate(LivingUpdateEvent event)
	{
		if(event.entity instanceof EntityPlayer)
		{
			EntityPlayer p = (EntityPlayer)event.entity;
			
			for(FollowSession sesh : FollowSession.followSessions)
			{
				if(sesh.getFollower().getEntityId() == p.getEntityId() && !p.worldObj.isRemote)
				{
					EntityPlayer target = sesh.getTarget();
					
					int var22 = MathHelper.floor_double(p.boundingBox.minY + 0.5D);
					/*boolean var3 = follower.isInWater();
					boolean var4 = follower.handleLavaMovement();
					follower.rotationPitch = 0.0F;*/

					if(target != null && target.dimension == p.dimension)
					{
						PathEntity path = sesh.getPath();

						if(path != null && !path.isFinished())
						{
							Vec3 var5 = path.getPosition(p);
							double var6 = (double)(p.width * 2.0F);

							while (var5 != null && var5.squareDistanceTo(p.posX, var5.yCoord, p.posZ) < var6 * var6)
							{
								path.incrementPathIndex();

								if (path.isFinished())
								{
									var5 = null;
									path = null;
								}
								else
								{
									var5 = path.getPosition(p);
								}
							}

							if (var5 != null)
							{
								double var8 = var5.xCoord - p.posX;
								double var10 = var5.zCoord - p.posZ;
								double var12 = var5.yCoord - (double)var22;
								float var14 = (float)(Math.atan2(var10, var8) * 180.0D / Math.PI) - 90.0F;
								float var15 = MathHelper.wrapAngleTo180_float(var14 - p.rotationYaw);
								p.moveForward = (float)p.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();

								p.moveEntityWithHeading(p.moveStrafing, p.moveForward);
								
								if (var15 > 30.0F)
								{
									var15 = 30.0F;
								}

								if (var15 < -30.0F)
								{
									var15 = -30.0F;
								}

								p.rotationYaw += var15;

								/*if (p.hasAttacked && p.entityToAttack != null)
				                {
				                    double var16 = this.entityToAttack.posX - this.posX;
				                    double var18 = this.entityToAttack.posZ - this.posZ;
				                    float var20 = this.rotationYaw;
				                    this.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
				                    var15 = (var20 - this.rotationYaw + 90.0F) * (float)Math.PI / 180.0F;
				                    this.moveStrafing = -MathHelper.sin(var15) * this.moveForward * 1.0F;
				                    this.moveForward = MathHelper.cos(var15) * this.moveForward * 1.0F;
				                }*/

								if (var12 > 0.0D)
								{
									p.jump();
								}
							}

							//p.faceEntity(target, 30.0F, 30.0F);
						}
					}
					break;
				}
				else if(sesh.getTarget().getEntityId() == p.getEntityId() && !p.worldObj.isRemote)
				{
					if(p.lastTickPosX != p.posX || p.lastTickPosY != p.posY || p.lastTickPosZ != p.posZ)
					{
						EntityPlayer follower = sesh.getFollower();

						if(follower != null && follower.dimension == p.dimension)
						{
							PathEntity path = sesh.getPath();

							if(path == null || System.currentTimeMillis() - sesh.getLastPathSetTime() > 5000)
							{
								path = follower.worldObj.getPathEntityToEntity(follower, p, 16.0F, true, false, false, true);
								sesh.setPath(path);
							}
						}
					}
					break;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void handleInteract(EntityInteractEvent event)
	{
		if(event.entityPlayer != null)
		{
			EntityPlayer p = event.entityPlayer;
			if(p.getHeldItem() == null && !event.isCanceled() && event.target instanceof EntityPlayer)
			{
				event.setCanceled(true);
				
				if(p.worldObj.isRemote)
				{
					EntityPlayer p2 = (EntityPlayer)event.target;
					
					RightClickOptions.wrapper.sendToServer(new InteractServerPacket(p2.getEntityId()));
				}
			}
		}
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void handleItemPickup(EntityItemPickupEvent event)
	{
		if(event.entityPlayer != null)
		{
			if(event.entityPlayer.openContainer instanceof TradeContainer)
			{
				event.setCanceled(true);
			}
		}
	}
}