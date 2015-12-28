import processing.core.*;
import java.util.ArrayList;
import java.awt.Color;
import java.util.Collections;
import java.util.Stack;

public class Polygon {
 
  Viewport vp;
  PApplet p;
  ArrayList<GeoPoint> points;
  GeoPoint lastGeoPoint;
  GeoPoint firstGeoPoint;
  boolean badTentative; // Whether or not tentative new line is bad (non-simple polygon)
  boolean closed; // Whether or not to connect first and last point in the polygon

  Color lineColor;
  Color intersectColor;

  private void _init(Viewport vp) {
    this.vp = vp;
    this.p = vp.p;
    this.points = new ArrayList<GeoPoint>();
    this.lastGeoPoint = null;
    this.closed = false;
    this.lineColor = Palette.get(2);
    this.intersectColor = Palette.RED;
  }
  Polygon(Viewport vp) { _init(vp); }

  Polygon(Viewport vp, ArrayList<GeoPoint> points) {
    _init(vp);
    this.points = points;
    this.closed = true;
  }

  void add(GeoPoint p) {
    lastGeoPoint = p;
    points.add(p);
  }

  // Whether or not the mouse contains the first point in the polygon
  boolean willNewGeoPointClosePolygon() {
    return (points.size() > 0) && points.get(0).mouseContains;
  }

  void add(float x, float y) { add(new GeoPoint(vp, x, y)); }

  // Draw the given point, updating badTentative if necessary:
  private void drawInternalGeoPoint(GeoPoint a, Color hovorColor) {
    a.draw(hovorColor);
    if (a.mouseContains) {
      badTentative = true;
    }
  }

  // Draw a point in the polygon and the line to the next point
  private void drawGeoPointAndLineToNext(GeoPoint a, GeoPoint b, Color hovorColor) {
    a.drawLineTo(b, lineColor);
    drawInternalGeoPoint(a, hovorColor);
  }

  // Update badTentative and stroke color based on whether or not new line segment is feasible:
  private void checkLineSegment(boolean cndn, GeoPoint a, GeoPoint b, GeoPoint tentative, GeoPoint last) {
    // !closed because only make RED if not closed
    if (!closed && cndn && GeoPoint.segmentsIntersect(a, b, tentative, last)) {
      p.stroke(intersectColor.getRGB());
      badTentative = true;
    } else {
      p.stroke(lineColor.getRGB());
    }
  }

  void draw(GeoPoint tentativeGeoPoint) {

    GeoPoint a, b;
    badTentative = false;
    
    if      (points.size() == 0) return;
    else if (points.size() == 1) {
      // Only one point - draw it.
      drawInternalGeoPoint(points.get(0), Palette.get(3,1));
      return;
    }
    
    // Known: we have 2 or more points already if we reach here.

    // Draw just first point without setting badTentative:
    a = points.get(0); b = points.get(1);
    
    checkLineSegment(points.size() > 2, a, b, tentativeGeoPoint, lastGeoPoint);
    
    a.drawLineTo(b, lineColor);
    //a.draw(Palette.get(3,1)); // Draw first point last so no line overlap

    // Draw all but first and last point:
    for (int i = 1; i < points.size() - 1; i++) {
      a = points.get(i);
      b = points.get(i + 1);
      
      //if (i < points.size() - 2) {
      checkLineSegment(i < points.size() - 2, a, b, tentativeGeoPoint, lastGeoPoint);
      //} else {
      //  p.stroke(lineColor.getRGB());
      //}
      
      p.strokeWeight(2);
      drawGeoPointAndLineToNext(a, b, Palette.RED);
    }

    // Draw the last point in the polygon (not the new / tentative point)
    // and possibly the closing edge:
    GeoPoint last = points.get(points.size() - 1);
    if (closed) {
      p.stroke(lineColor.getRGB());
      last.drawLineTo(points.get(0), lineColor);
    }
    // Draw first and last point last so no line overlap:
    points.get(0).draw(Palette.get(3,1));
    drawInternalGeoPoint(last, Palette.get(3,1));
  }

