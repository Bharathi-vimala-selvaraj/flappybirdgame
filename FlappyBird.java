import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    int birdWidth, birdHeight;
    int pipeWidth, pipeHeight;

    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width;
        int height;
        Image img;

        Bird(Image img, int width, int height) {
            this.img = img;
            this.width = width;
            this.height = height;
        }
    }

    class Pipe {
        int x = boardWidth;
        int y = 0;
        int width;
        int height;
        Image img;
        boolean passed = false;

        Pipe(Image img, int width, int height) {
            this.img = img;
            this.width = width;
            this.height = height;
        }
    }

    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = loadImage("resources/Flappybirdbg.png");
        birdImg = loadImage("resources/flappybird.png");
        topPipeImg = loadImage("resources/toppipe.png");
        bottomPipeImg = loadImage("resources/bottompipe.png");

        if (backgroundImg == null || birdImg == null || topPipeImg == null || bottomPipeImg == null) {
            throw new RuntimeException("One or more image files failed to load.");
        }

        birdWidth = birdImg.getWidth(null);
        birdHeight = birdImg.getHeight(null);
        pipeWidth = topPipeImg.getWidth(null);
        pipeHeight = topPipeImg.getHeight(null);

        bird = new Bird(birdImg, birdWidth, birdHeight);
        pipes = new ArrayList<>();

        placePipeTimer = new Timer(1500, e -> placePipes());
        placePipeTimer.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    private Image loadImage(String path) {
    URL url = getClass().getClassLoader().getResource(path);
    if (url == null) {
        System.err.println("ERROR: Image not found -> " + path);
        return null;
    }
    System.out.println("Loaded image: " + url.toExternalForm());
    return new ImageIcon(url).getImage();
}


    void placePipes() {
        int randomPipeY = (int) (0 - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg, pipeWidth, pipeHeight);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg, pipeWidth, pipeHeight);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        } else {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                placePipeTimer.start();
                gameLoop.start();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}