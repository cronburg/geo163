import processing.core.*;
import java.awt.Color;
import java.util.Vector;

public class Viewport {
  PApplet p;
  private float _x, _y, _w, _h;
  private float zoom; // negative == zoomed out, positive == zoomed in
  private Point zoomPos; // offset of zoom location from (0,0) of this viewport

  private void _init(Viewport parent, float _x, float _y, float _w, float _h, float zoom) {
    this._x = parent.getrelX() + _x*parent.getrelW();
    this._y = parent.getrelY() + _y*parent.getrelH();
    this._w = _w * parent.getrelW();
    this._h = _h * parent.getrelH();
    this.zoom = zoom;
    this.zoomPos = new Point((float)0.5, (float)0.5); // only zoom around the center of the Viewport for now
    this.p = parent.p;
  }
  
  Viewport(Viewport parent, float _x, float _y, float _w, float _h) { _init(parent,_x,_y,_w,_h,(float)0.0); }
  Viewport(Viewport parent, double _x, double _y, double _w, double _h) {
    _init(parent, (float)_x, (float)_y, (float)_w, (float)_h, (float)0);
  }
  Viewport(PApplet parent) {
    this._x = (float)0.0;
    this._y = (float)0.0;
    this._w = (float)1.0;
    this._h = (float)1.0;
    this.zoom = (float)0.0;
    this.p = parent;
  }

  // rel == relative == fraction from 0 to 1 of viewport width / height:
  private float getrelX() {return _x;}
  private float getrelY() {return _y;}
  private float getrelW() {return _w;}
  private float getrelH() {return _h;}

  // Get coordinates based on current screen width / height
  public float x() {return p.width  * _x;}
  public float y() {return p.height * _y;}
  public float w() {return p.width  * _w;}
  public float h() {return p.height * _h;}
  
  // Instance variables _x, _y, _w, and _h should NOT be used below this line!
  // --------------------------------------------------------------------------
  
  // TODO: setters?

  void drawBorder() {
    p.stroke(0x00);
    p.fill(0xffffff);
    p.rect(x(), y(), w(), h());
  }

  // Draw given text as hover-text over the current mouse location:
  void hoverText(String txt) {
    p.fill(0);
    p.textSize(12);
    p.textAlign(p.LEFT, p.BOTTOM);
    p.text(txt, p.mouseX + 10, p.mouseY + 10); // TODO
  }

  void text(String txt, float xPos, float yPos) { p.text(txt, (int)toAbsX(xPos), (int)toAbsY(yPos)); }

  float centerX() { return x() + (float)(w() / 2.0); }
  float centerY() { return y() + (float)(h() / 2.0); }

  // Does this Viewport contain the given point (posX, posY)?
  //    posX: x-position in window pixels
  //    posY: y-position in window pixels
  boolean containsRel(int posX, int posY) {
    // TODO: have this call contains()? (relative computation)
    return (posX >= x()) && (posX <= x() + w())
        && (posY >= y()) && (posY <= y() + h());
  }

  boolean contains(float posX, float posY) {
    return (posX >= 0.0) && (posX <= 1.0) && (posY >= 0.0) && (posY <= 1.0);
  }

  boolean containsMouse() {
    return contains(toRelX(p.mouseX), toRelY(p.mouseY));
  }

  boolean circleContains(float queryX, float queryY, float posX, float posY, float radius) {
    float x0 = queryX - posX;
    float y0 = queryY - posY;
    return (x0*x0) + (y0*y0) < (radius*radius);
  }
  
  // posX and poxY are in coordinates relative to this Viewport.
  boolean circleContainsMouse(float posX, float posY, float radius) {
    return circleContains(toRelX(p.mouseX), toRelY(p.mouseY), posX, posY, radius);
  }

  // Draw an ellipse given relative coordinates as input.
  void ellipse(float posX, float posY, float e1, float e2) {
    this.p.ellipse(toAbsX(posX), toAbsY(posY), toAbsXLen(e1), toAbsYLen(e2));
  }

  void circle(float posX, float posY, float r) {
    // TODO: don't just assume r is relative to x-width
    this.p.ellipse(toAbsX(posX), toAbsY(posY), toAbsXLen(r), toAbsXLen(r));
  }

  // Not to be confused with p.fill() - fill a rectangle corresponding to
  // this Viewport with the given color.
  void fillMe(Color c) {
    p.stroke(0x00);
    p.fill(c.getRGB());
    p.rect(x(), y(), w(), h());
  }

  // Absolute window position to relative conversion (relative to the current viewport)
  float toRelX(float val) { return (val - x()) / w(); }
  float toRelY(float val) { return (val - y()) / h(); }
  
  // Converting relative to absolute is affected by the current zoom level:
  //float toAbsX(float val) { return x() + toAbsXLen(val) + toAbsXLen(zoomDeltaX(val)); }
  //float toAbsY(float val) { return y() + toAbsYLen(val) + toAbsYLen(zoomDeltaY(val)); }
  float toAbsX(float val) { return x() + toAbsXLen(val); }
  float toAbsY(float val) { return y() + toAbsYLen(val); }

  // Calculate an absolute length given a relative value val:
  float toAbsXLen(float val) { return val * w(); }
  float toAbsYLen(float val) { return val * h(); }

  // TODO: magic scaling factor...
  float zoomDeltaX(float val) {
    float dist = val - zoomPos.x;
    return (float)(0 - 0.05 * zoom * dist);
  }
  float zoomDeltaY(float val) {
    float dist = val - zoomPos.y;
    return (float)(0 - 0.05 * zoom * dist);
  }
  
  void changeZoom(boolean zoomIn) {
    zoom += (zoomIn ? -1 : 1);
  }

}

