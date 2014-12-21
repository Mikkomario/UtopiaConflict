package conflict_collision;

import java.util.ArrayList;
import java.util.List;

import genesis_event.Actor;
import genesis_event.ActorHandler;
import genesis_event.Handled;
import genesis_event.Handler;
import genesis_event.HandlerRelay;
import genesis_event.HandlerType;
import genesis_util.StateOperator;
import genesis_util.Vector2D;

/**
 * CollisionHandlers inform collision listeners about collision events. They also keep track 
 * of all the collidables those listeners can collide with.
 * 
 * @author Mikko Hilpinen
 * @since 21.12.2014
 */
public class CollisionHandler extends Handler<CollisionListener> implements Actor
{
	// ATTRIBUTES	----------------------------
	
	private CollidableHandler collidableHandler;
	private double lastDuration;
	private StateOperator isActiveOperator;
	private List<CollisionListener> previousListeners;
	
	
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new handler
	 * 
	 * @param autoDeath Will the handler die once it runs out of listeners
	 * @param actorHandler The actorHandler that will inform the handler about step events
	 */
	public CollisionHandler(boolean autoDeath, ActorHandler actorHandler)
	{
		super(autoDeath);
		
		// Initializes attributes
		this.collidableHandler = new CollidableHandler(this);
		this.isActiveOperator = new AnyListenerIsActiveOperator();
		this.previousListeners = new ArrayList<>();
		
		if (actorHandler != null)
			actorHandler.add(this);
	}
	
	/**
	 * Creates a new handler
	 * 
	 * @param autoDeath Will the handler die once it runs out of listeners
	 * @param superHandlers The handlers that will handle this handler
	 */
	public CollisionHandler(boolean autoDeath, HandlerRelay superHandlers)
	{
		super(autoDeath, superHandlers);
		
		// Initializes attributes
		this.collidableHandler = new CollidableHandler(this);
		this.isActiveOperator = new AnyListenerIsActiveOperator();
		this.previousListeners = new ArrayList<>();
	}
	
	/**
	 * Creates a new handler. The handler must be added to an actorHandler manually.
	 * 
	 * @param autoDeath Will the handler die once it runs out of handleds.
	 */
	public CollisionHandler(boolean autoDeath)
	{
		super(autoDeath);
		
		// Initializes attributes
		this.collidableHandler = new CollidableHandler(this);
		this.isActiveOperator = new AnyListenerIsActiveOperator();
		this.previousListeners = new ArrayList<>();
	}
	
	
	// IMPLEMENTED METHODS	--------------------

	@Override
	public void act(double duration)
	{
		// Only works if the collidableHandler is still alive
		if (!this.collidableHandler.getIsDeadStateOperator().getState())
		{
			// Checks for collisions
			this.lastDuration = duration;
			handleObjects();
			this.previousListeners.clear();
		}
	}

	@Override
	public StateOperator getIsActiveStateOperator()
	{
		return this.isActiveOperator;
	}

	@Override
	public HandlerType getHandlerType()
	{
		return ConflictHandlerType.COLLISIONHANDLER;
	}

	@Override
	protected boolean handleObject(CollisionListener h)
	{
		// Only active listeners are informed
		if (h.getListensForCollisionStateOperator().getState())
		{
			// Checks for collisions with the already handled listeners
			for (CollisionListener previousListener : this.previousListeners)
			{
				CollisionEvent event = null;
				
				// Checks if either of the listeners wants to use the mtv
				if (h.getCollisionChecker().mtvShouldBeCalculated() || 
						previousListener.getCollisionChecker().mtvShouldBeCalculated())
				{
					Vector2D mtv = h.getCollisionChecker().objectCollidesMTV(previousListener);
					
					if (mtv != null)
						event = new CollisionEvent(h, previousListener, mtv, this.lastDuration);
				}
				else if (h.getCollisionChecker().objectCollides(previousListener))
					event = new CollisionEvent(h, previousListener, null, this.lastDuration);
				
				if (event != null)
				{
					h.onCollisionEvent(event);
					if (previousListener.getListensForCollisionStateOperator().getState())
						previousListener.onCollisionEvent(event);
				}
			}
			
			// Also checks for collisions with other collidables
			this.collidableHandler.checkForCollisionsWith(h, this.lastDuration);
		}
			
		this.previousListeners.add(h);
		return true;
	}
	
