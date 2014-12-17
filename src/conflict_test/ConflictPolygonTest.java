package conflict_test;

import exodus_util.Transformation;
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
		GameWindow window = new GameWindow(new Vector2D(600, 400), "PolygonTest", true, 
				120, 20);
		GamePanel panel = window.getMainPanel().addGamePanel();
		
		HandlerRelay handlers = new HandlerRelay();
		handlers.addHandler(new DrawableHandler(true, false, 0, 1, panel.getDrawer()));
		handlers.addHandler(new MouseListenerHandler(true, window.getHandlerRelay()));
		
		// Creates a polygon
		TestPolygonObject o = new MouseRotatingTestPolygonObject(5, handlers);
		o.setTrasformation(Transformation.transitionTransformation(new Vector2D(200, 200)));
		new TestMousePositionPolygonObject(3, handlers, o);
		
		// Checks performance as well
		new TextPerformanceMonitor(1000, window.getStepHandler());
	}
}
