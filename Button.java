import processing.core.*;
import java.awt.Color;

public class Button {
  
  Viewport vp;
  PApplet p;
  String txt;      // Button label
  boolean pressed; // Whether or not this button is currently pressed.
  Color bkgColor;
  Color pressedColor;

  private void _init(Viewport vp, String txt, boolean pressed) {
    this.vp = vp;
    this.p = vp.p;
    this.txt = txt;
    this.pressed = pressed;
    this.bkgColor = Palette.get(0);
    this.pressedColor = Palette.get(0,1);
  }

  Button(Viewport vp, String txt) { _init(vp, txt, false); }
  Button(Viewport vp)             { _init(vp, "", false);  }

  void draw() {
    
    if (this.pressed) {
      vp.fillMe(pressedColor);
    } else {
      vp.fillMe(bkgColor);
    }
    
    p.textSize(40);
    p.fill(0);
    
    p.textAlign(p.CENTER, p.CENTER);
    p.text(txt, vp.centerX(), vp.centerY());
  
  }

  void setPress(boolean val) { this.pressed = val; }

  void mousePressed() {
    if (vp.containsMouse()) setPress(true);
  }

}

