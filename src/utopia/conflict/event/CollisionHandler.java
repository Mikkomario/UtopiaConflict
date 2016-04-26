package utopia.conflict.event;

import utopia.genesis.event.Actor;
import utopia.genesis.event.ActorHandler;
import utopia.inception.handling.Handler;
import utopia.inception.handling.HandlerType;

/**
 * CollisionHandlers inform collision listeners about collision events. They also keep track 
 * of all the collidables those listeners can collide with.
 * @author Mikko Hilpinen
 * @since 21.12.2014
 */
public class CollisionHandler extends Handler<CollisionListener> implements Actor
{
	// ATTRIBUTES	----------------------------
	
	private CollidableHandler collidableHandler;
	private double lastDuration;
	
	
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new handler. Remember to add the handler to a sufficient actor handler
	 * @param collidableHandler The collidable handler that keeps track of collidable objects
	 */
	public CollisionHandler(CollidableHandler collidableHandler)
	{
		// Initializes attributes
		this.collidableHandler = collidableHandler;
	}
	
	/**
	 * Creates a new collision handler that will be ready to be used right away.
	 * @param collidableHandler The collidable handler that keeps track of collidable objects
	 * @param actorHandler The actor handler that will inform the handler about step events
	 * @return A collision handler ready to be used
	 */
	public static CollisionHandler createCollisionHandler(CollidableHandler collidableHandler, 
			ActorHandler actorHandler)
	{
		CollisionHandler handler = new CollisionHandler(collidableHandler);
		if (actorHandler != null)
			actorHandler.add(handler);
		return handler;
	}
	
	
	// IMPLEMENTED METHODS	--------------------

	@Override
	public void act(double duration)
	{
		// Only works if the collidableHandler is still alive
		if (!this.collidableHandler.getIsDeadStateOperator().getState())
		{
			// Checks for collisions
			this.lastDuration = duration;
			handleObjects(true);
		}
	}

	@Override
	public HandlerType getHandlerType()
	{
		return ConflictHandlerType.COLLISIONHANDLER;
	}

	@Override
	protected boolean handleObject(CollisionListener h)
	{	
		// Checks for collisions with the collidables
		this.collidableHandler.checkForCollisionsWith(h, this.lastDuration);
		return true;
	}
}
