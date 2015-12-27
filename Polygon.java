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

  Color lineColor;
  Color intersectColor;

  private void _init(Viewport vp) {
    this.vp = vp;
    this.p = vp.p;
    this.points = new ArrayList<Point>();
    this.lastPoint = null;
    this.closed = false;
    this.lineColor = Palette.get(2);
    this.intersectColor = Palette.RED;
  }
  Polygon(Viewport vp) { _init(vp); }

  Polygon(Viewport vp, ArrayList<Point> points) {
    _init(vp);
    this.points = points;
    this.closed = true;
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
    a.drawLineTo(b, lineColor);
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
    
    a.drawLineTo(b, lineColor);
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
      last.drawLineTo(points.get(0), lineColor);
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

    // Ad-hoc point "at infinity" (necessarily outside of convex hull):
    Point inf = new Point(vp, getMaxX().x * 2, getMaxY().y * 2);
    //p.drawLineTo(inf); // Draw infinite ray for debugging

    int count = 0;
    for (int i = 0; i < points.size(); i++) {
      count += Point.segmentsIntersect(getPoint(i), getPoint(i+1), p, inf) ? 1 : 0;
    }
    //count += Point.segmentsIntersect(points.get(points.size() - 1), points.get(0), p, inf) ? 1 : 0;
    //this.p.print("count = " + this.p.str(count) + "\n");

    return (count % 2) == 1; // Odd number of crossings == inside (JCT)
  }

  boolean contains(float x, float y) { return contains(new Point(vp,x,y)); }

  // Get the i-th point in the polygon (circular ArrayList):
  Point getPoint(int i) { return getPoint(points, i); }
  Point getPoint(ArrayList<Point> aPoints, int i) { return aPoints.get((i + aPoints.size()) % aPoints.size()); }

  // Convert the given visibility stack into a polygon:
  Polygon(Viewport vp, Stack<Edge> stack) {
    _init(vp);
    p.print("ENDING STACK:");
    Edge curr;
    while (!stack.empty()) {
      curr = (Edge)stack.pop();
      p.print("  " + curr.toString() + "\n");
      add(curr.a.x, curr.a.y);
    }
    closed = true;
    lineColor = Color.GREEN;
  }

  // No first class functions, therefore this:
  private boolean _left_turn;
  private boolean isSameTurn(Point a, Point b, Point c) {
    return _left_turn ? a.isLeftTurn(b,c) : a.isRightTurn(b,c);
  }

  // Determine the index of the closest vertex in this polygon to the given point:
  int closestVertex(Point pos) {
    int minPoint = 0;
    float min = pos.distance(getPoint(0));
    float dist;
    for (int i = 1; i < points.size(); i++) {
      dist = pos.distance(getPoint(i));
      if (dist < min) {
        minPoint = i;
        min = dist;
      }
    }
    return minPoint;
  }

  // Shoot off a ray at an angle theta to find the closest visible edge (return index of starting point)
  Point visiblePoint;
  
  int findVisibleEdge(Point pos, float theta) {  
    //Point fake = new Point(getMaxX().x * 2 * p.cos(theta), getMaxY().y * 2 * p.sin(theta));
    return findVisibleEdge(pos, new Point(vp, 10 * p.cos(theta), 10 * p.sin(theta)));
  }

  int findVisibleEdge(Point pos, Point fake) {
    int minPoint = 0;
    Point b,c,inter;
    float min = 100; // TODO: no magic
    float dist;
    for (int i = 0; i < points.size(); i++) {
      b = getPoint(i); c = getPoint(i+1);
      if (Point.segmentsIntersect(pos, fake, b, c)) {
        inter = Point.lineIntersect(pos, fake, b, c);
        dist = pos.distance(inter);
        if (dist < min) {
          minPoint = i;
          min = dist;
          visiblePoint = inter;
          visiblePoint.radius = (float)0.005; // tiny
        }
      }
    }
    return minPoint;
  }

  int indexOf(Point p) { return points.indexOf(p); }

  // Can pos see curr? - O(n)
  boolean isPointVisible(Point pos, Point curr) {
    for (int j = 0; j < points.size(); j++) {
      if (getPoint(j) == curr || getPoint(j + 1) == curr) continue; // skip edges involving current point
      if (Point.segmentsIntersect(pos, curr, getPoint(j), getPoint(j+1))) return false;
    }
    return true;
  }

  // Computes list of points pos can see unobstructed by edges in this polygon - O(n^2)
  ArrayList<Point> findVisiblePoints(Point pos) {
    ArrayList<Point> visPoints = new ArrayList<Point>();
    Point curr;
    for (int i = 0; i < points.size(); i++) {
      curr = getPoint(i);
      if (isPointVisible(pos, curr)) visPoints.add(curr);
    }
    return visPoints;
  }

  private void addWindowIfExists(Point pos, int currIndex, ArrayList<Point> visPoints, ArrayList<Point> visWithWindows) {
    Point curr = getPoint(visPoints, currIndex);
    
    Point nextInSorted = getPoint(visPoints, currIndex + 1);
    Point nextInPoly   = getPoint(indexOf(curr) + 1);
    Point prevInPoly   = getPoint(indexOf(curr) - 1);

    boolean turn0 = isSameTurn(pos, curr, nextInPoly);
    boolean turn1 = isSameTurn(pos, curr, prevInPoly);
    if (turn0 != turn1) {
      visWithWindows.add(curr);
      return; // no window here
    }
    
    Point a,b,fake;
    ArrayList<Point> intersections = new ArrayList<Point>();
    float theta = curr.polarTheta(pos);
    fake = new Point(0 - 10 * p.cos(theta), 0 - 10 * p.sin(theta));
    for (int i = 0; i < points.size(); i++) {
      a = getPoint(i); b = getPoint(i+1);
      if (curr == a || curr == b) continue;
      if (Point.segmentsIntersect(pos, fake, a, b)) {
        Point inter = Point.lineIntersect(pos, fake, a, b);
        inter.setViewport(vp);
        intersections.add(inter);
      }
    }
    if (intersections.size() == 0) {
      System.out.println("ERROR: polygon appears to not be closed.");
      return;
    }
    Point windowPoint = intersections.get(0);
    Point currInter;
    for (int i = 1; i < intersections.size(); i++) {
      currInter = intersections.get(i);
      if (pos.distance(currInter) < pos.distance(windowPoint)) {
        windowPoint = currInter;
      }
    }
    
    if (turn0) {
      visWithWindows.add(windowPoint);
      visWithWindows.add(curr);
    } else {
      visWithWindows.add(curr);
      visWithWindows.add(windowPoint);
    }
  }

  private ArrayList<Point> insertWindows(Point pos, ArrayList<Point> visPoints) {
    Point curr, nextInSorted, nextInPoly, inter;
    boolean turn0, turn1;
    ArrayList<Point> visWithWindows = new ArrayList<Point>();
    for (int i = 0; i < visPoints.size(); i++) {
      //curr = getPoint(visPoints, i);
      
      //visWithWindows.add(curr);
      addWindowIfExists(pos, i, visPoints, visWithWindows);
      /*
      nextInSorted = getPoint(visPoints, i + 1);
      nextInPoly = getPoint(indexOf(curr) + 1);
      if (nextInPoly == nextInSorted) continue;
      p.print(p.str(curr.unique_name) + ", " + p.str(nextInSorted.unique_name) + ", " + p.str(nextInPoly.unique_name) + "\n");
      turn0 = isSameTurn(pos, curr, nextInPoly);
      turn1 = isSameTurn(pos, curr, nextInSorted);
      if (turn0 == turn1) { // segment crosses the void cone - make single window point on it
        inter = Point.lineIntersect(pos, nextInSorted, curr, nextInPoly);
        inter.setViewport(vp);
        visWithWindows.add(inter);
      }
      */
    }
    return visWithWindows;
  }

  // Compute the visibility polygon of the given point:
  Polygon computeVisibility(Point pos) {
    _left_turn = true;
    ArrayList<Point> visPoints = findVisiblePoints(pos);
    Collections.sort(visPoints, Point.Comparators.getPolarTheta(pos));
    visPoints = insertWindows(pos, visPoints);

    Polygon visPoly = new Polygon(this.vp, visPoints);
    visPoly.lineColor = Color.GREEN;
    visPoly.closed = true;
    return visPoly;
  }


  //private static enum VisState { FORWARD, BACKTRACK, UPWARD_BACKTRACK; }

  /*
  Polygon computeVisibility2(Point pos) {
    Point b, c, windowPt;
    Edge peekEdge, prevEdge;

    VisState state = VisState.FORWARD;
    int start = findVisibleEdge(pos);
    _left_turn = pos.isLeftTurn(visiblePoint, getPoint(start + 1));

    Stack stack = new Stack<Edge>();
    stack.push(new Edge(vp, visiblePoint, getPoint(start + 1)));
    for (int i = start + 1; i < points.size() + start + 1; i++) {
      prevEdge = getPoint(i - 1);
      b = getPoint(i);
      c = getPoint(i + 1);
      if (isSameTurn(pos, b, c)) {
        if (state == VisState.FORWARD) {                           // CASE 1 - normal forward
          stack.push(new Edge(vp, b, c));
        } else if (state == VisState.UPWARD_BACKTRACK) {           // CASE 2 - upward left turn
          peekEdge = (Edge)stack.peek();
          if (isSameTurn(pos, peekEdge.b, c)) {
            windowPt = Point.lineIntersec(pos, peekEdge.b, b, c);
            stack.push(new Edge(vp, peekEdge.b, windowPt));
            stack.push(new Edge(vp, windowPt, c));
            state = VisState.FORWARD;
          }
        } else { // state == VisState.BACKTRACK
          if (isSameTurn(prevEdge.a, b, c)) {
            
          }
        }
    }
  }
  */

