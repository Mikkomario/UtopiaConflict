package conflict_collision;

import java.util.ArrayList;
import java.util.List;

import conflict_collision.CollisionChecker.CollisionData;
import genesis_event.Actor;
import genesis_event.ActorHandler;
import genesis_event.Handled;
import genesis_event.Handler;
import genesis_event.HandlerRelay;
import genesis_event.HandlerType;
import genesis_util.StateOperator;

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
	 * @param superHandler The actorHandler that will inform the handler about step events
	 * @param relay The relay the collisionHandler and the respective collidableHandler will 
	 * be added to (optional)
	 */
	public CollisionHandler(boolean autoDeath, ActorHandler superHandler, HandlerRelay relay)
	{
		super(autoDeath);
		
		// Initializes attributes
		this.collidableHandler = new CollidableHandler();
		this.isActiveOperator = new AnyListenerIsActiveOperator();
		this.previousListeners = new ArrayList<>();
		
		if (superHandler != null)
			superHandler.add(this);
		if (relay != null)
		{
			relay.addHandler(this);
			relay.addHandler(getCollidableHandler());
		}
	}
	
	/**
	 * Creates a new handler
	 * 
	 * @param autoDeath Will the handler die once it runs out of listeners
	 * @param superHandlers The handlers that will handle this handler (actorHandler required)
	 * @param relay The relay the collisionHandler and the respective collidableHandler will 
	 * be added to (optional)
	 */
	public CollisionHandler(boolean autoDeath, HandlerRelay superHandlers, HandlerRelay relay)
	{
		super(autoDeath, superHandlers);
		
		// Initializes attributes
		this.collidableHandler = new CollidableHandler();
		this.isActiveOperator = new AnyListenerIsActiveOperator();
		this.previousListeners = new ArrayList<>();
		
		if (relay != null)
		{
			relay.addHandler(this);
			relay.addHandler(getCollidableHandler());
		}
	}
	
	/**
	 * Creates a new handler. The handler must be added to an actorHandler manually.
	 * 
	 * @param autoDeath Will the handler die once it runs out of handleds.
	 * @param relay The relay the collisionHandler and the respective collidableHandler will 
	 * be added to (optional)
	 */
	public CollisionHandler(boolean autoDeath, HandlerRelay relay)
	{
		super(autoDeath);
		
		// Initializes attributes
		this.collidableHandler = new CollidableHandler();
		this.isActiveOperator = new AnyListenerIsActiveOperator();
		this.previousListeners = new ArrayList<>();
		
		if (relay != null)
		{
			relay.addHandler(this);
			relay.addHandler(getCollidableHandler());
		}
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
				// Checks if the collision should be checked at all / who is interested in 
				// the event
				boolean currentIsInterested = 
						h.getCollisionChecker().isInterestedInCollisionsWith(previousListener) 
						&& previousListener.getCollisionInformation().allowsCollisionEventsFor(h);
				boolean previousIsInterested = 
						previousListener.getListensForCollisionStateOperator().getState() && 
						previousListener.getCollisionChecker().isInterestedInCollisionsWith(h) 
						&& h.getCollisionInformation().allowsCollisionEventsFor(previousListener);
				
				if (!currentIsInterested && !previousIsInterested)
					continue;
				
				// Checks what information the listeners want to receive
				boolean mtvRequired = false;
				boolean pointsRequired = false;
				if ((currentIsInterested && 
						h.getCollisionChecker().collisionPointsShouldBeCalculated()) || 
						(previousIsInterested && 
						previousListener.getCollisionChecker().collisionPointsShouldBeCalculated()))
				{
					mtvRequired = true;
					pointsRequired = true;
				}
				else if ((currentIsInterested && h.getCollisionChecker().mtvShouldBeCalculated()) || 
						(previousIsInterested && 
						previousListener.getCollisionChecker().mtvShouldBeCalculated()))
					mtvRequired = true;
				
				// Checks for collisions
				CollisionData data = h.getCollisionChecker().checkForCollisionsWith(
						previousListener, mtvRequired, pointsRequired);
				// If there was a collision informs the interested participants
				if (data.collided())
				{
					CollisionEvent event = new CollisionEvent(h, previousListener, data, 
							this.lastDuration);
				
					if (currentIsInterested)
						h.onCollisionEvent(event);
					if (previousIsInterested)
					{
						if (!currentIsInterested)
							event = event.fromTargetsPointOfView();
						previousListener.onCollisionEvent(event);
					}
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
	
	
	// GETTERS & SETTERS	--------------------
	
	/**
	 * @return The collidableHandler used by this collision handler. The collidableHandler 
	 * handles collidable objects that are not collision listeners
	 */
	public CollidableHandler getCollidableHandler()
	{
		return this.collidableHandler;
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
	
	/**
	 * CollidableHandler keeps track of all the collidables that can collide with the 
	 * collision listeners
	 * @author Mikko Hilpinen
	 * @since 12.3.2015
	 */
	public class CollidableHandler extends Handler<Collidable>
	{
		// ATTRIBUTES	-----------------------------
		
		private CollisionListener lastListener;
		private double lastDuration;
		
		
		// CONSTRUCTOR	-----------------------------
		
		private CollidableHandler()
		{
			super(false);
			
			CollisionHandler.this.getIsDeadStateOperator().getListenerHandler().add(this);
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
			if (!h.getCanBeCollidedWithStateOperator().getState())
				return true;
			
			if (!h.getCollisionInformation().allowsCollisionEventsFor(this.lastListener) || 
					!this.lastListener.getCollisionChecker().isInterestedInCollisionsWith(h))
				return true;
			
			// Checks which data should be collected
			boolean mtvRequired = false;
			boolean pointsRequired = false;
			if (this.lastListener.getCollisionChecker().collisionPointsShouldBeCalculated())
			{
				mtvRequired = true;
				pointsRequired = true;
			}
			else if (this.lastListener.getCollisionChecker().mtvShouldBeCalculated())
				mtvRequired = true;
			
			// Checks for collisions between the collidable and the collision listener
			CollisionData data = 
					this.lastListener.getCollisionChecker().checkForCollisionsWith(h, 
					mtvRequired, pointsRequired);
			
			// If there was a collision, informs the listener
			if (data.collided())
				this.lastListener.onCollisionEvent(new CollisionEvent(this.lastListener, h, 
						data, this.lastDuration));
			
			return true;
		}
		
		@Override
		public void onStateChange(StateOperator source, boolean newState)
		{
			super.onStateChange(source, newState);
			
			// In addition to the normal state checking, dies if the collision handler dies
			if (source == CollisionHandler.this.getIsDeadStateOperator() && newState)
				getIsDeadStateOperator().setState(true);
		}
		
		@Override
		public void add(Collidable c)
		{
			// Doesn't accept collisionListeners
			if (c instanceof CollisionListener)
				CollisionHandler.this.add(c);
			else
				super.add(c);
		}
		
		
		// OTHER METHODS	------------------------
		
		private void checkForCollisionsWith(CollisionListener listener, double duration)
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
