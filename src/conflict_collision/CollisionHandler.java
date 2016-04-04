package conflict_collision;

import utopia.genesis.event.Actor;
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