/*
  Polygon computeVisibility2(Point pos) {
    Polygon visPoly = new Polygon(vp);
    Point a,b,c,d;
    Point aN,bN;   // neighbors of a and b
    for (int i = 0; i < points.size(); i++) {
      for (int j = 0; j < points.size(); j++) {
        boolean edgeIsVisible = (j == (i + 1) % points.size());
        a = getPoint(i);
        b = getPoint(j);
        for (int i0 = 0; i0 < points.size(); i0++) {
          for (int j0 = 0; j0 < points.size(); j0++) {
            //if (i == j || i == i0 || i == j0 || j == i0 || j == j0 || i0 == j0) continue; // only look at distinct points
            c = getPoint(i0); d = getPoint(j0);
            if (
                 (!Point.segmentsIntersectNotAtEndpoint(pos, a, c, d))
              && (!Point.segmentsIntersectNotAtEndpoint(pos, b, c, d))
              ) {
              edgeIsVisible = false; // found a blocking edge
            }
          }
        }
        if (edgeIsVisible) {
          // Edge(a,b) is completely visible - add to polygon:
          visPoly.add(a);
          visPoly.add(b);
        }
      }
    }
    visPoly.closed = true;
    visPoly.lineColor = Color.GREEN;
    return visPoly;
  }

  Polygon computeVisibility2(Point pos) {
    int start = findVisibleEdge(pos);

    // Sort points by angular position relative to visibility point:
    ArrayList<Point> sortedPoints = new ArrayList<Point>(points);
    Collections.sort(sortedPoints, Point.Comparators.getPolarTheta(pos));
    
    // List of starting points to edges we currently intersect with ray from pos through current sorted point in poly:
    ArrayList<Point> edges = new ArrayList<Point>();
    
    findVisibleEdge(pos);
    int start = sortedPoints.indexOf(visiblePoint);
    for (int i = start; i < points.size() + start + 1; i++) {
      Point currSorted = sortedPoints.get(i % points.size()); // current point in sorted order
      Point nextSorted = sortedPoints.get((i + 1) % points.size()); // next point in sorted order
      Point nextNoSort = points.get((points.indexOf(currSorted) + 1) % points.size()); // next point in actual polygon

      for (int i = 0; i < points.size(); i++) {
        Point b = getPoint(i);
        Point c = getPoint(i+1);
        if (b != currSorted && b != nextSorted && b != nextNoSort
      }

      if (nextNoSort == nextSorted) { // no windows - edge is completely visible:
        visPoly.add(currSorted.x, currSorted.y);
        visPoly.add(nextSorted.x, nextSorted.y);
      } else if (segmentsIntersect(pos, nextSorted, currSorted, nextNoSort)) { // nextSorted is hidden - add to edges list for later...
        
      } else { // nextSorted is partially hiding the edge we are currently looking at:
        Point newWindow = Point.lineIntersect(pos, nextSorted, currSorted, nextNoSort);
        visPoly.add(currSorted.x, currSorted.y);
        visPoly.add(newWindow.x, newWindow.y);
        visPoly.add(nextSorted.x, newxtSorted.y);
      }
    }


    Polygon visPoly = new Polygon(vp);
    

    return null;
  }
  */

