import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

// utils class for helpful methods
class Utils {

  // takes a list of SeamInfo and finds the one with the lowest totalWeight
  public SeamInfo findCheapest(ArrayList<SeamInfo> list) {
    int si = 0;
    for (int i = 1; i < list.size(); i++) {
      if (list.get(si).totalWeight > list.get(i).totalWeight) {
        si = i;
      }
    }
    return list.get(si);
  }
}

// represents the 2D Deque that extends World
// contains the sentinel, its height and width
// the boolean represents whether the shrinking process is paused or not
// the direction determines whether to shrink vertically or horizontally
class Picture extends World {
  Border header;
  int width;
  int height;
  boolean shrink;
  String direction;
  boolean gray;
  boolean random;

  // constructor takes in a fileName that takes the picture
  // turns it into a ComputedPixelImage
  // and turns that into a grid of pixel representation that is
  // kept in header which acts as a sentinel
  // also keeps track of the width and height for image computation purposes
  Picture(String fileName) {
    FromFileImage image = new FromFileImage(fileName);
    this.header = new Border();
    this.width = (int) image.getWidth();
    this.height = (int) image.getHeight();
    this.header.makeGrid(image);
    this.shrink = true;
    this.direction = "v";
    this.gray = false;
    this.random = false;
  }

  // draws the Picture as an image
  public WorldScene makeScene() {
    WorldScene scene = getEmptyScene();
    ComputedPixelImage pic = new ComputedPixelImage(this.width, this.height);
    ABlock v = this.header.accessPoint;
    for (int i = pic.height - 1; i > -1; i -= 1) {
      for (int c = pic.width - 1; c > -1; c -= 1) {
        if (this.gray) {
          pic.setPixel(i, c, new Color((int) v.brightness() * 255, (int) v.brightness() * 255,
              (int) v.brightness() * 255));
        } else {
          pic.setPixel(i, c, v.color);
        }
        v = v.left;
      }
      v = v.down;
    }
    scene.placeImageXY(pic, this.width / 2, this.height / 2);
    return scene;
  }

  // continuously mutates the image in the desired method
  public void onTick() {
    if (this.shrink) {
      if (this.random) {
        if (Math.random() < 0.5) {
          SeamInfo lowestVerticalSeam = this.findCheapestVertical();
          lowestVerticalSeam.makeRed();
          lowestVerticalSeam.removeSeam();
        }
        else {
          SeamInfo lowestHorizontalSeam = this.findCheapestHorizontal();
          lowestHorizontalSeam.makeRed();
          lowestHorizontalSeam.removeSeam();
        }
      }
    }
    else {
      if (this.direction.equals("v")) {
        SeamInfo lowestVerticalSeam = this.findCheapestVertical();
        lowestVerticalSeam.makeRed();
        lowestVerticalSeam.removeSeam();
      }
      else {
        if (this.direction.equals("h")) {
          SeamInfo lowestHorizontalSeam = this.findCheapestHorizontal();
          lowestHorizontalSeam.makeRed();
          lowestHorizontalSeam.removeSeam();
        }
      }
    }
  }

  SeamInfo findCheapestVertical() {
    ArrayList<SeamInfo> list = new ArrayList<SeamInfo>();
    Pixel v = this.header.accessPoint;
    for (int j = this.width - 1; j > -1; j -= 1) {
      new SeamInfo(v);
      v = (Pixel) v.left;
    }
    ArrayList<SeamInfo> holder = list;
    Pixel starting = (Pixel) this.header.accessPoint.down;
    Utils u = new Utils();
    for (int i = this.height - 2; i > -1; i -= 1) {
      for (int j = this.width - 1; j > -1; j -= 1) {
        ArrayList<SeamInfo> compare = new ArrayList<SeamInfo>();
        if (j == this.width - 1) {
          compare.add(holder.get(j));
          compare.add(holder.get(j - 1));
        }
        else if (j == 0) {
          compare.add(holder.get(j));
          compare.add(holder.get(j + 1));
        }
        else {
          compare.add(holder.get(j));
          compare.add(holder.get(j - 1));
          compare.add(holder.get(j + 1));
        }
        SeamInfo origin = u.findCheapest(compare);
        list.set(j, new SeamInfo(starting, origin));
      }
      holder = list;
      starting = (Pixel) starting.down;
    }
    return u.findCheapest(list);
  }
  
