
class Edge {

  Point a, b;

  private void _init(Point a, Point b) {
    this.a = a;
    this.b = b;
  }

  Edge(Point a, Point b) { _init(a,b); }
  Edge(Viewport vp, float x0, float y0, float x1, float y1) {
    _init(new Point(vp, x0, y0), new Point(vp, x1, y1));
  }

}

