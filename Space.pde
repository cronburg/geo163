//import processing.core.*;
//import java.awt.Color;

public class Space {

  Viewport vp;     // This viewport is locked to the screen where it is initialized.
  
  State state;

  Polygon room; // The room with all the art!
  Guard guard;  // The robot guard!

  // The current location of the mouse in coordinates relative to the Viewport of
  // this space. This point is allowed to be outside of the Viewport (i.e. x and y
  // need not be in the interval [0,1])
  GeoPoint mousePoint;

  Space(Viewport vp) {
    this.vp = vp;
    this.state = State.SELECT;
    this.room = new Polygon(vp);
    this.mousePoint = new GeoPoint(vp, (float)0.0, (float)0.0);
    this.guard = null;
  }

  // Called when in SELECT state and we clicked inside of this space:
  void newPoint() {
    if (!room.badTentative && room.willNewGeoPointClosePolygon()) {
      room.close();
      state = State.MAKEGUARD;
    } else if (room.badTentative) {
      // TODO: Put this in a text box:
      print("Hey! That line intersects with an existing one ");
      print("or is too close to another point. Try again.\n");
    } else {
      GeoPoint lastPoint = newAbsGeoPoint(vp, mouseX, mouseY);
      //lastPoint.setHover(true);
      lastPoint.setLabel(true);
      room.add(lastPoint);
    }
  }

  // Runs the visibility polygon computation for our guard:
  // Not in Guard because guard might eventually not be allowed to
  // see entire room / polygon:
  void updateVisibility() {
    guard.vis = room.computeVisibility(guard.pos);
    //guard.vis.lineColor = Palette.get(1);
  }

  // Called when in MAKEGUARD state and we clicked:
  void makeGuard() {
    if (room.contains(mousePoint)) {
      guard = new Guard(mousePoint.copy());
      guard.pos.setConstraint(room); // Constrain the guard's position to be inside the polygon
      updateVisibility();
    } else {
      print("The selected point is not inside the polygon. Please try again.\n");
    }
  }

  void mousePressed() {
    if (vp.containsMouse()) {
      if (state == State.SELECT) newPoint();
      if (state == State.MAKEGUARD) makeGuard();
    }
  }

  // Called when scrolling on mouse wheel:
  void zoom(boolean zoomIn) { vp.changeZoom(zoomIn); }
  
  void mouseReleased() {}

  void draw() {
    vp.drawBorder();
    mousePoint.setAbsX(mouseX);
    mousePoint.setAbsY(mouseY);
    if (vp.containsMouse()) {
      if (state == State.SELECT) {
        mousePoint.draw(Palette.get(3,3));
        if (room.points.size() > 0) {
          mousePoint.drawLineTo(room.points.get(room.points.size() - 1), 0x000000);
        }
      } else if (state == State.MAKEGUARD) {
        if (room.contains(mousePoint)) {
          mousePoint.draw(Palette.get(3,3)); // Draw the potential guard
        }
      }
    }
    room.draw(mousePoint);
    if (null != guard) guard.draw();
  }

  void keyReleased(int keyCode) {
    //TODO
  }

  void keyPressed(int keyCode) {
    if (state == State.COMPUTE_VIS) {
      if      (UP    == keyCode) guard.goUp();
      else if (DOWN  == keyCode) guard.goDown();
      else if (LEFT  == keyCode) guard.goLeft();
      else if (RIGHT == keyCode) guard.goRight();
      
      updateVisibility();
    }
  }

}

