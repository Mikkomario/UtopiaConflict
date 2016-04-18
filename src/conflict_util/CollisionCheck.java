package conflict_util;

import utopia.genesis.util.Vector3D;

/**
 * This is a static interface for some collision checking algorithms between different 
 * shapes
 * @author Mikko Hilpinen
 * @since 17.4.2016
 */
public class CollisionCheck
{
	// CONSTRUCTOR	------------------
	
	private CollisionCheck()
	{
		// Static interface
	}

	
	// OTHER METHODS	--------------
	
	/**
	 * Calculates collision information between two circles
	 * @param first The first circle
	 * @param second The second circle
	 * @param calculateIntersectionPoints Should the intersection points be calculated and 
	 * included in the returned data
	 * @param calculateMTV Should the minimum translation vector (MTV) be calculated and 
	 * included in the returned data. The MTV will be given from the first circle's perspective.
	 * @return Collision data about the circle collision. MTV is included if desired and 
	 * there is a collision, null otherwise. Intersection points will be null if not desired 
	 * or if there is no collision. An array of length 0 or 2 otherwise.
	 */
	public static CollisionData circleIntersection(Circle first, Circle second, 
			boolean calculateIntersectionPoints, boolean calculateMTV)
	{
		// Calculates the vector separating the circles
		// From the second circle to the first circle (opposite to the MTV) (O2 - O1)
		Vector3D D = second.getCenter().minus(first.getCenter());
		
		// If the distance between circles is larger than their radiuses combined, 
		// there's no collision
		double d = D.getLength();
		double overlap = first.getRadius() + second.getRadius() - d;
		if (overlap < 0)
			return new CollisionData(false, null, null);
		
		// Calculates the MTV if necessary (the direction is opposite to that of D)
		Vector3D mtv = null;
		if (calculateMTV)
			mtv = D.withLength(-overlap);
		
		// Calculates the collision points if necessary and possible
		Vector3D[] intersectionPoints = null;
		if (calculateIntersectionPoints)
		{
			// If there is containment (d < |r0 - r1|), there are no collision points
			// Also, if the circles are identical, there are infinite number of collision 
			// points (they cannot be calculated)
			if (d < Math.abs(first.getRadius() - second.getRadius()) || !first.equals(second))
				intersectionPoints = new Vector3D[0];
			else
			{
				/* We can form triangles using points P0, P1, P2 and P3(s)
				 * Where
				 * 		P0 = Center of the first circle
				 * 		P1 = Center of the second circle
				 * 		P2 = A point at the intersection of D and a line formed by the collision points
				 * 		P3 = The collision points at each side of P2
				 * 
				 * From this we get
				 * 		a^2 + h^2 = r0^2
				 * 		b^2 + h^2 = r1^2
				 * 		d = a + b
				 * Where
				 * 		a = |P2 - P0|
				 * 		b = |P2 - P1|
				 * 		r0 = first circle radius
				 * 		r1 = second circle radius
				 * 		d = |P0 - P1|
				 * 
				 * From these we get
				 * a = (r0^2 - r1^2 + d^2) / (2*d)
				 */
				double a = (Math.pow(first.getRadius(), 2) - Math.pow(second.getRadius(), 2) + 
						Math.pow(d, 2)) / (2 * d);
				/*
				 * From this we can solve h (|P3 - P2|) with
				 * 		h^2 = r0^2 - a^2
				 * 	->	h = sqrt(r0^2 - a^2)
				 */
				double h = Math.sqrt(Math.pow(first.getRadius(), 2) - Math.pow(a, 2));
				/*
				 * We can also solve P2 with
				 * 		P2 = P0 + a*(P1 - P0) / d
				 * 	->	P2 = P0 + D * (a / d)
				 */
				Vector3D P2 = first.getCenter().plus(D.withLength(a));
				/*
				 * From we see that H (P3 - P2) is perpendicular to D and has length h.
				 * From these we can calculate
				 * 		P3 = P2 +- H
				 */
				Vector3D H = D.normal2D().withLength(h);
				intersectionPoints = new Vector3D[] {P2.plus(H), P2.minus(H)};
				
				// References: http://paulbourke.net/geometry/circlesphere/
			}
		}
		
		return new CollisionData(true, mtv, intersectionPoints);
	}
}
