package conflict_collision;

import java.util.ArrayList;

import conflict_util.Polygon;
import genesis_util.Vector2D;

/**
 * CollisionCheckers make the necessary calculations to check if collidables collide with 
 * other collidables or points.
 * 
 * @author Mikko Hilpinen
 * @since 19.12.2014
 */
public class CollisionChecker
{
	// ATTRIBUTES	-----------------------
	
	private Collidable user;
	private boolean userWantsMTV;
	
	
	// CONSTRUCTOR	-----------------------
	
	/**
	 * Creates a new collisionChecker
	 * @param user The collidable that uses this checker
	 * @param mtvWanted Does the user want the minimum translation vector (MTV) to be 
	 * calculated when checking collisions with other objects. The value can be used later 
	 * but is slightly more taxing to calculate.
	 */
	public CollisionChecker(Collidable user, boolean mtvWanted)
	{
		// Initializes attributes
		this.user = user;
		this.userWantsMTV = mtvWanted;
	}
	
	
	// GETTERS & SETTERS	-------------------
	
	/**
	 * @return Should the MTV be calculated when dealing with this collision checker.
	 */
	public boolean mtvShouldBeCalculated()
	{
		return this.userWantsMTV;
	}
	
	
	// OTHER METHODS	-------------------
	
	/**
	 * Checks if a point collides with the object that uses this checker
	 * @param absolutePoint The point that may collide with the object
	 * @return Is the point within the object
	 */
	public boolean pointCollides(Vector2D absolutePoint)
	{
		// Transforms the point into relative space and checks collisions with the polygon
		Vector2D relativePoint = this.user.getTransformation().inverseTransform(absolutePoint);
		
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
	 * Checks if the two objects collide with each other
	 * @param other The other object that may collide with this one
	 * @return Do the two objects collide with each other
	 */
	public boolean objectCollides(Collidable other)
	{	
		// Checks if there are collisions between any of the polygons
		for (Polygon thisPolygon : getAbsolutePolygons(this.user))
		{
			for (Polygon otherPolygon : getAbsolutePolygons(other))
			{
				if (thisPolygon.collidesWith(otherPolygon))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if the two objects collide with each other and returns the minimal 
	 * translation vector (MTV) for the object used by this checker
	 * @param other The object the collisions are checked with
	 * @return The minimum translation vector from the point of view of this checker's object. 
	 * Null if there is no collision.
	 */
	public Vector2D objectCollidesMTV(Collidable other)
	{
		// Checks if there are collisions between any of the polygons
		for (Polygon thisPolygon : getAbsolutePolygons(this.user))
		{
			for (Polygon otherPolygon : getAbsolutePolygons(other))
			{
				Vector2D mtv = thisPolygon.collidesWithMTV(otherPolygon);
				
				if (mtv != null)
					return mtv;
			}
		}
		
		return null;
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
}
