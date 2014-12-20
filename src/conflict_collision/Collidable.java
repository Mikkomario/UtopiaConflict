package conflict_collision;

import exodus_util.Transformable;
import genesis_event.Handled;
import genesis_util.StateOperator;

/**
 * Collidable objects can be collided with and have transformation information.
 * 
 * @author Mikko Hilpinen
 * @since 19.12.2014
 */
public interface Collidable extends Handled, Transformable
{
	/**
	 * @return The object's collision information
	 */
	public CollisionInformation getCollisionInformation();
	
	/**
	 * @return The state operator that tells whether the object can be collided with 
	 * or not.
	 */
	public StateOperator getCanBeCollidedWithOperator();
}
