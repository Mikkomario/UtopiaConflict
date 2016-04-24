package conflict_util;

import java.util.ArrayList;
import java.util.List;

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
	private List<Vector3D> collisionPoints;
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Creates a new collisionData that holds information about the collision
	 * @param collides Did a collision occur or not
	 * @param mtv The minimum translation vector calculated during the operation
	 * @param collisionPoints The collision points calculated during the operation (optional)
	 */
	public CollisionData(boolean collides, Vector3D mtv, List<Vector3D> collisionPoints)
	{
		this.collides = collides;
		this.mtv = mtv;
		this.collisionPoints = collisionPoints;
	}
	
	/**
	 * This method may be used for constructing collision data of a non-collision
	 * @return CollisionData for no collision
	 */
	public static CollisionData noCollision()
	{
		return new CollisionData(false, null, null);
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
	public List<Vector3D> getCollisionPoints()
	{
		if (this.collisionPoints == null)
			this.collisionPoints = new ArrayList<>();
		return this.collisionPoints;
	}
}