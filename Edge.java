import processing.core.*;

class Edge {

  Viewport vp;
  PApplet p;
  GeoPoint a, b;

  private void _init(Viewport vp, GeoPoint a, GeoPoint b) {
    this.vp = vp;
    this.p = vp.p;
    this.a = a; // Starting point of edge
    this.b = b; // End-point of edge
  }

  Edge(Viewport vp, GeoPoint a, GeoPoint b) { _init(vp,a,b); }
  Edge(Viewport vp, float x0, float y0, float x1, float y1) {
    _init(vp, new GeoPoint(vp, x0, y0), new GeoPoint(vp, x1, y1));
  }

  // Magnitude of the line segment (edge) between two points:
  static float mag(GeoPoint a, GeoPoint b) {
    float dx = b.x - a.x;
    float dy = b.y - a.y;
    return PApplet.sqrt(dx*dx + dy*dy);
  }

  // Compute scalar cross product of line segments (ab) x (cd)
  static float cross(GeoPoint a, GeoPoint b, GeoPoint c, GeoPoint d) {
    return (b.x - a.x) * (d.y - c.y) - (b.y - a.y) * (d.x - c.x);
  }

  // Compute cross product of this edge with another edge:
  float cross(Edge e2) { return cross(this.a, this.b, e2.a, e2.b); }

  public String toString() {
    return "Edge(" + a.getName() + ", " + b.getName() + ")";
  }

}

