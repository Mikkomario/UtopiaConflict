package conflict_collision;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import conflict_util.Circle;
import conflict_util.Polygon;
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
		if (origin == null || origin.equals(Vector3D.zeroVector()))
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
	 * @param circles The circles that form this information
	 */
	public CollisionInformation(Vector3D origin, Circle... circles)
	{
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
	 * @param circles The circles that are used in the collision checking as well
	 */
	public CollisionInformation(Vector3D origin, Vector3D[] vertices, Circle[] circles)
	{
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
		if (origin == null || origin.equals(Vector3D.zeroVector()))
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
	
	public List<? extends Circle> getCircles()
	{
		if (usesCircles())
			return this.circles;
		else
			return new ArrayList<>();
	}
	
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
	
	public Polygon getBoundingBox()
	{
		if (this.boundingBox == null)
		{
			boolean first = true;
			double minX = 0, minY = 0;
			double maxX = 0, maxY = 0;
			
			if (usesCircles())
			{
				for (Circle circle : this.circles)
				{
					Vector3D topLeft = circle.getTopLeft();
					Vector3D bottomRight = circle.getBottomRight();
					
					if (first)
					{
						first = false;
						minX = topLeft.getFirst();
						maxX = bottomRight.getFirst();
						minY = topLeft.getSecond();
						maxY = bottomRight.getSecond();
					}
					Vector3D cTopLeft = circle.getTopLeft();
					Vector3D cBottomRight = circle.getBottomRight();
					
					if (topLeft == null)
						topLeft = cTopLeft;
					else
					{
						if (cTopLeft.getFirst() < topLeft.getFirst())
							topLeft = new Vector3D(cTopLeft.getFi)
					}
				}
			}
		}
		
		return this.boundingBox;
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
			for (Polygon p : getPolygons())
			{
				p.drawPolygon(g2d);
			}
		}
		else
			g2d.drawOval((int) getRadius() / 2, (int) getRadius() / 2, (int) getRadius(), 
					(int) getRadius());
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
}
