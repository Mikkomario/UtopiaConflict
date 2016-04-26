package utopia.conflict.test;

import utopia.conflict.event.CollidableHandler;
import utopia.conflict.event.CollisionHandler;
import utopia.genesis.event.StepHandler;
import utopia.genesis.util.Vector3D;
import utopia.genesis.video.GamePanel;
import utopia.genesis.video.GameWindow;
import utopia.genesis.video.GamePanel.ScalingPolicy;
import utopia.genesis.video.SplitPanel.ScreenSplit;
import utopia.genesis.video.WindowKeyListenerHandler;
import utopia.inception.handling.HandlerRelay;

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
		Vector3D resolution = new Vector3D(800, 600);
		
		// Creates the window
		GameWindow window = new GameWindow(resolution.toDimension(), "Conflict test 2", false, 
				true, ScreenSplit.HORIZONTAL);
		GamePanel panel = new GamePanel(resolution, ScalingPolicy.PROJECT, 120);
		window.addGamePanel(panel);
		
		// Creates the handlers
		StepHandler stepHandler = new StepHandler(120, 20);
		CollidableHandler collidableHandler = new CollidableHandler();
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(stepHandler, panel.getDrawer(), 
				WindowKeyListenerHandler.createWindowKeyListenerHandler(window, stepHandler), 
				collidableHandler, 
				CollisionHandler.createCollisionHandler(collidableHandler, stepHandler));
		
		// Creates the objects
		handlers.add(new TestCharacter(new Vector3D(400, 300)),
				new TestWall(Vector3D.ZERO, new Vector3D(50, 400)),
				new TestWall(new Vector3D(600, 200), new Vector3D(75, 75)), 
				new TestWall(new Vector3D(50, 600), 96));
		
		stepHandler.start();
	}
}
