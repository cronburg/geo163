import processing.core.*;

public class Robot {

  Point pos; // Current position of the robot

  Robot(Point p) {
    this.pos = p;
  }

  void draw() {
    pos.draw();
  }

}

