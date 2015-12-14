import processing.core.*;
import java.awt.Color;

public final class Palette {
  static Color[] primary = {
    new Color(0xFFC7AA),
    new Color(0xD48E6A),
    new Color(0xAA5F39),
    new Color(0x803915),
    new Color(0x551D00)
  };
  
  static Color[] second = {
    new Color(0xFFDDAA),
    new Color(0xD4AA6A),
    new Color(0xAA7D39),
    new Color(0x805515),
    new Color(0x553300),
  };

  static Color[] third = {
    new Color(0x738CA6),
    new Color(0x4A6A8A),
    new Color(0x2B4C6F),
    new Color(0x133253),
    new Color(0x041D37)
  };

  static Color[] fourth = {
    new Color(0x71AA97),
    new Color(0x478D76),
    new Color(0x267158),
    new Color(0x0E553E),
    new Color(0x003926)
  };

  public static Color get(int i, int j) {
    if (j < 0 || j > 4) return Color.BLACK;
    if (i == 0) return primary[j];
    if (i == 1) return second[j];
    if (i == 2) return third[j];
    if (i == 3) return fourth[j];
    return Color.BLACK;
  };

  public static Color get(int i) { return get(i,0); }

}

