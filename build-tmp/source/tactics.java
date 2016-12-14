import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class tactics extends PApplet {



// Universal access
GameState gameState;
ArrayList<GameObject> gameObjects; // move into state but remove default adding from base class
Cursor cursor; // move into state but don't add to list, don't need to extend gameobject

// Config
int TileSize = 30;
int Margin = 5;

public void setup() {
  size(800, 600);
  PFont font = createFont("SourceSansPro-Regular.otf", 22);
  textFont(font);
  gameState = new LevelOne(25, 18);
  gameState.init();
}

public void draw() {
  gameState.update();
}

public void mouseReleased() {
  cursor.click = false;
}

public void keyPressed() {
  // debugger just lists gameObjects
  for (int i = 0; i < gameObjects.size (); i++) {
    println(gameObjects.get(i).getClass().getName());
  }
}
// Commands
class Command {
  // Commands are issued from ContextMenus
  Pattern pattern;
  GameCharacter character;

  Command(String name, GameCharacter c) {
    character = c;
    delegateCommand(name);
  }

  Command(GameCharacter c) {
    character = c;
    // maybe the class of pattern and parameters regarding its size
    // should come from the character's ability
    changeCursor("pattern");
    cursor.command = this;
  }

  public void delegateCommand(String name) {
    int command = 0;
    if (name == "Move") command = 1;
    if (name == "Attack") command = 2;
    if (name == "Wait") command = 3;
    switch(command) {
    case 1:
      if (character.moveTokens > 0) new MoveCommand(character);
      else reject();
      break;
    case 2:
      if (character.actionTokens > 0) new AttackCommand(character);
      else reject();
      break;
    case 3:
      new WaitCommand(character);
      break;
    }
  }

  public void reject() {
    changeCursor("tile");
    println("You can't do that now");
  }

  public void execute(Tile activeTile) {
    println("Executing");
    character.endTurn();
  }
}

class MoveCommand extends Command {

  MoveCommand(GameCharacter c) {
    super(c);
    pattern = new AreaPattern(c.tile.x, c.tile.y);
  }

  public void execute(Tile activeTile) {
    if (pattern.tiles_.contains(activeTile)) {
      if (activeTile.hasEntity()) {
        println("That tile is already occupied.");
      } else {
        activeTile.addEntity(character);
        gameObjects.remove(pattern);
        if (character.actionTokens < 1) character.endTurn();
        else character.moveTokens -= 1;
        changeCursor("tile");
      }
    } else {
      gameObjects.remove(pattern);
      changeCursor("tile");
    }
  }
}

class WaitCommand extends Command {

  WaitCommand(GameCharacter c) {
    super(c);
    println("Waiting. . .");
    // gameObjects.remove(pattern);
    changeCursor("tile");
    c.endTurn();
  }
}

class AttackCommand extends Command {

  AttackCommand(GameCharacter c) {
    super(c);
    pattern = new CrossPattern(c.tile.x, c.tile.y);
  }

  public void cancelAction() {
    println("Not a valid target");
    gameObjects.remove(pattern);
    changeCursor("tile");
  }

  public void execute(Tile activeTile) {
    if (pattern.tiles_.contains(activeTile)) {
      if (activeTile.hasEntity() && character.tile != activeTile) {
        attemptPhysicalAttack(activeTile);
      } else {
        cancelAction();
      }
    } else {
      cancelAction();
    }
  }

  public void attemptPhysicalAttack(Tile activeTile) {
    Object a = activeTile.entities.get(0);
    if (a instanceof GameCharacter) {
      GameCharacter defender = (GameCharacter) a;
      if (defender.team != character.team) {
        new PhysicalAttack(character, defender);
        gameObjects.remove(pattern);
      } else {
        cancelAction();
      }
    } else {
      cancelAction();
    }
  }
}

class PhysicalAttack {

  GameCharacter attacker;
  GameCharacter defender;

  PhysicalAttack(GameCharacter a, GameCharacter d) {
    attacker = a;
    defender = d;
    execute();
  }

  public void execute() {
    int defenderHealth = defender.stats.get("hp");
    int attackerStrength = attacker.stats.get("strength");
    defender.stats.put("hp", defenderHealth - attackerStrength);
    if (attacker.moveTokens < 1) attacker.endTurn();
    else attacker.actionTokens -= 1;
    changeCursor("tile");    
  }
}
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

  public void render() {
    ellipse(location.x, location.y, 10, 10);
  }

  public void update() {
    location.x = mouseX;
    location.y = mouseY;

    if (mousePressed  && !click) {
      click = true;
      actOnce();
    }
    render();
  }

  public void actOnce() {
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

  public void render() {
    noStroke();
    if (mousePressed) fill (50, 50, 250, 150);
    else fill(50, 50, 50, 50);
    rect(activeTile.location.x, activeTile.location.y, TileSize, TileSize);
  }

  public void update() {
    getActiveTile();
    super.update();
  }

  public void actOnce() {
    if (activeTile.hasEntity()) {
      Entity e = (Entity) activeTile.entities.get(0);
      e.onClick();
    }
  }

  public void getActiveTile() {
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

  public void actOnce() {
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

  public void render() {
    super.render();
    if (activeButton != null) {
      fill(255, 255, 0, 100);
      rect(activeButton.location.x, activeButton.location.y, activeButton.width, activeButton.height);
    }
  }

  public void update() {
    getActiveButton();
    super.update();
  }

  public void actOnce() {
    if (activeButton == null) {
      menu.destroy();
      changeCursor("tile");
    } else {
      activeButton.issueCommand();
    }
  }

  public void getActiveButton() {
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

public void changeCursor(String type) {
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
class Effect extends GameObject {

  Effect() {
    layer = 5;
  }
}

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

  public void render() {
    fill(255, 0, 255);
    rect(tile.location.x, tile.location.y, TileSize, TileSize);
  }

  public void onClick() {
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

  public void render() {
    // fill(50, 0, 255);
    fill(team.uniformColor);
    if (framesUntilNextTurn <1) fill(120, 0, 105);
    rect(tile.location.x, tile.location.y, TileSize, TileSize);
  }

  public void onClick() {
    listStats();
    if (clickable) {
      ContextMenu cm = new ContextMenu(this);
    } else {
      println("It's not your turn");
    }
  }

  public void listStats() {
    Iterator iter = (Iterator) stats.entrySet().iterator();
    String message = name + ": ";
    while(iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      message += entry.getKey() + ": " + entry.getValue() + " ";
      Level gs = (Level) gameState;
      gs.sw.updateText(message);
    }
  }

  public void endTurn() {
    resetSpeed();
    moveTokens = 1;
    actionTokens = 1;
    clickable = false;
    turn.characterReady = false;
  }

  public void resetSpeed() {
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
  int uniformColor;
  
  Team(String n, int c) {
    name = n;
    uniformColor = c;
    characters = new ArrayList<GameCharacter>();
  }

  public void addCharacter(GameCharacter gc) {
    characters.add(gc);
    gc.team = this;
  }

  public boolean hasCharacter(GameCharacter gc) {
    return characters.contains(gc);
  }
}
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

  public void render() {
    stroke(100);
    fill(250);
    rect(location.x, location.y, width, height);
  }

  public void update() {
    render();
  }
  
  public boolean hasFocus() {
    return mouseX > location.x && mouseX < location.x + width && mouseY > location.y && mouseY < location.y + height;
  }

  public @Override
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

  public void render() {
    stroke(200);
    fill(250);
    rect(location.x, location.y, TileSize, TileSize);
  }

  public void update() {
    render();
  }

  public void addEntity(Entity e) {
    e.tile.entities.remove(e);
    entities.add(e);
    e.tile = this;
  }

  public boolean hasEntity() {
    return entities.size() > 0;
  }
}
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

  public void init() {
    // This method can be run after a state is instantiated
    // in case some objects rely on that instance
  }

  public void update() {
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

  public void init() {
    cursor = new TileCursor();
    cursor.click = false;
  }

  public void update() {
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

  public void init() {
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

  public void render() {
    noStroke();
    fill(255, 255, 0, 100);
    for (int i = 0; i < tiles_.size (); i++) {
      Tile tile = tiles_.get(i);
      rect(tile.location.x, tile.location.y, TileSize, TileSize);
    }
  }

  public void getPatternTiles(int x, int y) {
    tiles_.add(tiles[x][y]);
  }
}

class CrossPattern extends Pattern {

  CrossPattern(int x, int y) {
    super(x, y);
  }

  public void getPatternTiles(int x, int y) {
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

  public void getPatternTiles(int x, int y) {
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
class TurnController extends GameObject {
  ArrayList<GameCharacter> characters;
  boolean characterReady;
  // instead of watching the whole scene, this object could perhaps
  // create turn instances, which keep track of what's going on
  // for an entity's single turn?

  TurnController() {
    characters = new ArrayList<GameCharacter>();
    characterReady = false;
  }

  public void update() {
    // Is there a character ready?
    for (int i = 0; i < characters.size(); i++) {
      GameCharacter gc = characters.get(i);
      if (gc.framesUntilNextTurn < 1) {
        characterReady = true;
        gc.clickable = true;
      }
    }
    if (!characterReady) {
      for (int i = 0; i < characters.size(); i++) {
        characters.get(i).framesUntilNextTurn--;
      }
    }
  }
}
// UI
class UIElement extends GameObject {

  UIElement(PVector l, int w, int h) {
    super(l, w, h);
    layer = 4;
  }

  UIElement(Entity e) {
  }

  UIElement() {
  }
}

class UIButton extends UIElement {
  // This class should actually be called ContextMenuButton
  String label;
  ContextMenu cm;

  UIButton(PVector l, int w, int h, String t, ContextMenu c) {
    super(l, w, h);
    label = t;
    cm = c;
    layer = 6;
  }

  public void render() {
    fill(255);
    rect(location.x, location.y, width, height);
    fill(0);
    text(label, location.x + 10, location.y + 32);
  }
  
  public void issueCommand() {
    cm.destroy(); 
    Command command = new Command(label, cm.character);
  }
}

class StatusWindow extends UIElement {

  String message;

  StatusWindow(PVector l, int w, int h) {
    super(l, w, h);
    message = "";
  }

  public void render() {
    super.render();
    fill(20);
    text(message, location.x + 5, location.y + 32);
  }

  public void updateText(String s) {
    message = s;
  }
}

class Menu extends UIElement {

  Menu(PVector l, int w, int h) {
    super(l, w, h);
  }

  Menu() {
  }
}

class ContextMenu extends Menu {

  UIButton[] options;
  GameCharacter character;

  ContextMenu(GameCharacter e) {
    character = e;
    location = character.tile.location;
    width = 100;
    layer = 4;
    options = new UIButton[e.abilities.size()];
    for (int i = 0; i < e.abilities.size (); i++) {
      Ability a = e.abilities.get(i);
      options[i] = new UIButton(new PVector(location.x + 5, location.y + 5 + (i * 50)),
                                            90, 50, a.name, this);
    }
    height = (options.length * 50) + 10;
    changeCursor("menu");
    cursor.menu = this;
  }

  public void destroy() {
    for (int i = 0; i < options.length; i++) {
      gameObjects.remove(options[i]);
    }
    gameObjects.remove(this);
  }
}

class TitleMenu extends Menu {

  TitleMenu() {
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "tactics" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
