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

  void update() {
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