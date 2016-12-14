class GameObject extends Object implements Comparable {
  // Anything that gets updated and rendered every frame
  PVector location;
  int width;
  int height;
  int layer;

  GameObject() { // cursor constructor
    gameObjects.add(this);
  }

  GameObject(int x, int y) { // tile coordinates
    layer = 2;
    gameObjects.add(this);
  }

  GameObject(PVector l, int w, int h) { // ui elements
    location = l;
    width = w;
    height = h;
    layer = 1;
    gameObjects.add(this);
  }

  void render() {
    stroke(100);
    fill(250);
    rect(location.x, location.y, width, height);
  }

  void update() {
    render();
  }
  
  boolean hasFocus() {
    return mouseX > location.x && mouseX < location.x + width && mouseY > location.y && mouseY < location.y + height;
  }

  @Override
    int compareTo(Object o) {
    GameObject gameObject = (GameObject) o;
    int compareLayer = gameObject.layer;
    return this.layer-compareLayer;
  }
}

// Tiles and their entities
class Tile {
  // Tiles are game squares.  They can be empty or they can have entities
  // They do not need to update every frame and thus do not extend GameObject
  PVector location;
  int x, y;
  ArrayList entities;

  Tile(PVector l) {
    location = l;
    entities = new ArrayList<Entity>();
  }

  void render() {
    stroke(200);
    fill(250);
    rect(location.x, location.y, TileSize, TileSize);
  }

  void update() {
    render();
  }

  void addEntity(Entity e) {
    e.tile.entities.remove(e);
    entities.add(e);
    e.tile = this;
  }

  boolean hasEntity() {
    return entities.size() > 0;
  }
}