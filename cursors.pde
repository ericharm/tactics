// Cursors
class Cursor  extends GameObject {
  boolean click;
  Command command; // can this be moved to pattern cursor?
  ContextMenu menu; // can this be moved to menucursor?

  Cursor() {
    layer = 7;
    location = new PVector(mouseX, mouseY);
    click = true;
  }

  void render() {
    ellipse(location.x, location.y, 10, 10);
  }

  void update() {
    location.x = mouseX;
    location.y = mouseY;

    if (mousePressed  && !click) {
      click = true;
      actOnce();
    }
    render();
  }

  void actOnce() {
    println("X: " + location.x + " Y: " + location.y);
  }
}

class TileCursor extends Cursor {

  Tile[][] tiles; // reference to gameState
  Tile activeTile;

  TileCursor() {
    super();
    tiles = gameState.tiles;
    activeTile = null;
  }

  void render() {
    noStroke();
    if (mousePressed) fill (50, 50, 250, 150);
    else fill(50, 50, 50, 50);
    rect(activeTile.location.x, activeTile.location.y, TileSize, TileSize);
  }

  void update() {
    getActiveTile();
    super.update();
  }

  void actOnce() {
    if (activeTile.hasEntity()) {
      Entity e = (Entity) activeTile.entities.get(0);
      e.onClick();
    }
  }

  void getActiveTile() {
    int x = (mouseX - Margin) / TileSize;
    int y = (mouseY - Margin) / TileSize;
    if (x < gameState.gridWidth && y < gameState.gridHeight) {
      activeTile = tiles[x][y];
    }
  }
}

class PatternCursor extends TileCursor {
  // change this to command cursor perhaps?

  PatternCursor() {
    super();
  }

  void actOnce() {
    command.execute(activeTile);
  }
}

class MenuCursor extends Cursor {
  // This is really more of a ContextMenu-specific cursor
  UIButton activeButton;

  MenuCursor() {
    super();
    activeButton = null;
  }

  void render() {
    super.render();
    if (activeButton != null) {
      fill(255, 255, 0, 100);
      rect(activeButton.location.x, activeButton.location.y, activeButton.width, activeButton.height);
    }
  }

  void update() {
    getActiveButton();
    super.update();
  }

  void actOnce() {
    if (activeButton == null) {
      menu.destroy();
      changeCursor("tile");
    } else {
      activeButton.issueCommand();
    }
  }

  void getActiveButton() {
    boolean set = false;
    for (int i = 0; i < menu.options.length; i++) {
      UIButton b = menu.options[i];
      if (b.hasFocus()) {
        activeButton = b;
        set = true;
      }
    }
    if (!set) activeButton = null;
  }
}

void changeCursor(String type) {
  int choice = 0;
  if (type == "menu") choice = 1;
  if (type == "tile") choice = 2;
  if (type == "pattern") choice = 3;
  gameObjects.remove(cursor);
  switch (choice) {
  case 1:  
    cursor = new MenuCursor();
    break;
  case 2:  
    cursor = new TileCursor();
    break;
  case 3:
    cursor = new PatternCursor();
    break;
  }
}