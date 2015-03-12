package conflict_test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import genesis_event.AdvancedKeyEvent;
import genesis_event.AdvancedKeyEvent.KeyEventType;
import genesis_event.AdvancedKeyListener;
import genesis_event.Drawable;
import genesis_event.EventSelector;
import genesis_event.HandlerRelay;
import genesis_util.HelpMath;
import genesis_util.StateOperator;
import genesis_util.Vector2D;
import omega_util.SimpleGameObject;
import omega_util.Transformation;
import conflict_collision.CollisionChecker;
import conflict_collision.CollisionEvent;
import conflict_collision.CollisionInformation;
import conflict_collision.CollisionListener;

/**
 * This is a movable character that can collide with the walls
 * @author Mikko Hilpinen
 * @since 12.3.2015
 */
public class TestCharacter extends SimpleGameObject implements
		CollisionListener, Drawable, AdvancedKeyListener
{
	// ATTRIBUTES	----------------------------
	
	private CollisionChecker collisionChecker;
	private CollisionInformation collisionInformation;
	private EventSelector<AdvancedKeyEvent> keyEventSelector;
	private Transformation transformation;
	
	
	// CONSTRUCTOR	----------------------------
	
	/**
	 * Creates a new character to the given position
	 * @param handlers The handlers that will handle the character
	 * @param position The new position of the character
	 */
	public TestCharacter(HandlerRelay handlers, Vector2D position)
	{
		super(handlers);
		
		Vector2D[] vertices = {new Vector2D(30, 0), new Vector2D(-20, -20), 
				new Vector2D(-20, 20)};
		
		this.collisionChecker = new CollisionChecker(this, true);
		this.collisionInformation = new CollisionInformation(vertices);
		this.keyEventSelector = AdvancedKeyEvent.createEventTypeSelector(KeyEventType.DOWN);
		this.transformation = new Transformation(position);
	}
	
	
	// IMPLEMENTED METHODS	-------------------

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
	public EventSelector<AdvancedKeyEvent> getKeyEventSelector()
	{
		return this.keyEventSelector;
	}

	@Override
	public StateOperator getListensToKeyEventsOperator()
	{
		return getIsActiveStateOperator();
	}

	@Override
	public void onKeyEvent(AdvancedKeyEvent e)
	{
		double rotation = 0, speed = 0;
		
		if (e.getKey() == AdvancedKeyEvent.UP)
			speed = 5;
		else if (e.getKey() == AdvancedKeyEvent.DOWN)
			speed = -5;
		else if (e.getKey() == AdvancedKeyEvent.LEFT)
			rotation = 3;
		else if (e.getKey() == AdvancedKeyEvent.RIGTH)
			rotation = -3;
		
		Transformation change = null;
		
		if (!HelpMath.areApproximatelyEqual(rotation, 0))
			change = Transformation.rotationTransformation(rotation);
		else if (!HelpMath.areApproximatelyEqual(speed, 0))
			change = Transformation.transitionTransformation(Vector2D.unitVector(
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
			g2d.setTransform(last);
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
		// Not possible
	}

	@Override
	public CollisionChecker getCollisionChecker()
	{
		return this.collisionChecker;
	}

	@Override
	public void onCollisionEvent(CollisionEvent event)
	{
		// Makes sure the event is from the right perspective
		if (!event.getListener().equals(this))
			event = event.fromTargetsPointOfView();
		
		// Bounces away from the target
		setTrasformation(getTransformation().plus(
				Transformation.transitionTransformation(event.getMTV())));
	}

	@Override
	public StateOperator getListensForCollisionStateOperator()
	{
		return getIsActiveStateOperator();
	}
}