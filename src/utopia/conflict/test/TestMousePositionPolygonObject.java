package utopia.conflict.test;

import utopia.conflict.util.CollisionCheck;
import utopia.genesis.event.MouseEvent;
import utopia.genesis.event.MouseListener;
import utopia.genesis.util.Vector3D;
import utopia.inception.event.EventSelector;

/**
 * This polygon positions itself around the cursor. It also checks if it collides with another 
 * polygon.
 * 
 * @author Mikko Hilpinen
 * @since 10.12.2014
 */
public class TestMousePositionPolygonObject extends TestPolygonObject implements
		MouseListener
{
	// ATTRIBUTES	-------------------------------
	
	private TestPolygonObject other;
	private EventSelector selector;
	
	
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new object
	 * @param vertexAmount How many vertices the polygon has
	 * @param other The other polygon this one may collide with
	 */
	public TestMousePositionPolygonObject(int vertexAmount, TestPolygonObject other)
	{
		super(vertexAmount);
		
		// Intializes attributes
		this.other = other;
		this.selector = MouseEvent.createMouseMoveSelector();
	}
	
	
	// IMPLEMENTED METHODS	-------------------------

	@Override
	public EventSelector getMouseEventSelector()
	{
		return this.selector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector3D position)
	{
		return false;
	}

	@Override
	public void onMouseEvent(MouseEvent event)
	{
		// Updates position
		setTrasformation(getTransformation().withPosition(event.getPosition()));
		// Checks collisions
		if (CollisionCheck.checkCollidableCollisions(this, this.other, false, false).collided())
			System.out.println("Polygons collide");
	}
}
