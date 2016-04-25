package conflict_test;

import conflict_collision.CollisionListeningInformation;
import conflict_collision.CollisionEvent;
import conflict_collision.CollisionListener;
import utopia.genesis.util.Transformation;
import utopia.genesis.util.Vector3D;

/**
 * This polygon tries to get outside a polygon
 * @author Mikko Hilpinen
 * @since 18.12.2014
 */
public class TestEscapingPolygonObject extends TestPolygonObject implements CollisionListener
{
	// ATTRIBUTES	----------------------
	
	private CollisionListeningInformation collisionChecker;
	
	
	// CONSTRUCTOR	----------------------
	
	/**
	 * Creates a new object
	 * @param vertexAmount How many vertices the polygon will have
	 */
	public TestEscapingPolygonObject(int vertexAmount)
	{
		super(vertexAmount);
		
		// Initializes attributes
		this.collisionChecker = new CollisionListeningInformation(this, true, false);
		this.collisionChecker.limitCheckedClassesTo(new Class<?>[] {TestPolygonObject.class});
	}
	
	
	// IMPLEMENTED METHODS	-----------------

	@Override
	public CollisionListeningInformation getCollisionListeningInformation()
	{
		return this.collisionChecker;
	}

	@Override
	public void onCollisionEvent(CollisionEvent event)
	{
		Vector3D movement = event.getMTV();
		
		if (event.getTarget() instanceof TestEscapingPolygonObject)
			movement = movement.times(0.5);
		
		setTrasformation(getTransformation().plus(
				Transformation.transitionTransformation(movement)));
	}
}