  SeamInfo findCheapestHorizontal() {
    ArrayList<SeamInfo> list = new ArrayList<SeamInfo>();
    Pixel v = this.header.accessPoint;
    for (int i = this.height - 1; i > -1; i -= 1) {
      new SeamInfo(v);
      v = (Pixel) v.down;
    }
    ArrayList<SeamInfo> holder = list;
    Pixel starting = (Pixel) this.header.accessPoint.left;
    Utils u = new Utils();
    for (int j = this.width - 2; j > -1; j -= 1) {
      for (int i = this.height - 1; i > -1; i -= 1) {
        ArrayList<SeamInfo> compare = new ArrayList<SeamInfo>();
        if (i == this.height - 1) {
          compare.add(holder.get(i));
          compare.add(holder.get(i - 1));
        }
        else if (i == 0) {
          compare.add(holder.get(i));
          compare.add(holder.get(i + 1));
        }
        else {
          compare.add(holder.get(i));
          compare.add(holder.get(i - 1));
          compare.add(holder.get(i + 1));
        }
        SeamInfo origin = u.findCheapest(compare);
        list.set(i, new SeamInfo(starting, origin));
      }
      holder = list;
      starting = (Pixel) starting.left;
    }
    return u.findCheapest(list);
  }

  // uses key strokes to change the image in the desired way
  public void onKeyEvent(String key) {
    if (key.equals("space")) {
      this.shrink = !this.shrink;
    }
    if (key.equals("v")) {
      this.direction = "v";
    }
    if (key.equals("h")) {
      this.direction = "h";
    }
    if (key.equals("g")) {
      this.gray = !this.gray;
    }
  }
}

// represents a block in the grid that represents a picture
abstract class ABlock {
  ABlock left;
  ABlock up;
  ABlock right;
  ABlock down;
  Color color;

  // blocks have pointers that point left, up, right, down and contain its color
  ABlock(ABlock left, ABlock up, ABlock right, ABlock down, Color color) {
    this.left = left;
    this.up = up;
    this.right = right;
    this.down = down;
    this.color = color;
  }

  // returns the brightness of an ABlock
  // brightness of the sentinel (Border) will always come up as 0 since it is
  // black
  double brightness() {
    return ((double) (this.color.getRed() + this.color.getBlue() + this.color.getGreen())) / 765;
  }

  // returns the energy of an ABlock
  abstract double energy();

  // updates up/down pointers of seam removal
  abstract void updateVert();

  // updates left/right pointers of seam removal
  abstract void updateHoriz();

  abstract void updateLeft(ABlock node);

  abstract void updateRight(ABlock node);

  abstract void updateDown(ABlock node);

  abstract void updateUp(ABlock node);
}

// represents the sentinel of the 2D deque grid that represents a picture
class Border extends ABlock {
  Pixel accessPoint;

  // a sentinel is imagined to be a black border
  Border() {
    super(null, null, null, null, Color.BLACK);
    this.accessPoint = null;
    createBorder();
  }

  // a sentinel by itself will point to itself
  void createBorder() {
    this.left = this;
    this.up = this;
    this.right = this;
    this.down = this;
  }

  // uses the grid to create pixels in a 2D linked list and
  // and the access point is the top right pixel
  void makeGrid(FromFileImage grid) {
    ABlock below = this;
    ABlock behind = this;
    ABlock rowHead = this;
    Pixel current = new Pixel(Color.BLACK);
    // uses a nested for loop that basically parses through a
    // ComputedPixelImage by using each Pixel's Posn
    // it parses by looking through every x coordinate per y coordinate
    // so it looks row by row starting left to right from bottom up
    for (int i = 0; i < (int) grid.getHeight(); i += 1) {
      // for every x coordinate it gets the color at that point
      // and creates a Pixel (the Pixel will always have its up and right field as
      // the sentinel since it expands in that direction while keeping track of
      // what is below and to the left
      // this loop takes advantage of the fact that Pixel is designed in a way where
      // when its set it will auto set itself as the opposite direction of the ABlocks
      // around it
      // after setting that Pixel it will update the behind local variable as the left
      // of the sentinel
      // which as the head of the row
      // it will also set below as the Block to the right of it unless on the first
      // row in which
      // below should always be the sentinel
      for (int j = 0; j < (int) grid.getWidth(); j += 1) {
        current = new Pixel(behind, this, this, below, grid.getColorAt(j, i));
        behind = current;
        below = below.right;
        if (j == 0) {
          rowHead = current;
        }
      }
      // after the x coordinate loop terminates it will prep for the new y coordinate
      // by setting behind as the sentinel and below as the right of the sentinel
      // which is
      // the first pixel of the previous row
      behind = this;
      below = rowHead;
    }
    // at the end the left of the sentinel will be the top right Pixel of the grid
    // in which we set the up right and down back to itself
    // to create well-formness of the sentinel
    this.accessPoint = current;
  }

