class Entity extends GameObject {
  // Entities live on tiles
  Tile tile;
  boolean clickable;

  Entity(int x, int y) {
    super(x, y);
    tile = gameState.tiles[x][y];
    tile.entities.add(this);
    clickable = false;
  }

  void render() {
    fill(255, 0, 255);
    rect(tile.location.x, tile.location.y, TileSize, TileSize);
  }

  void onClick() {
    println(". . .");
  }
}

class GameCharacter extends Entity {
  // A GameCharacter is an entity that can take actions
  ArrayList<Ability> abilities;
  HashMap<String, Integer> stats;
  int framesUntilNextTurn;
  TurnController turn;
  String name;
  Team team;

  int moveTokens;   // These tokens are intended to allow a char
  int actionTokens; // to execute a move and an action each turn
                    // while status effects may change their values
                    // e.g. Paralyze (no move token) or Berserk (2 attack tokens)

  GameCharacter(int x, int y, HashMap<String, Integer> s, ArrayList<Ability> a, String n) {
    super(x, y);
    moveTokens = 1;
    actionTokens = 1;
    turn = gameState.turn;
    stats = s;
    abilities = a;
    name = n;
    resetSpeed();
    turn.characters.add(this);
  }

  void render() {
    // fill(50, 0, 255);
    fill(team.uniformColor);
    if (framesUntilNextTurn <1) fill(120, 0, 105);
    rect(tile.location.x, tile.location.y, TileSize, TileSize);
  }

  void onClick() {
    listStats();
    if (clickable) {
      ContextMenu cm = new ContextMenu(this);
    } else {
      println("It's not your turn");
    }
  }

  void listStats() {
    Iterator iter = (Iterator) stats.entrySet().iterator();
    String message = name + ": ";
    while(iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      message += entry.getKey() + ": " + entry.getValue() + " ";
      Level gs = (Level) gameState;
      gs.sw.updateText(message);
    }
  }

  void endTurn() {
    resetSpeed();
    moveTokens = 1;
    actionTokens = 1;
    clickable = false;
    turn.characterReady = false;
  }

  void resetSpeed() {
    framesUntilNextTurn = 100 - stats.get("speed");
  }
}

class Ability {

  String name;
  // Command command;
  // int range;

  // String patternType; // Cross, Area, Self
  // Some abilities might combine patterns
  // e.g. Heal could work on allies within an area
  // or on yourself

  Ability(String n) {
    name = n;
  }
}

class Team {
  ArrayList<GameCharacter> characters;
  String name;
  color uniformColor;
  
  Team(String n, color c) {
    name = n;
    uniformColor = c;
    characters = new ArrayList<GameCharacter>();
  }

  void addCharacter(GameCharacter gc) {
    characters.add(gc);
    gc.team = this;
  }

  boolean hasCharacter(GameCharacter gc) {
    return characters.contains(gc);
  }
}