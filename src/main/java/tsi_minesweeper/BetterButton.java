package tsi_minesweeper;

import javax.swing.*;
import java.awt.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class BetterButton extends JButton {
    public int x;
    public int y;
    public boolean flagged;

    private boolean hidden;
    private boolean bomb;
    private int value;

    private Clip clipButton;
    private Clip clipBomb;

    public static Color color1 = new Color(255, 203, 171);
    public static Color color2 = new Color(240, 203, 171);

    public static Color color3 = new Color(170, 215, 81);
    public static Color color4 = new Color(162, 209, 73);

    public static Color color5 = new Color(227, 23, 23);
    public static Color color6 = new Color(237, 24, 24);

    Color background;
    Color backgroundHidden;
    Color bombColor;

    public BetterButton(int x, int y){
        super();
        this.x = x;
        this.y = y;
        this.flagged = false;

        this.bomb = false;
        this.hidden = true;
        this.value = 0;

        if (x%2 == 0){
            if (y%2 == 0){
                background = color1;
                backgroundHidden = color3;
                bombColor = color5;
            }else{
                background = color2;
                backgroundHidden = color4;
                bombColor = color6;
            }
        }else {
            if (y%2 == 0){
                background = color2;
                backgroundHidden = color4;
                bombColor = color6;
            }else{
                background = color1;
                backgroundHidden = color3;
                bombColor = color5;
            }
        }

        try {
            clipButton = AudioSystem.getClip();
            clipBomb = AudioSystem.getClip();

            clipButton.open(AudioSystem.getAudioInputStream(BetterButton.class.getResourceAsStream("sounds/button.wav")));
            clipBomb.open(AudioSystem.getAudioInputStream(BetterButton.class.getResourceAsStream("sounds/bomb.wav")));
        }catch (Exception e){
            System.err.println(e.getMessage());
        }

        this.setBackground(backgroundHidden);
    }

    public void playSound() {

        if (this.bomb){
            clipBomb.start();
        } else{
            clipButton.start();
        }
        System.gc();
    }

    public void refresh(){
        this.flagged = false;
        this.bomb = false;
        this.hidden = true;
        this.value = 0;

        this.setBackground(backgroundHidden);
        this.setText("");
        this.setEnabled(true);
    }

    public boolean getHidden(){
        return hidden;
    }

    public boolean getBomb(){
        return bomb;
    }

    public void setBomb(){
        this.bomb = true;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getRawValue(){
        return value;
    }

    public boolean flag(){
        if (this.isEnabled()) {
            if (flagged) {
                this.setText("");
            }else{
                this.setText("■");
            }
            flagged = !flagged;
            return true;
        }
        return false;
    }

    public void setHidden(){
        if (bomb){
            this.setEnabled(false);
            this.setBackground(bombColor);
            this.hidden = false;
        } else if (value == 0){
            this.setEnabled(false);
            this.setBackground(background);
            this.hidden = false;
        } else{
            this.setText(String.valueOf(value));
            this.setEnabled(false);
            this.setBackground(background);
            this.hidden = false;
        }
    }

    public void bomb(){
        this.setBackground(bombColor);
        this.setEnabled(false);
    }
}
