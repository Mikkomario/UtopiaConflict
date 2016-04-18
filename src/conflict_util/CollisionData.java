package conflict_util;

import utopia.genesis.util.Vector3D;

/**
 * CollisionData is a simple structure that holds the basic data collected during a 
 * collision check
 * @author Mikko Hilpinen
 * @since 20.3.2015
 */
public class CollisionData
{
	// ATTRIBUTES	-----------------------------
	
	private boolean collides;
	private Vector3D mtv;
	private Vector3D[] collisionPoints;
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Creates a new collisionData that holds information about the collision
	 * @param collides Did a collision occur or not
	 * @param mtv The minimum translation vector calculated during the operation
	 * @param collisionPoints The collision points calculated during the operation
	 */
	public CollisionData(boolean collides, Vector3D mtv, Vector3D[] collisionPoints)
	{
		this.collides = collides;
		this.mtv = mtv;
		this.collisionPoints = collisionPoints;
	}
	
	
	// GETTERS & SETTERS	---------------------
	
	/**
	 * @return Was there a collision of some sort
	 */
	public boolean collided()
	{
		return this.collides;
	}
	
	/**
	 * @return The minimum translation vector calculated during the check
	 */
	public Vector3D getMtv()
	{
		return this.mtv;
	}
	
	/**
	 * @return The collision points calculated during the check
	 */
	public Vector3D[] getCollisionPoints()
	{
		return this.collisionPoints;
	}
}