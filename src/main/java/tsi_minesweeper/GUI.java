package tsi_minesweeper;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.*;

public class GUI extends JFrame implements MouseListener, ActionListener {

    public int sizeX = 16;
    public int sizeY = 16;
    public int numBombs = 40;
    public int numflags = 40;
    public int maxLives = 3;
    public int livesRemaining = maxLives;
    public boolean firstMove = true;
    public boolean gameFinished = false;
    public boolean lost = false;

    public int buttonSize = 40;
    public int border = 1;
    public BetterButton[][] buttons;
    public Thread audioThread;

    public JPanel gameArea = new JPanel();
    public JPanel gameScreen = new JPanel();
    public JPanel endScreen = new JPanel();
    public Timer timer;
    public Timer graphicsTimer;
    public JLabel time = new JLabel("Time: ", SwingConstants.CENTER);
    public JLabel lives = new JLabel("Lives: " + livesRemaining, SwingConstants.CENTER);
    public JLabel flags = new JLabel("hippos", SwingConstants.CENTER);
    public boolean flicker = false;
    public int timePassed = 0;
    public JButton newGame = new JButton("New Game");
    public long startTime;
    public ActionListener timerAction;
    public ActionListener graphicAction;
    public Clip audioClip;

    public JButton easy = new JButton("EASY");
    public JButton medium = new JButton("MEDIUM");
    public JButton hard = new JButton("HARD");

    private int endingPos = 0;
    private ArrayList<int[]> bombs = new ArrayList<>();

    public Color winColor = Color.green;
    public Color baseColor = Color.gray;

    public static void main(String[] args) {
        gui();
    }

    public static void gui() {

        GUI frame = new GUI("Minesweeper");
        frame.setVisible(true);
    }

