
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * @author Sheshagiri
 *
 */
public class GameStarter {

    public static void main(String args[]) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }
}

class startListener implements ActionListener {

    boolean gameGoingOn = false;
    boolean isPaused = false;
    GameComponent gameComponent;

    startListener(GameComponent gameComponent) {
        this.gameComponent = gameComponent;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!gameGoingOn) {
            Thread t = new Thread(gameComponent);
            gameComponent.numChances = 4;
            for (int i = 0; i < gameComponent.bricks.length; i++) {
                gameComponent.visibility[i] = true;
            }
            gameComponent.initial = true;
            t.start();
            gameGoingOn = true;
            ((JButton) arg0.getSource()).setText("Pause");
            gameComponent.requestFocusInWindow();
        } else {
            if (!isPaused) {
                if (gameComponent.numChances == 0) {
                    ((JButton) arg0.getSource()).setText("Start Game");
                    gameComponent.requestFocusInWindow();
                    gameGoingOn = false;
                    isPaused = false;
                    gameComponent.numChances = 4;
                } else {
                    gameComponent.pauseGame = true;
                    isPaused = true;
                    ((JButton) arg0.getSource()).setText("Resume");
                    gameComponent.requestFocusInWindow();
                }
            } else {
                gameComponent.pauseGame = false;
                isPaused = false;
                ((JButton) arg0.getSource()).setText("Pause");
                gameComponent.requestFocusInWindow();
            }
        }
    }
}

class MainFrame extends JFrame {
    /*
     *it is the actual window of the game
     * contains status panel and control panel and game component
     */

