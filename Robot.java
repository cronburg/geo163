import processing.core.*;

public class Robot {

  Point pos; // Current position of the robot
  float velocity; // Distance robot moves per key press
  // TODO: more general velcity vector and/or friction

  void _init(Point p, float v) {
    this.pos = p;
    this.velocity = v;
  }

  Robot(Point p)          { _init(p, (float)0.01); }
  Robot(Point p, float v) { _init(p, v); }

  void draw() {
    pos.draw();
  }

  void goUp() {
    pos.moveDelta(0, 0 - velocity);
  }

  void goDown() {
    pos.moveDelta(0, velocity);
  }

  void goLeft() {
    pos.moveDelta(0 - velocity, 0);
  }

  void goRight() {
    pos.moveDelta(velocity, 0);
  }

}

