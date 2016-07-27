# PokeMate ![travis](https://travis-ci.org/SwipeX/PokeMate.svg?branch=master) [![codecov](https://codecov.io/gh/SwipeX/PokeMate/branch/master/graph/badge.svg)](https://codecov.io/gh/SwipeX/PokeMate)


##Features
+ Catches visible pokemon
+ Supports both Ptc & Google Authentication
+ Transfers lowest levels of pokemon
+ Tags pokestops when in range
+ Traverses a set of nodes returned from Google's Directions API
+ Auto evolving when able.
+ Hatches eggs
+ Egg 'cracking'
+ Notifications

###Release
You can download the release from the Releases tab, only requirement is Java 8. Instructions can be found on that page.

## Usage

1. Clone this repo
2. Rename `config.properties.template` to `config.properties`
3. Add your PTC/Google username, password, speed, preferred-ball and starting location.
4. Obtain a Google Map's API key from https://developers.google.com/maps/documentation/javascript/get-api-key (For direction use).
5. Compile and Run `PokeMate.java`. Or `java -jar Pokemate.jar config.properties` from the jar.

## Planned Features
* User Interface improvments

![Image of map](http://i.imgur.com/W4hG8i6.png)

##Slack
Come join us on slack! https://shielded-earth-81203.herokuapp.com/ #pokemate-dev #pokemate-help

### Credits

* https://github.com/jabbink/PokemonGoBot
* https://github.com/Grover-c13/PokeGOAPI-Java
* https://github.com/mjmfighter/pokemon-go-bot/blob/master/src/main/java/com/mjmfighter/pogobot/LocationWalker.java
