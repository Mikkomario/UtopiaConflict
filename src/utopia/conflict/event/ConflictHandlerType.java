package utopia.conflict.event;

import utopia.inception.handling.HandlerType;

/**
 * These are the handler types introduced in the Conflict module
 * 
 * @author Mikko Hilpinen
 * @since 21.12.2014
 */
public enum ConflictHandlerType implements HandlerType
{
	/**
	 * CollisionHandler is the handler type that accepts both collidables and collision 
	 * listeners. CollisionHandler informs the listeners about collision events with the 
	 * collidables.
	 */
	COLLISIONHANDLER,
	/**
	 * CollidableHandler is a private handler type that handles collidable objects. It simply 
	 * keeps track of all the collidable object that may be collided with.
	 */
	COLLIDABLEHANLDER;

	
	// IMPLEMENTED METHODS	------------------------
	
	@Override
	public Class<?> getSupportedHandledClass()
	{
		switch (this)
		{
			case COLLISIONHANDLER: return CollisionListener.class;
			case COLLIDABLEHANLDER: return Collidable.class;
		}
		
		return null;
	}
}