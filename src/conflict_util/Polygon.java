package conflict_util;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import utopia.genesis.util.HelpMath;
import utopia.genesis.util.Line;
import utopia.genesis.util.Transformation;
import utopia.genesis.util.Vector3D;

/**
 * Polygon is a simple construction that consists of multiple points. Polygons can be traversed 
 * through clockwise and counter-clockwise.
 * @author Mikko Hilpinen
 * @since 8.12.2014
 */
public class Polygon
{
	// ATTRIBUTES	--------------------------
	
	private final Vector3D[] vertices;
	private CirculationDirection direction;
	private List<Vector3D> axes;
	private Vector3D topLeft, bottomRight;
	
	
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
		this.topLeft = null;
		this.bottomRight = null;
	}
	
	/**
	 * Creates a new polygon by copying another polygon
	 * @param other The polygon that will be copied
	 */
	public Polygon(Polygon other)
	{
		this.vertices = other.getVertices();
		this.direction = other.direction;
		this.axes = other.axes;
		this.topLeft = other.topLeft;
		this.bottomRight = other.bottomRight;
	}
	
	
	// IMPLEMENTED METHODS	------------------
	
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < getVertexAmount(); i++)
		{
			if (i != 0)
				s.append(";");
			s.append(getVertex(i));
		}
		
		return s.toString();
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
	 * @return The edges in the polygon. The edges are calculated each time this method is 
	 * called and altering them won't affect the polygon in any way.
	 */
	public Line[] getEdges()
	{
		Line[] edges = new Line[getVertexAmount()];
		for (int i = 0; i < edges.length; i++)
		{
			edges[i] = getEdge(i);
		}
		
		return edges;
	}
	
	/**
	 * @return The collision axes used with this polygon. Each axis is normalised and 
	 * perpendicular to an edge in the polygon.
	 */
	public List<Vector3D> getCollisionAxes()
	{
		if (this.axes == null)
			this.axes = calculateAxes();
		
		return this.axes;
	}
	
	/**
	 * @return Is the polygon convex. A polygon is convex when and only when one must only turn 
	 * left or only turn right when traversing through the polygon.
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
		
		Polygon reversed = new Polygon(newVertices);
		if (this.direction != null)
			reversed.direction = this.direction.reverse();
		return reversed;
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
		if (this.bottomRight == null)
		{
			if (getVertexAmount() == 0)
				return Vector3D.zeroVector();
			
			double largestX = getVertex(0).getFirst();
			double largestY = getVertex(0).getSecond();
			
			for (int i = 1; i < getVertexAmount(); i++)
			{
				Vector3D vertex = getVertex(i);
				
				if (vertex.getFirst() > largestX)
					largestX = vertex.getFirst();
				if (vertex.getSecond() > largestY)
					largestY = vertex.getSecond();
			}
			
			this.bottomRight = new Vector3D(largestX, largestY);
		}
		
		return this.bottomRight;
	}
	
	/**
	 * @return If a bounding box was formed around the polygon, this would be its top left
	 * corner
	 */
	public Vector3D getTopLeft()
	{
		if (this.topLeft == null)
		{
			if (getVertexAmount() == 0)
				return Vector3D.zeroVector();
			
			double smallestX = getVertex(0).getFirst();
			double smallestY = getVertex(0).getSecond();
			
			for (int i = 1; i < getVertexAmount(); i++)
			{
				Vector3D vertex = getVertex(i);
				
				if (vertex.getFirst() < smallestX)
					smallestX = vertex.getFirst();
				if (vertex.getSecond() < smallestY)
					smallestY = vertex.getSecond();
			}
			
			this.topLeft = new Vector3D(smallestX, smallestY);
		}
		
		return this.topLeft;
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
		// TODO: Consider using pair instead
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
		projections.sort(new CollisionCheck.ProjectionComparator());
		
		return new Line(projections.get(0), projections.get(projections.size() - 1));
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
	 * @return a circle centered to the polygon's center. The radius of the circle is the 
	 * average between the vertex radiuses.
	 */
	public Circle toAverageCircle()
	{
		// Finds the center point between the vertices
		Vector3D center = HelpMath.getAveragePoint(this.vertices);
		
		// Then finds the average radius
		double averageRadius = 0;
		for (Vector3D vertex : this.vertices)
		{
			averageRadius += HelpMath.pointDistance2D(vertex, center);
		}
		averageRadius /= getVertexAmount();
		
		return new Circle(center, averageRadius);
	}
	
	/**
	 * @return a circle centered to the polygon's center. All vertices of the polygon will 
	 * reside inside the circle.
	 */
	public Circle toMaximumCircle()
	{
		Vector3D center = HelpMath.getAveragePoint(this.vertices);
		
		double maxRadius = 0;
		for (Vector3D vertex : this.vertices)
		{
			double radius = HelpMath.pointDistance2D(center, vertex);
			if (radius > maxRadius)
				maxRadius = radius;
		}
		
		return new Circle(center, maxRadius);
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
	
	
	
	// ENUMS	--------------------------
	
	/**
	 * Angle can circle to either clockwise or counter-clockwise direction, and so can 
	 * polygons.
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
		 * @return The circulation direction opposite to this one
		 */
		public CirculationDirection reverse()
		{
			if (this == CLOCKWISE)
				return CirculationDirection.COUNTERCLOCKWISE;
			else
				return CirculationDirection.CLOCKWISE;
		}
		
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
