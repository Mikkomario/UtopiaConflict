package conflict_collision;

import java.util.List;

import conflict_collision.CollisionChecker.CollisionData;
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
	
	private Collidable listener, target;
	private Vector3D mtv;
	private double duration;
	private List<Vector3D> collisionPoints;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new event with the given data
	 * @param listener The primary listener of the event. The data should be presented from 
	 * the listener's point of view.
	 * @param target The object the listener collided with. This object may also be interested 
	 * in the event.
	 * @param mtv The minimum translation vector the listener has to take in order to escape 
	 * collision. This is only provided if either the listener or the target desires it. 
	 * The mtv is presented from the listener's point of view. mtv.reverse() would return the 
	 * target's point of view.
	 * @param collisionPoints The point(s) where the collision occurred
	 * @param duration How long the collision event took place (the amount of steps since the 
	 * last check)
	 */
	public CollisionEvent(Collidable listener, Collidable target, Vector3D mtv, 
			List<Vector3D> collisionPoints, double duration)
	{
		// Initializes attributes
		this.listener = listener;
		this.target = target;
		this.mtv = mtv;
		this.duration = duration;
		this.collisionPoints = collisionPoints;
	}
	
	/**
	 * Creates a new event with the given data
	 * @param listener The primary listener of the event. The data should be presented from 
	 * the listener's point of view.
	 * @param target The object the listener collided with. This object may also be interested 
	 * in the event.
	 * @param collisionData The collision data collected during the collision check operation
	 * @param duration How long the collision event took place (the amount of steps since the 
	 * last check)
	 */
	public CollisionEvent(Collidable listener, Collidable target, 
			CollisionData collisionData, double duration)
	{
		// Initializes attributes
		this.listener = listener;
		this.target = target;
		this.mtv = collisionData.getMtv();
		this.duration = duration;
		this.collisionPoints = collisionData.getCollisionPoints();
	}
	
	
	// GETTERS & SETTERS	---------------------
	
	/**
	 * @return The primary listener informed about the event
	 */
	public Collidable getListener()
	{
		return this.listener;
	}
	
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
	 * Checks if the given object is considered the primary listener for this event
	 * @param c The object that may be the listener
	 * @return Is the object the primary listener for this event
	 */
	public boolean isListener(Object c)
	{
		return c == getListener();
	}
	
	/**
	 * Checks if the given object is considered the target or the secondary listener for this event
	 * @param c The object that may be the target
	 * @return Is the object the target or the secondary listener for this event
	 */
	public boolean isTarget(Object c)
	{
		return c == getTarget();
	}
	
	/**
	 * Checks if the event contains information about a collision the object has been part of
	 * @param c An object that may be one of the sources of this event
	 * @return Does the event / the original collision concern this object
	 */
	public boolean concerns(Object c)
	{
		return isListener(c) || isTarget(c);
	}
	
	/**
	 * Finds the counterpart for the object this event concerns. For the listener, it's the 
	 * target and for the target it's the listener.
	 * @param c The object that this event concerns
	 * @return The counterpart for the object in this event. Null if the event doesn't concern 
	 * the given object
	 */
	public Collidable getCounterpart(Object c)
	{
		if (isListener(c))
			return getTarget();
		else if (isTarget(c))
			return getListener();
		else
			return null;
	}
	
	/**
	 * @return The collision event from the target object's point of view (the target will 
	 * be the listener of the new event)
	 */
	public CollisionEvent fromTargetsPointOfView()
	{
		return new CollisionEvent(getTarget(), getListener(), 
				getMTV() == null ? null : getMTV().reverse(), getCollisionPoints(), 
				getDuration());
	}
}
