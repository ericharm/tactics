class Pattern extends GameObject {

  Tile[][] tiles;
  ArrayList<Tile> tiles_;
  Tile baseTile;

  Pattern(int x, int y) {
    super(x, y);
    tiles = gameState.tiles;
    baseTile = tiles[x][y];
    layer = 3;
    tiles_ = new ArrayList<Tile>();
    getPatternTiles(x, y);
  }

  void render() {
    noStroke();
    fill(255, 255, 0, 100);
    for (int i = 0; i < tiles_.size (); i++) {
      Tile tile = tiles_.get(i);
      rect(tile.location.x, tile.location.y, TileSize, TileSize);
    }
  }

  void getPatternTiles(int x, int y) {
    tiles_.add(tiles[x][y]);
  }
}

class CrossPattern extends Pattern {

  CrossPattern(int x, int y) {
    super(x, y);
  }

  void getPatternTiles(int x, int y) {
    int range = 1; // work on pulling this value from an ability
    for (int i = 0; i < range; i++) {
      int distance = i;
      if (x > distance) tiles_.add(tiles[x-(distance + 1)][y]);
      if (x < gameState.gridWidth - distance) tiles_.add(tiles[x+(distance + 1)][y]);
      if (y > distance) tiles_.add(tiles[x][y-(distance + 1)]);
      if (y < gameState.gridHeight - distance) tiles_.add(tiles[x][y+(distance + 1)]);
    }
  }
}

class AreaPattern extends Pattern {

  AreaPattern(int x, int y) {
    super(x, y);
  }

  void getPatternTiles(int x, int y) {
    int range = 2; // this should be attached to the ability
    // the cross pattern comes packaged with this logic - can it be mixed into this class?
    for (int i = 0; i < range; i++) {
      int distance = i;
      if (x > distance) {
       tiles_.add(tiles[x-(distance + 1)][y]);
      }
      if (x < gameState.gridWidth - distance) { 
        tiles_.add(tiles[x+(distance + 1)][y]);
      }
      if (y > distance) { 
        tiles_.add(tiles[x][y-(distance + 1)]);
      }
      if (y < gameState.gridHeight - distance) {
        tiles_.add(tiles[x][y+(distance + 1)]);
      }
    }
    // TODO: check to make sure each tile is available
    tiles_.add(tiles[x-1][y-1]);
    tiles_.add(tiles[x+1][y-1]);
    tiles_.add(tiles[x+1][y+1]);
    tiles_.add(tiles[x-1][y+1]);
    // for the future: adding these tiles fills in area for
    // a range of 3
    // tiles_.add(tiles[x-2][y-1]);
    // tiles_.add(tiles[x+2][y-1]);
    // tiles_.add(tiles[x+2][y+1]);
    // tiles_.add(tiles[x-2][y+1]);
    
    // tiles_.add(tiles[x-1][y-2]);
    // tiles_.add(tiles[x+1][y-2]);
    // tiles_.add(tiles[x+1][y+2]);
    // tiles_.add(tiles[x-1][y+2]);
  }
}