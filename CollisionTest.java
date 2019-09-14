import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CollisionTest extends Applet implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
	FloatPoly[] fpoly;		//-------------- Data array of polygons added with creation of each polygon
	FloatPoly currentfpoly;	//-------------- The current polygon being built
	int numPoly;			//-------------- The number of polygons in "fpoly"
	int xMouse;				//-------------- The mouse X position on the screen
	int yMouse;				//-------------- The mouse Y position on the screen
	ZoomAndPanListener zoomAndPanListener;// Object for screen scaling and transformation of the grid
	int maxGridX;			//-------------- The max X value on the grid based on "bi" size
	int maxGridY;			//-------------- The max Y value on the grid based on "bi" size
	int screenX;			//-------------- The screen width
	int screenY;			//-------------- The screen height
	
	int createPolyState;	//-------------- State of constructing polygons // 0 - no current polygon // 1 - currently creating polygon
	int highlightedPoly;	//-------------- The index value of the current polygon being highlighted
	
	BufferedImage bi;		//-------------- The entire background image
	final String fileName = "AnAPolyData.txt";
	/**
	 * Initialization
	 */
	public void init() {
		screenX = 1600;
		screenY = 900;
		resize(screenX, screenY);
		try {
			bi = ImageIO.read(new File("AnAMap.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		maxGridX = bi.getWidth();
		maxGridY = bi.getHeight();
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		zoomAndPanListener = new ZoomAndPanListener(this,0,0,maxGridX,maxGridY,screenX,screenY);
        this.addMouseListener(zoomAndPanListener);
        this.addMouseMotionListener(zoomAndPanListener);
        this.addMouseWheelListener(zoomAndPanListener);
		fpoly = new FloatPoly[500];
		numPoly = 0;
		//makePoly();
		xMouse = -1;
		yMouse = -1;
		createPolyState = 0;
		currentfpoly = null;
		readFileData(fileName);
	}
	/**
	 * Looped method that draws data
	 */
	public void paint(Graphics g) {
		showStatus("( " + (int)((xMouse - zoomAndPanListener.getCoordTransform().getTranslateX())/scale()) + " , " + (int)((yMouse - zoomAndPanListener.getCoordTransform().getTranslateY())/scale()) + " )");
		Graphics2D g2 = (Graphics2D)g;
		g2.setTransform(zoomAndPanListener.getCoordTransform());
		drawBackGround(g2, bi);
		drawExistingPolys(g2);
		drawCurrentPoly(g2);
	}
	/**
	 * Double buffering
	 */
	public void update(Graphics g) {
		Graphics offgc;
		Image offscreen = null;
		@SuppressWarnings("deprecation")
		Dimension d = size();
		offscreen = createImage(d.width, d.height);
		offgc = offscreen.getGraphics();
		offgc.setColor(getBackground());
		offgc.fillRect(0, 0, d.width, d.height);
		offgc.setColor(getForeground());
		paint(offgc);
		g.drawImage(offscreen, 0, 0, this);
	}
	public void readFileData(String fileName) {
		try {
			System.out.println("Importing data");
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			boolean inPoly = false;
			FloatPoly currentPoly = null;
			int polyID = -1;
			while(br.ready()) {		// Will read if not end of file
				line = br.readLine();
				if(inPoly) {	
					if(line.contains("endpoly")) {	// Test if end of current polygon
						inPoly = false;
						if(currentPoly == null) {
							System.out.println("Error parsing polygon -- Lack of point data");
						}
						else if(polyID == -1) {
							System.out.println("Error parsing polygon -- No polygon ID");
						}
						else {
							fpoly[numPoly++] = currentPoly;
						}
					}
					else if(line.contains("pt=")) {	// Adds a point to the current polygon
						String pointData = line.substring(line.indexOf('=') + 1).trim(); // Gets the contents of the point after the '=' // ex. "3256,234"
						currentPoly.addPoint(Integer.parseInt(pointData.substring(0, pointData.indexOf(','))),
											 Integer.parseInt(pointData.substring(pointData.indexOf(',') + 1)));
					}
					else if(line.contains("id=")) {	// Gets the id index for the polygon, MUST HAVE ONLY ONE PER POLYGON
						polyID = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
					}
				}
				else if(line.contains("newpoly")) { // If not in a current polygon, test if new polygon
					currentPoly = new FloatPoly();
					inPoly = true;
					polyID = -1;
				}
			}
			br.close();
			System.out.println("Data loaded");
		} catch (IOException | NumberFormatException | IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
	public void writeFileData(String fileName) {
		try {
			System.out.println("Saving data");
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			for(int i = 0; i < numPoly; i++) {
				bw.write("newpoly");
				bw.newLine();
				bw.write("id=" + i);
				bw.newLine();
				for(int k = 0; k < fpoly[i].xPoints.length; k++) {
					bw.write("pt=" + (int)fpoly[i].xPoints[k] + "," + (int)fpoly[i].yPoints[k]);
					bw.newLine();
				}
				bw.write("endpoly");
				bw.newLine();
			}
			bw.close();
			System.out.println("Data saved");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Draws the existing polygons in fpoly
	 * @param g2
	 */
	public void drawExistingPolys(Graphics2D g2) {
		highlightedPoly = -1;
		for(int i = 0; i < numPoly; i++) {
			if(inPoly(fpoly[i].xPoints, fpoly[i].yPoints, fpoly[i].xMin, fpoly[i].xMax, fpoly[i].yMin, fpoly[i].yMax)) {
				highlightedPoly = i;
			}
			g2.setColor(Color.BLACK);
			g2.drawPolygon(fpoly[i].polyInt);
		}
		if(highlightedPoly >= 0) {
			g2.setColor(Color.ORANGE);
			g2.drawPolygon(fpoly[highlightedPoly].polyInt);
		}
	}
	/**
	 * Draws the current polygon being built
	 * @param g2
	 */
	public void drawCurrentPoly(Graphics g2) {
		if(createPolyState == 1) {
			for(int i = 0; i < currentfpoly.xPoints.length - 1; i++) {
				g2.setColor(Color.MAGENTA);
				g2.drawLine((int)currentfpoly.xPoints[i], (int)currentfpoly.yPoints[i], (int)currentfpoly.xPoints[i+1], (int)currentfpoly.yPoints[i+1]);
			}
			for(int i = 0; i < currentfpoly.xPoints.length; i++) {
				//System.out.println(i + " : ( " + currentfpoly.xPoints[i] + " , " + currentfpoly.yPoints[i] + " )");
				g2.setColor(Color.RED);
				g2.fillOval((int)currentfpoly.xPoints[i]-1, (int)currentfpoly.yPoints[i]-1, 2, 2);
			}
		}
	}
	/**
	 * Adds a point to an existing polygon or creates a starting point for a new polygon
	 */
	public void polyAddPoint() {
		if(createPolyState == 0) {
			currentfpoly = new FloatPoly();
			currentfpoly.addPoint(getXGrid(), getYGrid());
			createPolyState = 1;
		}
		else if(createPolyState == 1) {
			if(Math.sqrt(Math.pow(currentfpoly.getFirstX() - getXGrid(),2) + Math.pow(currentfpoly.getFirstY() - getYGrid(), 2)) < 1.5) { // true if mouse is within 1.5 pixels from start of polygon 
				fpoly[numPoly++] = currentfpoly;
				createPolyState = 0;
			}
			else {
				currentfpoly.addPoint(getXGrid(), getYGrid());
			}
		}
	}
	public void delPolyPoint() {
		if(createPolyState == 0 && numPoly >= 1) { // Delete last point of last polygon
			createPolyState = 1;
			currentfpoly = fpoly[numPoly-1];
			fpoly[numPoly-1] = null;
			numPoly--;
		}
		else if (createPolyState == 1) { // Delete the last point on the current polygon
			currentfpoly.removeLastPoint();
			if(currentfpoly.xPoints.length <= 0) {
				createPolyState = 0;
				currentfpoly = null;
			}
		}
		else {
			System.out.println("Nothing to delete");
		}
	}
	public void printPoints() {
		int numPoints = 0;
		for(int i = 0; i < numPoly; i++) {
			numPoints += fpoly[i].xPoints.length;
		}
		System.out.println("Number of polygons : " + numPoly);
		System.out.println("Number of total points : " + numPoints);
	}
	/**
	 * Draws the background image with proper rendering for focused section
	 * @param g2
	 * @param backPic
	 */
	public void drawBackGround(Graphics2D g2, BufferedImage backPic) {
		BufferedImage subbi;
		int originX = (int)(translatedX() / scale());
		int originY = (int)(translatedY() / scale());
		int capX = (int)(screenX / scale());
		int capY = (int)(screenY / scale());
		int drawXPlace = originX;
		int drawYPlace = originY;
		if(originX < 0) { // moved past left edge
			originX = 0;
			drawXPlace = 0;
			drawXPlace *= -1;
		}
		if(capX > maxGridX - (int)(translatedX() / scale())) { // moved past right edge
			capX = maxGridX - (int)(translatedX() / scale());
		}
		if(originY < 0) { // moved past top edge
			originY = 0;
			drawYPlace = 0;
			drawYPlace *= -1;
		}
		if(capY > maxGridY - (int)(translatedY() / scale())) { // moved past bottom edge
			capY = maxGridY - (int)(translatedY() / scale());
		}
		subbi = backPic.getSubimage(originX, originY, capX, capY);
		g2.drawImage(subbi, drawXPlace, 
							drawYPlace, null);
	}
	/**
	 * Tests if the point (mouse position) is in a polygon
	 * @param xPoints
	 * @param yPoints
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @return
	 */
	public boolean inPoly(float[] xPoints, float[] yPoints, float xMin, float xMax, float yMin, float yMax) {
	    if ( getXGrid() < xMin || getXGrid() > xMax || getYGrid() < yMin || getYGrid() > yMax ) {
	        return false;
	    }
	    int i, j;
	    boolean c = false;
	    for (i = 0, j = xPoints.length-1; i < xPoints.length; j = i++) {
	    	if (((yPoints[i]>getYGrid()) != (yPoints[j]>getYGrid())) && (getXGrid() < ((xPoints[j]-xPoints[i]) * (getYGrid()-yPoints[i])) / ((yPoints[j]-yPoints[i])) + xPoints[i]))
	    		c = !c;
	    }
	    return c;
	}
	/**
	 * Delay time
	 * @param time
	 */
	public void tick(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Translates the mouse X position on the screen to the respective X position on the grid
	 */
	public int getXGrid() {
		return (int) Math.round((xMouse + translatedX())/scale());
	}
	/**
	 * Translates the mouse Y position on the screen to the respective Y position on the grid
	 */
	public int getYGrid() {
		return (int) Math.round((yMouse + translatedY())/scale());
	}
	/**
	 * Gets the scale of screen from the original grid.
	 * Position on grid / scale() = position on screen
	 */
	public double scale() {
		return zoomAndPanListener.getCoordTransform().getScaleX();
	}
	/**
	 * Gets X position on the grid as the top left position of the screen
	 */
	public double translatedX() {
		return -zoomAndPanListener.getCoordTransform().getTranslateX();
	}
	/**
	 * Gets Y position on the grid as the top left position of the screen
	 */
	public double translatedY() {
		return -zoomAndPanListener.getCoordTransform().getTranslateY();
	}
	public void mouseDragged(MouseEvent e) {
		xMouse = e.getX();
		yMouse = e.getY();
		repaint();
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		xMouse = e.getX();
		yMouse = e.getY();
		repaint();
	}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == 1) {
			polyAddPoint();
		}
		if(e.getButton() == 3) {
			printPoints();
		}
		repaint();
	}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyChar()=='s') {
			writeFileData(fileName);
		}
		if(e.getKeyCode()==127) {
			delPolyPoint();
		}
		repaint();
	}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
}
