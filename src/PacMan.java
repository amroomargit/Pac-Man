import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PacMan extends JPanel implements ActionListener, KeyListener{
    class Block{
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; //U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height){
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        //direction update method
        void updateDirection(char direction){
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();

            /* makes it so that pacman and ghosts can only move in directions they are able to (i.e. not left or right
            in a horizontal corridor */
            this.x += this.velocityX;
            this.y +=  this.velocityY;
            for(Block wall : walls){
                if(collision(this,wall)){
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        //velocity update method
        void updateVelocity(){

            //velocity to move in directions up, down, left, right
            if(this.direction == 'U'){
                this.velocityX = 0;
                this.velocityY = -(tileSize/4);
            }
            else if(this.direction == 'D'){
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if(this.direction == 'L'){
                this.velocityX = -(tileSize/4);
                this.velocityY = 0;
            }
            else if(this.direction == 'R'){
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset(){
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    class Sound {
        private Clip clip;

        public Sound(String filePath) {
            try {
                InputStream audioSrc = getClass().getResourceAsStream(filePath);
                if(audioSrc == null){
                    throw new IOException("Sound file not found: " + filePath);
                }
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioSrc);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }

        //plays sound
        public void play() {
            if (clip != null) {
                clip.setFramePosition(0); //Rewinds audio if already been played
                clip.start();
            }
        }

        //to check if a certain sound is currently playing
        public boolean isPlaying(){
            return clip.isRunning();
        }

        //stop sound
        public void stop() {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }

        //loop sound
        public void loop() {
            if (clip != null) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }

        //sound loop with delayed start
        public void delayedLoop(int delayMillis) {
            if (clip != null) {
                new Thread(() -> {
                    try {
                        Thread.sleep(delayMillis);
                        clip.setFramePosition(0);
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "OOOX X XXrXX X XOOO",
            "OOOX    bpo    XOOO",
            "OOOX X XXXXX X XOOO",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    ScheduledExecutorService scheduler;
    Sound ghostSound;
    Sound startSong;
    Sound eatingSound;
    Sound gameEndSound;

    char[] directions = {'U', 'D', 'L', 'R'}; //up, down, left, right for ghosts
    Random random = new Random();
    int score = 0;
    int lives = 3;
    int level = 1;
    boolean gameOver = false;

    PacMan(){
        setPreferredSize(new Dimension(boardWidth, boardHeight)); //JPanel size
        setBackground(Color.BLACK); //JPanel colour
        addKeyListener(this); //listen for key presses
        setFocusable(true);// JPanel listens to key presses

        //instance variable initialization to allow images to appear in game
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();

        //randomly set ghost direction
        for(Block ghost : ghosts){
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        //calls actionPerformed to trigger repaint loop every 50ms for game frames
        gameLoop = new Timer(50, this); //20fps (1000ms/50ms)
        scheduler = Executors.newSingleThreadScheduledExecutor();

        //Freeze game at start for beginning jingle to play
        scheduler.schedule(()-> {
                    gameLoop.start();
        }, 4000, TimeUnit.MILLISECONDS);

        //Play intro song
        startSong = new Sound("/Intro.wav");
        startSong.play();

        //ghost movement sound
        ghostSound = new Sound("/ghost_sound_new.wav");
        ghostSound.delayedLoop(4000);

        eatingSound = new Sound("/Eating4.wav");

        gameEndSound = new Sound("/game_end.wav");
    }

    public void loadMap(){
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for(int r = 0;r < rowCount;r++){
            for(int c = 0;c < columnCount;c++){
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if(tileMapChar == 'X'){ //wall
                    Block wall = new Block(wallImage,x,y,tileSize,tileSize);
                    walls.add(wall);
                }
                else if(tileMapChar == 'b'){ //blue ghost
                    Block ghost = new Block(blueGhostImage,x,y,tileSize,tileSize);
                    ghosts.add(ghost);
                }
                else if(tileMapChar == 'o'){ //orange ghost
                    Block ghost = new Block(orangeGhostImage,x,y,tileSize,tileSize);
                    ghosts.add(ghost);
                }
                else if(tileMapChar == 'p'){ //pink ghost
                    Block ghost = new Block(pinkGhostImage,x,y,tileSize,tileSize);
                    ghosts.add(ghost);
                }
                else if(tileMapChar == 'r'){ //red ghost
                    Block ghost = new Block(redGhostImage,x,y,tileSize,tileSize);
                    ghosts.add(ghost);
                }
                else if(tileMapChar == 'P'){ //pacman
                    pacman = new Block(pacmanRightImage,x,y,tileSize,tileSize);
                }
                else if(tileMapChar == ' '){ //food
                    Block food = new Block(null,x + 14,y + 14,4,4); //manual positioning since no image needed
                    foods.add(food);
                }
            }
        }
    }

    //paints the screen so that we can see the images of the game
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        g.drawImage(pacman.image,pacman.x, pacman.y, pacman.width, pacman.height,null);

        for(Block ghost : ghosts){
            g.drawImage(ghost.image,ghost.x, ghost.y, ghost.width, ghost.height,null);
        }

        for(Block wall : walls){
            g.drawImage(wall.image,wall.x, wall.y, wall.width, wall.height,null);
        }

        g.setColor(Color.WHITE);
        for(Block food : foods){
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        //score update
        g.setFont(new Font("Arial", Font.PLAIN,18));
        if(gameOver){
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2,tileSize/2);
        }
        else{
            g.drawString("Lives: x" + String.valueOf(lives) +"    Level: " + String.valueOf(level) + "   Score: " + String.valueOf(score), tileSize/2,tileSize/2);
        }
    }

    //updates x and y position of pacman
    public void move(){
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        //check wall collisions
        for(Block wall : walls){
            if(collision(pacman,wall)){
                //move pacman in opposite direction if collision with wall detected
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        //check ghost collisions
        for(Block ghost : ghosts){
            if (collision(ghost,pacman)){
                lives -= 1;
                if(lives == 0){
                    gameOver = true;
                    return;
                }
                resetPositions();
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for(Block wall : walls){
                if(collision(ghost,wall)){
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        //check food collision
        Block foodEaten = null;
        boolean foodEatenThisCycle = false;
        for(Block food : foods){
            if(collision(pacman,food)){ //trigger eating sound if not already playing
                if(!eatingSound.isPlaying()){
                    eatingSound.play();
                }
                foodEaten = food;
                score += 10;
            }
        }

        foods.remove(foodEaten);

        //if hashset is empty then all food has been eaten, level complete, progress to next level
        if (foods.isEmpty()){
            loadMap();
            resetPositions();
            level++;
        }
    }



    //detects object collision in game
    public boolean collision(Block a, Block b){
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    //position reset after pacman loses a life
    public void resetPositions(){
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for(Block ghost : ghosts){
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    //Triggers the paintComponent to paint the screen again so the screen can display game movement
    @Override
    public void actionPerformed(ActionEvent e){
        move(); //updates positions of all objects in the game
        repaint(); //update frame
        if (gameOver){
            gameEndSound.play();
            ghostSound.stop();
            if(eatingSound.isPlaying()){
                eatingSound.stop();
            }
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e){}

    @Override
    public void keyPressed(KeyEvent e){}

    //Triggers if key is pressed and then let go (method used for arrow key functionality)
    @Override
    public void keyReleased(KeyEvent e){

        //press any key to restart game
        if(gameOver){
            loadMap(); //to add all food back into hashset
            resetPositions();
            lives = 3;
            level = 1;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }

        //updates velocity of pacman
        if (e.getKeyCode() == KeyEvent.VK_UP){
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN){
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT){
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT){
            pacman.updateDirection('R');
        }

        //Doing this separately because until this point we won't know if pacman can actually move in that direction
        if(pacman.direction == 'U'){
            pacman.image = pacmanUpImage;
        }
        else if(pacman.direction == 'D'){
            pacman.image = pacmanDownImage;
        }
        else if(pacman.direction == 'L'){
            pacman.image = pacmanLeftImage;
        }
        else if(pacman.direction == 'R'){
            pacman.image = pacmanRightImage;
        }
    }
}
