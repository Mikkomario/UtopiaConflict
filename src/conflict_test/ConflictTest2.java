package conflict_test;

import conflict_collision.CollisionHandler;
import genesis_event.ActorHandler;
import genesis_event.DrawableHandler;
import genesis_event.HandlerRelay;
import genesis_event.KeyListenerHandler;
import genesis_util.Vector3D;
import genesis_video.GamePanel;
import genesis_video.GameWindow;

/**
 * This class tests conflict features in a simple game-like environment
 * @author Mikko Hilpinen
 * @since 12.3.2015
 */
public class ConflictTest2
{
	// CONSTRUCTOR	------------------------------
	
	private ConflictTest2()
	{
		// The interface is static
	}

	
	// MAIN METHOD	------------------------------
	
	/**
	 * Starts the program
	 * @param args not used
	 */
	public static void main(String[] args)
	{
		// Creates the window
		GameWindow window = new GameWindow(new Vector3D(800, 600), "Conflict test 2", true, 
				120, 20);
		GamePanel panel = window.getMainPanel().addGamePanel();
		
		// Creates the handlers
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(new DrawableHandler(false, panel.getDrawer()));
		handlers.addHandler(new ActorHandler(false, window.getHandlerRelay()));
		handlers.addHandler(new KeyListenerHandler(false, window.getHandlerRelay()));
		new CollisionHandler(false, window.getHandlerRelay(), handlers);
		
		// Creates the objects
		new TestCharacter(handlers, new Vector3D(400, 300));
		new TestWall(handlers, Vector3D.zeroVector(), new Vector3D(50, 400));
		new TestWall(handlers, new Vector3D(600, 200), new Vector3D(75, 75));
	}
}
