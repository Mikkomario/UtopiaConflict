package conflict_collision;

import conflict_util.CollisionData;
import utopia.inception.handling.Handler;
import utopia.inception.handling.HandlerType;

/**
 * CollidableHandler keeps track of all the objects that may collide with specific collision 
 * listeners
 * @author Mikko Hilpinen
 * @since 12.3.2015
 */
public class CollidableHandler extends Handler<Collidable>
{
	// ATTRIBUTES	-----------------------------
	
	private CollisionListener lastListener;
	private double lastDuration;
	
	
	// IMPLEMENTED METHODS	---------------------

	@Override
	public HandlerType getHandlerType()
	{
		return ConflictHandlerType.COLLIDABLEHANLDER;
	}

	@Override
	protected boolean handleObject(Collidable h)
	{
		// An object can't collide with itself
		// TODO: Actually should check against the listener's collidable, since it may be different
		if (h == null || h == this.lastListener)
			return true;
		
		// Checks if the two objects accept each other as collided objects (= is collision 
		// checking necessary)
		if (h.getCollisionInformation() == null || 
				!h.getCollisionInformation().allowsCollisionEventsFor(this.lastListener) || 
				!this.lastListener.getCollisionChecker().isInterestedInCollisionsWith(h))
			return true;
		
		// Checks which data should be collected
		boolean mtvRequired = false;
		boolean pointsRequired = false;
		if (this.lastListener.getCollisionChecker().collisionPointsShouldBeCalculated())
		{
			mtvRequired = true;
			pointsRequired = true;
		}
		else if (this.lastListener.getCollisionChecker().mtvShouldBeCalculated())
			mtvRequired = true;
		
		// Checks for collisions between the collidable and the collision listener
		CollisionData data = 
				this.lastListener.getCollisionChecker().checkForCollisionsWith(h, 
				mtvRequired, pointsRequired);
		
		// If there was a collision, informs the listener
		if (data.collided())
			this.lastListener.onCollisionEvent(new CollisionEvent(h, data, this.lastDuration));
		
		return true;
	}
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * Checks for collisions between the collision listener and the collidable objects. The 
	 * listener will be informed about each collision event.
	 * @param listener The listener that will be informed about collision events concerning it.
	 * @param duration The duration of the collision
	 */
	public void checkForCollisionsWith(CollisionListener listener, double duration)
	{
		this.lastDuration = duration;
		this.lastListener = listener;
		handleObjects(true);
		
		this.lastListener = null;
	}
}