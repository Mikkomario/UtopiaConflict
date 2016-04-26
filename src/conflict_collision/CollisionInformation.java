package conflict_collision;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import conflict_util.Circle;
import conflict_util.Polygon;
import utopia.genesis.util.HelpMath;
import utopia.genesis.util.Vector3D;

/**
 * CollisionInformation is a collection of a resource's state regarding collision 
 * related data.
 * @author Mikko Hilpinen
 * @since 8.12.2014
 */
public class CollisionInformation
{
	// ATTRIBUTES	----------------------------
	
	// TODO: Add support for circles and "brach's bubble form"
	
	private List<Circle> circles = null;
	private List<Polygon> polygons = null;
	private Class<?>[] supportedListeners = null;
	private Polygon boundingBox = null;
	private boolean usesBoundingBox = false;
	
	private int minCircleVertexAmount = 8;
	private int maxcircleEdgeLength = -1;
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Creates new collision information based on the given point data. This works best 
	 * among other objects that have provided polygon data.
	 * @param vertices The vertices that form the object's collision polygon
	 * @param origin The origin point (x, y) of the collision information. The vertices will 
	 * be translated accordingly. Default origin is (0, 0)
	 */
	public CollisionInformation(Vector3D origin, Vector3D... vertices)
	{
		// Creates the polygon(s)
		Vector3D[] translatedVertices;
		if (origin == null || origin.equals(Vector3D.ZERO))
			translatedVertices = vertices;
		else
		{
			translatedVertices = new Vector3D[vertices.length];
			for (int i = 0; i < vertices.length; i++)
			{
				translatedVertices[i] = vertices[i].minus(origin);
			}
		}
		
		this.polygons = new Polygon(translatedVertices).toConvexPolygons();
		this.usesBoundingBox = this.polygons.size() > 1;
	}

	/**
	 * Creates new collision information based on circular forms. This constructor 
	 * should be used if polygon data can't be provided or isn't needed.
	 * @param origin The origin of the collision information. The circles are translated 
	 * accordingly. Default origin is (0, 0)
	 * @param minCircleVertexAmount How many vertices there needs to be at least when 
	 * transforming one of the circles into a polygon. Use a negative number for no limit.
	 * @param maxCircleEdgeLength How long can a single edge be when a circle is transformed 
	 * into a polygon. Use negative number for no limit.
	 * @param circles The circles that form this information
	 */
	public CollisionInformation(Vector3D origin, int minCircleVertexAmount, 
			int maxCircleEdgeLength, Circle... circles)
	{
		this.minCircleVertexAmount = minCircleVertexAmount;
		this.maxcircleEdgeLength = maxCircleEdgeLength;
		
		this.circles = new ArrayList<>();
		for (Circle circle : circles)
		{
			if (origin == null)
				this.circles.add(circle);
			else
				this.circles.add(circle.translated(origin.reverse()));
		}
		
		this.usesBoundingBox = true;
	}
	
	/**
	 * Creates new collision information that contains both polygon and circle data. 
	 * Both shapes are taken into account when checking for collision.
	 * @param origin The origin of the vertices / circles. Translation added accordingly. 
	 * Default at (0, 0).
	 * @param vertices The vertices that form the object's polygon data.
	 * @param minCircleVertexAmount How many vertices there needs to be at least when 
	 * transforming one of the circles into a polygon. Use a negative number for no limit.
	 * @param maxCircleEdgeLength How long can a single edge be when a circle is transformed 
	 * into a polygon. Use negative number for no limit.
	 * @param circles The circles that are used in the collision checking as well
	 */
	public CollisionInformation(Vector3D origin, Vector3D[] vertices, 
			int minCircleVertexAmount, int maxCircleEdgeLength, Circle[] circles)
	{
		this.minCircleVertexAmount = minCircleVertexAmount;
		this.maxcircleEdgeLength = maxCircleEdgeLength;
		this.circles = new ArrayList<>();
		for (Circle circle : circles)
		{
			if (origin == null)
				this.circles.add(circle);
			else
				this.circles.add(circle.translated(origin.reverse()));
		}
		
		// Creates the polygon(s)
		Vector3D[] translatedVertices;
		if (origin == null || origin.equals(Vector3D.ZERO))
			translatedVertices = vertices;
		else
		{
			translatedVertices = new Vector3D[vertices.length];
			for (int i = 0; i < vertices.length; i++)
			{
				translatedVertices[i] = vertices[i].minus(origin);
			}
		}
		
		this.polygons = new Polygon(translatedVertices).toConvexPolygons();
		this.usesBoundingBox = true;
	}
	
	
	// GETTERS & SETTERS	---------------------------
	
	/**
	 * @return The circles defined in this collision information
	 */
	public List<? extends Circle> getCircles()
	{
		if (usesCircles())
			return this.circles;
		else
			return new ArrayList<>();
	}
	
