package conflict_test;

import conflict_collision.CollidableHandler;
import conflict_collision.CollisionHandler;
import utopia.genesis.event.StepHandler;
import utopia.genesis.util.Transformation;
import utopia.genesis.util.Vector3D;
import utopia.genesis.video.GamePanel;
import utopia.genesis.video.GameWindow;
import utopia.genesis.video.PanelMouseListenerHandler;
import utopia.genesis.video.GamePanel.ScalingPolicy;
import utopia.genesis.video.SplitPanel.ScreenSplit;
import utopia.inception.handling.Handled;
import utopia.inception.handling.HandlerRelay;

/**
 * This class tests the collision methods of polygons
 * @author Mikko Hilpinen
 * @since 9.12.2014
 */
class ConflictPolygonTest
{
	// CONSTRUCTOR	------------------------
	
	private ConflictPolygonTest()
	{
		// The constructor is hidden since the interface is static
	}

	
	// MAIN METHOD	-------------------------
	
	/**
	 * Starts the test
	 * @param args not used
	 */
	public static void main(String[] args)
	{
		// Creates the window
		Vector3D resolution = new Vector3D(800, 600);
		GameWindow window = new GameWindow(resolution.toDimension(), "PolygonTest", false, false, 
				ScreenSplit.HORIZONTAL);
		
		// Creates the panel
		GamePanel panel = new GamePanel(resolution, ScalingPolicy.PROJECT, 120);
		window.addGamePanel(panel);
		
		// Creates the handlers
		StepHandler stepHandler = new StepHandler(120, 20);
		PanelMouseListenerHandler mouseHandler = new PanelMouseListenerHandler(panel, false);
		stepHandler.add(mouseHandler);
		CollidableHandler collidableHandler = new CollidableHandler();
		CollisionHandler collisionHandler = new CollisionHandler(collidableHandler);
		stepHandler.add(collisionHandler);
		
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(stepHandler, mouseHandler, collidableHandler, collisionHandler, 
				panel.getDrawer());
		
		// Creates polygons
		TestPolygonObject o = new MouseRotatingTestPolygonObject(5);
		o.setTrasformation(Transformation.transitionTransformation(new Vector3D(200, 200)));
		handlers.add(o);
		
		handlers.add(new TestMousePositionPolygonObject(3, o));
		
		TestPolygonObject o3 = new TestEscapingPolygonObject(6);
		o3.setTrasformation(Transformation.transitionTransformation(
				new Vector3D(400, 300)).withScaling(new Vector3D(0.5, 0.5)));
		
		TestPolygonObject o4 = new TestEscapingPolygonObject(7);
		o4.setTrasformation(Transformation.transitionTransformation(
				new Vector3D(600, 400)).withScaling(new Vector3D(0.5, 0.5)));
		
		handlers.add(o3, o4);
		
		Vector3D[] vertices = {new Vector3D(50, -50), new Vector3D(-50, -50), 
				/*new Vector3D(-25, 0),*/ new Vector3D(-50, 50), new Vector3D(50, 50), 
				new Vector3D(25, 0)};
		handlers.add(RandomConvexPolygonFactory.createPolygons(vertices, 
				new Vector3D(700, 100)).toArray(new Handled[0]));
		
		// Starts the game
		stepHandler.start();
	}
}
