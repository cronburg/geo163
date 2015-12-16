import processing.core.*;
import java.util.ArrayList;
import java.awt.Color;
import java.util.Collections;
import java.util.Stack;

public class Polygon {
 
  Viewport vp;
  PApplet p;
  ArrayList<Point> points;
  Point lastPoint;
  Point firstPoint;
  boolean badTentative; // Whether or not tentative new line is bad (non-simple polygon)
  boolean closed; // Whether or not to connect first and last point in the polygon

  static Color lineColor      = Palette.get(2);
  static Color intersectColor = Palette.RED; //Palette.get(3,1);

  Polygon(Viewport vp) {
    this.vp = vp;
    this.p = vp.p;
    this.points = new ArrayList<Point>();
    this.lastPoint = null;
    this.closed = false;
  }

  void add(Point p) {
    lastPoint = p;
    points.add(p);
  }

  // Whether or not the mouse contains the first point in the polygon
  boolean willNewPointClosePolygon() {
    return (points.size() > 0) && points.get(0).mouseContains;
  }

  void add(float x, float y) { add(new Point(vp, x, y)); }

  // Draw the given point, updating badTentative if necessary:
  private void drawInternalPoint(Point a, Color hovorColor) {
    a.draw(hovorColor);
    if (a.mouseContains) {
      badTentative = true;
    }
  }

  // Draw a point in the polygon and the line to the next point
  private void drawPointAndLineToNext(Point a, Point b, Color hovorColor) {
    a.drawLineTo(b);
    drawInternalPoint(a, hovorColor);
  }

  // Update badTentative and stroke color based on whether or not new line segment is feasible:
  private void checkLineSegment(boolean cndn, Point a, Point b, Point tentative, Point last) {
    // !closed because only make RED if not closed
    if (!closed && cndn && Point.segmentsIntersect(a, b, tentative, last)) {
      p.stroke(intersectColor.getRGB());
      badTentative = true;
    } else {
      p.stroke(lineColor.getRGB());
    }
  }

  void draw(Point tentativePoint) {

    Point a, b;
    badTentative = false;
    
    if      (points.size() == 0) return;
    else if (points.size() == 1) {
      // Only one point - draw it.
      drawInternalPoint(points.get(0), Palette.get(3,1));
      return;
    }
    
    // Known: we have 2 or more points already if we reach here.

    // Draw just first point without setting badTentative:
    a = points.get(0); b = points.get(1);
    
    checkLineSegment(points.size() > 2, a, b, tentativePoint, lastPoint);
    
    a.drawLineTo(b);
    //a.draw(Palette.get(3,1)); // Draw first point last so no line overlap

    // Draw all but first and last point:
    for (int i = 1; i < points.size() - 1; i++) {
      a = points.get(i);
      b = points.get(i + 1);
      
      //if (i < points.size() - 2) {
      checkLineSegment(i < points.size() - 2, a, b, tentativePoint, lastPoint);
      //} else {
      //  p.stroke(lineColor.getRGB());
      //}
      
      p.strokeWeight(2);
      drawPointAndLineToNext(a, b, Palette.RED);
    }

    // Draw the last point in the polygon (not the new / tentative point)
    // and possibly the closing edge:
    Point last = points.get(points.size() - 1);
    if (closed) {
      p.stroke(lineColor.getRGB());
      last.drawLineTo(points.get(0));
    }
    // Draw first and last point last so no line overlap:
    points.get(0).draw(Palette.get(3,1));
    drawInternalPoint(last, Palette.get(3,1));
  }

  // Open / close the polygon.
  void close() { this.closed = true; }
  void open()  { this.closed = false; }

  // Get the point with the maximum x-value:
  Point getMaxX() {
    Point m = points.get(0); // TODO empty polygon
    for (int i = 1; i < points.size(); i++) {
      if (m.x < points.get(i).x) m = points.get(i);
    }
    return m;
  }
  Point getMaxY() {
    Point m = points.get(0);
    for (int i = 1; i < points.size(); i++) {
      if (m.y < points.get(i).y) m = points.get(i);
    }
    return m;
  }

  // Does this polygon contain the given point?
  boolean contains(Point p) {
    if (!closed) {
      this.p.print("WARNING: Testing if a non-closed polygon contains a point...\n");
      return false;
    }

    // Ad-hoc point "at infinity":
    Point inf = new Point(vp, getMaxX().x * 2, getMaxY().y * 2);
    //p.drawLineTo(inf); // Draw infinite ray for debugging

    int count = 0;
    for (int i = 0; i < points.size(); i++) {
      count += Point.segmentsIntersect(getPoint(i), getPoint(i+1), p, inf) ? 1 : 0;
    }
    //count += Point.segmentsIntersect(points.get(points.size() - 1), points.get(0), p, inf) ? 1 : 0;
    this.p.print("count = " + this.p.str(count) + "\n");

    return (count % 2) == 1; // Odd number of crossings == inside (JCT)
  }

  boolean contains(float x, float y) { return contains(new Point(vp,x,y)); }

  // Get the i-th point in the polygon (circular ArrayList):
  Point getPoint(int i) { return points.get(i % points.size()); }

  // Compute the visibility polygon of a point inside this polygon:
  Polygon computeVisibility(Point pos) {
    Stack edgeStack = new Stack<Edge>();
    Point a, b;
    for (int i = 0; i < points.size(); i++) {
      a = points.get(i);
      b = points.get(i + 1);
    }
    // TODO: return...
    return new Polygon(vp);
  }

}

