package conflict_util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import utopia.genesis.util.HelpMath;
import utopia.genesis.util.Line;
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
			return CollisionData.noCollision();
		
		// Calculates the MTV if necessary (the direction is opposite to that of D)
		Vector3D mtv = null;
		if (calculateMTV)
			mtv = D.withLength(-overlap);
		
		// Calculates the collision points if necessary and possible
		List<Vector3D> intersectionPoints = null;
		if (calculateIntersectionPoints)
		{
			// If there is containment (d < |r0 - r1|), there are no collision points
			// Also, if the circles are identical, there are infinite number of collision 
			// points (they cannot be calculated)
			if (d < Math.abs(first.getRadius() - second.getRadius()) || !first.equals(second))
				intersectionPoints = null;
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
				intersectionPoints = new ArrayList<>();
				intersectionPoints.add(P2.plus(H));
				intersectionPoints.add(P2.minus(H));
				
				// References: http://paulbourke.net/geometry/circlesphere/
			}
		}
		
		return new CollisionData(true, mtv, intersectionPoints);
	}
	
	/**
	 * Finds collision points between a circle and a line
	 * @param circle A circle
	 * @param line A line
	 * @return The intersection points between the circle and the line (zero, one or two points)
	 */
	public static List<Vector3D> getCircleLineIntersectionPoints(Circle circle, Line line)
	{
		return line.circleIntersection2D(circle.getCenter(), circle.getRadius(), true);
	}
	
	/**
	 * Finds the edge that took part of a collision
	 * @param p The polygon the edge is from
	 * @param mtv The collision normal. The vector must point towards the polygon, not away 
	 * from it
	 * @return The polygon's edge that collided
	 */
	public static Line getCollisionEdge(Polygon p, Vector3D mtv)
	{
		Vector3D bestVertex = null;
		double bestProduct = 0;
		int bestIndex = -1;
		
		// Goes through all the vertices and finds the closest one
		for (int i = 0; i < p.getVertexAmount(); i++)
		{
			Vector3D v = p.getVertex(i);
			double product = v.dotProduct(mtv);
			
			// The best vertex has the smallest dot product with the collision normal
			if (bestVertex == null || product < bestProduct)
			{
				bestVertex = v;
				bestProduct = product;
				bestIndex = i;
			}
		}
		
		// Finds the better edge that is connected to the vertex
		Line left = new Line(bestVertex, p.getVertex(bestIndex - 1));
		Line right = new Line(bestVertex, p.getVertex(bestIndex + 1));
		
		// The better edge is more perpendicular to the collision normal
		if (Math.abs(left.toVector().dotProduct(mtv)) < 
				Math.abs(right.toVector().dotProduct(mtv)))
			return left;
		else
			return right;
	}
	
	/**
	 * Calculates the collision points between the two polygons
	 * @param p1 The first polygon
	 * @param p2 The second polygon
	 * @param mtv1 The minimum translation vector for the first polygon (points towards 
	 * the first polygon)
	 * @return A list of collision points. There will be zero, one or two collision points.
	 */
	public static List<Vector3D> getCollisionPoints(Polygon p1, Polygon p2, Vector3D mtv1)
	{
		Vector3D mtv2 = mtv1.reverse();
		Line edge1 = getCollisionEdge(p1, mtv1);
		Line edge2 = getCollisionEdge(p2, mtv2);
		
		Line referenceEdge = edge1;
		Line incidentEdge = edge2;
		Vector3D referenceMtv = mtv1;
		
		// The reference edge is the one that is more perpendicular to the collision normal
		if (Math.abs(edge2.toVector().dotProduct(mtv1)) < 
				Math.abs(edge1.toVector().dotProduct(mtv1)))
		{
			referenceEdge = edge2;
			incidentEdge = edge1;
			referenceMtv = mtv2;
		}
		
		return clip(referenceEdge, incidentEdge, referenceMtv);
	}
	
	/**
	 * Checks if two projections of polygons overlap each other.
	 * @param p1 The projection of the first polygon.
	 * @param p2 The projection of the second polygon.
	 * @return do the two projections overlap each other
	 */
	public static boolean projectionsOverlap(Line p1, Line p2)
	{
		ProjectionComparator comparator = new ProjectionComparator();
		
		if (comparator.compare(p1.getEnd(), p2.getStart()) < 0 || 
				comparator.compare(p2.getEnd(), p1.getStart()) < 0)
			return false;
		
		return true;
		
		// If either of the points in p2 is between the points in p1, there is a collision
		/*
		if (comparator.compare(p1.getStart(), p2.getEnd()) <= 0 && 
				comparator.compare(p1.getStart(), p2.getStart()) >= 0)
			return true;
		if (comparator.compare(p1.getEnd(), p2.getEnd()) <= 0 && 
				comparator.compare(p1.getEnd(), p2.getStart()) >= 0)
			return true;
		
		return false;
		*/
	}
	
	/**
	 * Finds the minimum translation vector (MTV) that makes the projection p1 move outside 
	 * the projection p2. If there is no overlapping, null is returned.
	 * @param p1 The projection of the first polygon.
	 * @param p2 The projection of the second polygon.
	 * @return The MTV from the perspective of the first projection, null if the projections 
	 * don't overlap each other
	 */
	public static Vector3D projectionsOverlapMTV(Line p1, Line p2)
	{
		ProjectionComparator comparator = new ProjectionComparator();
		
		// If there is no overlap, returns null
		if (comparator.compare(p1.getEnd(), p2.getStart()) < 0 || 
				comparator.compare(p2.getEnd(), p1.getStart()) < 0)
			return null;
		
		// Otherwise checks if there is containment
		if (comparator.compare(p1.getEnd(), p2.getStart()) >= 0 && 
				comparator.compare(p2.getEnd(), p1.getStart()) >= 0)
		{
			// There are two possible ways out of containment
			Vector3D mtv1 = p1.getEnd().minus(p2.getStart());
			Vector3D mtv2 = p2.getEnd().minus(p1.getStart());
			
			if (mtv1.getLength() < mtv2.getLength())
				return mtv1;
			else
				return mtv2;
		}
			
		// Otherwise just returns the easiest way out (MTV)
		if (comparator.compare(p1.getStart(), p2.getStart()) < 0)
			return p2.getStart().minus(p1.getEnd());
		else
			return p2.getEnd().minus(p1.getStart());
	}
	
	/**
	 * Checks if the two polygons collide with each other
	 * @param first The first polygon
	 * @param second The second polygon
	 * @param calculateCollisionPoints Should the collision points be calculated
	 * @param calculateMTV Should the minimum translation vector (MTV) be calculated. The 
	 * MTV will be calculated from the first polygon's perspective
	 * @return Do the two polygons collide with each other
	 */
	public static CollisionData checkPolygonCollision(Polygon first, Polygon second, 
			boolean calculateCollisionPoints, boolean calculateMTV)
	{
		// Uses collision axes from both polygons
		List<Vector3D> firstPolygonAxes = first.getCollisionAxes();
		List<Vector3D> axes = new ArrayList<>(firstPolygonAxes);
		// Second polygon axes are only added where they are not paraller with some other axis
		for (Vector3D axis : second.getCollisionAxes())
		{
			boolean skip = false;
			for (Vector3D checkedAxis : firstPolygonAxes)
			{
				if (checkedAxis.isParallerWith(axis))
				{
					skip = true;
					break;
				}
			}
			
			if (!skip)
				axes.add(axis);
		}
		
		// Checks for collision along each axis
		Vector3D mtv = null;
		double smallestOverlap = -1;
		
		for (Vector3D axis : axes)
		{
			// MTV is required for collision point checking as well
			if (calculateMTV || calculateCollisionPoints)
			{
				Vector3D overlapVector = projectionsOverlapMTV(first.getProjection(axis), 
						second.getProjection(axis));
				
				if (overlapVector == null)
					return CollisionData.noCollision();
				
				double overlapAmount = overlapVector.getLength();
				
				if (mtv == null || overlapAmount < smallestOverlap)
				{
					mtv = overlapVector;
					smallestOverlap = overlapAmount;
				}
			}
			// If MTV isn't required, the collision check is a bit simpler
			else if (!projectionsOverlap(first.getProjection(axis), second.getProjection(axis)))
				return CollisionData.noCollision();
		}
		
		// Checks whether collision points need to be calculated as well
		List<Vector3D> collisionPoints = null;
		if (calculateCollisionPoints)
			collisionPoints = getCollisionPoints(first, second, mtv);
		
		return new CollisionData(true, mtv, collisionPoints);
	}
	
	/*
	 * Checks whether a polygon collides with a circle
	 * @param polygon A polygon
	 * @param circle A circle
	 * @param calculateCollisionPoints Should the collision points be calculated
	 * @param calculateMTV Should the minimum translation vector (MTV) be calculated. The 
	 * MTV will be calculated from the polygon's perspective
	 * @return Does the polygon collide with the circle
	 */
	/*
	public static CollisionData checkPolygonCircleCollision(Polygon polygon, Circle circle, 
			boolean calculateCollisionPoints, boolean calculateMTV)
	{
		// TODO: This won't work. One needs to either change the polygon into a circle or 
		// vice versa
		
		// Uses collision axes from the polygon
		// Checks for collision along each axis
		Vector3D mtv = null;
		double smallestOverlap = -1;
		
		for (Vector3D axis : polygon.getCollisionAxes())
		{
			if (calculateMTV)
			{
				Vector3D overlapVector = projectionsOverlapMTV(polygon.getProjection(axis), 
						circle.getProjection(axis));
				
				if (overlapVector == null)
					return CollisionData.noCollision();
				
				double overlapAmount = overlapVector.getLength();
				
				if (mtv == null || overlapAmount < smallestOverlap)
				{
					mtv = overlapVector;
					smallestOverlap = overlapAmount;
				}
			}
			// If MTV isn't required, the collision check is a bit simpler
			else if (!projectionsOverlap(polygon.getProjection(axis), circle.getProjection(axis)))
				return new CollisionData(false, null, null);
		}
		
		// Checks whether collision points need to be calculated as well
		List<Vector3D> collisionPoints = null;
		if (calculateCollisionPoints)
		{
			collisionPoints = new ArrayList<>();
			for (Line edge : polygon.getEdges())
			{
				collisionPoints.addAll(getCircleLineIntersectionPoints(circle, edge));
			}
		}
		
		return new CollisionData(true, mtv, collisionPoints);
	}*/
	
	private static List<Vector3D> clip(Line reference, Line incident, Vector3D referenceMtv)
	{
		// Clips from both sides
		Line clipped = getClippedEdge(incident, reference.getStart(), reference.toVector());
		if (clipped == null)
			return new ArrayList<>();
		
		clipped = getClippedEdge(clipped, reference.getEnd(), reference.toVector().reverse());
		if (clipped == null)
			return new ArrayList<>();
		
		// Removes the edges from outside the third side
		Vector3D lastNormal = reference.toVector().normal();
		if (!HelpMath.areApproximatelyEqual(lastNormal.getZDirection(), 
				referenceMtv.getZDirection()))
			lastNormal = referenceMtv;
		
		List<Vector3D> collisionPoints = new ArrayList<>();
		
		double origin = reference.getStart().dotProduct(lastNormal);
		double distance1 = clipped.getStart().dotProduct(lastNormal) - origin;
		double distance2 = clipped.getEnd().dotProduct(lastNormal) - origin;
		
		if (distance1 >= 0)
			collisionPoints.add(clipped.getStart());
		if (distance2 >= 0)
			collisionPoints.add(clipped.getEnd());
		
		return collisionPoints;
	}
	
	private static Line getClippedEdge(Line edge, Vector3D edgePoint, 
			Vector3D clippingPlaneNormal)
	{
		double origin = edgePoint.dotProduct(clippingPlaneNormal);
		
		double distance1 = edge.getStart().dotProduct(clippingPlaneNormal) - origin;
		double distance2 = edge.getEnd().dotProduct(clippingPlaneNormal) - origin;
		
		List<Vector3D> clippedPoints = new ArrayList<>();
		
		// If the vertices are inside the desired area, preserves them
		if (distance1 >= 0)
			clippedPoints.add(edge.getStart());
		if (distance2 >= 0)
			clippedPoints.add(edge.getEnd());
		
		// If one of them wasn't, clips the edge
		if (distance1 * distance2 < 0)
		{
			Vector3D edgeVector = edge.toVector();
			double u = distance1 / (distance1 - distance2);
			edgeVector = edgeVector.times(u).plus(edge.getStart());
			clippedPoints.add(edgeVector);
		}
		
		if (clippedPoints.size() >= 2)
			return new Line(clippedPoints.get(0), clippedPoints.get(1));
		else
			return null;
	}
	
	
	// NESTED CLASSES	----------------------
	
	/**
	 * This class is used for arranging projections from smallest to largest
	 * @author Mikko Hilpinen
	 * @since 19.4.2016
	 */
	public static class ProjectionComparator implements Comparator<Vector3D>
	{
		@Override
		public int compare(Vector3D o1, Vector3D o2)
		{
			if (HelpMath.areApproximatelyEqual(o1.getFirst(), o2.getFirst()))
				return (int) (100 * (o1.getSecond() - o2.getSecond()));
			else
				return (int) (100* (o1.getFirst() - o2.getFirst()));
		}	
	}
}
