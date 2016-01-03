//import processing.core.*;
//import java.awt.Color;

public class Button {
  
  Viewport vp;
  String txt;      // Button label
  boolean pressed; // Whether or not this button is currently pressed.
  color bkgColor;
  color pressedColor;

  private void _init(Viewport vp, String txt, boolean pressed) {
    this.vp = vp;
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
    
    textSize(40);
    fill(0);
    
    textAlign(CENTER, CENTER);
    text(txt, vp.centerX(), vp.centerY());
  
  }

  void setPress(boolean val) { this.pressed = val; }

  void mousePressed() {
    if (vp.containsMouse()) setPress(true);
  }

}

