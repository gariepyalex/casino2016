# casino2016

This is the game presented by IFT-GLO at the 2016 casino night of the FSG festival. This is a simple gambling game where the players choose left or right to avoid incoming asteroids. 


### Run the application:

To run the application from command-line, you first need to build the front-end code:
```
lein cljsbuild once
```

Then, do:
```
lein run
```

This will start the application on port 8000.

### Figwheel (development):

To run from repl with figwheel (after cider-jack-in), do:
```
user=> (start)
```
This function is defined in the ```dev/user.clj``` file.

To stop figwheel, do:
```
user=> (stop)
```