	@Override
	public void removeHandled(Handled c)
	{
		if (c instanceof CollisionListener)
			super.removeHandled(c);
		else
			this.collidableHandler.removeHandled(c);
	}
	
	@Override
	public void removeAllHandleds()
	{
		super.removeAllHandleds();
		this.collidableHandler.removeAllHandleds();
	}
	
	
	// OTHER METHODS	------------------------
	
	/**
	 * Adds a new collidable or a collision listener to the handler
	 * @param c The collidable / collision listener that will be added to the handler
	 */
	public void add(Collidable c)
	{
		if (c instanceof CollisionListener)
			super.add((CollisionListener) c);
		else
			this.collidableHandler.add(c);
	}
	
	/**
	 * Transfers all the collision listeners and collidables from the other collision handler 
	 * to this collision handler
	 * @param other The collision handler that will be emptied
	 */
	public void transferHandledsFrom(CollisionHandler other)
	{
		super.transferHandledsFrom(other);
		this.collidableHandler.transferHandledsFrom(other.collidableHandler);
	}

	
	// SUBCLASSES	----------------------------
	
	private static class CollidableHandler extends Handler<Collidable>
	{
		// ATTRIBUTES	-----------------------------
		
		private CollisionListener lastListener;
		private double lastDuration;
		private CollisionHandler masterHandler;
		
		
		// CONSTRUCTOR	-----------------------------
		
		public CollidableHandler(CollisionHandler masterHandler)
		{
			super(false);
			
			this.masterHandler = masterHandler;
			this.masterHandler.getIsDeadStateOperator().getListenerHandler().add(this);
		}
		
		
		// IMPLEMENTED METHODS	---------------------

		@Override
		public HandlerType getHandlerType()
		{
			return ConflictHandlerType.COLLIDABLEHANLDER;
		}

		@Override
		protected boolean handleObject(Collidable h)
		{
			// Checks if the two objects accept each other as collided objects (= is collision 
			// checking necessary)
			if (!h.getCanBeCollidedWithOperator().getState())
				return true;
			
			if (!h.getCollisionInformation().allowsCollisionEventsFor(this.lastListener) || 
					!this.lastListener.getCollisionChecker().isInterestedInCollisionsWith(h))
				return true;
			
			// Checks for collisions between the collidable and the collision listener
			// Calculates the mtv if necessary
			if (this.lastListener.getCollisionChecker().mtvShouldBeCalculated())
			{
				Vector2D mtv = this.lastListener.getCollisionChecker().objectCollidesMTV(h);
				
				if (mtv != null)
					this.lastListener.onCollisionEvent(
							new CollisionEvent(this.lastListener, h, mtv, this.lastDuration));
			}
			else if (this.lastListener.getCollisionChecker().objectCollides(h))
				this.lastListener.onCollisionEvent(new CollisionEvent(this.lastListener, h, 
						null, this.lastDuration));
			
			return true;
		}
		
		@Override
		public void onStateChange(StateOperator source, boolean newState)
		{
			super.onStateChange(source, newState);
			
			// In addition to the normal state checking, dies if the collision handler dies
			if (source == this.masterHandler.getIsDeadStateOperator() && newState)
				getIsDeadStateOperator().setState(true);
		}
		
		
		// OTHER METHODS	------------------------
		
		public void checkForCollisionsWith(CollisionListener listener, double duration)
		{
			this.lastDuration = duration;
			this.lastListener = listener;
			handleObjects();
			this.lastListener = null;
		}
	}
	
	private class AnyListenerIsActiveOperator extends ForAnyHandledsOperator
	{
		// CONSTRUCTOR	-------------------------
		
		public AnyListenerIsActiveOperator()
		{
			super(true);
		}
		
		
		// IMPLEMENTED METHODS	-------------------

		@Override
		protected void changeHandledState(CollisionListener c, boolean newState)
		{
			c.getListensForCollisionStateOperator().setState(newState);
		}

		@Override
		protected boolean getHandledState(CollisionListener c)
		{
			return c.getListensForCollisionStateOperator().getState();
		}
	}
}
