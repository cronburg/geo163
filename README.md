Computational Geometry Final Project
====================================

Comp 163 project @ Tufts.

For most recent compiled version of the dynamic visibility polygon
computation description, see:

[Dynamic Visibility description](https://www.eecs.tufts.edu/~karl/geo/dynamic-visibility.pdf)

Or it can be compiled by running *make* in the *paper* directory.

System Requirements:
--------------------
- processing-java
- mktemp

To run this, you need to clone this repository into a directory
named 'geo', then cd into the repo and run './run'.

Currently you can select a chain of points to form a polygon
(which you must then close off). The polygon must be
simple. You can then place a point inside of the polygon to
be the guard, after which a visibility polygon is computed.
Clicking 'continue' then allows you to use the arrow keys to
move the guard around, adjusting the visibility polygon
accordingly.

Experimental Features:
- Zoom in/out using the scroll on your mouse

Future Work:
- Second player
- Fix the zooming
- Smooth guard velocity based on key press / release timing
- Save / load polygons
- Port to javascript
- Implement dynamic visibility algorithms

