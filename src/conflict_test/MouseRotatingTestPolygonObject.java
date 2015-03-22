package conflict_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import omega_util.Transformation;
import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_event.MouseEvent;
import genesis_event.MouseListener;
import genesis_event.StrictEventSelector;
import genesis_util.StateOperator;
import genesis_util.Vector3D;

/**
 * This test polygon rotates around it's origin (0,0) when mouse is over it
 * 
 * @author Mikko Hilpinen
 * @since 9.12.2014
 */
public class MouseRotatingTestPolygonObject extends TestPolygonObject implements
		MouseListener
{
	// ATTRIBUTES	-------------------------
	
	private StrictEventSelector<MouseEvent, MouseEvent.Feature> selector;
	private Vector3D lastMousePosition;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new object
	 * @param vertexAmount How many vertices the polygon will have
	 * @param handlers The handlers that will handle this object
	 */
	public MouseRotatingTestPolygonObject(int vertexAmount, HandlerRelay handlers)
	{
		super(vertexAmount, handlers);
		
		// Initializes attributes
		this.selector = new StrictEventSelector<>();
		this.selector.addRequiredFeature(MouseEvent.MouseMovementEventType.OVER);
		this.lastMousePosition = Vector3D.zeroVector();
	}
	
	
	// IMPLEMENTED METHODS	------------------

	@Override
	public StateOperator getListensToMouseEventsOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public EventSelector<MouseEvent> getMouseEventSelector()
	{
		return this.selector;
	}

	@Override
	public boolean isInAreaOfInterest(Vector3D position)
	{
		if (getPolygon() != null)
		{
			this.lastMousePosition = getTransformation().inverseTransform(position);
			return getPolygon().pointisWithin(this.lastMousePosition);
		}
		
		return false;
	}

	@Override
	public void onMouseEvent(MouseEvent event)
	{
		// Rotates
		setTrasformation(getTransformation().plus(Transformation.rotationTransformation(
				2 * event.getDuration())));
	}
	
	@Override
	public void drawSelf(Graphics2D g2d)
	{
		if (getTransformation() != null && getPolygon() != null && this.lastMousePosition != null)
		{
			AffineTransform lastTransform = g2d.getTransform();
			getTransformation().transform(g2d);
			g2d.setColor(Color.BLACK);
			g2d.drawOval(this.lastMousePosition.getFirstInt() - 5, 
					this.lastMousePosition.getSecondInt() - 5, 10, 10);
			g2d.setTransform(lastTransform);
		}
		
		super.drawSelf(g2d);
	}
}
