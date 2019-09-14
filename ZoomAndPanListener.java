import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * Listener that can be attached to a Component to implement Zoom and Pan functionality.
 *
 * Edited by Colby Kuhnel
 *
 * @author Sorin Postelnicu
 * @since Jul 14, 2009
 * 
 */
public class ZoomAndPanListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    public static final int DEFAULT_MIN_ZOOM_LEVEL = -20;
    public static final int DEFAULT_MAX_ZOOM_LEVEL = 3;
    public static final double DEFAULT_ZOOM_MULTIPLICATION_FACTOR = 1.2;

    private Component targetComponent;

    private int zoomLevel = 0;
    private int minZoomLevel = DEFAULT_MIN_ZOOM_LEVEL;
    private int maxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
    private double zoomMultiplicationFactor = DEFAULT_ZOOM_MULTIPLICATION_FACTOR;
    
    private Point dragStartScreen;
    private Point dragEndScreen;
    private AffineTransform coordTransform = new AffineTransform();
    private Integer minXTransition;
    private Integer minYTransition;
    private Integer maxXTransition;
    private Integer maxYTransition;
    public int screenX;
    public int screenY;
    public boolean dragPressed;

    public ZoomAndPanListener(Component targetComponent) {
        this.targetComponent = targetComponent;
    }
    
    public ZoomAndPanListener(Component targetComponent, int minXTransition, int minYTransition, int maxXTransition, int maxYTransition, int screenX, int screenY) {
        this.targetComponent = targetComponent;
        this.minXTransition = minXTransition;
        this.minYTransition = minYTransition;
        this.maxXTransition = maxXTransition;
        this.maxYTransition = maxYTransition;
        this.screenX = screenX;
        this.screenY = screenY;
        dragPressed = false;
    }
    
    public ZoomAndPanListener(Component targetComponent, int minZoomLevel, int maxZoomLevel, double zoomMultiplicationFactor) {
        this.targetComponent = targetComponent;
        this.minZoomLevel = minZoomLevel;
        this.maxZoomLevel = maxZoomLevel;
        this.zoomMultiplicationFactor = zoomMultiplicationFactor;
    }
    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
        dragStartScreen = e.getPoint();
        dragEndScreen = null;
        if(e.getButton() == 2) {
        	dragPressed = true;
        }
    }
    public void mouseReleased(MouseEvent e) {
    	if(e.getButton() == 2) {
        	dragPressed = false;
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {
    	if(dragPressed)
    		moveCamera(e);
    }
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoomCamera(e);
    }
    /////////////////////////////////////////////////////
    private void moveCamera(MouseEvent e) {
        try {
            dragEndScreen = e.getPoint();
            Point2D.Float dragStart = transformPoint(dragStartScreen);
            Point2D.Float dragEnd = transformPoint(dragEndScreen);
            double dx = 0;
            double dy = 0;
            //System.out.println(dragEnd.getX() + " :: " + dragEnd.getY());
            //if(!(dragStart.getX() - dragEnd.getX() - coordTransform.getTranslateX()/coordTransform.getScaleX() < minXTransition || dragStart.getX() - dragEnd.getX() - coordTransform.getTranslateX()/coordTransform.getScaleX() > maxXTransition - screenX/coordTransform.getScaleX())) {
            	//System.out.println("a : " + coordTransform.getTranslateX());
            dx = dragEnd.getX() - dragStart.getX();
            //}
            	
            //if(!(dragStart.getY() - dragEnd.getY() - coordTransform.getTranslateY()/coordTransform.getScaleX() < minYTransition || dragStart.getY() - dragEnd.getY() - coordTransform.getTranslateY()/coordTransform.getScaleX() > maxYTransition - screenY/coordTransform.getScaleX())) {
            	//System.out.println("b : " + coordTransform.getTranslateY());
            dy = dragEnd.getY() - dragStart.getY();
            //}
            	
            coordTransform.translate(dx, dy);
            dragStartScreen = dragEndScreen;
            dragEndScreen = null;
            targetComponent.repaint();
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }
    private void zoomCamera(MouseWheelEvent e) {
        try {
            int wheelRotation = e.getWheelRotation();
            Point p = e.getPoint();
            double dx;
            double dy;
            if (wheelRotation > 0) {
                if (zoomLevel < maxZoomLevel) { // Zoom out
                    zoomLevel++;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    //System.out.println("===============================================");
                    //System.out.println("P1: ( " + p1.getX() + " , " + p1.getY() + " )");
                    //System.out.println("P2: ( " + p2.getX() + " , " + p2.getY() + " )");
                    //System.out.println(coordTransform.getTranslateX() + " , " + coordTransform.getTranslateY());
                    //coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    dx = p2.getX() - p1.getX();
                    dy = p2.getY() - p1.getY();
                    //if(dx + coordTransform.getTranslateX()/coordTransform.getScaleX() > 0)
                    //	dx = -coordTransform.getTranslateX();
                    //if(dy + coordTransform.getTranslateY()/coordTransform.getScaleX() > 0)
                    //	dy = -coordTransform.getTranslateY();
                    //System.out.println((dx + coordTransform.getTranslateX()) + " < " + ((screenX - maxXTransition)*coordTransform.getScaleX()));
                    //System.out.println(dx);
                    //if(dx + coordTransform.getTranslateX() < (screenX - maxXTransition)*coordTransform.getScaleX()) {
                    //	dx = -coordTransform.getTranslateX() + (screenX - maxXTransition)*coordTransform.getScaleX();
                    	//dx = 0;
                    //	System.out.println("yo - " + coordTransform.getScaleX());
                    //	System.out.println("pos: " + coordTransform.getTranslateX() + " + " + dx);
                    //}
                    //if(dy + coordTransform.getTranslateY() < (screenY - maxYTransition)*coordTransform.getScaleX()) {
                    //	dy = -coordTransform.getTranslateY()/coordTransform.getScaleX() + (maxYTransition - screenY)/coordTransform.getScaleX();
                    	//dy = 0;
                    //}
                    	
                    coordTransform.translate(dx,dy);
                    targetComponent.repaint();
                }
            } else {
                if (zoomLevel > minZoomLevel) { // Zoom in
                    zoomLevel--;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    targetComponent.repaint();
                }
            }
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }
    private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = coordTransform.createInverse();
        Point2D.Float p2 = new Point2D.Float();
        inverse.transform(p1, p2);
        return p2;
    }
    private void showMatrix(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);  // { m00 m10 m01 m11 m02 m12 }
        int[] loRow = {0, 0, 1};
        for (int i = 0; i < 2; i++) {
            System.out.print("[ ");
            for (int j = i; j < matrix.length; j += 2) {
                System.out.printf("%5.1f ", matrix[j]);
            }
            System.out.print("]\n");
        }
        System.out.print("[ ");
        for (int i = 0; i < loRow.length; i++) {
            System.out.printf("%3d   ", loRow);
        }
        System.out.print("]\n");
        System.out.println("---------------------");
    }
    public int getZoomLevel() {
    	return zoomLevel;
    }
    public void setZoomLevel(int zoomLevel) {
    	this.zoomLevel = zoomLevel;
    }
    public AffineTransform getCoordTransform() {
    	return coordTransform;
    }
    public void setCoordTransform(AffineTransform coordTransform) {
    	this.coordTransform = coordTransform;
    }
}