  // Open / close the polygon.
  void close() { this.closed = true; }
  void open()  { this.closed = false; }

  // Get the point with the maximum x-value:
  GeoPoint getMaxX() {
    GeoPoint m = points.get(0); // TODO empty polygon
    for (int i = 1; i < points.size(); i++) {
      if (m.x() < points.get(i).x()) m = points.get(i);
    }
    return m;
  }
  GeoPoint getMaxY() {
    GeoPoint m = points.get(0);
    for (int i = 1; i < points.size(); i++) {
      if (m.y() < points.get(i).y()) m = points.get(i);
    }
    return m;
  }

  // Does this polygon contain the given point?
  boolean contains(GeoPoint p) {
    if (!closed) {
      this.p.print("WARNING: Testing if a non-closed polygon contains a point...\n");
      return false;
    }

    // Ad-hoc point "at infinity" (necessarily outside of convex hull):
    GeoPoint inf = new GeoPoint(vp, getMaxX().x() * 2, getMaxY().y() * 2);
    //p.drawLineTo(inf); // Draw infinite ray for debugging

    int count = 0;
    for (int i = 0; i < points.size(); i++) {
      count += GeoPoint.segmentsIntersect(getGeoPoint(i), getGeoPoint(i+1), p, inf) ? 1 : 0;
    }
    //count += GeoPoint.segmentsIntersect(points.get(points.size() - 1), points.get(0), p, inf) ? 1 : 0;
    //this.p.print("count = " + this.p.str(count) + "\n");

    return (count % 2) == 1; // Odd number of crossings == inside (JCT)
  }

  boolean contains(float x, float y) { return contains(new GeoPoint(vp,x,y)); }

  // Get the i-th point in the polygon (circular ArrayList):
  GeoPoint getGeoPoint(int i) { return getGeoPoint(points, i); }
  GeoPoint getGeoPoint(ArrayList<GeoPoint> aGeoPoints, int i) { return aGeoPoints.get((i + aGeoPoints.size()) % aGeoPoints.size()); }

  // Convert the given visibility stack into a polygon:
  Polygon(Viewport vp, Stack<Edge> stack) {
    _init(vp);
    p.print("ENDING STACK:");
    Edge curr;
    while (!stack.empty()) {
      curr = (Edge)stack.pop();
      p.print("  " + curr.toString() + "\n");
      add(curr.a.x(), curr.a.y());
    }
    closed = true;
    lineColor = Color.GREEN;
  }

  // No first class functions, therefore this:
  private boolean _left_turn;
  private boolean isSameTurn(GeoPoint a, GeoPoint b, GeoPoint c) {
    return _left_turn ? a.isLeftTurn(b,c) : a.isRightTurn(b,c);
  }

  // Determine the index of the closest vertex in this polygon to the given point:
  int closestVertex(GeoPoint pos) {
    int minGeoPoint = 0;
    float min = pos.distance(getGeoPoint(0));
    float dist;
    for (int i = 1; i < points.size(); i++) {
      dist = pos.distance(getGeoPoint(i));
      if (dist < min) {
        minGeoPoint = i;
        min = dist;
      }
    }
    return minGeoPoint;
  }

  // Shoot off a ray at an angle theta to find the closest visible edge (return index of starting point)
  GeoPoint visibleGeoPoint;
  
  int findVisibleEdge(GeoPoint pos, float theta) {  
    //GeoPoint fake = new GeoPoint(getMaxX().x * 2 * p.cos(theta), getMaxY().y * 2 * p.sin(theta));
    return findVisibleEdge(pos, new GeoPoint(vp, 10 * p.cos(theta), 10 * p.sin(theta)));
  }

