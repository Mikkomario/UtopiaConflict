package conflict_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import conflict_collision.Collidable;
import conflict_collision.CollisionInformation;
import conflict_util.Polygon;
import exodus_object.SimpleGameObject;
import exodus_util.Transformable;
import exodus_util.Transformation;
import genesis_event.Drawable;
import genesis_event.HandlerRelay;
import genesis_util.StateOperator;
import genesis_util.Vector2D;

/**
 * This class is made for simple polygon tests
 * 
 * @author Mikko Hilpinen
 * @since 9.12.2014
 */
public class TestPolygonObject extends SimpleGameObject implements Transformable, Drawable, 
		Collidable
{
	// ATTRIBUTES	-------------------------
	
	private Polygon polygon;
	private Transformation transformation;
	private CollisionInformation collisionInformation;
	
	
	// CONSTRUCTOR	-------------------------
	
	/**
	 * Creates a new polygon object
	 * @param vertexAmount How many vertices / sides the polygon will have
	 * @param handlers The handlers that will handle this object
	 */
	public TestPolygonObject(int vertexAmount, HandlerRelay handlers)
	{
		super(handlers);
		
		// Initializes attributes
		this.transformation = new Transformation();
		
		// Creates the polygon
		Vector2D[] vertices = new Vector2D[vertexAmount];
		for (int i = 0; i < vertexAmount; i++)
		{
			vertices[i] = new Vector2D(100, 0).withDirection(360 / vertexAmount * i);
		}
		this.polygon = new Polygon(vertices);
		
		// Creates collision information
		this.collisionInformation = new CollisionInformation(vertices);
	}
	
	/**
	 * Creates a new object
	 * @param polygon The polygon this object uses
	 * @param handlers The handlers that will handle this object
	 */
	public TestPolygonObject(Polygon polygon, HandlerRelay handlers)
	{
		super(handlers);
		
		this.polygon = polygon;
		this.transformation = new Transformation();
		this.collisionInformation = new CollisionInformation(polygon.getVertices());
	}
	
	
	// IMPLEMENTED METHODS	--------------------

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		if (getTransformation() != null && getPolygon() != null)
		{
			AffineTransform lastTransform = g2d.getTransform();
			getTransformation().transform(g2d);
			g2d.setColor(Color.RED);
			this.polygon.drawBoundingBox(g2d);
			
			g2d.setColor(Color.BLUE);
			this.polygon.drawPolygon(g2d);
			
			g2d.setColor(Color.BLACK);
			this.polygon.drawCollisionAxes(g2d);
			
			g2d.translate(0, 100);
			this.polygon.getProjection(new Vector2D(1, 0)).draw(g2d);
			
			g2d.setTransform(lastTransform);
		}
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

	@Override
	public void setDepth(int depth)
	{
		// Can't do that
	}

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
		return getIsActiveStateOperator();
	}
	
	
	// GETTERS & SETTERS	-------------------
	
	/**
	 * @return The polygon used by this object
	 */
	public Polygon getPolygon()
	{
		return this.polygon;
	}
}
