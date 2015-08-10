package conflict_util;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import genesis_util.HelpMath;
import genesis_util.Line;
import genesis_util.Transformation;
import genesis_util.Vector3D;

/**
 * Polygon is a simple construction that consists of multiple points
 * 
 * @author Mikko Hilpinen
 * @since 8.12.2014
 */
public class Polygon
{
	// ATTRIBUTES	--------------------------
	
	private final Vector3D[] vertices;
	private CirculationDirection direction;
	private List<Vector3D> axes;
	
	
	// CONSTRUCTOR	--------------------------
	
	/**
	 * Creates a new polygon from the given points
	 * @param vertices The points that form this polygon. The points should be in order so that 
	 * they form the lines that form this polygon.
	 */
	public Polygon(Vector3D[] vertices)
	{
		// Initializes attributes
		this.vertices = vertices;
		this.direction = null;
		this.axes = null;
	}
	
	/**
	 * Creates a new polygon by copying the other polygon
	 * 
	 * @param other The polygon that will be copied
	 */
	public Polygon(Polygon other)
	{
		this.vertices = other.getVertices();
		this.direction = other.direction;
		this.axes = other.axes;
	}
	
	
	// IMPLEMENTED METHODS	------------------
	
	@Override
	public String toString()
	{
		String s = getVertex(0).toString();
		for (int i = 1; i < getVertexAmount(); i++)
		{
			s += ";" + getVertex(i).toString();
		}
		
		return s;
	}
	
	
	// GETTERS & SETTERS	------------------
	
	/**
	 * @return The vertices that form this polygon (cloned, can't be used for changing the 
	 * polygon)
	 */
	public Vector3D[] getVertices()
	{
		return this.vertices.clone();
	}

	
	// OTHER METHODS	----------------------
	
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
	 * @return A list of collision points. There will be one or two collision points.
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
	
	/**
	 * @return How many vertexes does this polygon have
	 */
	public int getVertexAmount()
	{
		return this.vertices.length;
	}
	
	/**
	 * Finds a vertex with the given index (loops around the polygon if necessary)
	 * @param index The index of the vertex in the polygon
	 * @return The vertex at the given index in this polygon
	 */
	public Vector3D getVertex(int index)
	{
		while (index < 0)
			index += getVertexAmount();
		
		return this.vertices[index % getVertexAmount()];
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
	public List<Vector3D> getCollisionAxes()
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
					List<Vector3D> newPolyVertices = new ArrayList<Vector3D>();
					newPolyVertices.add(getVertex(lastBrokenIndex));
					
					for (int newPolyIndex = lastBrokenIndex + 1; newPolyIndex < index; 
							newPolyIndex ++)
					{
						newPolyVertices.add(getVertex(newPolyIndex));
						movedIndices.add(newPolyIndex);
					}
					
					newPolyVertices.add(getVertex(index));
					
					polygons.add(new Polygon(newPolyVertices.toArray(new Vector3D[0])));
				}
				
