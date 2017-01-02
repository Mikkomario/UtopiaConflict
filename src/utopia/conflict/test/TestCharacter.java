package utopia.conflict.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import utopia.conflict.event.Collidable;
import utopia.conflict.event.CollisionEvent;
import utopia.conflict.event.CollisionInformation;
import utopia.conflict.event.CollisionListener;
import utopia.conflict.event.CollisionListeningInformation;
import utopia.conflict.util.CollisionCheck;
import utopia.genesis.event.Drawable;
import utopia.genesis.event.KeyEvent;
import utopia.genesis.event.KeyEvent.KeyEventType;
import utopia.genesis.event.KeyListener;
import utopia.genesis.util.HelpMath;
import utopia.genesis.util.Line;
import utopia.genesis.util.Transformation;
import utopia.genesis.util.Vector3D;
import utopia.inception.event.EventSelector;
import utopia.inception.util.SimpleHandled;

/**
 * This is a movable character that can collide with the walls
 * @author Mikko Hilpinen
 * @since 12.3.2015
 */
public class TestCharacter extends SimpleHandled implements
		CollisionListener, Collidable, Drawable, KeyListener
{
	// ATTRIBUTES	----------------------------
	
	private CollisionListeningInformation collisionChecker;
	private CollisionInformation collisionInformation;
	private EventSelector keyEventSelector;
	private Transformation transformation;
	private List<Vector3D> lastCollisionPoints;
	private Line lastEdge;
	
	
	// CONSTRUCTOR	----------------------------
	
	/**
	 * Creates a new character to the given position
	 * @param position The new position of the character
	 */
	public TestCharacter(Vector3D position)
	{	
		this.collisionChecker = new CollisionListeningInformation(this, true, true);
		this.collisionInformation = new CollisionInformation(null, new Vector3D(30, 0), 
				new Vector3D(-20, -20), new Vector3D(-20, 20));
		this.keyEventSelector = KeyEvent.createEventTypeSelector(KeyEventType.DOWN);
		this.transformation = new Transformation(position);
		this.lastCollisionPoints = new ArrayList<>();
		this.lastEdge = new Line(Vector3D.ZERO);
	}
	
	
	// IMPLEMENTED METHODS	-------------------

	@Override
	public CollisionInformation getCollisionInformation()
	{
		return this.collisionInformation;
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
	public EventSelector getKeyEventSelector()
	{
		return this.keyEventSelector;
	}

	@Override
	public void onKeyEvent(KeyEvent e)
	{
		double rotation = 0, speed = 0;
		
		if (e.getKey() == KeyEvent.UP)
			speed = 5;
		else if (e.getKey() == KeyEvent.DOWN)
			speed = -5;
		else if (e.getKey() == KeyEvent.LEFT)
			rotation = 3;
		else if (e.getKey() == KeyEvent.RIGHT)
			rotation = -3;
		
		rotation *= e.getDurationMillis();
		speed *= e.getDurationMillis();
		
		Transformation change = null;
		
		if (!HelpMath.areApproximatelyEqual(rotation, 0))
			change = Transformation.rotationTransformation(rotation);
		else if (!HelpMath.areApproximatelyEqual(speed, 0))
			change = Transformation.transitionTransformation(Vector3D.unitVector(
					getTransformation().getAngle()).times(speed));
		
		if (change != null)
			setTrasformation(getTransformation().plus(change));
	}

	@Override
	public void drawSelf(Graphics2D g2d)
	{
		if (getTransformation() != null)
		{
			g2d.setColor(Color.BLUE);
			AffineTransform last = g2d.getTransform();
			getTransformation().transform(g2d);
			getCollisionInformation().drawCollisionArea(g2d);
			
			g2d.setColor(Color.RED);
			
			g2d.setTransform(last);
			
			this.lastEdge.draw(g2d);
			for (Vector3D lastCollisionPosition : this.lastCollisionPoints)
			{
				lastCollisionPosition.drawAsPoint(4, g2d);
			}
		}
	}

	@Override
	public int getDepth()
	{
		return 0;
	}

	@Override
	public CollisionListeningInformation getCollisionListeningInformation()
	{
		return this.collisionChecker;
	}

	@Override
	public void onCollisionEvent(CollisionEvent event)
	{
		if (!event.getMTV().equals(Vector3D.ZERO))
		{
				//System.out.println(event.getMTV().getZDirection());
			//if (event.getMTV().equals(Vector3D.zeroVector()))
			//	return;
			
			// Calculates the collision point
			this.lastEdge = CollisionCheck.getCollisionEdge(
					getCollisionInformation().getPolygons().get(0).transformedWith(getTransformation()), 
					event.getMTV());
			/*
			this.lastCollisionPoints = CollisionChecker.getCollisionPoints(
					getCollisionInformation().getPolygons().get(0).transformedWith(
					getTransformation()), 
					event.getTarget().getCollisionInformation().getPolygons().get(0).transformedWith(
					event.getTarget().getTransformation()), event.getMTV());
			*/
			this.lastCollisionPoints = event.getCollisionPoints();
			//System.out.println(this.lastCollisionPoints.size());
			
			// Bounces away from the target
			setTrasformation(getTransformation().plus(
					Transformation.transitionTransformation(event.getMTV())));
		}
	}
}