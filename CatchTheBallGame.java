import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.sound.sampled.*;

public class CatchTheBallGame extends JFrame {

    public CatchTheBallGame() {
        setTitle("Catch the Ball");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(600, 600);
        setLocationRelativeTo(null);

        GamePanel panel = new GamePanel();
        add(panel);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CatchTheBallGame::new);
    }
}

class GamePanel extends JPanel implements ActionListener, MouseListener {

    private final int BALL_RADIUS = 30;
    private int ballX, ballY;
    private int score = 0;
    private int highScore = 0;
    private int timeLeft = 30;
    private Timer gameTimer;
    private Timer moveTimer;
    private final Random random = new Random();
    private final String HIGH_SCORE_FILE = "highscore.dat";
    private String playerName;
    private boolean gameOver = false;
    private Image backgroundImage;

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.BLACK);
        loadHighScore();

        playerName = JOptionPane.showInputDialog(this, "Enter your name:");
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }

        addMouseListener(this);

        loadBackground();
        moveBall(); // initial position

        // Timer for countdown
        gameTimer = new Timer(1000, e -> {
            timeLeft--;
            if (timeLeft <= 0) {
                endGame();
            }
            repaint();
        });
        gameTimer.start();

        // Timer for moving the ball
        moveTimer = new Timer(1000, this);
        moveTimer.start();
    }

    private void loadBackground() {
        try {
            backgroundImage = new ImageIcon("background/image.jpg").getImage();
        } catch (Exception e) {
            backgroundImage = null; // fallback: gradient
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Gradient fallback
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, Color.BLUE, getWidth(), getHeight(), Color.CYAN);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw the ball
        g.setColor(Color.RED);
        g.fillOval(ballX, ballY, BALL_RADIUS * 2, BALL_RADIUS * 2);

        // HUD (Score, Time, High Score)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("High Score: " + highScore, 230, 30);
        g.drawString("Time Left: " + timeLeft + "s", 450, 30);

        // Game Over message
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.setColor(Color.YELLOW);
            g.drawString("Game Over!", 200, getHeight() / 2);
        }
    }

    private void moveBall() {
        int panelWidth = Math.max(getWidth(), BALL_RADIUS * 2 + 10);
        int panelHeight = Math.max(getHeight(), BALL_RADIUS * 2 + 60);

        ballX = random.nextInt(panelWidth - BALL_RADIUS * 2);
        ballY = 50 + random.nextInt(panelHeight - BALL_RADIUS * 2 - 50);
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            moveBall();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameOver) return;

        int mx = e.getX();
        int my = e.getY();

        double dist = Math.hypot(mx - (ballX + BALL_RADIUS), my - (ballY + BALL_RADIUS));
        if (dist <= BALL_RADIUS) {
            score++;
            playSound("sound/click.wav");
            moveBall();
            repaint();
        }
    }

    private void endGame() {
        gameOver = true;
        gameTimer.stop();
        moveTimer.stop();
        playSound("sound/end.wav");

        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                (score >= highScore ? "🏆 NEW HIGH SCORE!\n" : "") +
                        playerName + ", your score is: " + score + "\nPlay again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    private void restartGame() {
        score = 0;
        timeLeft = 30;
        gameOver = false;
        moveBall();
        gameTimer.start();
        moveTimer.start();
        repaint();
    }

    private void loadHighScore() {
        try (DataInputStream in = new DataInputStream(new FileInputStream(HIGH_SCORE_FILE))) {
            highScore = in.readInt();
        } catch (IOException e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
            out.writeInt(highScore);
        } catch (IOException e) {
            System.err.println("Failed to save high score");
        }
    }

    private void playSound(String filename) {
        try {
            File soundFile = new File(filename);
            if (!soundFile.exists()) return;

            AudioInputStream audio = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception ex) {
            System.err.println("Sound Error: " + ex.getMessage());
        }
    }

    // Required MouseListener methods
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

