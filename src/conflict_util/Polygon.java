package conflict_util;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import exodus_util.Transformation;
import genesis_util.HelpMath;
import genesis_util.Line;
import genesis_util.Vector2D;

/**
 * Polygon is a simple construction that consists of multiple points
 * 
 * @author Mikko Hilpinen
 * @since 8.12.2014
 */
public class Polygon
{
	// ATTRIBUTES	--------------------------
	
	private final Vector2D[] points;
	private CirculationDirection direction;
	private List<Vector2D> axes;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new polygon from the given points
	 * @param vertexes The points that form this polygon. The points should be in order so that 
	 * they form the lines that form this polygon.
	 */
	public Polygon(Vector2D[] vertexes)
	{
		// Initializes attributes
		this.points = vertexes;
		this.direction = null;
		this.axes = null;
	}

	
	// OTHER METHODS	----------------------
	
	/**
	 * @return How many vertexes does this polygon have
	 */
	public int getVertexAmount()
	{
		return this.points.length;
	}
	
	/**
	 * Finds a vertex with the given index (loops around the polygon if necessary)
	 * @param index The index of the vertex in the polygon
	 * @return The vertex at the given index in this polygon
	 */
	public Vector2D getVertex(int index)
	{
		return this.points[index % getVertexAmount()];
	}
	
	/**
	 * @param index The index of the vertex the vector starts from
	 * @return A line from the given vertex to the next vertex
	 */
	public Line getEdge(int index)
	{
		return new Line(getVertex(index), getVertex(index + 1));
	}
	
	/**
	 * @return The collision axes used with this polygon. Each axis is normalized and 
	 * perpendicular to an edge in the polygon.
	 */
	public List<Vector2D> getCollisionAxes()
	{
		if (this.axes == null)
			this.axes = calculateAxes();
		
		return this.axes;
	}
	
	/**
	 * @return Is the polygon convex
	 */
	public boolean isConvex()
	{
		// A polygon is convex if one only has to turn right or left when traversing the 
		// polygon's sides
		for (int i = 0; i < getVertexAmount(); i++)
		{
			if (getCirculationDirectionAt(i) != getCirculationDirection())
				return false;
		}
		
		return true;
	}
	
	/**
	 * Divides a non-convex polygon into multiple convex polygons
	 * @return Convex polygons that form this polygon
	 */
	public ArrayList<Polygon> toConvexPolygons()
	{
		ArrayList<Polygon> polygons = new ArrayList<>();
		List<Integer> movedIndices = new ArrayList<>();
		
		int lastBrokenIndex = -1;
		
		for (int index = 1; index <= getVertexAmount(); index ++)
		{
			// Checks if an index is broken (if there is a wrong direction at that point)
			if (getCirculationDirectionAt(index - 1) != getCirculationDirection())
			{
				// Constructs a new polygon from the "broken" area, non-borken vertexes that 
				// will be added will be removed from this polygon
				if (lastBrokenIndex != -1 && index - lastBrokenIndex > 1)
				{
					List<Vector2D> newPolyVertices = new ArrayList<Vector2D>();
					newPolyVertices.add(getVertex(lastBrokenIndex));
					
					for (int newPolyIndex = lastBrokenIndex + 1; newPolyIndex < index; 
							newPolyIndex ++)
					{
						newPolyVertices.add(getVertex(newPolyIndex));
						movedIndices.add(newPolyIndex);
					}
					
					newPolyVertices.add(getVertex(index));
					
					polygons.add(new Polygon(newPolyVertices.toArray(new Vector2D[0])));
				}
				
				// Remembers the broken part
				lastBrokenIndex = index;
			}
		}
		
		// If broken indexes were found but not cut, cuts the polygon in half (takes a triangle 
		// out of the original polygon)
		if (movedIndices.isEmpty() && lastBrokenIndex != -1)
		{
			Vector2D[] newPolyVertices = {getVertex(lastBrokenIndex), 
					getVertex(lastBrokenIndex + 1), 
					getVertex(lastBrokenIndex + 2)};
			
			polygons.add(new Polygon(newPolyVertices));
			
			movedIndices.add(lastBrokenIndex + 1);
		}
		
		// Finally checks if another round is required
		if (movedIndices.isEmpty())
			polygons.add(this);
		else
		{
			// Sometimes a new polygon is formed to the center of cut polygons and must be 
			// check separately
			Vector2D[] remainingVertexes = 
					new Vector2D[getVertexAmount() - movedIndices.size()];
			
			int newIndex = 0;
			
			for (int index = 0; index < getVertexAmount(); index ++)
			{
				if (!movedIndices.contains(index))
				{
					remainingVertexes[newIndex] = getVertex(index);
					newIndex ++;
				}
			}
			
			polygons.addAll(new Polygon(remainingVertexes).toConvexPolygons());
		}
		
		return polygons;
	}
	
