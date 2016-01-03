
Viewport root    = new Viewport(this);
Viewport vpText  = new Viewport(root, 0.01, 0.01, 0.38, 0.98);
Viewport vpSpace = new Viewport(root, 0.40, 0.01, 0.58, 0.98);
Space space = new Space(vpSpace);
Text text   = new Text(vpText, space);

int bkg = 255;

void setup() {
  background(bkg);
  //size(1350,900);
  size(1300,860);
  surface.setResizable(true);

  PFont mono = createFont("fonts/UbuntuMono-B.ttf", 32);
  //textFont(mono);

}

void draw() {
  background(bkg);
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

void mouseWheel(MouseEvent event) {
  float e = event.getCount();
	space.zoom(e == -1);
}

void keyPressed() {
  space.keyPressed(keyCode);
}

void keyReleased() {
  space.keyReleased(keyCode);
}

int POLAR_THETA_METHOD  = 0;
int POLAR_RADIUS_METHOD = 1;

boolean mergesort_compare(GeoPoint p1, GeoPoint p2, int method, GeoPoint origin) {
  if (method == POLAR_THETA_METHOD) return p1.polarTheta(origin) < p2.polarTheta(origin);
  else if (method == POLAR_RADIUS_METHOD) return p1.polarRadius(origin) < p2.polarTheta(origin);
  else {
    print("ERROR: invalid mergesort compare method #" + str(method));
    return false;
  }
}	

/* http://blog.ktbyte.com/2014/06/15/mergesort-for-processing-and-processing-js/ */
void mergesort(ArrayList<GeoPoint> inters, int method, GeoPoint origin) {
 if(inters.size() <= 1) return;
 
 ArrayList<GeoPoint> left = new ArrayList<GeoPoint>();
 ArrayList<GeoPoint> right = new ArrayList<GeoPoint>();
 for(int i = 0 ; i < inters.size(); i++) {
   if(i < inters.size() / 2) left.add(inters.get(i));
   else right.add(inters.get(i));
 }
 
 mergesort(left, method, origin);
 mergesort(right, method, origin);
 
 for(int i=0,j=0,k=0; i < inters.size(); i++) {
   if(k >= right.size() || (j < left.size() && mergesort_compare(left.get(j), right.get(k), method, origin))) {
     inters.set(i, left.get(j++));
   } else {
     inters.set(i, right.get(k++));
   }
 }
}

