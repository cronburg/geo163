import processing.core.*;
import java.awt.geom.Line2D;
import java.awt.Color;

public class Point {
  float x;     // x-pos relative to viewport
  float y;     // y-pos relative to viewport
  PApplet p;
  Viewport vp;  // The viewport this point belongs to (Viewport is larger)
  float radius; // radius of the point (absolute position value for now)
  
  // whether or not we are hovering over this point (valid after drawing)
  boolean mouseContains;

  private void _init(Viewport vp, float x, float y, float r) {
    this.vp = vp;
    this.p = vp.p;
    this.x = x;
    this.y = y;
    this.radius = r;
    this.mouseContains = false;
  }
  
  Point(Viewport vp, float x, float y) { _init(vp, x, y, (float)0.02); }
  Point(Viewport vp, float x, float y, float r) { _init(vp, x, y, r); }

  void draw(Color hovorColor) {
    mouseContains = false;
    if (vp.circleContainsMouse(x, y, radius)) {
      p.fill(hovorColor.getRGB());
      mouseContains = true;
    } else {
      p.fill(Palette.get(3,3).getRGB()); // color inside circle
      p.stroke(Palette.get(3,3).getRGB()); // color around edge of circle

    }
    vp.circle(x, y, radius);
  }

  // Draw a line from this point to another point:
  void drawLineTo(Point b) {
    p.line( vp.toAbsX(this.x), vp.toAbsY(this.y)
          , vp.toAbsX(b.x), vp.toAbsY(b.y));
  }

  void setAbsX(float absX) { this.x = vp.toRelX(absX); }
  void setAbsY(float absY) { this.y = vp.toRelY(absY); }

  // Whether or not line segments (a,b) and (c,d) intersect:
  static boolean segmentsIntersect(Point a, Point b, Point c, Point d) {
    // TODO: not library code
    Line2D s1 = new Line2D.Float(a.x, a.y, b.x, b.y);
    Line2D s2 = new Line2D.Float(c.x, c.y, d.x, d.y);
    return s2.intersectsLine(s1);
  }

}
