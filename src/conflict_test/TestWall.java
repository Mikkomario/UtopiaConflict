package conflict_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import genesis_event.Drawable;
import genesis_event.HandlerRelay;
import genesis_util.StateOperator;
import genesis_util.Vector3D;
import omega_util.SimpleGameObject;
import omega_util.Transformation;
import conflict_collision.Collidable;
import conflict_collision.CollisionInformation;
import conflict_util.Polygon;

/**
 * This is a simple wall that can be placed and collided with
 * 
 * @author Mikko Hilpinen
 * @since 12.3.2015
 */
public class TestWall extends SimpleGameObject implements Collidable, Drawable
{
	// ATTRIBUTES	----------------------------
	
	private Transformation transformation;
	private CollisionInformation collisionInformation;
	private StateOperator canBeCollidedWithOperator;
	
	
	// CONSTRUCTOR	----------------------------
	
	/**
	 * Creates a new testWall to the given position and with the given size
	 * @param handlers The handlers that will handle this wall (collidable, drawable)
	 * @param topLeft The position of the top left corner of this wall
	 * @param dimensions The size of this wall
	 */
	public TestWall(HandlerRelay handlers, Vector3D topLeft, Vector3D dimensions)
	{
		super(handlers);
		
		this.transformation = new Transformation(topLeft);
		this.collisionInformation = new CollisionInformation(Polygon.getRectangleVertices(
				Vector3D.zeroVector(), dimensions));
		this.canBeCollidedWithOperator = new StateOperator(true, false);
	}
	
	
	// IMPLEMENTED METHODS	--------------------

	@Override
	public Transformation getTransformation()
	{
		return this.transformation;
	}

	@Override
	public void setTrasformation(Transformation t)
	{
		this.transformation = t;
	}

	@Override
	public CollisionInformation getCollisionInformation()
	{
		return this.collisionInformation;
	}

	@Override
	public StateOperator getCanBeCollidedWithStateOperator()
	{
		return this.canBeCollidedWithOperator;
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		g2d.setColor(Color.BLACK);
		AffineTransform last = g2d.getTransform();
		getTransformation().transform(g2d);
		getCollisionInformation().drawCollisionArea(g2d);
		
		g2d.setColor(Color.GRAY);
		int r = (int) getCollisionInformation().getRadius();
		g2d.drawOval(-r, -r, r * 2, r * 2);
		
		g2d.setTransform(last);
	}

	@Override
	public int getDepth()
	{
		return 0;
	}

	@Override
	public StateOperator getIsVisibleStateOperator()
	{
		return getIsActiveStateOperator();
	}
}
