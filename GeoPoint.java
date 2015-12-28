import processing.core.*;
import java.awt.geom.Line2D;
import java.awt.Color;
import java.util.*;

public class GeoPoint implements Comparable<GeoPoint> {
  float x;     // x-pos relative to viewport
  float y;     // y-pos relative to viewport
  PApplet p;
  Viewport vp;  // The viewport this point belongs to (Viewport is larger)
  float radius; // radius of the point (absolute position value for now)
  
  // whether or not we are hovering over this point (valid after drawing)
  boolean mouseContains;
  
  Polygon constraint; // possible constraint on point location (must be inside poly)

  static int curr_name = 0;
  int unique_name;
  boolean hover;
  boolean label; // whether or not to always draw label on this point.

  private void _init(Viewport vp, float x, float y, float r) {
    this.vp = vp;
    if (null != vp) this.p = vp.p;
    this.x = x;
    this.y = y;
    this.radius = r;
    this.mouseContains = false;
    this.constraint = null;
    this.unique_name = curr_name++;
    this.hover = false; // TODO: debug mode?
    this.label = false;
  }
  
  GeoPoint(Viewport vp, float x, float y) { _init(vp, x, y, (float)0.02); }
  GeoPoint(Viewport vp, float x, float y, float r) { _init(vp, x, y, r); }
  GeoPoint(float x, float y) { _init(null, x, y, (float)0.02); }

  void setHover(boolean val) { this.hover = val; }
  void setLabel(boolean val) { this.label = val; }

  void setViewport(Viewport vp) {
    this.vp = vp;
    this.p = vp.p;
  }

  // Macro for auto-converting window coordinates to relative coordinates
  // when creating a new point:
  static GeoPoint newAbsGeoPoint(Viewport vp, int x, int y) {
    return new GeoPoint(vp, vp.toRelX(x), vp.toRelY(y));
  }

  void draw(Color hovorColor) {
    mouseContains = false;
    if (vp.circleContainsMouse(x, y, radius)) {
      p.fill(hovorColor.getRGB());
      if (hover) vp.hoverText(getName());
      mouseContains = true;
    } else {
      p.fill(Palette.get(3,3).getRGB()); // color inside circle
      p.stroke(Palette.get(3,3).getRGB()); // color around edge of circle

    }
    // TODO: circle color
    vp.circle(x, y, radius);
    if (label) {
      p.textAlign(p.CENTER, p.CENTER);
      p.fill(255);
      p.textSize(10);
      vp.text(getName(), x, y);
    }
  }

  // TODO: magic color...
  void draw() { draw(Palette.get(3,3)); }
  
  // Draw a line from this point to another point:
  void drawLineTo(GeoPoint b, Color c) {
    p.stroke(c.getRGB()); // TODO color
    p.line( vp.toAbsX(this.x), vp.toAbsY(this.y)
          , vp.toAbsX(b.x), vp.toAbsY(b.y));
  }

  // Distance from this point to another:
  float distance(GeoPoint b) { return p.sqrt(p.pow(b.x - x, 2) + p.pow(b.y - y, 2)); }

  void setAbsX(float absX) { this.x = vp.toRelX(absX); }
  void setAbsY(float absY) { this.y = vp.toRelY(absY); }

  // Whether or not line segments (a,b) and (c,d) intersect:
  static boolean segmentsIntersect(GeoPoint a, GeoPoint b, GeoPoint c, GeoPoint d) {
    // TODO: not library code
    Line2D s1 = new Line2D.Float(a.x, a.y, b.x, b.y);
    Line2D s2 = new Line2D.Float(c.x, c.y, d.x, d.y);
    return s2.intersectsLine(s1);
  }

  // Like segmentsIntersect, but ignore case where the intersection point is at one of
  // the given points. (general position assumed)
  static boolean segmentsIntersectNotAtEndpoint(GeoPoint a, GeoPoint b, GeoPoint c, GeoPoint d) {
    if ((a == c && b == d) || (a == d && b == c)) return true; // intersection is the entire segment(s)
    if (a == b || a == c || a == d || b == c || b == d || c == d) return false; // the segments share a point
    return segmentsIntersect(a,b,c,d);
  }

  static float slope     (GeoPoint a, GeoPoint b) { return (b.y - a.y) / (b.x - a.x); }
  static float intercept (GeoPoint a, float m) { return a.y - m * a.x; }

  // Find the point where Line(a,b) intersects with Line(c,d)
  // Assumption: lines are not identical & no infinite slopes...
  static GeoPoint lineIntersect(GeoPoint n, GeoPoint o, GeoPoint p, GeoPoint q) {
    float m1 = slope(n,o);
    float m2 = slope(p,q);
    float b1 = intercept(n, m1);
    float b2 = intercept(p, m2);
    float x = (b2 - b1) / (m1 - m2);
    float y = (m1*b2 - m2*b1) / (m1 - m2);
    return new GeoPoint(x, y);
  }

  GeoPoint copy() { return new GeoPoint(vp, x, y, radius); }

  // Adjust our position by the given delta (relative coordinates):
  // return: whether or not we actually performed the move.
  boolean moveDelta(float dx, float dy) {
    float newX = x + dx;
    float newY = y + dy;
    if (null != this.constraint && !constraint.contains(newX, newY)) {
      return false;
    }
    p.print("x = " + p.str(x) + ", y = " + p.str(y) + "\n");
    this.x = newX;
    this.y = newY;
    return true;
  }

  void setConstraint(Polygon p) { this.constraint = p; }

  // Does (this --> b --> c) form a left turn?
  boolean isLeftTurn(GeoPoint b, GeoPoint c)  { return Edge.cross(this, b, b, c) > 0; }
  boolean isRightTurn(GeoPoint b, GeoPoint c) { return Edge.cross(this, b, b, c) < 0; }
  // TODO: enforce general position...
  
  public String getName() { return p.str(unique_name); }

  public String toString() {
    return "(" + p.str(x) + "," + p.str(y) + ")";
  }

  // Compute the angle in radians on [pi, -pi] of this point using the given point as the origin:
  public float polarTheta(GeoPoint origin)  { return p.atan2(y - origin.y, x - origin.x) + p.PI; }
  public float polarRadius(GeoPoint origin) { return p.sqrt(p.pow(y - origin.y, 2) + p.pow(x - origin.x, 2)); }

	@Override
  public int compareTo(GeoPoint p2) { return Comparators.polarTheta.compare(this, p2); }

  public static class Comparators {
    public static Comparator<GeoPoint> getPolarTheta(GeoPoint pos) {
      final GeoPoint origin = new GeoPoint(null, pos.x, pos.y);
      return new Comparator<GeoPoint>() {
        @Override
        public int compare(GeoPoint p1, GeoPoint p2) {
          return new Float(p1.polarTheta(origin)).compareTo(p2.polarTheta(origin));
        }
      }; 
    }
    public static Comparator<GeoPoint> polarTheta = getPolarTheta(new GeoPoint(null,0,0));
	}

}
