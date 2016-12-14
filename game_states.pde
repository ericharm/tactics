class GameState {
  // These variables are all Level-specific and should be moved to that class
  Tile[][] tiles;
  TurnController turn;
  int gridWidth;
  int gridHeight;

  GameState(int width_, int height_) {
  }

  GameState() {
  }

  void init() {
    // This method can be run after a state is instantiated
    // in case some objects rely on that instance
  }

  void update() {
    Collections.sort(gameObjects);
    for (int i = 0; i < gameObjects.size (); i++) {
      gameObjects.get(i).update();
    }
  }
}

class TitleScreen extends GameState {
  
  TitleScreen(){

  }
}

class Level extends GameState {

  StatusWindow sw;

  Level(int width_, int height_) {
    gridWidth = width_;
    gridHeight = height_;
    gameObjects = new ArrayList<GameObject>();
    turn = new TurnController();
    sw = new StatusWindow(new PVector(5, 552), 400, 40);
    tiles = new Tile[width_][height_];
    for (int x = 0; x < width_; x++) {
      for (int y = 0; y < height_; y++) {
        tiles[x][y] = new Tile(new PVector(x * TileSize+Margin, y * TileSize+Margin));
        tiles[x][y].x = x;
        tiles[x][y].y = y;
      }
    }
  }

  void init() {
    cursor = new TileCursor();
    cursor.click = false;
  }

  void update() {
    background(55);
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        tiles[x][y].update();
      }
    }
    super.update();
  } 
}

class LevelOne extends Level {

  HashMap<String, Integer> statsA;
  HashMap<String, Integer> statsB;
  ArrayList<Ability> abilities;

  LevelOne(int width_, int height_) {
    super(width_, height_);
  }

  void init() {
    // This is a huge mess, hopefully a level editor can read in all
    // this nonsense from an external file (build an interpreter)
    super.init();
    new Entity(10, 10);
    // Initialize character stats
    statsA = new HashMap<String, Integer>();
    statsB = new HashMap<String, Integer>();
    // Initialize character abilities
    abilities = new ArrayList<Ability>();
    // new characters will need to be initialized with stats
    // These can pull from a file
    statsA.put("speed", 30);
    statsA.put("strength", 10);
    statsA.put("hp", 40);
    // Eventually, these will be kept in a file so that
    // bringing that character back in a later level will
    // load those stats unless client decides to overwrite
    statsB.put("speed", 50);
    statsB.put("strength", 8);
    statsB.put("hp", 50);
    abilities.add(new Ability("Move"));
    abilities.add(new Ability("Attack"));
    abilities.add(new Ability("Wait"));
    // For now, our characters have the same abilities
    GameCharacter dave    = new GameCharacter(8, 8, statsA, abilities, "Dave");
    GameCharacter lou     = new GameCharacter(13, 8, statsB, abilities, "Lou");
    GameCharacter seymour = new GameCharacter(14, 8, statsB, abilities, "Seymour");
    // We split them into teams
    Team red = new Team("red", color(200, 20, 10));
    Team blue = new Team("blue", color(10, 20, 200));
    // red and blue
    red.addCharacter(dave);
    blue.addCharacter(lou);
    blue.addCharacter(seymour);
  }
}