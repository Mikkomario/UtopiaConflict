package conflict_collision;

import java.util.ArrayList;
import java.util.List;

import conflict_util.Polygon;
import genesis_util.HelpMath;
import genesis_util.Vector3D;

/**
 * CollisionCheckers make the necessary calculations to check if collidables collide with 
 * other collidables or points.
 * 
 * @author Mikko Hilpinen
 * @since 19.12.2014
 */
public class CollisionChecker
{
	// TODO: Create a new system where some of the collision checking is done at 
	// collisionInformation.
	// !!!Add collisions between circles and polygons!!!
	// Each collisionInformation may contain circles and polygons
	
	// ATTRIBUTES	-----------------------
	
	private Collidable user;
	private boolean userWantsMTV, userWantsPoints;
	private Class<?>[] interestingClasses;
	
	
	// CONSTRUCTOR	-----------------------
	
	/**
	 * Creates a new collisionChecker
	 * @param user The collidable that uses this checker
	 * @param mtvWanted Does the user want the minimum translation vector (MTV) to be 
	 * calculated when checking collisions with other objects. The value can be used later 
	 * but is slightly more taxing to calculate.
	 * @param collisionPointWanted Does the user want the collision points to be calculated.
	 */
	public CollisionChecker(Collidable user, boolean mtvWanted, boolean collisionPointWanted)
	{
		// Initializes attributes
		this.user = user;
		this.userWantsMTV = mtvWanted || collisionPointWanted;
		this.interestingClasses = null;
		this.userWantsPoints = collisionPointWanted;
	}
	
	
	// GETTERS & SETTERS	-------------------
	
	/**
	 * @return Should the MTV be calculated when dealing with this collision checker.
	 */
	public boolean mtvShouldBeCalculated()
	{
		return this.userWantsMTV;
	}
	
	/**
	 * @return Should the collision points be calculated when dealing with this collision 
	 * checker.
	 */
	public boolean collisionPointsShouldBeCalculated()
	{
		return this.userWantsPoints;
	}
	
	
	// OTHER METHODS	-------------------
	
	/**
	 * Checks if a point collides with the object that uses this checker
	 * @param absolutePoint The point that may collide with the object
	 * @return Is the point within the object
	 */
	public boolean pointCollides(Vector3D absolutePoint)
	{
		// Transforms the point into relative space and checks collisions with the polygon
		Vector3D relativePoint = this.user.getTransformation().inverseTransform(absolutePoint);
		
		// If no polygon data is available, checks with radius
		if (!this.user.getCollisionInformation().usesPolygons())
			return relativePoint.getLength() < this.user.getCollisionInformation().getRadius();
		
		for (Polygon polygon : this.user.getCollisionInformation().getPolygons())
		{
			if (polygon.pointisWithin(relativePoint))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the two objects collide with each other and returns the collected data
	 * @param other The object the collisions are checked with
	 * @param calculateMtv In case of a collision, should the minimum translation vector be 
	 * calculated.
	 * @param calculateCollisionPoints In case of a collision, should the collision points be 
	 * calculated
	 * @return The data collected during the collision check
	 */
	public CollisionData checkForCollisionsWith(Collidable other, boolean calculateMtv, 
			boolean calculateCollisionPoints)
	{
		if (calculateCollisionPoints)
			calculateMtv = true;
		
		// The check is a bit different if polygons are not supported
		if (!this.user.getCollisionInformation().usesPolygons() || 
				!other.getCollisionInformation().usesPolygons())
			return checkCircleCollision(other, calculateMtv, calculateCollisionPoints);
		
		// Checks if there are collisions between any of the polygons
		for (Polygon thisPolygon : getAbsolutePolygons(this.user))
		{
			for (Polygon otherPolygon : getAbsolutePolygons(other))
			{
				// Calculates the mtv only if necessary
				Vector3D mtv = null;
				if (calculateMtv)
					mtv = thisPolygon.collidesWithMTV(otherPolygon);
				
				// Checks if there was a collision
				boolean collided = (mtv != null && !mtv.equals(Vector3D.zeroVector()));
				if (!calculateMtv)
					collided = thisPolygon.collidesWith(otherPolygon);
				
				// On collision, may calculate points and return the data, otherwise moves 
				// forward
				if (collided)
				{
					List<Vector3D> collisionPoints = null;
					if (calculateCollisionPoints)
						collisionPoints = Polygon.getCollisionPoints(thisPolygon, 
								otherPolygon, mtv);
					
					return new CollisionData(collided, mtv, collisionPoints);
				}
			}
		}
		
		return new CollisionData(false, null, null);
	}
	
	/**
	 * Changes the set of classes this collision checker is interested in. By default the 
	 * checker is interested in collisions with all classes but this can be limited with this 
	 * method.
	 * @param checkedClasses The classes the checker should be interested in. Null if the 
	 * checker should be interested in collisions with all objects (default)
	 */
	public void limitCheckedClassesTo(Class<?>[] checkedClasses)
	{
		this.interestingClasses = checkedClasses;
	}
	
	/**
	 * Tells whether the collision checker should be used with the given object.
	 * @param c The object that may be checked for collisions.
	 * @return Should the collisions be checked for the object.
	 */
	public boolean isInterestedInCollisionsWith(Object c)
	{
		if (this.interestingClasses == null)
			return true;
		
		for (int i = 0; i < this.interestingClasses.length; i++)
		{
			if (this.interestingClasses[i].isInstance(c))
				return true;
		}
		
		return false;
	}
	
	private static ArrayList<Polygon> getAbsolutePolygons(Collidable c)
	{
		ArrayList<Polygon> polygons = new ArrayList<>();
		for (Polygon polygon : c.getCollisionInformation().getPolygons())
		{
			polygons.add(polygon.transformedWith(c.getTransformation()));
		}
		return polygons;
	}
	
	private CollisionData checkCircleCollision(Collidable other, boolean calculateMtv, 
			boolean calculateCollisionPoints)
	{
		Vector3D origin1 = this.user.getTransformation().getPosition();
		Vector3D origin2 = other.getTransformation().getPosition();
		double d = HelpMath.pointDistance2D(origin1, origin2);
		double r1 = this.user.getCollisionInformation().getRadius();
		double r2 = other.getCollisionInformation().getRadius();
		double minimumDistance = r1 + r2;
		
		// Checks for the collision
		boolean collides = (d < minimumDistance);
		
		if (!collides)
			return new CollisionData(false, null, null);
		
		// May collect extra data
		Vector3D mtv = null;
		List<Vector3D> collisionPoints = null;
		if (calculateMtv)
		{
			double overlap = d - minimumDistance;
			mtv = origin2.minus(origin1).withLength(overlap);
			
			if (calculateCollisionPoints)
			{
				collisionPoints = new ArrayList<>();
				collisionPoints.add(origin1.plus(mtv.reverse().withLength(r1)));
				collisionPoints.add(origin2.plus(mtv.withLength(r2)));
			}
		}
		
		return new CollisionData(collides, mtv, collisionPoints);
	}
	
	
	// SUBCLASSES	---------------------------------
	
	/**
	 * CollisionData is a simple structure that holds the basic data collected during a 
	 * collision check
	 * @author Mikko Hilpinen
	 * @since 20.3.2015
	 */
	public static class CollisionData
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
		 * @param collisionPoints The collision points calculated during the operation
		 */
		public CollisionData(boolean collides, Vector3D mtv, List<Vector3D> collisionPoints)
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
		public List<Vector3D> getCollisionPoints()
		{
			return this.collisionPoints;
		}
	}
}