				// Remembers the broken part
				lastBrokenIndex = index;
			}
		}
		
		// If broken indexes were found but not cut, cuts the polygon in half (takes a triangle 
		// out of the original polygon)
		if (movedIndices.isEmpty() && lastBrokenIndex != -1)
		{
			Vector3D[] newPolyVertices = {getVertex(lastBrokenIndex), 
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
			Vector3D[] remainingVertexes = 
					new Vector3D[getVertexAmount() - movedIndices.size()];
			
			int newIndex = 0;
			
			for (int index = 0; index < getVertexAmount(); index ++)
			{
				if (!movedIndices.contains(index) && !movedIndices.contains(index + getVertexAmount()))
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
		Vector3D[] newVertices = new Vector3D[getVertexAmount()];
		
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
		Vector3D[] transformedPoints = new Vector3D[getVertexAmount()];
		
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
	public Vector3D getBottomRight()
	{
		if (getVertexAmount() == 0)
			return Vector3D.zeroVector();
		
		double largestX = -100000;
		double largestY = -100000;
		
		for (Vector3D vertex : this.vertices)
		{
			if (vertex.getFirst() > largestX)
				largestX = vertex.getFirst();
			if (vertex.getSecond() > largestY)
				largestY = vertex.getSecond();
		}
		
		return new Vector3D(largestX, largestY);
	}
	
	/**
	 * @return If a bounding box was formed around the polygon, this would be its top left
	 * corner
	 */
	public Vector3D getTopLeft()
	{
		if (getVertexAmount() == 0)
			return Vector3D.zeroVector();
		
		double smallestX = 100000;
		double smallestY = 100000;
		
		for (Vector3D vertex : this.vertices)
		{
			if (vertex.getFirst() < smallestX)
				smallestX = vertex.getFirst();
			if (vertex.getSecond() < smallestY)
				smallestY = vertex.getSecond();
		}
		
		return new Vector3D(smallestX, smallestY);
	}
	
	/**
	 * @return The width and height of the bounding box around this polygon
	 */
	public Vector3D getDimensions()
	{
		return getBottomRight().minus(getTopLeft());
	}
	
	/**
	 * Checks if a point collides with the bounding box drawn around the polygon
	 * @param point The point that will be checked
	 * @return is the point inside the bounding box
	 */
	public boolean pointIsWithinBoundingBox(Vector3D point)
	{
		return HelpMath.pointIsInRange(point, getTopLeft(), getBottomRight());
	}
	
	/**
	 * Checks if a point is inside this polygon. This doesn't always work with non-convex 
	 * polygons, please transform the polygon into convex polygons before using this method.
	 * @param point The point that is checked
	 * @return Is the point inside this polygon.
	 */
	public boolean pointisWithin(Vector3D point)
	{
		if (!pointIsWithinBoundingBox(point))
			return false;
		
		// Finds two vertices that are nearest to the given point
		List<Vector3D> closestPoints = new LinkedList<Vector3D>();
		
		for (int i = 0; i < getVertexAmount(); i++)
		{
			closestPoints.add(new Vector3D(i, HelpMath.pointDistance2D(point, getVertex(i))));
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
				getVertex(closestPoints.get(0).getFirstInt())).getZDirection());
		double dir2 = HelpMath.checkDirection(getVertex(
				closestPoints.get(1).getFirstInt()).minus(point).getZDirection());
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
	public Line getProjection(Vector3D axis)
	{
		List<Vector3D> projections = new ArrayList<>();
		
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
	public boolean overlapsAlongAxis(Polygon other, Vector3D axis)
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
	public Vector3D overlapsAlongAxisMTV(Polygon other, Vector3D axis)
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
		for (Vector3D axis : getCollisionAxes())
		{
			if (!overlapsAlongAxis(other, axis))
				return false;
		}
		for (Vector3D axis : other.getCollisionAxes())
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
	public Vector3D collidesWithMTV(Polygon other)
	{
		Vector3D mtv = null;
		double smallestOverlap = -1;
		
		// Checks each axis, if there's an overlap on all of them, there is a collision
		for (Vector3D axis : getCollisionAxes())
		{
			Vector3D overlapVector = overlapsAlongAxisMTV(other, axis);
			
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
		for (Vector3D axis : other.getCollisionAxes())
		{
			Vector3D overlapVector = overlapsAlongAxisMTV(other, axis);
			
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
		Vector3D topLeft = getTopLeft(), dimensions = getDimensions();
		
		g2d.drawRect(topLeft.getFirstInt(), topLeft.getSecondInt(), dimensions.getFirstInt(), 
				dimensions.getSecondInt());
	}
	
	/**
	 * Draws visual representations of the collision axes on screen
	 * @param g2d The graphics object that does the drawing
	 */
	public void drawCollisionAxes(Graphics2D g2d)
	{
		for (Vector3D axis : getCollisionAxes())
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
	public static Vector3D projectionsOverlapMTV(Line p1, Line p2)
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
	
	/**
	 * Creates a polygon by parsing it from a string.
	 * @param s The string that contains the polygon's vertex data. The vertices are separated 
	 * with a ';' while the x and y components of each vertex are separated with a ','. For 
	 * example, "3,2;2,2;5,4" would be a valid polygon string.
	 * @return The polygon parsed from the string
	 */
	public static Polygon parseFromString(String s)
	{
		String[] verticeStrings = s.split(";");
		Vector3D[] vertices = new Vector3D[verticeStrings.length];
		for (int i = 0; i < vertices.length; i++)
		{
			vertices[i] = Vector3D.parseFromString(verticeStrings[i]);
		}
		
		return new Polygon(vertices);
	}
	
	/**
	 * Creates a set of vertices to form a rectangle of the given shape
	 * @param topLeft The top left corner of the rectangle
	 * @param bottomRight The bottom right corner of the rectangle
	 * @return The vertices that form the rectangle
	 */
	public static Vector3D[] getRectangleVertices(Vector3D topLeft, Vector3D bottomRight)
	{
		Vector3D[] vertices = {topLeft, new Vector3D(topLeft.getFirst(), 
				bottomRight.getSecond()), bottomRight, new Vector3D(bottomRight.getFirst(), 
				topLeft.getSecond())};
		return vertices;
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
	
	private List<Vector3D> calculateAxes()
	{
		List<Vector3D> newAxes = new ArrayList<>();
		
		for (int i = 0; i < getVertexAmount(); i ++)
		{
			Vector3D axis = getEdge(i).toVector().normal();
			
			// If there already is a paraller axis, doesn't use this one
			boolean paraller = false;
			for (Vector3D previousAxis : newAxes)
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
	
	private static class ProjectionComparator implements Comparator<Vector3D>
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
