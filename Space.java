import processing.core.*;

public class Space {

  Viewport vp;     // This viewport is locked to the screen where it is initialized.
  
  PApplet p;
  State state;

  Polygon room; // The room with all the art!
  Robot guard;  // The robot guard!

  // The current location of the mouse in coordinates relative to the Viewport of
  // this space. This point is allowed to be outside of the Viewport (i.e. x and y
  // need not be in the interval [0,1])
  Point mousePoint;

  Space(Viewport vp) {
    this.vp = vp;
    this.p = vp.p;
    this.state = State.SELECT;
    this.room = new Polygon(vp);
    this.mousePoint = new Point(vp, (float)0.0, (float)0.0);
    this.guard = null;
  }

  // Called when in SELECT state and we clicked inside of this space:
  void newPoint() {
    if (!room.badTentative && room.willNewPointClosePolygon()) {
      room.close();
      state = State.MAKEGUARD;
    } else if (room.badTentative) {
      // TODO: Put this in a text box:
      p.print("Hey! That line intersects with an existing one ");
      p.print("or is too close to another point. Try again.\n");
    } else {
      Point lastPoint = Point.newAbsPoint(vp, p.mouseX, p.mouseY);
      room.add(lastPoint);
    }
  }

  // Called when in MAKEGUARD state and we clicked:
  void makeGuard() {
    // TODO 
    if (room.contains(mousePoint)) {
      guard = new Robot(mousePoint.copy());
    } else {
      p.print("The selected point is not inside the polygon. Please try again.\n");
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
    mousePoint.setAbsX(p.mouseX);
    mousePoint.setAbsY(p.mouseY);
    if (vp.containsMouse()) {
      if (state == State.SELECT) {
        mousePoint.draw(Palette.get(3,3));
        if (room.points.size() > 0) {
          mousePoint.drawLineTo(room.points.get(room.points.size() - 1));
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

}