    public MainFrame() {
        setTitle("Block Breaker");
        setSize(500, 500);

        JPanel statusPanel = new JPanel();
        JLabel lifeLabel = new JLabel("You have ");
        JLabel scoreLabel = new JLabel("Score:");
        GameComponent gameComponent = new GameComponent(lifeLabel, scoreLabel);
        add(gameComponent);
        JPanel controlPanel = new JPanel();

        JLabel welcomeLabel = new JLabel("Welcome to the Block Breaker game");
        controlPanel.add(welcomeLabel);

        statusPanel.add(lifeLabel);
        statusPanel.add(scoreLabel);
        JButton startButton = new JButton("Start Game");
        controlPanel.add(startButton);
        startButton.addActionListener(new startListener(gameComponent));
        //when we click the start button it invokes the action performed method in startlistener class
        gameComponent.startButton = startButton;
        JButton stopButton = new JButton("Exit");
        controlPanel.add(stopButton);
        stopButton.addActionListener(new ActionListener() {
            //stop button will simply exit from the program
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        gameComponent.stopButton = stopButton;
        add(controlPanel, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.NORTH);
    }
}
/*
 *extends JComponent and implements Runnable inerface
 * the main thred of the game
 */

class GameComponent extends JComponent implements Runnable {

    boolean checkGame = false;//checks the game if its going on or not
    boolean pauseGame = false;//pause button
    JButton startButton = null;
    JButton stopButton = null;
    private int brickRows = 5;//number of brick rows/block rows
    private int brickColumns = 10;//number of brick columns/block columns
    public Rectangle2D[] bricks = new Rectangle2D[30];//blocks/bricks array(30)
    boolean[] visibility = new boolean[30];//visibility array to check if the block is there or no
    private Rectangle2D base;//base bar
    private Ellipse2D el;//circle
    private int margin = 30;
    private int brickHeight = 7;
    int brickWidth = 30;//length of the block
    int brickGap = 3;//space between the blocks
    Graphics2D g2;
    boolean xDirection = true;
    boolean yDirection = true;
    int currentX;
    int currentY;
    int baseX = 0;
    int baseY = 0;
    boolean chanceGone = false;
    int numChances = 4;
    int score = 0;
    boolean initial = true;
    double ballX = 0;
    double ballY = 0;
    int lifeX = 10;
    JLabel lifeLabel;
    JLabel scoreLabel;
    BaseKeyListener bkl = new BaseKeyListener(this);//BaseKeyListener instance

    public GameComponent(JLabel lifeLabel, JLabel scoreLabel) {
        this.lifeLabel = lifeLabel;
        this.scoreLabel = scoreLabel;
        this.setFocusable(true);
        //initially make all the brick visibility true
        for (int i = 0; i < bricks.length; i++) {
            visibility[i] = true;
        }
    }

    public void run() {
        //add the keyListener
        this.addKeyListener(bkl);
        while (numChances != 0) {
            if (!pauseGame) {
                ballX = el.getX();
                ballY = el.getY();
                int increment = 1;
                //calculate the x and y directions of the ball
                if (xDirection) {
                    ballX = ballX + increment;
                } else {
                    ballX = ballX - increment;
                }
                if (yDirection) {
                    ballY = ballY - increment;
                } else {
                    ballY = ballY + increment;
                }
                boolean collided = false;
                for (int i = 0; i < bricks.length; i++) {
                    Rectangle2D rect = bricks[i];
                    if (visibility[i]) {
                        //if the ball collides with the brick then make the visibility of the brick false
                        //increase the score by 10
                        if (el.intersects(rect)) {
                            visibility[i] = false;
                            collided = true;
                            Toolkit.getDefaultToolkit().beep();
                            score = score + 10;
                            scoreLabel.setText("Score : " + score);
                        }
                    }
                }
                   //check if the ball collided
                if (collided) {
                    yDirection = !yDirection;
                }
                if (el.getMinX() < 0) {
                    xDirection = true;//move in X positive direction
                } else if ((int) el.getMaxX() > this.getWidth()) {
                    xDirection = false;//move in X negative direction
                }

                if (el.getMinY() < 0) {
                    yDirection = false;
                } else if (el.intersects(base)) {
                    yDirection = true;
                } else if ((int) el.getMaxY() > this.getHeight()) {
                    chanceGone = true;
                    numChances--;
                    //check the number of chances
                    if (numChances == 0) {
                        initial = false;
                    } else {
                        initial = true;
                    }
                }
                this.paint(g2);//calling the paintComponent
                checkGame = false;
                for (int i = 0; i < bricks.length; i++) {
                    if (visibility[i]) {
                        checkGame = true;
                    }
                }
                if (!checkGame || numChances == 0) {
                    initial = true;
                    numChances = 0;
                    scoreLabel.setText("Score : " + score);
                    startButton.doClick();
                    this.paint(g2);//calling the paint component
                    lifeLabel.setText("GAME OVER");
                    JOptionPane.showMessageDialog(this, "your score is : " + score);
                    score = 0;
                    this.removeKeyListener(bkl);//remove the key listener if the thresd is killed
                    break;
                }
            }
            //make the thread sleep for 5 milliseconds
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
    /*
     * paintComponent draws the bricks , base and the ball respectively
     *
     */
    public void paintComponent(Graphics g) {
        g2 = (Graphics2D) g;
        int compWidth = this.getWidth();
        int compHeight = this.getHeight();
        this.setBorder(BorderFactory.createLineBorder(Color.black, 5));
        brickRows = 5;
        brickColumns = 10;
        margin = compWidth / (brickColumns + 5);
        brickHeight = compHeight / (brickRows * 10);
        brickWidth = margin;
        brickGap = margin / 9;

        currentX = margin * 2 + (((brickColumns / 2) - 1) * margin);
        currentY = compHeight - margin;
        baseY = currentY;
        if (initial) {
            //calculate the baseX and bsaeY dynamically from the component width and height
            scoreLabel.setText("Score : " + score);
            ballX = currentX + margin - brickGap * 2;
            ballY = currentY - brickGap * 4;
            initial = false;
            baseX = currentX;

            xDirection = true;
            yDirection = true;
            if (numChances != 0) {
                //set the number of chances left
                lifeLabel.setText(numChances + " Chances Left");

            } else {
                initial = false;
                //set the text to game over and score to current score
                lifeLabel.setText("GAME OVER");
                scoreLabel.setText("Score : " + score);
            }
            int bX = margin * 2;
            int bY = margin * 2;
            int pos = 0;
            int count = 10;
            //for creating the array of bricks with positions
            for (int i = 0; i < brickRows; i++) {
                bX = brickWidth * i + margin * 2 + brickGap * i;
                for (int j = 0; j < count; j++) {
                    Rectangle2D rect = new Rectangle2D.Double(bX, bY, brickWidth, brickHeight);
                    bX = bX + brickWidth + brickGap;
                    bricks[pos] = rect;
                    pos = pos + 1;
                }
                count = count - 2;
                bY = bY + brickHeight + brickGap;
            }
        }
        //drawing the base with black color
        base = new Rectangle2D.Double(baseX, baseY, brickWidth * 2, brickGap);
        g2.draw(base);
        g2.setColor(Color.BLACK);
        g2.fill(base);
        //drawing the ball with green color
        el = new Ellipse2D.Double(ballX, ballY, 10, 10);
        g2.draw(el);
        g2.setColor(Color.green);
        g2.fill(el);



        //draw the brick if visibility is true
        for (int i = 0; i < bricks.length; i++) {
            if (visibility[i] == true) {
                Rectangle2D b = bricks[i];
                g2.draw(b);
                g2.setColor(Color.blue);
                g2.fill(b);
            }
        }
    }
}
/*
 * BaseKeyListener implements KeyListener interface
 * for moving the base left anf right
 */
class BaseKeyListener implements KeyListener {

    GameComponent gameComponent;

    BaseKeyListener(GameComponent gameComponent) {
        this.gameComponent = gameComponent;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        displayInfo(event, "Key pressed");
    }

    @Override
    public void keyReleased(KeyEvent event) {
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }
    /*
     * for handling the key board events
     * to move right press right arrow key(39)
     * to move left press left arrow key(37)
     */
    public void displayInfo(KeyEvent event, String keyStatus) {
        if (!gameComponent.initial) {//check if the game is started if not then the base will not move
            //for left keyCode is 37
            if (event.getKeyCode() == 37) {
                //check if the position of the base is more than the left margin
                if ((gameComponent.baseX - gameComponent.brickGap * 4) > 0) {
                    //if not move the base by some value i.e 4 brickGaps
                    gameComponent.baseX = gameComponent.baseX - gameComponent.brickGap * 4;
                } else {
                    //else move to the starting position
                    gameComponent.baseX = 0;
                }
                //for right keyCode is 39
            } else if (event.getKeyCode() == 39) {
                //move the base to right by 4 brickGaps
                if ((gameComponent.baseX + gameComponent.brickWidth * 2 + gameComponent.brickGap * 4) < gameComponent.getWidth()) {
                    gameComponent.baseX = gameComponent.baseX + gameComponent.brickGap * 4;
                } else {
                    //if not then move the base to end
                    gameComponent.baseX = gameComponent.getWidth() - gameComponent.brickWidth * 2;
                }
            }
        }
    }
}
