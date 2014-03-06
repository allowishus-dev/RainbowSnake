import java.awt.image.BufferedImage;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;

public class SnakeCanvas extends Canvas implements Runnable, KeyListener {
    private final int BOX_HEIGHT = 16;
    private final int BOX_WIDTH = 16;
    private final int GRID_HEIGHT = 28;
    private final int GRID_WIDTH = 28;
    
    private LinkedList<Point> snake;
    private ArrayList<Point> boxes = new ArrayList<Point>();
    private Point fruit;
    private int direction = Direction.NO_DIRECTION;
    
    private Thread runThread;

    private int score = 0;
    
    public void paint(Graphics g) {
        this.addKeyListener(this);
        this.setPreferredSize(new Dimension(640, 480));

//        placeBoxes(2);

        if (snake == null) {
            snake = new LinkedList<Point>();

            generateDefaultSnake();
            placeFruit();
        }

        if (runThread == null) {
            runThread = new Thread(this);
            runThread.start();
        }
        
        drawFruit(g);
        drawBoxes(g);
        drawGrid(g);
        drawSnake(g);
        drawScore(g);


    }
    
    public void update(Graphics g) {
        // this is the default update method which contains our double buffering
        Graphics offScreenGraphics;
        BufferedImage offScreen = null;
        Dimension d = this.getSize();
        
        offScreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        offScreenGraphics = offScreen.getGraphics();
        offScreenGraphics.setColor(this.getBackground());
        offScreenGraphics.fillRect(0, 0, d.width, d.height);
        offScreenGraphics.setColor(this.getForeground());
        paint(offScreenGraphics);
        
        // flip
        
        g.drawImage(offScreen, 0, 0, this);
        
    }
    
    public void generateDefaultSnake() {
        snake.clear();
        snake.add(new Point(3,3));
        snake.add(new Point(3,2));
        snake.add(new Point(3,1));
        
        direction = Direction.NO_DIRECTION;
    }
    
    public void drawScore(Graphics g) {
        g.drawString("Score: " + score, 0, BOX_HEIGHT * GRID_HEIGHT + 20);
    }
    
    public void drawGrid(Graphics g) {
        // drawing an outside rect
        g.drawRect(0, 0, GRID_WIDTH * BOX_WIDTH, GRID_HEIGHT * BOX_HEIGHT);
        // drawing the vertical lines
//         for (int x = BOX_WIDTH; x < GRID_WIDTH * BOX_WIDTH; x += BOX_WIDTH) {
//             g.drawLine(x, 0, x, BOX_HEIGHT * GRID_HEIGHT);
//         }
//         // drawing the horizontal lines
//         for (int y = BOX_HEIGHT; y < GRID_HEIGHT * BOX_HEIGHT; y += BOX_HEIGHT) {
//             g.drawLine(0, y, GRID_WIDTH * BOX_WIDTH, y);
//         }
    
    }
    
