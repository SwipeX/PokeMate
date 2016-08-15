# PokeMate ![travis](https://travis-ci.org/SwipeX/PokeMate.svg?branch=master) [![codecov](https://codecov.io/gh/SwipeX/PokeMate/branch/master/graph/badge.svg)](https://codecov.io/gh/SwipeX/PokeMate)

##Note: With Niantic's recent update(s), this may or may not be safe to run. Use with extreme caution.

##Features
+ Catches visible pokemon
+ Supports both Ptc & Google Authentication
+ Transfers lowest levels of pokemon
+ Tags pokestops when in range
+ Traverses a set of nodes returned from Google's Directions API
+ Auto evolving when able.
+ Hatches eggs
+ Egg 'cracking' - (hatching an egg? [look at our wiki](https://github.com/SwipeX/PokeMate/wiki/Eggs))
+ Notifications

###Release
You can download the release from the Releases tab, only requirement is Java 8. Instructions can be found on that page.

## Usage

1. Clone this repo
2. Rename `config.properties.template` to `config.properties`
3. Add your PTC/Google username, password, speed, preferred-ball and starting location.
4. Obtain a Google Map's API key from https://developers.google.com/maps/documentation/javascript/get-api-key (For direction use).
5. Compile and Run `PokeMate.java`. Or `java -jar Pokemate.jar` from the jar.

## Planned Features
* User Interface improvments

![Image of map](http://i.imgur.com/pCVLX6y.png)

##Slack
Come join us on slack! [![Slack](http://i.imgur.com/XeDYkVL.png)](https://frozen-sea-68189.herokuapp.com/)

### Credits

* https://github.com/jabbink/PokemonGoBot
* https://github.com/Grover-c13/PokeGOAPI-Java
* https://github.com/mjmfighter/pokemon-go-bot/blob/master/src/main/java/com/mjmfighter/pogobot/LocationWalker.java