  // returns a number unobtainable using brightness values so that the seam will
  // never
  // path through the sentinel
  public double energy() {
    return 6;
  }

  // updates up/down pointers of seam removal
  public void updateVert() {
    this.left.up = this.up.left;
    this.left.down = this.down.left;
  }

  // updates left/right pointers of seam removal
  public void updateHoriz() {
    this.left.right = this.right;
    this.right.left = this.left;
  }

  public void updateLeft(ABlock node) {
    // does nothing because its border
  }

  public void updateRight(ABlock node) {
    // does nothing because its border
  }

  public void updateDown(ABlock node) {
    // does nothing because its border
  }

  public void updateUp(ABlock node) {
    // does nothing because its border
  }
}

// represents a Pixel from a picture
class Pixel extends ABlock {

  // empty constructor for a Pixel that doesn't point anywhere
  Pixel(Color color) {
    super(null, null, null, null, color);
  }

  // a well formed Pixel that makes sure it always points to something
  // as well as auto updates its surrounding ABlocks
  Pixel(ABlock left, ABlock up, ABlock right, ABlock down, Color color) {
    super(left, up, right, down, color);
    if (left == null || right == null || down == null || up == null) {
      throw new RuntimeException();
    }
    left.updateRight(this);
    right.updateLeft(this);
    up.updateDown(this);
    down.updateUp(this);
  }

  // calculates the energy of a Pixel
  public double energy() {
    return Math.sqrt(Math.pow(this.horizontalEnergy(this.left, this.right), 2)
        + Math.pow(this.verticalEnergy(this.up, this.down), 2));
  }

  // calculates the Horizontal energy of a Pixel
  public double horizontalEnergy(ABlock left, ABlock right) {
    return 2 * left.brightness() + left.up.brightness() + left.down.brightness()
        - 2 * right.brightness() - right.up.brightness() - right.down.brightness();
  }

  // calculates the Vertical energy of a Pixel
  public double verticalEnergy(ABlock up, ABlock down) {
    return 2 * up.brightness() + up.left.brightness() + up.right.brightness()
        - 2 * down.brightness() - down.left.brightness() - down.right.brightness();
  }

  // updates up/down pointers of seam removal
  public void updateVert() {
    this.left.up = this.up.left;
    this.right.down = this.down.right;
    this.up = this.right.up.left;
    this.down = this.right.down.left;
  }

  // updates left/right pointers of seam removal
  public void updateHoriz() {
    this.left.right = this.right;
    this.right.left = this.left;
  }

  public void updateLeft(ABlock node) {
    this.left = node;
  }

  public void updateRight(ABlock node) {
    this.right = node;
  }

  public void updateDown(ABlock node) {
    this.down = node;
  }

  public void updateUp(ABlock node) {
    this.up = node;
  }
}

// represents the information of a Seam during whichever Pixel it is at
class SeamInfo {
  Pixel pixel;
  double totalWeight;
  SeamInfo cameFrom;

  // constructor for the first Pixel in a Seam where it's cameFrom is null
  SeamInfo(Pixel pixel) {
    this.pixel = pixel;
    this.totalWeight = pixel.energy();
    this.cameFrom = null;
  }

  // it sets the Pixel and identifies the accumulated energy at this pixel
  SeamInfo(Pixel pixel, SeamInfo cameFrom) {
    if (cameFrom == null) {
      throw new RuntimeException();
    }
    this.pixel = pixel;
    this.totalWeight = cameFrom.totalWeight + pixel.energy();
    this.cameFrom = cameFrom;
  }

  // using removeH and removeV it completely removes the seam
  void removeSeam() {
    this.removeSeamH();
    this.pixel.down.up = this.pixel.left;
    this.removeSeamV();
  }

  // makes a Seam red
  void makeRed() {
    this.pixel.color = Color.RED;
    this.cameFrom.makeRed();
  }

  // removes the seam horizontally
  void removeSeamH() {
    this.pixel.updateHoriz();

    if (this.cameFrom != null) {
      this.cameFrom.removeSeamH();
    }
  }

  // removes the seam vertically
  void removeSeamV() {
    this.pixel.updateVert();

    if (this.cameFrom != null) {
      this.cameFrom.removeSeamV();
    }
    else {
      pixel.up.down = pixel.left;
    }
  }
}

