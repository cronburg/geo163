//import processing.core.*;
//import java.awt.color;

public static final class Palette {
  static color[] primary = {
    0xFFC7AA,
    0xD48E6A,
    0xAA5F39,
    0x803915,
    0x551D00
  };
  
  static color[] second = {
    0xFFDDAA,
    0xD4AA6A,
    0xAA7D39,
    0x805515,
    0x553300
  };

  static color[] third = {
    0x738CA6,
    0x4A6A8A,
    0x2B4C6F,
    0x133253,
    0x041D37
  };

  static color[] fourth = {
    0x71AA97,
    0x478D76,
    0x267158,
    0x0E553E,
    0x003926
  };

  public static color get(int i, int j) {
    if (j < 0 || j > 4) return 0x000000; // black
    if (i == 0) return primary[j];
    if (i == 1) return second[j];
    if (i == 2) return third[j];
    if (i == 3) return fourth[j];
    return 0x000000; // black
  };

  public static color get(int i) { return get(i,0); }

  public static color RED = 0xff0000;

}

