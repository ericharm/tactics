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

  void render() {
    fill(255);
    rect(location.x, location.y, width, height);
    fill(0);
    text(label, location.x + 10, location.y + 32);
  }
  
  void issueCommand() {
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

  void render() {
    super.render();
    fill(20);
    text(message, location.x + 5, location.y + 32);
  }

  void updateText(String s) {
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

  void destroy() {
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