package com.twitter.and01;

public class FollowerObj {
	
	private String id;
	private String name;
	private String handle;
	private String bio;
	

	public FollowerObj (String _id,String _name,String _handle,String _bio)
	{
		this.id=_id;
		this.name=_name;
		this.handle=_handle;
		this.bio=_bio;
		
	}
	public String GetID()
	{
		return id;
	}
	public String Getname()
	{
		return name;
	}
	public String GetHandle()
	{
		return handle;
	}
	public String GetBio()
	{
		return bio;
	}
	
}
