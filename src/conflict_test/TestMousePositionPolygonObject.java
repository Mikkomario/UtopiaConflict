package conflict_test;

import genesis_event.AdvancedMouseEvent;
import genesis_event.AdvancedMouseListener;
import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_util.StateOperator;
import genesis_util.Vector2D;

/**
 * This polygon positions itself around the cursor. It also checks if it collides with another 
 * polygon.
 * 
 * @author Mikko Hilpinen
 * @since 10.12.2014
 */
public class TestMousePositionPolygonObject extends TestPolygonObject implements
		AdvancedMouseListener
{
	// ATTRIBUTES	-------------------------------
	
	private TestPolygonObject other;
	private EventSelector<AdvancedMouseEvent> selector;
	
	
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
		this.selector = AdvancedMouseEvent.createMouseMoveSelector();
	}
	
	
	// IMPLEMENTED METHODS	-------------------------

	@Override
	public StateOperator getListensToMouseEventsOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public EventSelector<AdvancedMouseEvent> getMouseEventSelector()
	{
		return this.selector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector2D position)
	{
		return false;
	}

	@Override
	public void onMouseEvent(AdvancedMouseEvent event)
	{
		// Updates position
		setTrasformation(getTransformation().withPosition(event.getPosition()));
		// Checks collisions
		if (getPolygon().transformedWith(getTransformation()).collidesWith(
				this.other.getPolygon().transformedWith(this.other.getTransformation())))
			System.out.println("Polygons collide");
			
		/*
		if (getPolygon().transformedWith(getTransformation()).overlapsAlongAxis(
				this.other.getPolygon().transformedWith(this.other.getTransformation()), 
				new Vector2D(1, 0)))
			System.out.println("Collide on x-axis");
			*/
	}
}
