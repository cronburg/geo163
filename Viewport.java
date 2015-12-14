import processing.core.*;
import java.awt.Color;
import java.util.Vector;

public class Viewport {
  PApplet p;
  private float _x, _y, _w, _h;
  private float zoom; // negative == zoomed out, positive == zoomed in
  private float zoomOffset; // offset of zoom location from (0,0) of this viewport

  private void _init(Viewport parent, float _x, float _y, float _w, float _h, float zoom) {
    this._x = parent.getrelX() + _x*parent.getrelW();
    this._y = parent.getrelY() + _y*parent.getrelH();
    this._w = _w * parent.getrelW();
    this._h = _h * parent.getrelH();
    this.zoom = zoom;
    this.p = parent.p;
  }
  
  Viewport(PApplet parent) { _init(parent, 0, 0, 1, 1, 0); }
  Viewport(Viewport parent, float _x, float _y, float _w, float _h) { _init(parent,_x,_y,_w,_h,0); }
  Viewport(Viewport parent, double _x, double _y, double _w, double _h) {
    _init(parent, (float)_x, (float)_y, (float)_w, (float)_h, (float)0);
  }

  // rel == relative == fraction from 0 to 1 of viewport width / height:
  public float getrelX() {return _x;}
  public float getrelY() {return _y;}
  public float getrelW() {return _w;}
  public float getrelH() {return _h;}

  // Get coordinates based on current screen width / height
  public float x() {return p.width  * _x;}
  public float y() {return p.height * _y;}
  public float w() {return p.width  * _w;}
  public float h() {return p.height * _h;}

  // TODO: setters?

  void drawBorder() {
    p.stroke(0x00);
    p.fill(0xffffff);
    p.rect(x(), y(), w(), h());
  }

  float centerX() { return x() + (float)(w() / 2.0); }
  float centerY() { return y() + (float)(h() / 2.0); }

  // Does this Viewport contain the given point (posX, posY)?
  //    posX: x-position in window pixels
  //    posY: y-position in window pixels
  boolean contains(int posX, int posY) {
    return (posX >= x()) && (posX <= x() + w())
        && (posY >= y()) && (posY <= y() + h());
  }

  // Implicitly does the current mouse press contain me?
  boolean contains() { return this.contains(p.mouseX, p.mouseY); }

  // Not to be confused with p.fill() - fill a rectangle corresponding to
  // this Viewport with the given color.
  void fillMe(Color c) {
    p.stroke(0x00);
    p.fill(c.getRGB());
    p.rect(x(), y(), w(), h());
  }

  // Absolute window posotion to relative conversion (relative to the current viewport)
  float toRelX(float val) { return (val - x()) / w(); }
  float toRelY(float val) { return (val - y()) / h(); }
  
  // Converting relative to absolute is affected by the current zoom level:
  float toAbsX(float val) { return x() + val * w(); }
  float toAbsY(float val) { return y() + val * h(); }

  void changeZoom(boolean zoomIn) {
    zoom += (zoomIn ? -1 : 1);
  }

}