  int findVisibleEdge(GeoPoint pos, GeoPoint fake) {
    int minGeoPoint = 0;
    GeoPoint b,c,inter;
    float min = 100; // TODO: no magic
    float dist;
    for (int i = 0; i < points.size(); i++) {
      b = getGeoPoint(i); c = getGeoPoint(i+1);
      if (GeoPoint.segmentsIntersect(pos, fake, b, c)) {
        inter = GeoPoint.lineIntersect(pos, fake, b, c);
        dist = pos.distance(inter);
        if (dist < min) {
          minGeoPoint = i;
          min = dist;
          visibleGeoPoint = inter;
          visibleGeoPoint.radius = (float)0.005; // tiny
        }
      }
    }
    return minGeoPoint;
  }

  int indexOf(GeoPoint p) { return points.indexOf(p); }

  // Can pos see curr? - O(n)
  boolean isGeoPointVisible(GeoPoint pos, GeoPoint curr) {
    for (int j = 0; j < points.size(); j++) {
      if (getGeoPoint(j) == curr || getGeoPoint(j + 1) == curr) continue; // skip edges involving current point
      if (GeoPoint.segmentsIntersect(pos, curr, getGeoPoint(j), getGeoPoint(j+1))) return false;
    }
    return true;
  }

  // Computes list of points pos can see unobstructed by edges in this polygon - O(n^2)
  ArrayList<GeoPoint> findVisibleGeoPoints(GeoPoint pos) {
    ArrayList<GeoPoint> visGeoPoints = new ArrayList<GeoPoint>();
    GeoPoint curr;
    for (int i = 0; i < points.size(); i++) {
      curr = getGeoPoint(i);
      if (isGeoPointVisible(pos, curr)) visGeoPoints.add(curr);
    }
    return visGeoPoints;
  }

  private void addWindowIfExists(GeoPoint pos, int currIndex, ArrayList<GeoPoint> visGeoPoints, ArrayList<GeoPoint> visWithWindows) {
    GeoPoint curr = getGeoPoint(visGeoPoints, currIndex);
    
    GeoPoint nextInSorted = getGeoPoint(visGeoPoints, currIndex + 1);
    GeoPoint nextInPoly   = getGeoPoint(indexOf(curr) + 1);
    GeoPoint prevInPoly   = getGeoPoint(indexOf(curr) - 1);

    boolean turn0 = isSameTurn(pos, curr, nextInPoly);
    boolean turn1 = isSameTurn(pos, curr, prevInPoly);
    if (turn0 != turn1) {
      visWithWindows.add(curr);
      return; // no window here
    }
    
    GeoPoint a,b,fake;
    ArrayList<GeoPoint> intersections = new ArrayList<GeoPoint>();
    float theta = curr.polarTheta(pos);
    fake = new GeoPoint(0 - 10 * p.cos(theta), 0 - 10 * p.sin(theta));
    for (int i = 0; i < points.size(); i++) {
      a = getGeoPoint(i); b = getGeoPoint(i+1);
      if (curr == a || curr == b) continue;
      if (GeoPoint.segmentsIntersect(pos, fake, a, b)) {
        GeoPoint inter = GeoPoint.lineIntersect(pos, fake, a, b);
        inter.setViewport(vp);
        intersections.add(inter);
      }
    }
    if (intersections.size() == 0) {
      System.out.println("ERROR: polygon appears to not be closed.");
      return;
    }
    GeoPoint windowGeoPoint = intersections.get(0);
    GeoPoint currInter;
    for (int i = 1; i < intersections.size(); i++) {
      currInter = intersections.get(i);
      if (pos.distance(currInter) < pos.distance(windowGeoPoint)) {
        windowGeoPoint = currInter;
      }
    }
    
    if (turn0) {
      visWithWindows.add(windowGeoPoint);
      visWithWindows.add(curr);
    } else {
      visWithWindows.add(curr);
      visWithWindows.add(windowGeoPoint);
    }
  }