    public void drawBoxes(Graphics g) {
        g.setColor(Color.GRAY);
        for (Point p : boxes) {
            g.fillRect(p.x * BOX_WIDTH, p.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
        }
        g.setColor(Color.BLACK);
    }
    
    public void drawSnake(Graphics g) {
        int red = 5;
        int blue = 5;
        int green = 5;
        int i = 0;
        for (Point p : snake) {
            if (i == 0) {
                if (red < 255) {
                    g.setColor(new Color(red, blue, green));
                    red += 50;
                }
                else if (red == 255) {
                    g.setColor(new Color(red, blue, green));
                    i++;
                }
            }
            if (i == 1) {
                if (blue < 255) {
                    g.setColor(new Color(red, blue, green));
                    red -= 25;
                    blue += 25;
                }
                else if (blue == 255) {
                    g.setColor(new Color(red, blue, green));
                    i++;
                }
            }
            if (i == 2) {
                if (green < 255) {
                    g.setColor(new Color(red, blue, green));
                    blue -= 25;
                    green += 25;
                }
                else if (green == 255) {
                    g.setColor(new Color(red, blue, green));
                    i = 0;
                }
            }

            g.fillRect(p.x * BOX_WIDTH, p.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
        }
        g.setColor(Color.BLACK);
    }
    
    public void drawFruit(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(fruit.x * BOX_WIDTH, fruit.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
        g.setColor(Color.BLACK);
    }
    
    public void move() {
        Point head = snake.peekFirst();
        Point newPoint = head;
        switch (direction) {
        case Direction.NORTH:
            newPoint = new Point(head.x, head.y - 1);
            break;
        case Direction.SOUTH:
            newPoint = new Point(head.x, head.y + 1);
            break;
        case Direction.WEST:
            newPoint = new Point(head.x - 1, head.y);
            break;
        case Direction.EAST:
            newPoint = new Point(head.x + 1, head.y);
            break;
        }
        
        snake.remove(snake.peekLast());
        
        if (newPoint.equals(fruit)) {
            // the snake has hit fruit
            Point addPoint = (Point) newPoint.clone();
            
            switch (direction) {
            case Direction.NORTH:
                newPoint = new Point(head.x, head.y - 1);
                break;
            case Direction.SOUTH:
                newPoint = new Point(head.x, head.y + 1);
                break;
            case Direction.WEST:
                newPoint = new Point(head.x - 1, head.y);
                break;
            case Direction.EAST:
                newPoint = new Point(head.x + 1, head.y);
                break;
            }
            snake.push(addPoint);
            score += 10;
            placeFruit();
            
            if (score == 450) {
                // start level 2
                placeBoxes(1);
                generateDefaultSnake();
                direction = Direction.SOUTH;
            }
            else if (score == 900) {
                // start level 3
                placeBoxes(2);
                generateDefaultSnake();
                direction = Direction.SOUTH;
                
            }
        }
        else if (newPoint.x < 0) {
            newPoint = new Point(GRID_WIDTH - 1, head.y);            
        }
        else if (newPoint.x > GRID_WIDTH - 1) {
            // we went out of bounds, reset game
            newPoint = new Point(0, head.y);            

        } 
        else if (newPoint.y < 0) {
            // we went out of bounds, reset game
            newPoint = new Point(head.x, GRID_HEIGHT -1);            
        }
        else if (newPoint.y > GRID_HEIGHT - 1) {
            newPoint = new Point(head.x, 0);
        }
        else if (snake.contains(newPoint) || boxes.contains(newPoint)) {
            // we ran into ourselves, reset game
            placeBoxes(0);
            score=0;
            generateDefaultSnake();
            return;            
        }
        
        // if we reach this point in code, we're still good
        snake.push(newPoint);
    }
    
    public void placeFruit() {
        Random rand = new Random();
        int randomX = rand.nextInt(GRID_WIDTH);
        int randomY = rand.nextInt(GRID_HEIGHT);
        
        Point randomPoint = new Point(randomX, randomY);
        
        while (snake.contains(randomPoint) || boxes.contains(randomPoint)) {
            randomX = rand.nextInt(GRID_WIDTH);
            randomY = rand.nextInt(GRID_HEIGHT);
            
            randomPoint = new Point(randomX, randomY);
        }
        fruit = randomPoint;
    }
    
    public void placeBoxes(int level) {
        boxes.clear();

        if (level == 0) {
            score = 0;
        }
        else if (level == 1) {  // level 1 boxes
            for (int y = 6; y < 22; y++) {
                boxes.add(new Point(13,y));
            }
        }        
        else if (level == 2) {  // level 2 boxes

            for (int x = 7; x < 10; x++) {
                boxes.add(new Point(x,6));
                boxes.add(new Point(x,12));
            }
            for (int y = 6; y < 13; y++) {
                boxes.add(new Point(10,y));
            }
            
            for (int x = 17; x < 20; x++) {
                boxes.add(new Point(x,15));
                boxes.add(new Point(x,21));
            }
            for (int y = 15; y < 22; y++) {
                boxes.add(new Point(16,y));
            }
        }
    }
    
    public void run() {
        while (true) {
            // runs forevah
           move();
           repaint();
           
           try {
               Thread.currentThread();
               Thread.sleep(100);
           }
           catch (Exception e) {
           
           }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (direction != Direction.SOUTH) {
                    direction = Direction.NORTH;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (direction != Direction.NORTH) {
                    direction = Direction.SOUTH;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (direction != Direction.WEST) {
                    direction = Direction.EAST;
                }
                break;
            case KeyEvent.VK_LEFT:
                if (direction != Direction.EAST) {
                    direction = Direction.WEST;
                }
                break;
        }

    }
}