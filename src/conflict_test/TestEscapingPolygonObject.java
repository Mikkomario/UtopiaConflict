package conflict_test;

import conflict_collision.CollisionChecker;
import conflict_collision.CollisionEvent;
import conflict_collision.CollisionListener;
import exodus_util.Transformation;
import genesis_event.GenesisHandlerType;
import genesis_event.HandlerRelay;
import genesis_util.StateOperator;
import genesis_util.Vector2D;

/**
 * This polygon tries to get outside a polygon
 * 
 * @author Mikko Hilpinen
 * @since 18.12.2014
 */
public class TestEscapingPolygonObject extends TestPolygonObject implements CollisionListener
{
	// ATTRIBUTES	----------------------
	
	private CollisionChecker collisionChecker;
	
	
	// CONSTRUCTOR	----------------------
	
	/**
	 * Creates a new object
	 * @param vertexAmount How many vertices the polygon will have
	 * @param handlers The handlers that will handle this polygon
	 */
	public TestEscapingPolygonObject(int vertexAmount, HandlerRelay handlers)
	{
		super(vertexAmount, handlers);
		
		System.out.println("ActorHandler provied: " + 
				handlers.containsHandlerOfType(GenesisHandlerType.ACTORHANDLER));
		
		// Initializes attributes
		this.collisionChecker = new CollisionChecker(this, true);
		Class<?>[] interestingClasses = {TestPolygonObject.class};
		this.collisionChecker.limitCheckedClassesTo(interestingClasses);
	}
	
	
	// IMPLEMENTED METHODS	-----------------

	@Override
	public CollisionChecker getCollisionChecker()
	{
		return this.collisionChecker;
	}

	@Override
	public void onCollisionEvent(CollisionEvent event)
	{
		if (event.isTarget(this))
			event = event.fromTargetsPointOfView();
		
		Vector2D movement = event.getMTV();
		
		if (event.getTarget() instanceof TestEscapingPolygonObject)
			movement = movement.times(0.5);
		
		setTrasformation(getTransformation().plus(
				Transformation.transitionTransformation(movement)));
	}

	@Override
	public StateOperator getListensForCollisionStateOperator()
	{
		return getIsActiveStateOperator();
	}
}
