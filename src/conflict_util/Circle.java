package conflict_util;

import utopia.genesis.util.HelpMath;
import utopia.genesis.util.Line;
import utopia.genesis.util.Transformation;
import utopia.genesis.util.Vector3D;

/**
 * Circles can be used in collision checking as well. A circle contains a center point and 
 * a radius. When dealing with ellipsoid (transformed circles), a polygon interface should be 
 * used instead
 * @author Mikko Hilpinen
 * @since 8.4.2016
 */
public class Circle
{
	// ATTRIBUTES	--------------
	
	private Vector3D center;
	private double radius;
	
	
	// CONSTRUCTOR	--------------
	
	/**
	 * Creates a new circle
	 * @param center The circle's center point
	 * @param radius The circle's radius
	 */
	public Circle(Vector3D center, double radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	/**
	 * Copies another circle
	 * @param other another circle
	 */
	public Circle(Circle other)
	{
		this.center = other.center;
		this.radius = other.radius;
	}
	
	
	// IMPLEMENTED METHODS	-----
	
	@Override
	public String toString()
	{
		return getCenter() + ";" + getRadius();
	}

	
	// ACCESSORS	-------------
	
	/**
	 * @return The center point of the circle
	 */
	public Vector3D getCenter()
	{
		return this.center;
	}
	
	/**
	 * @return The radius of the circle in pixels
	 */
	public double getRadius()
	{
		return this.radius;
	}
	
	
	// OTHER METHODS	---------
	
	/**
	 * Transforms the circle from relative space to absolute space. The radius of the circle 
	 * is scaled, but the circle can't be skewed or become ellipsoid.
	 * @param transformation The transformation applied to the circle
	 * @return The transformed circle
	 * @see #supportsTransformation(Transformation)
	 */
	public Circle transformedWith(Transformation transformation)
	{
		// Transforms the center coordinate
		Vector3D transformedCenter = transformation.transform(getCenter());
		
		// Scales the radius as well
		double scaledRadius = getRadius() * (transformation.getScaling().getFirst() + 
				transformation.getScaling().getSecond()) / 2;
		
		return new Circle(transformedCenter, scaledRadius);
	}
	
	/**
	 * Creates a new polygon based on this circle
	 * @param minVertexAmount The minimum amount of vertices the polygon must have. Use a 
	 * negative number for no limit.
	 * @param maxEdgeLength How long can one polygon edge get at maximum (in pixels). Use 
	 * a negative number for no limit.
	 * @return A polygon representation of this circle
	 */
	public Polygon toPolygon(int minVertexAmount, int maxEdgeLength)
	{
		// Calculates the angle increment between the vertices
		/*
		 * Each increment creates a sector triangle (r, r, d), which can be split into two 
		 * right-angled triangles (?, d/2, r). Calculating the angle (a in first triangle, 
		 * a/2 in the second):
		 * sin(a/2) = (d/2)/r
		 * sin(a/2) = d/2r
		 * a/2 = asin(d/2r)
		 * a = 2*asin(d/2r)
		 */
		double angleIncrement = -1;
		if (maxEdgeLength > 0 && maxEdgeLength <= getRadius() * 2)
			angleIncrement = 2 * Math.toDegrees(Math.asin(maxEdgeLength / (2 * getRadius())));
		
		// The minimum vertex amount is taken into account as well
		double maxAngleIncrement = -1;
		if (minVertexAmount > 0)
			maxAngleIncrement = 360.0 / minVertexAmount;
		
		// If neither of the values was provided, uses the default 45 degrees per edge
		if (angleIncrement < 0 && maxAngleIncrement < 0)
			angleIncrement = 45.0;
		else if (maxAngleIncrement > 0 && 
				(angleIncrement > maxAngleIncrement || angleIncrement < 0))
			angleIncrement = maxAngleIncrement;
		
		// Creates the vertices for the polygon
		int vertexAmount = (int) (360 / angleIncrement);
		Vector3D[] vertices = new Vector3D[vertexAmount];
		for(int i = 0; i < vertexAmount; i++)
		{
			vertices[i] = getCenter().plus(HelpMath.lenDir(getRadius(), i * angleIncrement));
		}
		
		return new Polygon(vertices);
	}
	
	/**
	 * Checks whether a point lies inside the circle
	 * @param point A point
	 * @return Does the point lie within the circle
	 */
	public boolean pointIsWithin(Vector3D point)
	{
		return HelpMath.pointDistance2D(point, getCenter()) <= getRadius();
	}
	
	/**
	 * Projects the circle into a line on the provided axis
	 * @param axis The axis the circle is projected to
	 * @return The projection of the circle on the provided axis
	 */
	public Line getProjection(Vector3D axis)
	{
		// Projects the max and min point of this circle to the axis
		Vector3D min = getCenter().minus(axis.withLength(getRadius()));
		Vector3D max = getCenter().plus(axis.withLength(getRadius()));
		
		return new Line(min.vectorProjection(axis), max.vectorProjection(axis));
	}
	
	/**
	 * Checks whether the circle can be transformed without a problem. When transforming 
	 * circles with shear or uneven scale transformations, the end results may vary.
	 * @param transformation A transformation
	 * @return Can the circle be reliably transformed using the provided transformation. If the 
	 * transformation is not supported, one should consider casting the circle into a polygon 
	 * first
	 */
	public static boolean supportsTransformation(Transformation transformation)
	{
		return HelpMath.areApproximatelyEqual(transformation.getScaling().getFirst(), 
				transformation.getScaling().getSecond()) && 
				transformation.getShear().equalsIn2D(Vector3D.zeroVector());
	}
}
