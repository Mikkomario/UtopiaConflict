package conflict_test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import omega_util.Transformation;
import conflict_util.Polygon;
import genesis_event.HandlerRelay;
import genesis_util.Vector3D;

/**
 * This class is able to produce a set of convex polygons from a random set of vertices. This 
 * is for testing polygon splitting.
 * 
 * @author Mikko Hilpinen
 * @since 18.12.2014
 */
public class RandomConvexPolygonFactory
{
	// CONSTRUCTOR	---------------------
	
	private RandomConvexPolygonFactory()
	{
		// The constructor is hidden since the interface is static
	}

	
	// OTHER METHODS	-----------------
	
	/**
	 * Creates a new set of convex polygon objects from a set of random vertices
	 * @param handlers The handlers that will handle the polygons
	 * @param vertexAmount How many vertices will be generated
	 * @param position The position to which the polygons will be created
	 * @return A list containing all the objects that were just created
	 */
	public static ArrayList<TestPolygonObject> createPolygons(HandlerRelay handlers, 
			int vertexAmount, Vector3D position)
	{
		// Creates the first polygon and the splits it
		Vector3D[] vertices = new Vector3D[vertexAmount];
		Random random = new Random();
		
		for (int i = 0; i < vertexAmount; i++)
		{
			vertices[i] = new Vector3D(random.nextDouble() * 200 - 100, 
					random.nextDouble() * 200 - 100);
		}
		
		return createPolygons(handlers, vertices, position);
	}
	
	/**
	 * Creates a new set of convex polygon objects from a set of vertices
	 * @param handlers The handlers that will handle the polygons
	 * @param vertices The vertices that form the polygons
	 * @param position The position to which the polygons will be created
	 * @return A list containing all the objects that were just created
	 */
	public static ArrayList<TestPolygonObject> createPolygons(HandlerRelay handlers, 
			Vector3D[] vertices, Vector3D position)
	{
		List<Polygon> polygons = new Polygon(vertices).toConvexPolygons();
		ArrayList<TestPolygonObject> objects = new ArrayList<>();
		
		for (Polygon polygon : polygons)
		{
			TestPolygonObject o = new TestPolygonObject(polygon, handlers);
			o.setTrasformation(Transformation.transitionTransformation(position));
			objects.add(o);
		}
		
		return objects;
	}
}
