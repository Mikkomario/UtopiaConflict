package conflict_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import conflict_collision.Collidable;
import conflict_collision.CollisionInformation;
import conflict_util.Polygon;
import utopia.genesis.event.Drawable;
import utopia.genesis.util.Transformation;
import utopia.genesis.util.Vector3D;
import utopia.inception.util.SimpleHandled;

/**
 * This is a simple wall that can be placed and collided with
 * @author Mikko Hilpinen
 * @since 12.3.2015
 */
public class TestWall extends SimpleHandled implements Collidable, Drawable
{
	// ATTRIBUTES	----------------------------
	
	private Transformation transformation;
	private CollisionInformation collisionInformation;
	
	
	// CONSTRUCTOR	----------------------------
	
	/**
	 * Creates a new testWall to the given position and with the given size. Remember to add 
	 * the wall to correct handlers (collidable, drawable)
	 * @param topLeft The position of the top left corner of this wall
	 * @param size The size of this wall
	 */
	public TestWall(Vector3D topLeft, Vector3D size)
	{
		this.transformation = new Transformation(topLeft);
		this.collisionInformation = new CollisionInformation(Polygon.getRectangleVertices(
				Vector3D.zeroVector(), size));
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
}