/*
  // Compute the visibility polygon of a point inside this polygon:
  Polygon computeVisibility(Point pos) {
    
    Point b, c, windowPt;
    Edge peekEdge;
    VisState state = VisState.FORWARD;
    
    // Find first visible edge in arbitrary direction:
    //int end = findVisibleEdge(pos, (float)(p.PI / 2.0));
    //Point endVisiblePoint = visiblePoint;
    int start = findVisibleEdge(pos, 0);

    // Determine which way we are rotating:
    _left_turn = pos.isLeftTurn(visiblePoint, getPoint(start + 1));
    p.print("_left_turn = " + p.str(_left_turn) + "\n");

    Stack stack = new Stack<Edge>();
    //stack.push(new Edge(vp, pos, visiblePoint));
    
    Edge prevEdge = new Edge(vp, visiblePoint, getPoint(start + 1));
    stack.push(prevEdge);
    for (int i = start; i < points.size() + start + 1; i++) {
      b = getPoint(i);
      c = getPoint(i + 1);
      if (isSameTurn(pos, b, c)) {
        //p.print("isLeftTurn(" + pos.toString() + ", " + b.toString() + ", " + c.toString() + ") => TRUE\n");
        if (state == VisState.FORWARD) {
          // Already moving forward - add the new edge to our stack:
          stack.push(new Edge(vp, b, c));
        } else if (state == VisState.UPWARD_BACKTRACK) {
          // Need to check if this new left turn makes us visible now:
          peekEdge = (Edge)stack.peek(); // TODO: stack empty :-( 
          if (isSameTurn(pos, peekEdge.b, c)) {
            // The end-point of the new edge is visible - add intersecting point (window)
            // and end-point of new edge to stack:
            windowPt = Point.lineIntersect(pos, peekEdge.b, b, c);
            windowPt.setViewport(vp);
            stack.push(new Edge(vp, peekEdge.b, windowPt)); // "Window" edge along pos's line of sight past peekEdge.b
            stack.push(new Edge(vp, windowPt, c)); // Edge from window intersection point to endpoint of new visible segment.
            state = VisState.FORWARD;
          }
        }
      } else {
        // Right turn - go into one of two backtracking modes... (TODO: general position verification)
        if (state == VisState.FORWARD) {
          // Possibly going invisible - need to check if above or below previous edge
          // (downward or upward backtrack as described in notes: http://www.cs.tufts.edu/comp/163/lectures/163-chapter07.pdf)
        
          //peekEdge = (Edge)stack.peek();
          if (isSameTurn(prevEdge.a, b, c)) {
            // This is a downward backtrack
            Edge lastPopped;
            do {
              // Keep popping until we see a right turn (i.e. visible 
              lastPopped = (Edge)stack.pop();
              p.print("pop(" + lastPopped.toString() + ") - \n");
              peekEdge = null;
              if (stack.empty()) break; // Nothing visible - stop popping? TODO: bad...
              peekEdge = (Edge)stack.peek();
            } while (Point.segmentsIntersect(pos, peekEdge.a, b, c));
            
            // Create window on 'e' from downward backtrack
            //windowPt = Point.lineIntersect(pos, c, lastPopped.b, peekEdge.a);
            //windowPt.setViewport(vp);
            //stack.push(new Edge(vp, lastPopped.a, windowPt));
            //stack.push(new Edge(vp, windowPt, c));
            
            // Now check to see if we entered a window - if so enter upward-backtrack mode:
            
            // Stack should never be empty...
            //if (peekEdge != null && peekEdge.a.isRightTurn(peekEdge.b, c)) {
            if (peekEdge != null && peekEdge.a.isRightTurn(peekEdge.b, c)) {
              // Entered a window - go into upward backtrack mode
              state = VisState.UPWARD_BACKTRACK;
              // Push the new window created by the upward backtrack onto the stack: ???
              //windowPt = Point.lineIntersect(pos, peekEdge.b, b, c);
              //windowPt.setViewport(vp);
              //stack.push(new Edge(vp, windowPt, newWindow));
            } else {
              
              // As described on page 14 of the notes (link ~10 lines up) we do not need to push
              // edge to 'c' onto the stack because it will necessarily be hidden b/c Jordan-Curve-Theorem.
            }

            // Need to recompute the edge on top of the stack because now the endpoint (peekEdge.b)
            // is hidden by the downward backtrack, but peekEdge.a is not:
            Edge e = (Edge)stack.peek();
            e.b = Point.lineIntersect(pos, c, e.a, e.b); // Shorten the existing edge
            stack.push(new Edge(vp, e.b, c)); // Add the new window
          } else {
            // This is an upward backtrack - ignore this edge and go into UPWARD_BACKTRACK state
            state = VisState.UPWARD_BACKTRACK;
          }
        
        } else if (state == VisState.UPWARD_BACKTRACK) {
          // Right turn and already invisible - IGNORE
        }
      }
      prevEdge = new Edge(vp, b, c); // Save previous edge for use in next loop to see which way we are turning
    }
    //stack.push(new Edge(vp, pos, endVisiblePoint));
    Polygon ret = new Polygon(vp, stack);
    return ret;
  }
*/

}

