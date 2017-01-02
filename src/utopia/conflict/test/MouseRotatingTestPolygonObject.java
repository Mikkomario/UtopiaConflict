package utopia.conflict.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import utopia.genesis.event.MouseEvent;
import utopia.genesis.event.MouseListener;
import utopia.genesis.util.Transformation;
import utopia.genesis.util.Vector3D;
import utopia.inception.event.EventSelector;
import utopia.inception.event.StrictEventSelector;

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
	
	private StrictEventSelector selector;
	private Vector3D lastMousePosition;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new object
	 * @param vertexAmount How many vertices the polygon will have
	 */
	public MouseRotatingTestPolygonObject(int vertexAmount)
	{
		super(vertexAmount);
		
		// Initializes attributes
		this.selector = new StrictEventSelector();
		this.selector.addRequiredFeature(MouseEvent.MouseMovementEventType.OVER);
		this.lastMousePosition = Vector3D.ZERO;
	}
	
	
	// IMPLEMENTED METHODS	------------------

	@Override
	public EventSelector getMouseEventSelector()
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
				2 * event.getDurationMillis())));
	}
	
	@Override
	public void drawSelf(Graphics2D g2d)
	{
		if (getTransformation() != null && getPolygon() != null && this.lastMousePosition != null)
		{
			AffineTransform lastTransform = g2d.getTransform();
			getTransformation().transform(g2d);
			g2d.setColor(Color.BLACK);
			this.lastMousePosition.drawAsPoint(10, g2d);
			g2d.setTransform(lastTransform);
		}
		
		super.drawSelf(g2d);
	}
}
