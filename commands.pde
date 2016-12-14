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

  void delegateCommand(String name) {
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

  void reject() {
    changeCursor("tile");
    println("You can't do that now");
  }

  void execute(Tile activeTile) {
    println("Executing");
    character.endTurn();
  }
}

class MoveCommand extends Command {

  MoveCommand(GameCharacter c) {
    super(c);
    pattern = new AreaPattern(c.tile.x, c.tile.y);
  }

  void execute(Tile activeTile) {
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

  void cancelAction() {
    println("Not a valid target");
    gameObjects.remove(pattern);
    changeCursor("tile");
  }

  void execute(Tile activeTile) {
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

  void attemptPhysicalAttack(Tile activeTile) {
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

  void execute() {
    int defenderHealth = defender.stats.get("hp");
    int attackerStrength = attacker.stats.get("strength");
    defender.stats.put("hp", defenderHealth - attackerStrength);
    if (attacker.moveTokens < 1) attacker.endTurn();
    else attacker.actionTokens -= 1;
    changeCursor("tile");    
  }
}