  private ArrayList<GeoPoint> insertWindows(GeoPoint pos, ArrayList<GeoPoint> visGeoPoints) {
    GeoPoint curr, nextInSorted, nextInPoly, inter;
    boolean turn0, turn1;
    ArrayList<GeoPoint> visWithWindows = new ArrayList<GeoPoint>();
    for (int i = 0; i < visGeoPoints.size(); i++) {
      //curr = getGeoPoint(visGeoPoints, i);
      
      //visWithWindows.add(curr);
      addWindowIfExists(pos, i, visGeoPoints, visWithWindows);
      /*
      nextInSorted = getGeoPoint(visGeoPoints, i + 1);
      nextInPoly = getGeoPoint(indexOf(curr) + 1);
      if (nextInPoly == nextInSorted) continue;
      p.print(p.str(curr.unique_name) + ", " + p.str(nextInSorted.unique_name) + ", " + p.str(nextInPoly.unique_name) + "\n");
      turn0 = isSameTurn(pos, curr, nextInPoly);
      turn1 = isSameTurn(pos, curr, nextInSorted);
      if (turn0 == turn1) { // segment crosses the void cone - make single window point on it
        inter = GeoPoint.lineIntersect(pos, nextInSorted, curr, nextInPoly);
        inter.setViewport(vp);
        visWithWindows.add(inter);
      }
      */
    }
    return visWithWindows;
  }

  // Compute the visibility polygon of the given point:
  Polygon computeVisibility(GeoPoint pos) {
    _left_turn = true;
    ArrayList<GeoPoint> visGeoPoints = findVisibleGeoPoints(pos);
    Collections.sort(visGeoPoints, GeoPoint.Comparators.getPolarTheta(pos));
    visGeoPoints = insertWindows(pos, visGeoPoints);

    Polygon visPoly = new Polygon(this.vp, visGeoPoints);
    visPoly.lineColor = Color.GREEN;
    visPoly.closed = true;
    return visPoly;
  }


  //private static enum VisState { FORWARD, BACKTRACK, UPWARD_BACKTRACK; }

