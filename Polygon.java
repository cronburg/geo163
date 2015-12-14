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

  static Color lineColor      = Palette.get(2);
  static Color intersectColor = Color.RED; //Palette.get(3,1);

  Polygon(Viewport vp) {
    this.vp = vp;
    this.p = vp.p;
    this.points = new ArrayList<Point>();
    this.lastPoint = null;
  }

  void add(Point p) {
    lastPoint = p;
    points.add(p);
  }

  void add(float x, float y) { add(new Point(vp, x, y)); }

  void draw(Point tentativePoint) {

    if      (points.size() == 0) return;
    else if (points.size() == 1) {
      points.get(0).draw();
      return;
    }

    badTentative = false;
    Point a, b;
    for (int i = 0; i < points.size() - 1; i++) {
      a = points.get(i);
      b = points.get(i + 1);
      
      if (i < points.size() - 2
          && Point.segmentsIntersect(a, b, tentativePoint, lastPoint)) {
        p.stroke(intersectColor.getRGB());
        badTentative = true;
      } else {
        p.stroke(lineColor.getRGB());
      }
      p.strokeWeight(2);
      a.drawLineTo(b);
      
      a.draw();
    
    }
    points.get(points.size() - 1).draw();
  }

}
