
Viewport root    = new Viewport(this);
Viewport vpText  = new Viewport(root, 0.01, 0.01, 0.38, 0.98);
Viewport vpSpace = new Viewport(root, 0.40, 0.01, 0.58, 0.98);
Text text   = new Text(vpText);
Space space = new Space(vpSpace);

void setup() {
  background(255);
  size(1350,900);
  surface.setResizable(true);

  PFont mono = createFont("fonts/UbuntuMono-B.ttf", 32);
  textFont(mono);

}

void draw() {
  background(255);
  text.draw();
  space.draw();
  //fill(100, 150, 200);
  //ellipse(p.x, p.y, 50, 50);
}

void mousePressed() {
  text.mousePressed();
  space.mousePressed();
}

void mouseReleased() {
  text.mouseReleased();
  space.mouseReleased();
}