	/**
	 * @return The direction the polygon's lines point to in relation to each other
	 */
	public CirculationDirection getCirculationDirection()
	{
		if (this.direction == null)
			this.direction = calculateCirculationDirection();
		
		return this.direction;
	}
	
	/**
	 * @return A similar polygon to this except with different circular direction
	 */
	public Polygon reverse()
	{
		Vector2D[] newVertices = new Vector2D[getVertexAmount()];
		
		for (int i = 0; i < getVertexAmount(); i++)
		{
			newVertices[i] = getVertex(getVertexAmount() - 1 - i);
		}
		
		return new Polygon(newVertices);
	}
	
	/**
	 * @param direction The direction that is suitable for this polygon
	 * @return This polygon but with the given circular direction
	 */
	public Polygon withCircularDirection(CirculationDirection direction)
	{
		if (getCirculationDirection() == direction)
			return this;
		else
			return reverse();
	}
	
	/**
	 * Transforms this polygon (from relative space to absolute space)
	 * @param transformation The transformation that transforms this polygon
	 * @return A transformed version of this polygon
	 */
	public Polygon transformedWith(Transformation transformation)
	{
		Vector2D[] transformedPoints = new Vector2D[getVertexAmount()];
		
		for (int i = 0; i < getVertexAmount(); i++)
		{
			transformedPoints[i] = transformation.transform(getVertex(i));
		}
		
		return new Polygon(transformedPoints);
	}
	
	/**
	 * @return If a bounding box was formed around the polygon, this would be its bottom right 
	 * corner
	 */
	public Vector2D getBottomRight()
	{
		if (getVertexAmount() == 0)
			return Vector2D.zeroVector();
		
		double largestX = -100000;
		double largestY = -100000;
		
		for (Vector2D vertex : this.points)
		{
			if (vertex.getFirst() > largestX)
				largestX = vertex.getFirst();
			if (vertex.getSecond() > largestY)
				largestY = vertex.getSecond();
		}
		
		return new Vector2D(largestX, largestY);
	}
	
	/**
	 * @return If a bounding box was formed around the polygon, this would be its top left
	 * corner
	 */
	public Vector2D getTopLeft()
	{
		if (getVertexAmount() == 0)
			return Vector2D.zeroVector();
		
		double smallestX = 100000;
		double smallestY = 100000;
		
		for (Vector2D vertex : this.points)
		{
			if (vertex.getFirst() < smallestX)
				smallestX = vertex.getFirst();
			if (vertex.getSecond() < smallestY)
				smallestY = vertex.getSecond();
		}
		
		return new Vector2D(smallestX, smallestY);
	}
	
	/**
	 * @return The width and height of the bounding box around this polygon
	 */
	public Vector2D getDimensions()
	{
		return getBottomRight().minus(getTopLeft());
	}
	
	/**
	 * Checks if a point collides with the bounding box drawn around the polygon
	 * @param point The point that will be checked
	 * @return is the point inside the bounding box
	 */
	public boolean pointIsWithinBoundingBox(Vector2D point)
	{
		return HelpMath.pointIsInRange(point, getTopLeft(), getBottomRight());
	}
	
	/**
	 * Checks if a point is inside this polygon. This doesn't always work with non-convex 
	 * polygons, please transform the polygon into convex polygons before using this method.
	 * @param point The point that is checked
	 * @return Is the point inside this polygon.
	 */
	public boolean pointisWithin(Vector2D point)
	{
		if (!pointIsWithinBoundingBox(point))
			return false;
		
		// Finds two vertices that are nearest to the given point
		List<Vector2D> closestPoints = new LinkedList<Vector2D>();
		
		for (int i = 0; i < getVertexAmount(); i++)
		{
			closestPoints.add(new Vector2D(i, HelpMath.pointDistance(point, getVertex(i))));
		}
		
		// Finds the two most close points
		while (closestPoints.size() > 2)
		{
			double farthestRange = closestPoints.get(0).getSecond();
			int farthestIndex = 0;
			
			for (int i = 1; i < 3; i++)
			{
				double range = closestPoints.get(i).getSecond();
				if (range > farthestRange)
				{
					farthestRange = range;
					farthestIndex = i;
				}
			}
			
			closestPoints.remove(farthestIndex);
		}
		
		// Checks that the points are in right order (the first and the last go the 
		// wrong way (index) by default)
		
		if (closestPoints.get(1).getFirstInt() - closestPoints.get(0).getFirstInt() > 
				getVertexAmount() / 2)
			closestPoints.add(closestPoints.remove(0));
		
		double dir1 = HelpMath.checkDirection(point.minus(
				getVertex(closestPoints.get(0).getFirstInt())).getDirection());
		double dir2 = HelpMath.checkDirection(getVertex(
				closestPoints.get(1).getFirstInt()).minus(point).getDirection());
		double turn = HelpMath.checkDirection(dir2 - dir1);
		
		// If a line going through the three points would make the polygon non-convex, 
		// there is a collision 
		return CirculationDirection.getTurnDirection(turn) != getCirculationDirection();
	}
	
