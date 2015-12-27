import processing.core.*;

public class Text {
  
  Viewport vp;
  PApplet p;
  Space space;

  Button bckBtn; // Go back
  Button fwdBtn; // Go forwards

  Text(Viewport vp, Space s) {
    this.vp = vp;
    this.p = vp.p;
    this.space = s;

    Viewport bckVP = new Viewport(vp, 0.01, 0.90, 0.48, 0.09);
    Viewport fwdVP = new Viewport(vp, 0.51, 0.90, 0.48, 0.09);
    bckBtn = new Button(bckVP, "Go Back");
    fwdBtn = new Button(fwdVP, "Continue");
  }

  void mousePressed() {
    if (vp.containsMouse()) {
      bckBtn.mousePressed();
      fwdBtn.mousePressed();
    }
    if (fwdBtn.pressed && space.state == State.MAKEGUARD) {
      space.state = State.COMPUTE_VIS;
    }
  }
  
  void mouseReleased() {
    bckBtn.setPress(false);
    fwdBtn.setPress(false);
  }

  void draw() {
    vp.drawBorder();
    bckBtn.draw();
    fwdBtn.draw();
  }

}

