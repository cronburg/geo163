//import processing.core.*;
//import java.awt.geom.Line2D;
//import java.awt.Color;
//import java.util.*;

int curr_name = 0;
  
// Macro for auto-converting window coordinates to relative coordinates
// when creating a new point:
GeoPoint newAbsGeoPoint(Viewport vp, int x, int y) {
  return new GeoPoint(vp, vp.toRelX(x), vp.toRelY(y));
}

public class GeoPoint {
  Point pos;   // (x,y)-pos relative to viewport
  Viewport vp;  // The viewport this point belongs to (Viewport is larger)
  float radius; // radius of the point (absolute position value for now)
  
  // whether or not we are hovering over this point (valid after drawing)
  boolean mouseContains;
  
  Polygon constraint; // possible constraint on point location (must be inside poly)

  int unique_name;
  boolean hover;
  boolean label; // whether or not to always draw label on this point.

  private void _init(Viewport vp, float x, float y, float r) {
    this.vp = vp;
    this.pos = new Point(x,y);
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
  }

  public float x() { return pos.x; }
  public float y() { return pos.y; }

  void draw(color hovorColor) {
    mouseContains = false;
    if (vp.circleContainsMouse(x(), y(), radius)) {
      vp.vpFill(hovorColor);
      if (hover) vp.hoverText(getName());
      mouseContains = true;
    } else {
      //println(hex(Palette.get(3,3)) + ", " + str(radius));
      vp.vpFill(Palette.get(3,3)); // color inside circle
      stroke(Palette.get(3,3)); // color around edge of circle

    }
    // TODO: circle color
    vp.circle(x(), y(), radius);
    if (label) {
      textAlign(CENTER, CENTER);
      fill(255);
      textSize(10);
      vp.vpText(getName(), x(), y());
    }
  }

  // TODO: magic color...
  void draw() { draw(Palette.get(3,3)); }
  
  // Draw a line from this point to another point:
  void drawLineTo(GeoPoint b, color c) {
    //println(hex(c));
    strokeWeight(1);
    stroke(red(c), green(c), blue(c)); // TODO color
    //stroke(120);
    line( vp.toAbsX(x()), vp.toAbsY(y()), vp.toAbsX(b.x()), vp.toAbsY(b.y()));
  }

  // Distance from this point to another:
  float distance(GeoPoint b) { return sqrt(pow(b.x() - x(), 2) + pow(b.y() - y(), 2)); }

  void setAbsX(float absX) { pos.x = vp.toRelX(absX); }
  void setAbsY(float absY) { pos.y = vp.toRelY(absY); }

  /* http://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/ */
  // Given three colinear points p, q, r, the function checks if
  // point q lies on line segment 'pr'
  boolean onSegment(Point p, Point q, Point r)
  {
      if (q.x <= max(p.x, r.x) && q.x >= min(p.x, r.x) &&
          q.y <= max(p.y, r.y) && q.y >= min(p.y, r.y))
         return true;
   
      return false;
  }
   
  // To find orientation of ordered triplet (p, q, r).
  // The function returns following values
  // 0 --> p, q and r are colinear
  // 1 --> Clockwise
  // 2 --> Counterclockwise
  int orientation(Point p, Point q, Point r)
  {
      // See http://www.geeksforgeeks.org/orientation-3-ordered-points/
      // for details of below formula.
      float val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);
   
      float epsilon = 0.000001;
      if (val < epsilon && val > -epsilon) return 0;  // colinear
   