  /*
  Polygon computeVisibility2(GeoPoint pos) {
    GeoPoint b, c, windowPt;
    Edge peekEdge, prevEdge;

    VisState state = VisState.FORWARD;
    int start = findVisibleEdge(pos);
    _left_turn = pos.isLeftTurn(visibleGeoPoint, getGeoPoint(start + 1));

    Stack stack = new Stack<Edge>();
    stack.push(new Edge(vp, visibleGeoPoint, getGeoPoint(start + 1)));
    for (int i = start + 1; i < points.size() + start + 1; i++) {
      prevEdge = getGeoPoint(i - 1);
      b = getGeoPoint(i);
      c = getGeoPoint(i + 1);
      if (isSameTurn(pos, b, c)) {
        if (state == VisState.FORWARD) {                           // CASE 1 - normal forward
          stack.push(new Edge(vp, b, c));
        } else if (state == VisState.UPWARD_BACKTRACK) {           // CASE 2 - upward left turn
          peekEdge = (Edge)stack.peek();
          if (isSameTurn(pos, peekEdge.b, c)) {
            windowPt = GeoPoint.lineIntersec(pos, peekEdge.b, b, c);
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
  Polygon computeVisibility2(GeoPoint pos) {
    Polygon visPoly = new Polygon(vp);
    GeoPoint a,b,c,d;
    GeoPoint aN,bN;   // neighbors of a and b
    for (int i = 0; i < points.size(); i++) {
      for (int j = 0; j < points.size(); j++) {
        boolean edgeIsVisible = (j == (i + 1) % points.size());
        a = getGeoPoint(i);
        b = getGeoPoint(j);
        for (int i0 = 0; i0 < points.size(); i0++) {
          for (int j0 = 0; j0 < points.size(); j0++) {
            //if (i == j || i == i0 || i == j0 || j == i0 || j == j0 || i0 == j0) continue; // only look at distinct points
            c = getGeoPoint(i0); d = getGeoPoint(j0);
            if (
                 (!GeoPoint.segmentsIntersectNotAtEndpoint(pos, a, c, d))
              && (!GeoPoint.segmentsIntersectNotAtEndpoint(pos, b, c, d))
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

  Polygon computeVisibility2(GeoPoint pos) {
    int start = findVisibleEdge(pos);

    // Sort points by angular position relative to visibility point:
    ArrayList<GeoPoint> sortedGeoPoints = new ArrayList<GeoPoint>(points);
    Collections.sort(sortedGeoPoints, GeoPoint.Comparators.getPolarTheta(pos));
    
    // List of starting points to edges we currently intersect with ray from pos through current sorted point in poly:
    ArrayList<GeoPoint> edges = new ArrayList<GeoPoint>();
    
    findVisibleEdge(pos);
    int start = sortedGeoPoints.indexOf(visibleGeoPoint);
    for (int i = start; i < points.size() + start + 1; i++) {
      GeoPoint currSorted = sortedGeoPoints.get(i % points.size()); // current point in sorted order
      GeoPoint nextSorted = sortedGeoPoints.get((i + 1) % points.size()); // next point in sorted order
      GeoPoint nextNoSort = points.get((points.indexOf(currSorted) + 1) % points.size()); // next point in actual polygon

      for (int i = 0; i < points.size(); i++) {
        GeoPoint b = getGeoPoint(i);
        GeoPoint c = getGeoPoint(i+1);
        if (b != currSorted && b != nextSorted && b != nextNoSort
      }

      if (nextNoSort == nextSorted) { // no windows - edge is completely visible:
        visPoly.add(currSorted.x, currSorted.y);
        visPoly.add(nextSorted.x, nextSorted.y);
      } else if (segmentsIntersect(pos, nextSorted, currSorted, nextNoSort)) { // nextSorted is hidden - add to edges list for later...
        
      } else { // nextSorted is partially hiding the edge we are currently looking at:
        GeoPoint newWindow = GeoPoint.lineIntersect(pos, nextSorted, currSorted, nextNoSort);
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
  Polygon computeVisibility(GeoPoint pos) {
    
    GeoPoint b, c, windowPt;
    Edge peekEdge;
    VisState state = VisState.FORWARD;
    
    // Find first visible edge in arbitrary direction:
    //int end = findVisibleEdge(pos, (float)(p.PI / 2.0));
    //GeoPoint endVisibleGeoPoint = visibleGeoPoint;
    int start = findVisibleEdge(pos, 0);

    // Determine which way we are rotating:
    _left_turn = pos.isLeftTurn(visibleGeoPoint, getGeoPoint(start + 1));
    p.print("_left_turn = " + p.str(_left_turn) + "\n");

    Stack stack = new Stack<Edge>();
    //stack.push(new Edge(vp, pos, visibleGeoPoint));
    
    Edge prevEdge = new Edge(vp, visibleGeoPoint, getGeoPoint(start + 1));
    stack.push(prevEdge);
    for (int i = start; i < points.size() + start + 1; i++) {
      b = getGeoPoint(i);
      c = getGeoPoint(i + 1);
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
            windowPt = GeoPoint.lineIntersect(pos, peekEdge.b, b, c);
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
            } while (GeoPoint.segmentsIntersect(pos, peekEdge.a, b, c));
            
            // Create window on 'e' from downward backtrack
            //windowPt = GeoPoint.lineIntersect(pos, c, lastPopped.b, peekEdge.a);
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
              //windowPt = GeoPoint.lineIntersect(pos, peekEdge.b, b, c);
              //windowPt.setViewport(vp);
              //stack.push(new Edge(vp, windowPt, newWindow));
            } else {
              
              // As described on page 14 of the notes (link ~10 lines up) we do not need to push
              // edge to 'c' onto the stack because it will necessarily be hidden b/c Jordan-Curve-Theorem.
            }

            // Need to recompute the edge on top of the stack because now the endpoint (peekEdge.b)
            // is hidden by the downward backtrack, but peekEdge.a is not:
            Edge e = (Edge)stack.peek();
            e.b = GeoPoint.lineIntersect(pos, c, e.a, e.b); // Shorten the existing edge
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
    //stack.push(new Edge(vp, pos, endVisibleGeoPoint));
    Polygon ret = new Polygon(vp, stack);
    return ret;
  }
*/

}

