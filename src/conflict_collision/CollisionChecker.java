package conflict_collision;

/**
 * CollisionCheckers make the necessary calculations to check if collidables collide with 
 * other collidables or points.
 * @author Mikko Hilpinen
 * @since 19.12.2014
 */
public class CollisionChecker
{	
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
	
	/**
	 * @return The collidable instance that is checked against
	 */
	public Collidable getCollidable()
	{
		return this.user;
	}
	
	
	// OTHER METHODS	-------------------
	
	
	
	/**
	 * Changes the set of classes this collision checker is interested in. By default the 
	 * checker is interested in collisions with all classes but this can be limited with this 
	 * method.
	 * @param checkedClasses The classes the checker should be interested in.
	 */
	public void limitCheckedClassesTo(Class<?>... checkedClasses)
	{
		this.interestingClasses = checkedClasses;
	}
	
	/**
	 * Makes it so that the checker will receive events from all object classes, which is the 
	 * default state of the checker.
	 */
	public void resetCheckedClassesLimit()
	{
		this.interestingClasses = null;
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
	
	/*
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
				// These are not the actual collision points
				collisionPoints = new ArrayList<>();
				collisionPoints.add(origin1.plus(mtv.reverse().withLength(r1)));
				collisionPoints.add(origin2.plus(mtv.withLength(r2)));
			}
		}
		
		return new CollisionData(collides, mtv, collisionPoints);
	}
	*/
}