      return (val > 0)? 1: 2; // clock or counterclock wise
  }
   
  // The main function that returns true if line segment 'p1q1'
  // and 'p2q2' intersect.
  boolean doIntersect(Point p1, Point q1, Point p2, Point q2)
  {
      // Find the four orientations needed for general and
      // special cases
      int o1 = orientation(p1, q1, p2);
      int o2 = orientation(p1, q1, q2);
      int o3 = orientation(p2, q2, p1);
      int o4 = orientation(p2, q2, q1);
   
      // General case
      if (o1 != o2 && o3 != o4)
          return true;
   
      // Special Cases
      // p1, q1 and p2 are colinear and p2 lies on segment p1q1
      if (o1 == 0 && onSegment(p1, p2, q1)) return true;
   
      // p1, q1 and p2 are colinear and q2 lies on segment p1q1
      if (o2 == 0 && onSegment(p1, q2, q1)) return true;
   
      // p2, q2 and p1 are colinear and p1 lies on segment p2q2
      if (o3 == 0 && onSegment(p2, p1, q2)) return true;
   
       // p2, q2 and q1 are colinear and q1 lies on segment p2q2
      if (o4 == 0 && onSegment(p2, q1, q2)) return true;
   
      return false; // Doesn't fall in any of the above cases
  }

  // Whether or not line segments (a,b) and (c,d) intersect:
  boolean segmentsIntersect(GeoPoint b, GeoPoint c, GeoPoint d) {
    return doIntersect(this.pos, b.pos, c.pos, d.pos);
  }

  // Like segmentsIntersect, but ignore case where the intersection point is at one of
  // the given points. (general position assumed)
  boolean segmentsIntersectNotAtEndpoint(GeoPoint a, GeoPoint b, GeoPoint c, GeoPoint d) {
    if ((a == c && b == d) || (a == d && b == c)) return true; // intersection is the entire segment(s)
    if (a == b || a == c || a == d || b == c || b == d || c == d) return false; // the segments share a point
    return a.segmentsIntersect(b,c,d);
  }

  float slope     (GeoPoint a, GeoPoint b) { return (b.y() - a.y()) / (b.x() - a.x()); }
  float intercept (GeoPoint a, float m) { return a.y() - m * a.x(); }

  // Find the point where Line(a,b) intersects with Line(c,d)
  // Assumption: lines are not identical & no infinite slopes...
  GeoPoint lineIntersect(GeoPoint n, GeoPoint o, GeoPoint p, GeoPoint q) {
    float m1 = slope(n,o);
    float m2 = slope(p,q);
    float b1 = intercept(n, m1);
    float b2 = intercept(p, m2);
    float x = (b2 - b1) / (m1 - m2);
    float y = (m1*b2 - m2*b1) / (m1 - m2);
    return new GeoPoint(x, y);
  }

  GeoPoint copy() { return new GeoPoint(vp, x(), y(), radius); }

  // Adjust our position by the given delta (relative coordinates):
  // return: whether or not we actually performed the move.
  boolean moveDelta(float dx, float dy) {
    float newX = x() + dx;
    float newY = y() + dy;
    if (null != this.constraint && !constraint.contains(newX, newY)) {
      return false;
    }
    print("x = " + str(x()) + ", y = " + str(y()) + "\n");
    pos.x = newX;
    pos.y = newY;
    return true;
  }

  void setConstraint(Polygon p) { this.constraint = p; }

  // Compute scalar cross product of line segments (ab) x (cd)
  float cross(GeoPoint b, GeoPoint c, GeoPoint d) {
    return (b.x() - this.x()) * (d.y() - c.y()) - (b.y() - this.y()) * (d.x() - c.x());
  }

  // Does (this --> b --> c) form a left turn?
  boolean isLeftTurn(GeoPoint b, GeoPoint c)  { return this.cross(b, b, c) > 0; }
  boolean isRightTurn(GeoPoint b, GeoPoint c) { return this.cross(b, b, c) < 0; }
  // TODO: enforce general position...
  
  public String getName() { return str(unique_name); }

  public String toString() {
    return "(" + str(x()) + "," + str(y()) + ")";
  }

  // Compute the angle in radians on [pi, -pi] of this point using the given point as the origin:
  public float polarTheta(GeoPoint origin)  { return atan2(y() - origin.y(), x() - origin.x()) + PI; }
  public float polarRadius(GeoPoint origin) { return sqrt(pow(y() - origin.y(), 2) + pow(x() - origin.x(), 2)); }

  /*
  @Override
  public int compareTo(GeoPoint p2) { return Comparators.polarTheta.compare(this, p2); }

  public static class Comparators {
    public static Comparator<GeoPoint> getPolarTheta(GeoPoint pos) {
      final GeoPoint origin = new GeoPoint(null, pos.x(), pos.y());
      return new Comparator<GeoPoint>() {
        @Override
        public int compare(GeoPoint p1, GeoPoint p2) {
          return new Float(p1.polarTheta(origin)).compareTo(p2.polarTheta(origin));
        }
      }; 
    }
    public static Comparator<GeoPoint> polarTheta = getPolarTheta(new GeoPoint(null,0,0));
  }
  */
}
