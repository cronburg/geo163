
PVector p;
int dx = 1;
int dy = 1;

void setup() {
  background(255);
  size(900,600);
  surface.setResizable(false);
  p = new PVector(50,50);
}

void draw() {
  background(255);
  fill(100, 150, 200);
  ellipse(p.x, p.y, 50, 50);
  p.x += dx * 10;
  p.y += dy * 10;
  if (p.x > width) dx = -1;
  if (p.x < 0) dx = 1;
  if (p.y > height) dy = -1;
  if (p.y < 0) dy = 1;
}

