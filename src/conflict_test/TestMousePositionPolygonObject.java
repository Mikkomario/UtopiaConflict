package conflict_test;

import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_event.MouseEvent;
import genesis_event.MouseListener;
import genesis_util.Vector3D;

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
	private EventSelector<MouseEvent> selector;
	
	
	// CONSTRUCTOR	-------------------------------
	
	/**
	 * Creates a new object
	 * @param vertexAmount How many vertices the polygon has
	 * @param handlers The handlers that will handle this object
	 * @param other The other polygon this one may collide with
	 */
	public TestMousePositionPolygonObject(int vertexAmount,
			HandlerRelay handlers, TestPolygonObject other)
	{
		super(vertexAmount, handlers);
		
		// Intializes attributes
		this.other = other;
		this.selector = MouseEvent.createMouseMoveSelector();
	}
	
	
	// IMPLEMENTED METHODS	-------------------------

	@Override
	public EventSelector<MouseEvent> getMouseEventSelector()
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
		if (getPolygon().transformedWith(getTransformation()).collidesWith(
				this.other.getPolygon().transformedWith(this.other.getTransformation())))
			System.out.println("Polygons collide");
	}
}
