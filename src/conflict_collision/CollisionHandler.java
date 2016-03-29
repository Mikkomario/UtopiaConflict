package conflict_collision;

import java.util.ArrayList;
import java.util.List;

import conflict_collision.CollisionChecker.CollisionData;
import utopia.genesis.event.Actor;
import utopia.inception.handling.Handler;
import utopia.inception.handling.HandlerType;

/**
 * CollisionHandlers inform collision listeners about collision events. They also keep track 
 * of all the collidables those listeners can collide with.
 * @author Mikko Hilpinen
 * @since 21.12.2014
 */
public class CollisionHandler extends Handler<CollisionListener> implements Actor
{
	// ATTRIBUTES	----------------------------
	
	private CollidableHandler collidableHandler;
	private double lastDuration;
	private List<CollisionListener> previousListeners; // TODO: Move to a previous operator
	
	
	// CONSTRUCTOR	---------------------------
	
	/**
	 * Creates a new handler. Remember to add the handler to a sufficient actor handler
	 * @param collidableHandler The collidable handler that keeps track of collidable objects
	 */
	public CollisionHandler(CollidableHandler collidableHandler)
	{
		// Initializes attributes
		this.collidableHandler = collidableHandler;
	}
	
	
	// IMPLEMENTED METHODS	--------------------

	@Override
	public void act(double duration)
	{
		// Only works if the collidableHandler is still alive
		if (!this.collidableHandler.getIsDeadStateOperator().getState())
		{
			// Checks for collisions
			// TODO: Use both operations here
			this.lastDuration = duration;
			handleObjects(false);
			this.previousListeners.clear();
		}
	}

	@Override
	public HandlerType getHandlerType()
	{
		return ConflictHandlerType.COLLISIONHANDLER;
	}

	@Override
	protected boolean handleObject(CollisionListener h)
	{
		// Only active listeners are informed about events
		if (h.getHandlingOperators().getShouldBeHandledOperator(getHandlerType()).getState())
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
		
		// Even inactive listeners can be collided with though
		this.previousListeners.add(h);
		return true;
	}
	
	
	// GETTERS & SETTERS	--------------------
	
	/**
	 * @return The collidableHandler used by this collision handler. The collidableHandler 
	 * handles collidable objects that are not collision listeners
	 */
	private CollidableHandler getCollidableHandler()
	{
		return this.collidableHandler;
	}
	
	
	// NESTED CLASSES	---------------------
	
	private class CheckCollisionsBetweenListenersOperator extends Handler<CollisionListener>.HandlingOperator
	{
		// ATTIBUTES	---------------------
		
		private List<Collidable> checkedTargets = new ArrayList<>();
		private CollisionListener listener;
		
		
		// CONSTRUCTOR	---------------------
		
		public CheckCollisionsBetweenListenersOperator(CollisionListener listener)
		{
			this.listener = listener;
		}
		
		
		// IMPLEMENTED METHODS	-------------
		
		@Override
		protected boolean handleObject(CollisionListener h)
		{
			// Only checks listeners that can be collided with
			if (h instanceof Collidable)
			{
				Collidable target = (Collidable) h;
				this.checkedTargets.add(target);
				
				if (!target.getHandlingOperators().getShouldBeHandledOperator(
						ConflictHandlerType.COLLIDABLEHANLDER).getState() || 
						target.getCollisionInformation() == null)
					return true;
				
				// Checks whether the two instances accept each other as targets
				
				/*
				 * // Checks for collisions with the already handled listeners
			for (CollisionListener previousListener : this.previousListeners)
			{
				// Checks if the collision should be checked at all / who is interested in 
				// the event
				boolean currentIsInterested = 
						h.getCollisionChecker().isInterestedInCollisionsWith(previousListener) 
						&& previousListener.getCollisionInformation().allowsCollisionEventsFor(h);
				boolean previousIsInterested =  
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
				 */
				
				return false;
			}
			else
				return true;
		}
	}
}
