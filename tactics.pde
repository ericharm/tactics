import java.util.*;

// Universal access
GameState gameState;
ArrayList<GameObject> gameObjects; // move into state but remove default adding from base class
Cursor cursor; // move into state but don't add to list, don't need to extend gameobject

// Config
int TileSize = 30;
int Margin = 5;

void setup() {
  size(800, 600);
  PFont font = createFont("SourceSansPro-Regular.otf", 22);
  textFont(font);
  gameState = new LevelOne(25, 18);
  gameState.init();
}

void draw() {
  gameState.update();
}

void mouseReleased() {
  cursor.click = false;
}

void keyPressed() {
  // debugger just lists gameObjects
  for (int i = 0; i < gameObjects.size (); i++) {
    println(gameObjects.get(i).getClass().getName());
  }
}