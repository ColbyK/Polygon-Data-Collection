import java.awt.Polygon;

public class FloatPoly {
	public float xPoints[];
	public float yPoints[];
	public float xMin;
	public float xMax;
	public float yMin;
	public float yMax;
	public Polygon polyInt;
	public FloatPoly() {
		xPoints = new float[0];
		yPoints = new float[0];
		xMin = Float.MAX_VALUE;
		xMax = Float.MIN_VALUE;
		yMin = Float.MAX_VALUE;
		yMax = Float.MIN_VALUE;
		polyInt = new Polygon();
	}
	public void addPoint(float x, float y) {
		float[] xTemp = new float[xPoints.length+1];
		float[] yTemp = new float[yPoints.length+1];
		for(int i = 0; i < xPoints.length; i++) {
			xTemp[i] = xPoints[i];
			yTemp[i] = yPoints[i];
			updateAddExtremes(xPoints[i],yPoints[i]);
		}
		xTemp[xTemp.length-1] = x;
		yTemp[yTemp.length-1] = y;
		updateAddExtremes(x,y);
		polyInt.addPoint((int)x,(int)y);
		xPoints = xTemp;
		yPoints = yTemp;
	}
	public void removeLastPoint() {
		if(xPoints.length >= 1) {
			float[] xTemp = new float[xPoints.length-1];
			float[] yTemp = new float[yPoints.length-1];
			for(int i = 0; i < xPoints.length-1; i++) {
				xTemp[i] = xPoints[i];
				yTemp[i] = yPoints[i];
			}
			updateAllExtremes();
			xPoints = xTemp;
			yPoints = yTemp;
			Polygon tempPoly = new Polygon();
			for(int i = 0; i < xPoints.length-1; i++) {
				tempPoly.addPoint((int)xPoints[i], (int)yPoints[i]);
			}
			polyInt = tempPoly;
		}
	}
	public void convertPoly(int[] xPs, int[] yPs) {
		xPoints = new float[xPs.length];
		yPoints = new float[yPs.length];
		for(int i = 0; i < xPoints.length; i++) {
			xPoints[i] = xPs[i];
			yPoints[i] = yPs[i];
			updateAddExtremes(xPoints[i],yPoints[i]);
			polyInt.addPoint(xPs[i], xPs[i]);
		}
	}
	public void updateAddExtremes(float x, float y) {
		xMin = Math.min(x, xMin);
		xMax = Math.max(x, xMax);
		yMin = Math.min(y, yMin);
		yMax = Math.max(y, yMax);
	}
	public void updateAllExtremes() {
		xMin = Float.MAX_VALUE;
		xMax = Float.MIN_VALUE;
		yMin = Float.MAX_VALUE;
		yMax = Float.MIN_VALUE;
		for(int i = 0; i < xPoints.length; i++) {
			updateAddExtremes(xPoints[i], yPoints[i]);
		}
	}
	public int getLastX() {
		if(xPoints.length > 0)
			return (int)xPoints[xPoints.length-1];
		return -1;
	}
	public int getLastY() {
		if(yPoints.length > 0)
			return (int)yPoints[yPoints.length-1];
		return -1;
	}
	public int getFirstX() {
		if(xPoints.length > 0)
			return (int)xPoints[0];
		return -1;
	}
	public int getFirstY() {
		if(yPoints.length > 0)
			return (int)yPoints[0];
		return -1;
	}
}