	/**
	 * Projects this polygon to the given axis.
	 * 
	 * @param axis The axis along which the projection is done.
	 * @return A line projected from this polygon
	 */
	public Line getProjection(Vector2D axis)
	{
		List<Vector2D> projections = new ArrayList<>();
		
		// Projects all points in this polygon to the given axis
		for (int i = 0; i < getVertexAmount(); i++)
		{
			 projections.add(getVertex(i).vectorProjection(axis));
		}
		
		// Picks two projected points, the smallest and the largest
		projections.sort(new ProjectionComparator());
		
		return new Line(projections.get(0), projections.get(projections.size() - 1));
	}
	
	/**
	 * Checks if the projections of the two vectors overlap each other when using the given 
	 * axis. If this is false, the polygon's are certainly not colliding with each other.
	 * @param other The other polygon
	 * @param axis The axis to which the polygons are projected to
	 * @return Are the projections of the polygons overlapping each other
	 */
	public boolean overlapsAlongAxis(Polygon other, Vector2D axis)
	{
		return projectionsOverlap(getProjection(axis), other.getProjection(axis));
	}
	
	/**
	 * Checks if the projections of the two vectors overlap each other when using the given 
	 * axis. Returns the MTV applicable for this axis from the perspective of this polygon. 
	 * If there is no overlap, returns null (the polygons can't be overlapping each other)
	 * @param other The other polygon
	 * @param axis The axis to which the polygons are projected to
	 * @return The movement vector that will take this polygon outside the other polygon. Null 
	 * if there is no overlapping.
	 */
	public Vector2D overlapsAlongAxisMTV(Polygon other, Vector2D axis)
	{
		return projectionsOverlapMTV(getProjection(axis), other.getProjection(axis));
	}
	
