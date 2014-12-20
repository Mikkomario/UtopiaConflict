package conflict_collision;

import java.util.List;

import genesis_util.Vector2D;
import conflict_util.Polygon;

/**
 * CollisionInformation is a collection of a resource's state regarding collision 
 * related data.
 * 
 * @author Mikko Hilpinen
 * @since 8.12.2014
 */
public class CollisionInformation
{
	// ATTRIBUTES	----------------------------
	
	private double radius;
	private List<Polygon> polygons;
	private Class<?>[] supportedListeners;
	
	
	// CONSTRUCTOR	-----------------------------
	
	/**
	 * Creates new collision information based on the given point data. This works best 
	 * among other objects that have provided polygon data. The origin of the 
	 * object is expected to be at (0, 0).
	 * @param vertices The vertices that form the object's collision polygon
	 */
	public CollisionInformation(Vector2D[] vertices)
	{
		// Creates the polygon(s) and calculates radius
		this.radius = -1;
		
		for (int i = 0; i < vertices.length; i++)
		{
			double r = vertices[i].getLength();
			if (r > this.radius)
				this.radius = r;
		}
		
		this.polygons = new Polygon(vertices).toConvexPolygons();
		this.supportedListeners = null;
	}

	/**
	 * Creates new collision information based on just object radius. This constructor 
	 * should be used if polygon data can't be provided or isn't needed. It is best used 
	 * with other circular objects. The origin is considered to be at (0, 0).
	 * @param radius The object's radius.
	 */
	public CollisionInformation(int radius)
	{
		this.radius = radius;
		this.polygons = null;
		this.supportedListeners = null;
	}
	
	/**
	 * Creates new collision information that contains both the polygon and radius data. 
	 * This can be used among both polygon-based and circular objects. The origin of the 
	 * object is considered to be at (0, 0).
	 * @param vertices The vertices that form the object's polygon data.
	 * @param radius The object's radius.
	 */
	public CollisionInformation(Vector2D[] vertices, int radius)
	{
		this.radius = radius;
		this.polygons = new Polygon(vertices).toConvexPolygons();
		this.supportedListeners = null;
	}
	
	
	// GETTERS & SETTERS	---------------------------
	
	/**
	 * @return The object's radius
	 */
	public double getRadius()
	{
		return this.radius;
	}
	
	/**
	 * @return The object's collision polygons
	 */
	public List<Polygon> getPolygons()
	{
		return this.polygons;
	}
	
	/**
	 * @return Does the information contain usable polygon data
	 */
	public boolean usesPolygons()
	{
		return this.polygons != null;
	}
	
	
	// OTHER METHODS	--------------------------
	
	/**
	 * Changes the set of classes that can receive collision events when colliding with 
	 * this object. By default, every class can receive these events.
	 * @param supportedListeners The set of classes that can receive collision events that 
	 * include this object / information.
	 */
	public void limitSupportedListenersTo(Class<?>[] supportedListeners)
	{
		this.supportedListeners = supportedListeners;
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
