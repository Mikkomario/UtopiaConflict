package conflict_collision;

import java.util.List;

import conflict_util.CollisionData;
import utopia.genesis.util.Vector3D;

/**
 * CollisionEvents are collections of collision information. They are usually generated 
 * when two objects collide with each other.
 * @author Mikko Hilpinen
 * @since 20.12.2014
 */
public class CollisionEvent
{
	// ATTRIBUTES	--------------------------
	
	private Collidable target;
	private Vector3D mtv;
	private double duration;
	private List<Vector3D> collisionPoints;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new event with the given data
	 * @param target The object the listener collided with.
	 * @param mtv The minimum translation vector the listener has to take in order to escape 
	 * collision. This is only provided if either the listener or the target desires it. 
	 * The mtv is presented from the listener's point of view. mtv.reverse() would return the 
	 * target's point of view.
	 * @param collisionPoints The point(s) where the collision occurred
	 * @param duration How long the collision event took place (the amount of steps since the 
	 * last check)
	 */
	public CollisionEvent(Collidable target, Vector3D mtv, List<Vector3D> collisionPoints, 
			double duration)
	{
		// Initializes attributes
		this.target = target;
		this.mtv = mtv;
		this.duration = duration;
		this.collisionPoints = collisionPoints;
	}
	
	/**
	 * Creates a new event with the given data
	 * @param target The object the listener collided with
	 * @param collisionData The collision data collected during the collision check operation
	 * @param duration How long the collision event took place (the amount of steps since the 
	 * last check)
	 */
	public CollisionEvent(Collidable target, CollisionData collisionData, double duration)
	{
		// Initializes attributes
		this.target = target;
		this.mtv = collisionData.getMtv();
		this.duration = duration;
		this.collisionPoints = collisionData.getCollisionPoints();
	}
	
	
	// GETTERS & SETTERS	---------------------
	
	/**
	 * @return The target or the secondary listener of the event
	 */
	public Collidable getTarget()
	{
		return this.target;
	}
	
	/**
	 * @return The minimum translation vector from the listener's point of view. May be null 
	 * if the mtv was not requested.
	 */
	public Vector3D getMTV()
	{
		return this.mtv;
	}
	
	/**
	 * This is an utility method for requesting the reversed mtv, since getMtv().reverse() may 
	 * throw a {@link NullPointerException}
	 * @return The reversed mtv (= the target's minimum translation vector)
	 */
	public Vector3D getReversedMTV()
	{
		if (this.mtv == null)
			return null;
		else
			return getMTV().reverse();
	}
	
	/**
	 * @return How long the event took place (steps since the last check)
	 */
	public double getDuration()
	{
		return this.duration;
	}
	
	/**
	 * @return The points where the collision occurred (absolute). May be null if the collision 
	 * points were not requested.
	 */
	public List<Vector3D> getCollisionPoints()
	{
		return this.collisionPoints;
	}
	
	
	// OTHER METHODS	-----------------------
	
	/**
	 * Checks if the given object is considered the target or the secondary listener for this event
	 * @param c The object that may be the target
	 * @return Is the object the target or the secondary listener for this event
	 */
	public boolean isTarget(Object c)
	{
		return c == getTarget();
	}
}
