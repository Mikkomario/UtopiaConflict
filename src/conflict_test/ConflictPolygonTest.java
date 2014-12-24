package conflict_test;

import omega_util.Transformation;
import conflict_collision.CollisionHandler;
import genesis_event.ActorHandler;
import genesis_event.DrawableHandler;
import genesis_event.HandlerRelay;
import genesis_event.MouseListenerHandler;
import genesis_test.TextPerformanceMonitor;
import genesis_util.Vector2D;
import genesis_video.GamePanel;
import genesis_video.GameWindow;

/**
 * This class tests the collision methods of polygons
 * 
 * @author Mikko Hilpinen
 * @since 9.12.2014
 */
public class ConflictPolygonTest
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
		GameWindow window = new GameWindow(new Vector2D(800, 600), "PolygonTest", true, 
				120, 20);
		GamePanel panel = window.getMainPanel().addGamePanel();
		
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(new DrawableHandler(true, false, 0, 1, panel.getDrawer()));
		handlers.addHandler(new MouseListenerHandler(true, window.getHandlerRelay()));
		handlers.addHandler(new ActorHandler(true, window.getHandlerRelay()));
		handlers.addHandler(new CollisionHandler(true, handlers));
		
		// Creates polygons
		TestPolygonObject o = new MouseRotatingTestPolygonObject(5, handlers);
		o.setTrasformation(Transformation.transitionTransformation(new Vector2D(200, 200)));
		
		new TestMousePositionPolygonObject(3, handlers, o);
		
		TestPolygonObject o3 = new TestEscapingPolygonObject(6, handlers);
		o3.setTrasformation(Transformation.transitionTransformation(
				new Vector2D(400, 300)).withScaling(new Vector2D(0.5, 0.5)));
		
		TestPolygonObject o4 = new TestEscapingPolygonObject(7, handlers);
		o4.setTrasformation(Transformation.transitionTransformation(
				new Vector2D(600, 400)).withScaling(new Vector2D(0.5, 0.5)));
		
		Vector2D[] vertices = {new Vector2D(50, -50), new Vector2D(-50, -50), 
				/*new Vector2D(-25, 0),*/ new Vector2D(-50, 50), new Vector2D(50, 50), 
				new Vector2D(25, 0)};
		RandomConvexPolygonFactory.createPolygons(handlers, vertices, new Vector2D(700, 100));
		
		// Checks performance as well
		new TextPerformanceMonitor(1000, window.getStepHandler());
	}
}
