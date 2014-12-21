package conflict_collision;

import genesis_util.StateOperator;

/**
 * CollisionListeners are interested in collision events with other objects and are informed 
 * about those events by collisionHandlers.
 * 
 * @author Mikko Hilpinen
 * @since 21.12.2014
 */
public interface CollisionListener extends Collidable
{
	/**
	 * Each collisionListener should be able to provide a collision checker that helps with 
	 * collision checking
	 * @return The collision checker the object uses
	 */
	public CollisionChecker getCollisionChecker();
	
	/**
	 * CollisionListeners should react to collision events with certain objects
	 * @param event The event that originated from a collision between the listener and 
	 * another object.
	 */
	public void onCollisionEvent(CollisionEvent event);
	
	/**
	 * @return The stateOperator that determines whether the object should be informed about 
	 * collision events at all.
	 */
	public StateOperator getListensForCollisionStateOperator();
}