	/**
	 * @return The collision information's circles transformed into polygon format
	 */
	public List<Polygon> getCirclePolygons()
	{
		List<Polygon> polygons = new ArrayList<>();
		if (usesCircles())
		{
			for (Circle circle : this.circles)
			{
				polygons.add(circle.toPolygon(getMinCircleVertexAmount(), getMaxCircleEdgeLength()));
			}
		}
		
		return polygons;
	}
	
	/**
	 * @return Does the collision information contain any circle data
	 */
	public boolean usesCircles()
	{
		return this.circles != null;
	}
	
	/**
	 * @return The object's collision polygons
	 */
	public List<? extends Polygon> getPolygons()
	{
		if (usesPolygons())
			return this.polygons;
		else
			return new ArrayList<>();
	}
	
	/**
	 * @return Does the information contain usable polygon data
	 */
	public boolean usesPolygons()
	{
		return this.polygons != null;
	}
	
	/**
	 * @return The minimum amount of vertices for each circle when transforming them into 
	 * polygons. Negative if no limit.
	 */
	public int getMinCircleVertexAmount()
	{
		return this.minCircleVertexAmount;
	}
	
	/**
	 * @return The maximum length of each circle vertex when transforming them into polygons. 
	 * Negative if no limit.
	 */
	public int getMaxCircleEdgeLength()
	{
		return this.maxcircleEdgeLength;
	}
	
	/**
	 * @return The bounding box drawn around the collision shape(s). All collidable area 
	 * fits inside this bounding box.
	 */
	public Polygon getBoundingBox()
	{
		if (this.boundingBox == null)
		{
			List<Vector3D> mins = new ArrayList<>();
			List<Vector3D> maxes = new ArrayList<>();
			if (usesCircles())
			{
				for (Circle circle : this.circles)
				{
					mins.add(circle.getTopLeft());
					maxes.add(circle.getBottomRight());
				}
			}
			if (usesPolygons())
			{
				for (Polygon polygon : this.polygons)
				{
					mins.add(polygon.getTopLeft());
					maxes.add(polygon.getBottomRight());
				}
			}
			
			this.boundingBox = new Polygon(Polygon.getRectangleVertices(
					HelpMath.min(mins.toArray(new Vector3D[0])), 
					HelpMath.max(maxes.toArray(new Vector3D[0]))));
		}
		
		return this.boundingBox;
	}
	
	/**
	 * @return Should bounding box be used when checking for collision with this information. 
	 * Using bounding box is recommended for complex shapes
	 */
	public boolean usesBoundingBox()
	{
		return this.usesBoundingBox;
	}
	
	
	// OTHER METHODS	--------------------------
	
	/**
	 * This method draws a line around the collision area specified within this collision 
	 * information should be used mostly for testing purposes.
	 * @param g2d The graphics object that will draw the lines
	 */
	public void drawCollisionArea(Graphics2D g2d)
	{
		if (usesPolygons())
		{
			for (Polygon p : this.polygons)
			{
				p.drawPolygon(g2d);
			}
		}
		if (usesCircles())
		{
			for (Circle c : this.circles)
			{
				c.drawCircle(g2d);
			}
		}
	}
	
	/**
	 * Changes the set of classes that can receive collision events when colliding with 
	 * this object. By default, every class can receive these events.
	 * @param supportedListeners The set of classes that can receive collision events that 
	 * include this object / information.
	 */
	public void limitSupportedListenersTo(Class<?>... supportedListeners)
	{
		this.supportedListeners = supportedListeners;
	}
	
	/**
	 * Makes it so that all classes can receive collision events when colliding with this 
	 * object, which is the default state of this object.
	 */
	public void resetLimitedClasses()
	{
		this.supportedListeners = null;
	}
	
	/**
	 * Checks if the given object should be informed about collision events that include this 
	 * information.
	 * @param o The object that may be interested about collision events
	 * @return Should the object be informed about events concerning this information.
	 */
	public boolean allowsCollisionEventsFor(Object o)
	{
		if (this.supportedListeners == null)
			return true;
		
		for (int i = 0; i < this.supportedListeners.length; i++)
		{
			if (this.supportedListeners[i].isInstance(o))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Checks whether the provided (relative) point lies within the collidable area
	 * @param point The (relative) point that is checked
	 * @return Does the point lie in the collision area
	 */
	public boolean pointIsWithin(Vector3D point)
	{
		// May first check the bounding box
		if (usesBoundingBox())
		{
			if (!getBoundingBox().pointisWithin(point))
				return false;
		}
		
		// Then checks the circles
		if (usesCircles())
		{
			for (Circle circle : this.circles)
			{
				if (circle.pointIsWithin(point))
					return true;
			}
		}
		
		// Then checks the polygons
		if (usesPolygons())
		{
			for (Polygon polygon : this.polygons)
			{
				if (polygon.pointisWithin(point))
					return true;
			}
		}
		
		return false;
	}
}