    /**
     Creates a frame with the given title <p>
     The frame contains three buttons for difficulty
     @param title The title for the window
     **/
    public GUI(String title) {
        super(title);
        frameInit();

        try {
            audioClip = AudioSystem.getClip();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }

        this.addMouseListener(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setSize(buttonSize * 2,(buttonSize * 3));
        endScreen.setLayout(new GridLayout(3, 1, border, border));
        easy.addMouseListener(this);
        medium.addMouseListener(this);
        hard.addMouseListener(this);
        easy.setBackground(new Color(170, 215, 81));
        medium.setBackground(new Color(255, 203, 171));
        hard.setBackground(new Color(227, 23, 23));
        endScreen.add(easy);
        endScreen.add(medium);
        endScreen.add(hard);
        this.add(endScreen);

    }

    /**
     * Called once difficulty has been selected <p>
     *     Creates and populates a grid of buttons based on the difficulty selected
     */
    public void setupGame(){
        this.setVisible(false);
        endScreen.setEnabled(false);
        endScreen.setVisible(false);

        GridLayout gridLayout = new GridLayout(sizeY, sizeX, border, border);
        gameScreen.setLayout(new BoxLayout(this.gameScreen, BoxLayout.PAGE_AXIS));

        gameArea.setLayout(gridLayout);
        gameArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameScreen.add(gameArea);
        this.setSize(((sizeX * buttonSize) + (sizeX * border) + border), ((sizeY * buttonSize) + ((sizeY + 1) * border) + (buttonSize * 3)));
        gameScreen.setBackground(new Color(188, 168, 159));
        gameArea.setBackground(Color.gray);
        newGame.addMouseListener(this);
        newGame.setSize(buttonSize * 2, buttonSize);
        setupButtons();

        newGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        lives.setAlignmentX(Component.CENTER_ALIGNMENT);
        time.setAlignmentX(Component.CENTER_ALIGNMENT);
        flags.setAlignmentX(Component.CENTER_ALIGNMENT);

        gameScreen.add(newGame);
        gameScreen.add(lives);

        time.setText("Time: 0s");
        time.setBackground(Color.RED);
        gameScreen.add(time);

        numflags = numBombs;
        updateFlags();
        gameScreen.add(flags);

        //Add all elements to gameScreen
        this.add(gameScreen);

        timerAction = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                getTime();
            }
        };
        graphicAction = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                updateBoard();
            }
        };

        timer = new Timer(1000,timerAction);
        graphicsTimer = new Timer(250,graphicAction);

        this.add(gameScreen);
        this.setVisible(true);
    }

    public void updateFlags(){

        String temp = "<html>";
        if (numflags == 0){
            flags.setText("-");
            return;
        }
        for (int i = 0; i < numflags; i++){
            if (i%2 == 0){
                temp = temp.concat("<font color=rgb(" + BetterButton.color5.getRed() + "," + BetterButton.color5.getGreen() + "," + BetterButton.color5.getBlue() + ")>■</font>");
            }else {
                temp = temp.concat("<font color=rgb(" + BetterButton.color6.getRed() + "," + BetterButton.color6.getGreen() + "," + BetterButton.color6.getBlue() + ")>■</font>");
            }
        }
        temp = temp.concat("</html>");
        flags.setText(temp);
    }

    public synchronized void playSound(final String url) {
        if (audioThread != null) {
            if (audioThread.isAlive()) {
                audioThread.interrupt();
            }
        }
        audioThread = new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                            GUI.class.getResourceAsStream("sounds/" + url));
                    clip.open(inputStream);
                    clip.start();
                    while(clip.getMicrosecondLength() != clip.getMicrosecondPosition())
                    {
                    }
                    clip.stop();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
        audioThread.start();
        System.gc();
    }

    public synchronized void playSound2(final String url) {
        if (audioClip.isActive()){
            audioClip.close();
        }
        try {
            audioClip.open(AudioSystem.getAudioInputStream(GUI.class.getResourceAsStream("sounds/" + url)));
            audioClip.start();
        } catch (Exception e) {
            System.out.print("Error in playSound: ");
            System.err.println(e.getMessage());
        }
        System.gc();
    }

    public static synchronized void playSound3(final String url) {

        try {
            Clip clip = AudioSystem.getClip();
            //clip.loop(0);
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                    GUI.class.getResourceAsStream("sounds/" + url));
            clip.open(inputStream);
            inputStream.close();
            clip.start();
            while(clip.getMicrosecondLength() != clip.getMicrosecondPosition())
            {
            }
            clip.stop();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Called once a game has finished<p>
     * If the game was won, an animation is played, otherwise it slowly reveals each mine
     */
    public void updateBoard(){
        if (gameFinished){
            if (!lost){
                if (flicker){
                    for (int i = 0; i < sizeY; i++){
                        for (int j = 0; j < sizeX; j++){
                            if (j%2 == 0){
                                if (i%2 == 0){
                                    buttons[i][j].setBackground(baseColor);
                                }else{
                                    buttons[i][j].setBackground(winColor);
                                }
                            }else {
                                if (i%2 == 0){
                                    buttons[i][j].setBackground(winColor);
                                }else{
                                    buttons[i][j].setBackground(baseColor);
                                }
                            }
                        }
                    }
                }else{
                    for (int i = 0; i < sizeY; i++){
                        for (int j = 0; j < sizeX; j++){
                            if (j%2 == 0){
                                if (i%2 == 0){
                                    buttons[i][j].setBackground(winColor);
                                }else{
                                    buttons[i][j].setBackground(baseColor);
                                }
                            }else {
                                if (i%2 == 0){
                                    buttons[i][j].setBackground(baseColor);
                                }else{
                                    buttons[i][j].setBackground(winColor);
                                }
                            }
                        }
                    }
                }
                flicker = !flicker;
            }
            else{
                if (endingPos < bombs.size()) {
                    int x = bombs.get(endingPos)[0];
                    int y = bombs.get(endingPos)[1];
                    buttons[y][x].bomb();
                    //playSound("bomb.wav");
                    buttons[y][x].playSound();
                    endingPos++;
                }
            }
        }

    }

    public void getTime(){
        timePassed++;
        if (timePassed < 60) {
            time.setText("Time: " + timePassed + "s");
        }else{
            int temp = timePassed%60;
            time.setText("Time: " + (timePassed / 60) + "m " + temp + "s");
        }
    }

    public void setupButtons() {
        buttons = new BetterButton[sizeY][sizeX];

        for (int i = 0; i < sizeY; i++) {
            for (int j = 0; j < sizeX; j++) {
                buttons[i][j] = new BetterButton(j, i);
                buttons[i][j].setSize(buttonSize, buttonSize);
                //buttons[i][j].setBackground(Color.green);
                buttons[i][j].setMargin(new Insets(1, 1, 1, 1));
                buttons[i][j].addMouseListener(this);
                gameArea.add(buttons[i][j]);

                //board[i][j] = new Cell();
            }
        }
    }

    public boolean checkForWin(){
        for (int i = 0; i < sizeY; i++) {
            for (int j = 0; j < sizeX; j++) {
                if (!buttons[i][j].getBomb() && buttons[i][j].isEnabled()){
                    return false;
                }
            }
        }
        timer.stop();
        return true;
    }

    public void generateGame(int[] userInputs) {
        //Create bombs
        SecureRandom rand = new SecureRandom();
        for (int i = 0; i < numBombs; i++) {
            int x = rand.nextInt(sizeX - 1);
            int y = rand.nextInt(sizeY - 1);

            if ((x < userInputs[0] - 1 || x > userInputs[0] + 1) && (y < userInputs[1] - 1 || y > userInputs[1] + 1)) {
                if (!buttons[y][x].getBomb()) {
                    buttons[y][x].setBomb();
                } else {
                    i--;
                }
            } else {
                i--;
            }
        }

        //Calculate values for remaining cells
        int count = 0;
        for (int i = 0; i < sizeY; i++) {
            for (int j = 0; j < sizeX; j++) {

                for (int k = -1; k < 2; k++) {
                    int y = i + k;

                    if (y < 0) {
                        continue;
                    }
                    if (y >= sizeY) {
                        continue;
                    }

                    for (int l = -1; l < 2; l++) {
                        int x = j + l;

                        if (x < 0) {
                            continue;
                        }
                        if (x >= sizeX) {
                            continue;
                        }

                        if (buttons[y][x].getBomb()) {
                            count++;
                        }
                    }
                }

                buttons[i][j].setValue(count);
                count = 0;

            }
        }

        //Reveal initial area
        revealArea(userInputs);
    }

    public void actionPerformed(ActionEvent e) {
        getTime();
    }

    public void mouseClicked(MouseEvent e) {
        System.gc();
        if (!(e.getSource() instanceof BetterButton) && !(e.getSource() instanceof JButton)){return;}
        if (e.getSource() == easy){
            sizeX = 9;
            sizeY = 9;
            numBombs = 5;
            System.out.println("EASY");
            setupGame();
        } else if (e.getSource() == medium){
            sizeX = 16;
            sizeY = 16;
            numBombs = 40;
            setupGame();
        } else if (e.getSource() == hard) {
            sizeX = 30;
            sizeY = 16;
            numBombs = 99;
            setupGame();
        }
        if (SwingUtilities.isRightMouseButton(e)){
            if (e.getSource() != newGame) {
                if (((BetterButton) e.getSource()).isEnabled()){
                    if ((!((BetterButton) e.getSource()).flagged && numflags > 0) || (((BetterButton) e.getSource()).flagged)) {
                        ((BetterButton) e.getSource()).flag();
                        playSound("rightClick.wav");
                        if (((BetterButton) e.getSource()).flagged) {
                            numflags--;

                        } else {
                            numflags++;
                        }
                        updateFlags();
                    }
                }
            }
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.getSource() == newGame) {
                endingPos = 0;
                livesRemaining = maxLives;
                lives.setText("Lives: " + livesRemaining);
                playSound("newGame.wav");
                timer.stop();
                timePassed = 0;
                graphicsTimer.stop();
                gameFinished = false;
                timer = new Timer(1000,timerAction);
                time.setText("Time: 0s");
                //Make all buttons green and clickable again
                for (int i = 0; i < sizeY; i++) {
                    for (int j = 0; j < sizeX; j++) {
                        buttons[i][j].refresh();
                    }
                }
                firstMove = true;

            } else {
                if (((BetterButton) e.getSource()).isEnabled() && !((BetterButton) e.getSource()).flagged) {
                    //playSound("button.wav");
                    int[] userInputs = {((BetterButton) e.getSource()).x, ((BetterButton) e.getSource()).y};
                    buttons[userInputs[1]][userInputs[0]].playSound();

                    if (firstMove) {
                        timer.start();
                        firstMove = false;
                        //board[userInputs[1]][userInputs[0]].setHidden();
                        buttons[userInputs[1]][userInputs[0]].setHidden();

                        generateGame(userInputs);
                        startTime = System.currentTimeMillis();
                    } else {
                        //Check if bomb
                        if (!buttons[userInputs[1]][userInputs[0]].getBomb()) {
                            //board[userInputs[1]][userInputs[0]].setHidden();
                            int tempValue = buttons[userInputs[1]][userInputs[0]].getRawValue();
                            if (tempValue == 0) {
                                revealArea(userInputs);
                                buttons[userInputs[1]][userInputs[0]].setHidden();
                            }else{
                                buttons[userInputs[1]][userInputs[0]].setHidden();
                            }
                            if (checkForWin()) {
                                lost = false;
                                playSound("gameWon.wav");
                                gameFinished = true;
                                graphicsTimer.start();
                                for (int i = 0; i < sizeY; i++) {
                                    for (int j = 0; j < sizeX; j++) {
                                        buttons[i][j].setEnabled(false);
                                    }
                                }
                            }
                        } else {
                            livesRemaining--;
                            lives.setText("Lives: " + livesRemaining);
                            buttons[userInputs[1]][userInputs[0]].bomb();
                            //playSound("bomb.wav");
                            buttons[userInputs[1]][userInputs[0]].playSound();
                            if (livesRemaining < 1) {
                                bombs = new ArrayList<>();
                                gameFinished = true;
                                lost = true;
                                playSound("gameLost.wav");
                                timer.stop();

                                for (int i = 0; i < sizeY; i++) {
                                    for (int j = 0; j < sizeX; j++) {
                                        if (buttons[i][j].getBomb() && buttons[i][j].isEnabled()) {
                                            // && (userInputs[1] != i && userInputs[0] != j)
                                            int[] temp = {j,i};
                                            bombs.add(temp);
                                        }else{
                                            buttons[i][j].setEnabled(false);
                                        }
                                    }
                                }

                                //Randomise bombs
                                ArrayList<int[]> temp = new ArrayList<>();
                                SecureRandom rand = new SecureRandom();
                                while(bombs.size() > 0){
                                    int tempRand = rand.nextInt(bombs.size());
                                    temp.add(bombs.get(tempRand));
                                    bombs.remove(tempRand);
                                }
                                bombs = temp;

                                //Sort bombs into path
                                //sortBombs();

                                graphicsTimer.start();
                            }
                        }
                    }
                }
            }
        }
    }

    public void revealArea(int[] userInputs) {
        int[] temp = {userInputs[0], userInputs[1]};


        int x = userInputs[0];
        int y = userInputs[1];

        //Check right
        if (x < sizeX - 1) {
            if (buttons[y][x + 1].getRawValue() == 0 && buttons[y][x + 1].getHidden()) {
                //board[y][x + 1].setHidden();
                buttons[y][x + 1].setHidden();
                temp[0] = x + 1;
                temp[1] = y;
                revealArea(temp);
            } else if (!buttons[y][x + 1].getBomb() && buttons[y][x + 1].getHidden()) {
                //board[y][x + 1].setHidden();
                buttons[y][x + 1].setHidden();
            }
        }
        //Check left
        if (x > 0) {
            if (buttons[y][x - 1].getRawValue() == 0 && buttons[y][x - 1].getHidden()) {
                //board[y][x - 1].setHidden();
                buttons[y][x - 1].setHidden();
                temp[0] = x - 1;
                temp[1] = y;
                revealArea(temp);
            } else if (!buttons[y][x - 1].getBomb() && buttons[y][x - 1].getHidden()) {
                //board[y][x - 1].setHidden();
                buttons[y][x - 1].setHidden();
            }
        }
        //Check up
        if (y < sizeY - 1) {
            if (buttons[y + 1][x].getRawValue() == 0 && buttons[y + 1][x].getHidden()) {
                //board[y + 1][x].setHidden();
                buttons[y + 1][x].setHidden();
                temp[0] = x;
                temp[1] = y + 1;
                revealArea(temp);
            } else if (!buttons[y + 1][x].getBomb() && buttons[y + 1][x].getHidden()) {
                //board[y + 1][x].setHidden();
                buttons[y + 1][x].setHidden();
            }
        }
        //Check down
        if (y > 0) {
            if (buttons[y - 1][x].getRawValue() == 0 && buttons[y - 1][x].getHidden()) {
                //board[y - 1][x].setHidden();
                buttons[y - 1][x].setHidden();
                temp[0] = x;
                temp[1] = y - 1;
                revealArea(temp);
            } else if (!buttons[y - 1][x].getBomb() && buttons[y - 1][x].getHidden()) {
                //board[y - 1][x].setHidden();
                buttons[y - 1][x].setHidden();
            }
        }
        //Check top right
        if (x < sizeX - 1 && y < sizeY - 1) {
            if (buttons[y + 1][x + 1].getRawValue() == 0 && buttons[y + 1][x + 1].getHidden()) {
                //board[y + 1][x + 1].setHidden();
                buttons[y + 1][x + 1].setHidden();
                temp[0] = x + 1;
                temp[1] = y + 1;
                revealArea(temp);
            } else if (!buttons[y + 1][x + 1].getBomb() && buttons[y + 1][x + 1].getHidden()) {
                //board[y + 1][x + 1].setHidden();
                buttons[y + 1][x + 1].setHidden();
            }
        }
        //Check bottom right
        if (x < sizeX - 1 && y > 0) {
            if (buttons[y - 1][x + 1].getRawValue() == 0 && buttons[y - 1][x + 1].getHidden()) {
                //board[y - 1][x + 1].setHidden();
                buttons[y - 1][x + 1].setHidden();
                temp[0] = x + 1;
                temp[1] = y - 1;
                revealArea(temp);
            } else if (!buttons[y - 1][x + 1].getBomb() && buttons[y - 1][x + 1].getHidden()) {
                //board[y - 1][x + 1].setHidden();
                buttons[y - 1][x + 1].setHidden();
            }
        }
        //Check bottom left
        if (x > 0 && y > 0) {
            if (buttons[y - 1][x - 1].getRawValue() == 0 && buttons[y - 1][x - 1].getHidden()) {
                //board[y - 1][x - 1].setHidden();
                buttons[y - 1][x - 1].setHidden();
                temp[0] = x - 1;
                temp[1] = y - 1;
                revealArea(temp);
            } else if (!buttons[y - 1][x - 1].getBomb() && buttons[y - 1][x - 1].getHidden()) {
                //board[y - 1][x - 1].setHidden();
                buttons[y - 1][x - 1].setHidden();
            }
        }
        //Check top left
        if (x > 0 && y < sizeY - 1) {
            if (buttons[y + 1][x - 1].getRawValue() == 0 && buttons[y + 1][x - 1].getHidden()) {
                //board[y + 1][x - 1].setHidden();
                buttons[y + 1][x - 1].setHidden();
                temp[0] = x - 1;
                temp[1] = y + 1;
                revealArea(temp);
            } else if (!buttons[y + 1][x - 1].getBomb() && buttons[y + 1][x - 1].getHidden()) {
                //board[y + 1][x - 1].setHidden();
                buttons[y + 1][x - 1].setHidden();
            }
        }

        //return board;
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

}

