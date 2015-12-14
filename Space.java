import processing.core.*;

public class Space {

  Viewport vp;     // This viewport is locked to the screen where it is initialized.
  
  PApplet p;
  State state;
  boolean closed; // Whether or not the last point connects to the first

  Polygon room; // The room with all the art!

  Point selectPoint;

  Space(Viewport vp) {
    this.vp = vp;
    this.p = vp.p;
    this.state = State.SELECT;
    this.room = new Polygon(vp);
    this.closed = false; // not closed until completed / created
    this.selectPoint = new Point(vp, (float)0.0, (float)0.0);
  }

  // Called when in SELECT state and we clicked inside of this space:
  void newPoint() {
    if (room.badTentative) {
      // TODO: Put this in a text box:
      p.print("Hey! That line intersects with an existing one. Try again.\n");
    } else {
      Point lastPoint = new Point(vp, vp.toRelX(p.mouseX), vp.toRelY(p.mouseY));
      room.add(lastPoint);
    }
  }

  void mousePressed() {
    if (vp.contains()) {
      if (state == State.SELECT) newPoint();
    }
  }

  // Called when scrolling on mouse wheel:
  void zoom(boolean zoomIn) { vp.changeZoom(zoomIn); }
  
  void mouseReleased() {}

  void draw() {
    vp.drawBorder();
    selectPoint.setAbsX(p.mouseX);
    selectPoint.setAbsY(p.mouseY);
    if (vp.contains()) {
      if (state == State.SELECT) {
        selectPoint.draw();
        if (room.points.size() > 0) {
          selectPoint.drawLineTo(room.points.get(room.points.size() - 1));
        }
      }
    }
    room.draw(selectPoint);
  }

}