	/**
	 * Checks if the two polygons collide with each other
	 * @param other The other polygon
	 * @return Do the two polygons collide with each other
	 */
	public boolean collidesWith(Polygon other)
	{
		// Checks each axis, if there's an overlap on all of them, there is a collision
		for (Vector2D axis : getCollisionAxes())
		{
			if (!overlapsAlongAxis(other, axis))
				return false;
		}
		for (Vector2D axis : other.getCollisionAxes())
		{
			if (!overlapsAlongAxis(other, axis))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if the two polygons collide with each other and returns the minimum translation 
	 * vector that gets this polygon outside the other polygon.
	 * @param other The other polygon
	 * @return The minimum translation vector that gets this polygon outside the other vector 
	 * or null if the polygon's don't collide
	 */
	public Vector2D collidesWithMTV(Polygon other)
	{
		Vector2D mtv = null;
		double smallestOverlap = -1;
		
		// Checks each axis, if there's an overlap on all of them, there is a collision
		for (Vector2D axis : getCollisionAxes())
		{
			Vector2D overlapVector = overlapsAlongAxisMTV(other, axis);
			
			if (overlapVector == null)
				return null;
			
			double overlapAmount = overlapVector.getLength();
			
			if (smallestOverlap < 0 || overlapAmount < smallestOverlap)
			{
				mtv = overlapVector;
				smallestOverlap = overlapAmount;
			}
		}
		// I know this is not dry, but I wouldn't want to create new lists at every check
		for (Vector2D axis : other.getCollisionAxes())
		{
			Vector2D overlapVector = overlapsAlongAxisMTV(other, axis);
			
			if (overlapVector == null)
				return null;
			
			double overlapAmount = overlapVector.getLength();
			
			if (smallestOverlap < 0 || overlapAmount < smallestOverlap)
			{
				mtv = overlapVector;
				smallestOverlap = overlapAmount;
			}
		}
		
		return mtv;
	}
	
	/**
	 * Draws this polygon. Can be used for testing, for example.
	 * @param g2d The graphics object that does the drawing
	 */
	public void drawPolygon(Graphics2D g2d)
	{
		for (int i = 0; i < getVertexAmount(); i++)
		{
			g2d.drawLine(getVertex(i).getFirstInt(), getVertex(i).getSecondInt(), 
					getVertex(i + 1).getFirstInt(), getVertex(i + 1).getSecondInt());
		}
	}
	
	/**
	 * Draws the bounding box around this polygon
	 * @param g2d The object that does the actual drawing
	 */
	public void drawBoundingBox(Graphics2D g2d)
	{
		Vector2D topLeft = getTopLeft(), dimensions = getDimensions();
		
		g2d.drawRect(topLeft.getFirstInt(), topLeft.getSecondInt(), dimensions.getFirstInt(), 
				dimensions.getSecondInt());
	}
	
	/**
	 * Draws visual representations of the collision axes on screen
	 * @param g2d The graphics object that does the drawing
	 */
	public void drawCollisionAxes(Graphics2D g2d)
	{
		for (Vector2D axis : getCollisionAxes())
		{
			axis.times(100).drawAsLine(g2d);
		}
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
	 * 
	 * @param p1 The projection of the first polygon.
	 * @param p2 The projection of the second polygon.
	 * @return The MTV from the perspective of the first projection, null if the projections 
	 * don't overlap each other
	 */
	public static Vector2D projectionsOverlapMTV(Line p1, Line p2)
	{
		ProjectionComparator comparator = new ProjectionComparator();
		
		// If there is no overlap, returns null
		if (comparator.compare(p1.getEnd(), p2.getStart()) < 0 || 
				comparator.compare(p2.getEnd(), p1.getStart()) < 0)
			return null;
		
		// Otherwise checks if there is containment
		/*
		if (comparator.compare(p1.getEnd(), p2.getStart()) >= 0 && 
				comparator.compare(p2.getEnd(), p1.getStart()) >= 0)
		{
			// TODO: Implement once the basic MTV has been tested
		}
		*/
			
		// Otherwise just returns the easiest way out (MTV)
		if (comparator.compare(p1.getStart(), p2.getStart()) < 0)
			return p2.getStart().minus(p1.getEnd());
		else
			return p2.getEnd().minus(p1.getStart());
	}
	
	private CirculationDirection getCirculationDirectionAt(int startIndex)
	{
		return CirculationDirection.getTurnDirection(getEdge(startIndex + 1).getDirection() - 
				getEdge(startIndex).getDirection());
	}
	
	private CirculationDirection calculateCirculationDirection()
	{
		double totalTurn = 0;
		
		for (int i = 0; i < getVertexAmount(); i++)
		{
			double dir1 = getEdge(i).getDirection();
			double dir2 = getEdge(1 + i).getDirection();
			
			if (dir1 > 180)
				dir1 -= 360;
			if (dir2 > 180)
				dir2 -= 360;
			
			totalTurn += dir2 - dir1;
		}
		
		return CirculationDirection.getTurnDirection(totalTurn);
	}
	
	private List<Vector2D> calculateAxes()
	{
		List<Vector2D> newAxes = new ArrayList<>();
		
		for (int i = 0; i < getVertexAmount(); i ++)
		{
			Vector2D axis = getEdge(i).toVector().normal();
			
			// If there already is a paraller axis, doesn't use this one
			boolean paraller = false;
			for (Vector2D previousAxis : newAxes)
			{
				if (previousAxis.isParallerWith(axis))
				{
					paraller = true;
					break;
				}
			}
			
			if (!paraller)
				newAxes.add(axis);
		}
		
		return newAxes;
	}
	
	
	// SUBCLASSES	----------------------
	
	private static class ProjectionComparator implements Comparator<Vector2D>
	{
		@Override
		public int compare(Vector2D o1, Vector2D o2)
		{
			if (HelpMath.areApproximatelyEqual(o1.getFirst(), o2.getFirst()))
				return (int) (100 * (o1.getSecond() - o2.getSecond()));
			else
				return (int) (100* (o1.getFirst() - o2.getFirst()));
		}	
	}
	
	
	// ENUMS	--------------------------
	
	/**
	 * Angle can circle to either clockwise or counter-clockwise direction, and so can 
	 * polygons.
	 * 
	 * @author Mikko Hilpinen
	 * @since 8.12.2014
	 */
	public enum CirculationDirection
	{
		/**
		 * The clockwise direction, this is the negative direction in Utopia
		 */
		CLOCKWISE,
		/**
		 * The counter-clockwise direction, this is the positive direction in Utopia
		 */
		COUNTERCLOCKWISE;
		
		
		// OTHER METHODS	-----------------------
		
		/**
		 * Checks the circulation direction corresponding with an angular turn
		 * @param turnAngle The size of the turn in degrees
		 * @return The circular direction of the turn
		 */
		public static CirculationDirection getTurnDirection(double turnAngle)
		{
			double a = HelpMath.checkDirection(turnAngle);
			
			if (a >= 0 && a < 180)
				return COUNTERCLOCKWISE;
			else
				return CLOCKWISE;
		}
	}
}