class Examples {

  // creates a grid of pixels to test the graph
  Border test = new Border();
  Pixel one = new Pixel(test, test, test, test, Color.BLACK);
  Pixel two = new Pixel(one, test, test, test, Color.BLUE);
  Pixel three = new Pixel(two, test, test, test, Color.cyan);
  Pixel four = new Pixel(test, test, test, one, Color.red);
  Pixel five = new Pixel(four, test, test, two, Color.green);
  Pixel six = new Pixel(five, test, test, three, Color.BLACK);
  Pixel seven = new Pixel(test, test, test, four, Color.BLACK);
  Pixel eight = new Pixel(seven, test, test, five, Color.DARK_GRAY);
  Pixel nine = new Pixel(eight, test, test, six, Color.pink);
  
  SeamInfo s1 = new SeamInfo(one,
      (new SeamInfo(four, (new SeamInfo(eight)))));
  SeamInfo s2 = new SeamInfo(two,
      (new SeamInfo(four, (new SeamInfo(seven)))));
  SeamInfo s3 = new SeamInfo(three,
      (new SeamInfo(five, (new SeamInfo(nine)))));
     

  public boolean testAutoUpdate(Tester t) {
    test.accessPoint = nine;
    return t.checkExpect(test.left, test) 
        && t.checkExpect(test.right, test)
        && t.checkExpect(test.up, test)
        && t.checkExpect(test.down, test)
        && t.checkExpect(test.accessPoint, nine) 
        && t.checkExpect(nine.left, eight)
        && t.checkExpect(nine.left.left, seven)
        && t.checkExpect(nine.down, six)
        && t.checkExpect(seven.right, eight) 
        && t.checkExpect(seven.right.right, nine)
        && t.checkExpect(six.up, nine) 
        && t.checkExpect(two.up.up, eight);
  }

  Border s = new Border();

  // tests calculating brightness
  public boolean testBrightness(Tester t) {
    Pixel colored = new Pixel(Color.YELLOW);
    return t.checkInexact(s.brightness(), 0.0, 0.1)
        && t.checkInexact(colored.brightness(), 0.667, 0.01);
  }
  
  // tests calculating energy
  public boolean testEnergy(Tester t) {
    return t.checkInexact(s.energy(), 6.0, 0.1)
        && t.checkInexact(nine.energy(), .899, 0.001);
  }
  
  public void testUpdateHorizAndUpdateVert(Tester t) {
    // testingUpdateHoriz
    // tests initial left and right of Pixel Two
    t.checkExpect(two.right, three);
    t.checkExpect(two.left, one); 
    
    // tests initial left of Pixel One
    t.checkExpect(one.right, two);
    
    // tests initial right of Pixel Three
    t.checkExpect(three.left, two);
    
    // mutates pointers two by calling updateHoriz
    two.updateHoriz();
    
    // tests left and right of Pixel One,Three
    // and checking if two is removed
    t.checkExpect(one.right, three);
    t.checkExpect(three.left, one);
    
    // testingUpdateVert
    // tests initial up and down of Pixel Five
    t.checkExpect(five.down, two);
    t.checkExpect(five.up, eight); 
    
    // tests initial down of Pixel Two
    t.checkExpect(two.up, five);
    
    // tests initial right of Pixel Eight
    t.checkExpect(eight.down, five);
    
    // mutates pointers two by calling updateVert
    two.updateVert();
    
    // tests left and right of Pixel One,Three
    // and checking if two is removed
    t.checkExpect(two.up, five);
    t.checkExpect(eight.down, five);    
  }
  
  // finds the cheapest Seam in the SeamInfo
  public void testFindCheapestSeam(Tester t) {
    ArrayList<SeamInfo> list = new ArrayList<SeamInfo>();
    list.add(s1);
    list.add(s2);
    list.add(s3);
    
    t.checkExpect(new Utils().findCheapest(list), s3);  
    list.remove(s3);   
    t.checkExpect(new Utils().findCheapest(list), s2);
    list.remove(s2);
    t.checkExpect(new Utils().findCheapest(list), s1);
  }
  
  // tests well formness of the pixel
  public boolean testWellForm(Tester t) {
    return t.checkConstructorException(new RuntimeException(), "Pixel", s, s, s, null, Color.black);
  }
  
  void testImage(Tester t) {
    Picture balloon = new Picture("./images/balloons.jpeg");
    balloon.bigBang(balloon.width, balloon.height);
  }
}