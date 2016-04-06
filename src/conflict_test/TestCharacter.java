package conflict_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import conflict_collision.Collidable;
import conflict_collision.CollisionChecker;
import conflict_collision.CollisionEvent;
import conflict_collision.CollisionInformation;
import conflict_collision.CollisionListener;
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
	
	private CollisionChecker collisionChecker;
	private CollisionInformation collisionInformation;
	private EventSelector<KeyEvent> keyEventSelector;
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
		Vector3D[] vertices = {new Vector3D(30, 0), new Vector3D(-20, -20), 
				new Vector3D(-20, 20)};
		
		this.collisionChecker = new CollisionChecker(this, true, true);
		this.collisionInformation = new CollisionInformation(vertices);
		this.keyEventSelector = KeyEvent.createEventTypeSelector(KeyEventType.DOWN);
		this.transformation = new Transformation(position);
		this.lastCollisionPoints = new ArrayList<>();
		this.lastEdge = new Line(Vector3D.zeroVector());
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
	public EventSelector<KeyEvent> getKeyEventSelector()
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
		
		rotation *= e.getDuration();
		speed *= e.getDuration();
		
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
			
			g2d.setColor(Color.GRAY);
			int r = (int) getCollisionInformation().getRadius();
			g2d.drawOval(-r, -r, r * 2, r * 2);
			
			g2d.setColor(Color.RED);
			
			g2d.setTransform(last);
			
			this.lastEdge.draw(g2d);
			for (Vector3D lastCollisionPosition : this.lastCollisionPoints)
			{
				g2d.drawOval(lastCollisionPosition.getFirstInt() - 2, 
						lastCollisionPosition.getSecondInt() - 2, 4, 4);
			}
		}
	}

	@Override
	public int getDepth()
	{
		return 0;
	}

	@Override
	public CollisionChecker getCollisionChecker()
	{
		return this.collisionChecker;
	}

	@Override
	public void onCollisionEvent(CollisionEvent event)
	{
		//if (event.getMTV().equals(Vector3D.zeroVector()))
		//	return;
		
		// Calculates the collision point
		//this.lastEdge = CollisionChecker.getCollisionEdge(getCollisionInformation().getPolygons().get(0).transformedWith(getTransformation()), event.getMTV());
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