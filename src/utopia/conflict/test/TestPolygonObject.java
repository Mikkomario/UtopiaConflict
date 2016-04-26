package utopia.conflict.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import utopia.conflict.event.Collidable;
import utopia.conflict.event.CollisionInformation;
import utopia.conflict.util.Polygon;
import utopia.genesis.event.Drawable;
import utopia.genesis.util.Transformable;
import utopia.genesis.util.Transformation;
import utopia.genesis.util.Vector3D;
import utopia.inception.util.SimpleHandled;

/**
 * This class is made for simple polygon tests
 * @author Mikko Hilpinen
 * @since 9.12.2014
 */
public class TestPolygonObject extends SimpleHandled implements Transformable, Drawable, 
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
	 */
	public TestPolygonObject(int vertexAmount)
	{
		// Initializes attributes
		this.transformation = new Transformation();
		
		// Creates the polygon
		Vector3D[] vertices = new Vector3D[vertexAmount];
		for (int i = 0; i < vertexAmount; i++)
		{
			vertices[i] = new Vector3D(100, 0).withZDirection(360 / vertexAmount * i);
		}
		this.polygon = new Polygon(vertices);
		
		// Creates collision information
		this.collisionInformation = new CollisionInformation(null, vertices);
	}
	
	/**
	 * Creates a new object
	 * @param polygon The polygon this object uses
	 */
	public TestPolygonObject(Polygon polygon)
	{
		this.polygon = polygon;
		this.transformation = new Transformation();
		this.collisionInformation = new CollisionInformation(null, polygon.getVertices());
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
			
			g2d.setColor(Color.ORANGE);
			this.polygon.drawCollisionAxes(g2d);
			
			g2d.setColor(Color.BLACK);
			g2d.translate(0, 100);
			this.polygon.getProjection(new Vector3D(1, 0)).draw(g2d);
			
			g2d.setTransform(lastTransform);
		}
	}

	@Override
	public int getDepth()
	{
		return 0;
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
	
	
	// GETTERS & SETTERS	-------------------
	
	/**
	 * @return The polygon used by this object
	 */
	public Polygon getPolygon()
	{
		return this.polygon;
	}
}
