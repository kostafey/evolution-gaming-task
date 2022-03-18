# Game Server task implementation

## Known limitations of this solution
* No obvious game stages ("Login", "Choose game type", "Make a decision") from 
  the server point of view (partially implemented on the client side).
* No UI for summary page (raw JSON with users turns is shown).

## Key design decisions made, especially if you considered multiple options
* Implemented as Web application on Scala Play + React
* UI interaction is based on periodical HTTP AJAX requests, but for production
  app something better sholuld be considered, WebSockets for example.

##  How to test and launch the solution
* Build and launch app
```bash
git clone https://github.com/kostafey/evolution-gaming-task.git
cd evolution-gaming-task/ui
npm install
npm run build
cd ..
sbt run
```
* Open 2 or more browser tabs and type `http://localhost:9000/`
* Type 2 or more different logins in this tabs
* Select same game types for at least 2 different players

## Optional Tasks implemented
* Web UI added
* Support "multi-tabling" - players can play multiple games in parallel at once
* Any game can be turned into 3, 4 or more players per game by updating 
  `USERS_PER_GAME` value.

Screenshot below shows the example.

Player "1" plays `single-card-game` with Player "2" and Player "1" plays `double-card-game` with Player "3" simultaneously.

![evolution-gaming-task-screenshot](https://user-images.githubusercontent.com/1282079/159043236-412f7f14-2412-46c3-b548-b709b0418cae.png)
