This is prototype for a turn-based tactics engine for Processing 3

- A typical 2d tactics games operates on a square grid of tiles

- Tiles can contain Entities (heroes, villains, treasures, rocks...)

- GameCharacters are Entities that can execute Commands when it's their turn

- Commands are executed against other GameCharacters by clicking on their Tiles

- You can constrain the available Commands by providing GameCharacters with specific Abilities

- You can constrain the tiles available to a Command using a Pattern

- You can further constrain how a Command works by grouping GameCharacters into Teams and writing some conditional behavior around what team the target GameCharacter belongs to

- I've used the Cursor object as a way of designating the game's state


