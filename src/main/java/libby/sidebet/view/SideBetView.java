/*
 Copyright (c) 2014 Ron Coleman

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package libby.sidebet.view;

import charlie.audio.Effect;
import charlie.audio.SoundFactory;
import charlie.card.Hid;
import charlie.plugin.ISideBetView;
import charlie.view.AMoneyManager;
import charlie.view.sprite.Chip;
import charlie.view.sprite.ChipButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * This class implements the side bet view
 * @author Ron.Coleman
 */
public class SideBetView implements ISideBetView {
    private final Logger LOG = Logger.getLogger(SideBetView.class);

    public final static int X = 400;
    public final static int Y = 200;
    public final static int DIAMETER = 50;

    protected Font font = new Font("Arial", Font.BOLD, 18);
    protected BasicStroke stroke = new BasicStroke(3);

    // See http://docs.oracle.com/javase/tutorial/2d/geometry/strokeandfill.html
    protected float dash1[] = {10.0f};
    protected BasicStroke dashed
            = new BasicStroke(3.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f, dash1, 0.0f);

    // side bet outcomes
    enum Outcome { None, Win, Lose }

    protected List<ChipButton> buttons;
    protected List<Chip> chips = new ArrayList<>();
    protected Random ran = new Random();
    protected int amt = 0;
    protected Outcome outcome = Outcome.None;
    protected AMoneyManager moneyManager;

    protected Font outcomeFont = new Font("Arial", Font.BOLD, 18);
    protected Color winColorBg  = Color.GREEN;
    protected Color winColorFg  = Color.BLACK;
    protected Color loseColorBg = new Color(250, 58, 5);
    protected Color loseColorFg = Color.WHITE;

    public SideBetView() {
        LOG.info("side bet view constructed");
    }

    /**
     * Sets the money manager.
     * @param moneyManager
     */
    @Override
    public void setMoneyManager(AMoneyManager moneyManager) {
        this.moneyManager = moneyManager;
        this.buttons = moneyManager.getButtons();
    }

    /**
     * Registers a click for the side bet.
     * This method gets invoked on right mouse click.
     * @param x X coordinate
     * @param y Y coordinate
     */
    @Override
    public void click(int x, int y) {
        int oldAmt = amt;

        // Test if any chip button has been pressed.
        for(ChipButton button: buttons) {
            if(button.isPressed(x, y)) {
                // place chip randomly to the right of the at-stake circle
                int n = chips.size();
                int chipWidth = button.getImage().getWidth(null);
                int placeX = X + DIAMETER/2 + 5 + n * chipWidth/3 + ran.nextInt(10) - 10;
                int placeY = Y - chipWidth/2 + ran.nextInt(5) - 5;

                chips.add(new Chip(button.getImage(), placeX, placeY, button.getAmt()));

                // make the sound that chips are being entered
                SoundFactory.play(Effect.CHIPS_IN);
                amt += button.getAmt();
                LOG.info("A. side bet amount "+button.getAmt()+" updated new amt = "+amt);
            }
        }

        // clear the bet only if the click is inside the side bet circle
        boolean inCircle = Math.pow(x - X, 2) + Math.pow(y - Y, 2) <= Math.pow(DIAMETER / 2.0, 2);
        if(oldAmt == amt && inCircle) {
            amt = 0;
            chips.clear();
            // make the sound that chips are being removed
            SoundFactory.play(Effect.CHIPS_OUT);
            LOG.info("B. side bet amount cleared");
        }
    }

    /**
     * Informs view the game is over and it's time to update the bankroll for the hand.
     * @param hid Hand id
     */
    @Override
    public void ending(Hid hid) {
        double bet = hid.getSideAmt();

        if(bet == 0)
            return;

        LOG.info("side bet outcome = "+bet);

        // set win or lose outcome to display over chips
        outcome = bet > 0 ? Outcome.Win : Outcome.Lose;

        // Update the bankroll
        moneyManager.update(bet);

        LOG.info("new bankroll = "+moneyManager.getBankroll());
    }

    /**
     * Informs view the game is starting.
     */
    @Override
    public void starting() {
        // clear outcome from previous game
        outcome = Outcome.None;
    }

    /**
     * Gets the side bet amount.
     * @return Bet amount
     */
    @Override
    public Integer getAmt() {
        return amt;
    }

    /**
     * Updates the view.
     */
    @Override
    public void update() {
    }

    /**
     * Renders the view.
     * @param g Graphics context
     */
    @Override
    public void render(Graphics2D g) {
        // Draw the at-stake circle
        g.setColor(Color.RED);
        g.setStroke(dashed);
        g.drawOval(X-DIAMETER/2, Y-DIAMETER/2, DIAMETER, DIAMETER);

        // Draw the at-stake dollar amount centered in the circle,
        // using same approach as AtStakeSprite -- top-left of circle as reference
        g.setFont(font);
        g.setColor(Color.WHITE);

        int circleX = X - DIAMETER/2;
        int circleY = Y - DIAMETER/2;

        String amtText = "" + amt;
        FontMetrics amtFm = g.getFontMetrics(font);
        int textX = circleX + DIAMETER/2 - amtFm.charsWidth(amtText.toCharArray(), 0, amtText.length()) / 2;
        int textY = circleY + DIAMETER/2 + amtFm.getHeight() / 4;
        g.drawString(amtText, textX, textY);

        // Draw payout labels below the tray/shoe images, above the chip buttons
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(255, 215, 0));

        g.drawString("SUPER 7 pays 3:1",      430, 95);
        g.drawString("ROYAL MATCH pays 25:1",  430, 111);
        g.drawString("EXACTLY 13 pays 1:1",    430, 127);

        // Draw chips placed on the side bet area
        for(Chip chip: chips)
            chip.render(g);

        // Draw WIN or LOSE highlight over the chips if the game is over
        if(outcome == Outcome.None || chips.isEmpty())
            return;

        String outcomeText = " " + outcome.toString().toUpperCase() + " ! ";

        FontMetrics fm = g.getFontMetrics(outcomeFont);
        int w = fm.charsWidth(outcomeText.toCharArray(), 0, outcomeText.length());
        int h = fm.getHeight();

        // position badge at the first chip location
        int bx = chips.get(0).getX();
        int by = chips.get(0).getY() + h;

        // paint colored background
        g.setColor(outcome == Outcome.Win ? winColorBg : loseColorBg);
        g.fillRoundRect(bx, by - h + 5, w, h, 5, 5);

        // paint outcome text
        g.setColor(outcome == Outcome.Win ? winColorFg : loseColorFg);
        g.setFont(outcomeFont);
        g.drawString(outcomeText, bx, by);
    }
}
