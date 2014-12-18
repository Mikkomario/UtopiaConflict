package conflict_test;

import exodus_util.Transformation;
import genesis_event.Actor;
import genesis_event.GenesisHandlerType;
import genesis_event.HandlerRelay;
import genesis_util.Vector2D;

/**
 * This polygon tries to get outside a polygon
 * 
 * @author Mikko Hilpinen
 * @since 18.12.2014
 */
public class TestEscapingPolygonObject extends TestPolygonObject implements
		Actor
{
	// ATTRIBUTES	----------------------
	
	private TestPolygonObject other;
	
	
	// CONSTRUCTOR	----------------------
	
	/**
	 * Creates a new object
	 * @param vertexAmount How many vertices the polygon will have
	 * @param handlers The handlers that will handle this polygon
	 * @param other The polygon this one tries to stay away from
	 */
	public TestEscapingPolygonObject(int vertexAmount, HandlerRelay handlers, 
			TestPolygonObject other)
	{
		super(vertexAmount, handlers);
		
		System.out.println("ActorHandler provied: " + 
				handlers.containsHandlerOfType(GenesisHandlerType.ACTORHANDLER));
		
		// Initializes attributes
		this.other = other;
	}
	
	
	// IMPLEMENTED METHODS	-----------------

	@Override
	public void act(double duration)
	{
		// If there's a collision with the other polygon, moves away
		Vector2D mtv = getPolygon().transformedWith(getTransformation()).collidesWithMTV(
				this.other.getPolygon().transformedWith(this.other.getTransformation()));
		
		if (mtv != null)
		{
			setTrasformation(getTransformation().plus(
					Transformation.transitionTransformation(mtv)));
			System.out.println("Escape!");
		}
	}
}
