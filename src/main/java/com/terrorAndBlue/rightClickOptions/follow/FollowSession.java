package com.terrorAndBlue.rightClickOptions.follow;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;

public class FollowSession
{
	public static final List<FollowSession> followSessions = new ArrayList<FollowSession>();
	
	
	private EntityPlayer follower;
	private EntityPlayer target;
	
	private PathEntity path;
	private long lastPathSetTime;
	
	public FollowSession(EntityPlayer follower, EntityPlayer target)
	{
		this.follower = follower;
		this.target = target;
		this.path = null;
		this.lastPathSetTime = 0;
	}
	
	public EntityPlayer getFollower()
	{
		return follower;
	}
	
	public EntityPlayer getTarget()
	{
		return target;
	}
	
	public PathEntity getPath()
	{
		return path;
	}
	
	public void setPath(PathEntity path)
	{
		this.path = path;
		
		if(path != null)
			lastPathSetTime = System.currentTimeMillis();
	}
	
	public long getLastPathSetTime()
	{
		return lastPathSetTime;
	}
}
