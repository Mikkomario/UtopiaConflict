package conflict_util;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import exodus_util.Transformation;
import genesis_util.HelpMath;
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
	private final CirculationDirection direction;
	
	
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
		this.direction = getCirculationDirection();
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
	 * A vector at the given index of the polygon. The vector always points from the given index 
	 * to the next vertex.
	 * @param index The index of the vertex the vector starts from
	 * @return A vector from the given vertex to the next vertex
	 */
	public Vector2D getLine(int index)
	{
		return getVertex(index + 1).minus(getVertex(index));
	}
	
	/**
	 * @return Is the polygon convex
	 */
	public boolean isConvex()
	{
		// A polygon is convex if one only has to turn right or left when traversing the 
		// polygon's sides
		int vertexID = 1;
		
		while (vertexID < getVertexAmount())
		{
			if (getCirculationDirection(vertexID) != this.direction)
				return false;
			
			vertexID ++;
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
			if (getCirculationDirection(index - 1) != this.direction)
			{
				// Constructs a new polygon from the "broken" area, non-borken vertexes that 
				// will be added will be removed from this polygon
				if (lastBrokenIndex != -1)
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
		double totalTurn = 0;
		
		for (int i = 0; i < getVertexAmount(); i++)
		{
			totalTurn += getLine(i + 1).getDirection() - getLine(i).getDirection();
		}
		
		return CirculationDirection.getTurnDirection(totalTurn);
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
		
		/*
		System.out.println("0 vs. 3: " + HelpMath.pointDistance(point, getVertex(0)) + " vs. " 
				+ HelpMath.pointDistance(point, getVertex(3)));
		*/
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
		return CirculationDirection.getTurnDirection(turn) != this.direction;
	}
	
	/**
	 * Projects this polygon to the given axis. Returns both the starting point and the 
	 * end point of the projection.
	 * 
	 * @param axis The axis along which the projection is done.
	 * @return The starting point of the projection and the end point of the projection in 
	 * an array with size 2.
	 */
	public Vector2D[] getProjection(Vector2D axis)
	{
		List<Vector2D> projections = new ArrayList<>();
		
		// Projects all points in this polygon to the given axis
		for (int i = 0; i < getVertexAmount(); i++)
		{
			 projections.add(getLine(i).vectorProjection(axis));
		}
		
		// Picks two projected points, the smallest and the largest
		Vector2D[] projection = new Vector2D[2];
		
		projections.sort(new ProjectionComparator());
		projection[0] = projections.get(0);
		projection[1] = projections.get(projections.size() - 1);
		
		return projection;
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
	 * Checks if the two polygons collide with each other
	 * @param other The other polygon
	 * @return Do the two polygons collide with each other
	 */
	public boolean collidesWith(Polygon other)
	{
		// Keeps track of all the axes that have been used
		List<Vector2D> usedAxes = new ArrayList<>();
		
		Polygon[] polygons = {this, other};
		for (Polygon axisProvider : polygons)
		{
			for (int lineIndex = 0; lineIndex < axisProvider.getVertexAmount(); lineIndex ++)
			{
				// Used axes are the normal vectors of each side of the polygon
				Vector2D axis = axisProvider.getLine(lineIndex).normal();
				
				// Doesn't use axes that are parallel with any of the previous axes
				boolean axisIsImportant = true;
				for (Vector2D oldAxis : usedAxes)
				{
					if (axis.isParallerWith(oldAxis))
					{
						axisIsImportant = false;
						break;
					}
				}
				if (!axisIsImportant)
					continue;
				else
					usedAxes.add(axis);
				
				// If there isn't overlapping on an axis, there is no collision
				if (!overlapsAlongAxis(other, axis))
					return false;
			}
		}
	
		// TODO: Add the MTV calculation once this version is tested
		return true;
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
	 * Checks if two projections of polygons overlap each other.
	 * @param p1 The projection of the first polygon. Must be an array with size 2.
	 * @param p2 The projection of the second polygon. Must be an array with size 2.
	 * @return do the two projections overlap each other
	 */
	public static boolean projectionsOverlap(Vector2D[] p1, Vector2D[] p2)
	{
		if (p1.length < 2 || p2.length < 2)
			throw new IllegalArgumentException("Invalid projections");
		
		ProjectionComparator comparator = new ProjectionComparator();
		
		// If either of the points in p2 is between the points in p1, there is a collision
		for (int testPointIndex = 0; testPointIndex < 2; testPointIndex ++)
		{
			Vector2D testPoint = p2[testPointIndex];
			if (comparator.compare(testPoint, p1[1]) <= 0 && 
					comparator.compare(testPoint, p1[0]) >= 0)
				return true;
		}
		
		return false;
	}
	
	private CirculationDirection getCirculationDirection(int startIndex)
	{
		return CirculationDirection.getTurnDirection(getLine(startIndex + 1).getDirection() - 
				getLine(startIndex).getDirection());
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
			if (turnAngle >= 0 && turnAngle < 180)
				return COUNTERCLOCKWISE;
			else
				return CLOCKWISE;
		}
	}
}
