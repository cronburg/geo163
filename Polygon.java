import processing.core.*;
import java.util.ArrayList;
import java.awt.Color;

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

  private void checkLineSegment(boolean cndn, Point a, Point b, Point tentative, Point last) {
    if (cndn && Point.segmentsIntersect(a, b, tentative, last)) {
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
    a.draw(Palette.get(3,1));

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
    drawInternalPoint(last, Palette.get(3,1));
    if (closed) last.drawLineTo(points.get(0));
  }

  // Open / close the polygon.
  void close() { this.closed = true; }
  void open()  { this.closed = false; }

}

