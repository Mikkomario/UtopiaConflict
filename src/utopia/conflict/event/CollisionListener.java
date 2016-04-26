package utopia.conflict.event;

import utopia.inception.handling.Handled;

/**
 * CollisionListeners are interested in collision events with other objects and are informed 
 * about those events by collisionHandlers.
 * @author Mikko Hilpinen
 * @since 21.12.2014
 */
public interface CollisionListener extends Handled
{
	/**
	 * Each collisionListener should be able to provide a collision checker that helps with 
	 * collision checking
	 * @return The collision checker the object uses
	 */
	public CollisionListeningInformation getCollisionListeningInformation();
	
	/**
	 * CollisionListeners should react to collision events with certain objects
	 * @param event The event that originated from a collision between the listener and 
	 * another object.
	 */
	public void onCollisionEvent(CollisionEvent event);
}
