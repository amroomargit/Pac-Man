<h1>Pac-Man</h1>
<p>This is a Pac-Man clone coded completely in Java, it has sound included too (so turn your volume up!). Use the arrowkeys to move Pac-Man around, avoid the ghosts, and collect points. There are counters for lives, level, and score in the top left of the screen. Intentional delays have been included at the start and end of the game to let their respective jingles play and to closely mimic the original Pac-Man game.</p>
<br/>
<p>Do note that the arrowkeys do depend on timing a littlebit, as they are coded with the keyReleased() method, so you might have to click multiple times when Pac-Man is at certain openings in the wall to have him go through (I find spamming the arrow when close to the opening works best) (this happens in one block openings in corridors, corners and changing to the opposite direction don't have this happen) <strong>just note that this is intentional to make the game more challenging and intense</strong></p>
<br/>
<a href = "https://imgur.com/a/ezLMNEE">Image of the game being played</a>
<br/>
<br/>
<h1>How It's Made:</h1>
<p>Main Tech Used: Java, JFrame library, javax.sound library</p>
<br/>
<p>I used two classes, App.java for JFrame and PacMan.java for all the actual logistics of coding the game.</p>
<br/>
<p>App.java was really only used for defining the width and height of the game and game window. PacMan.java, as stated previously, holds all the actual code for the game. To build it, I utilized hashsets to store and cycle through the game objects (PacMan, ghosts, food, and walls), and to check for certain triggers, mainly collisions between game objects that trigger certain events and need to be removed/added from the hashsets. Action listeners and key listeners also served a major role, they were responsible for registering arrowkey directions from the user, as well as triggering a repaint for the paintComponent() to update at 1000ms/50ms (aka 20fps) so that the characters could move across the screen. A custom Sound class was implemented using the sound library for sounds to start, stop, loop, and have timed delays. Timer and ScheduledExecutorService objects were utilized to start and stop the game at certain points/delays.</p>
<br/>
<h1>Lessons Learned:</h1>
<p>I have only used JFrame once before, and this is my first time using a sound library, so it served for great experience which I can now use on future projects! I also had to learn how to use Timer and ScheduledExecutorService objects to start and stop the game after certain delays so that music or the game over screen could run without having the game start or reset instantly, so this is now valuable information I have in my repertoire.</p>
