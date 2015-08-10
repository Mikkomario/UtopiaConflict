package conflict_collision;

import genesis_event.Handled;
import genesis_util.Transformable;

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
}
