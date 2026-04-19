/*
 * Copyright (c) Ron Coleman
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package libby.test.shoe;


import charlie.actor.Courier;
import charlie.card.Card;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IUi;
import charlie.test.framework.Perfect;
import java.util.List;
import charlie.card.Hand;

/**
 * This class is the minimalist perfect  test case.
 * @author Ron.Coleman
 */
public class MyPerfectShoeTest extends Perfect implements IUi {
    Hid you;

    boolean myTurn = false; // set myTurn to false

    // keep track of number of cards in hand
    int playerHandCount = 0;
    int dealerHandCount = 0;

    // keep track of value of players hand
    int playerValue = 0;

    // set the bet amounts
    // moved up here so the full program can use the variables
    final int BET_AMT = 5;
    final int SIDE_BET_AMT = 0;

    // declare the variables for main and side PNL
    double mainPNL;
    double sidePNL;

    /**
     * Runs the test.
     */
    public void test() throws Exception {

        // set the shoe property
        System.setProperty("charlie.shoe","libby.plugin.MyShoe02");

        // Starts the server and logs in using only defaults
        go(this);

        // Now that the game server is ready, to start a game, we just need to
        // send in a bet which in the GUI is like pressing DEAL.
        bet(BET_AMT,SIDE_BET_AMT);
        info("bet amt: "+BET_AMT+", side bet: "+SIDE_BET_AMT);

        //All test logic at this point done by IUi implementation.

        // Wait for dealer to call end of game.
        assert await(20000);

        // End of scope closes sockets which shuts down client and server.
        info("DONE !");
    }

    /**
     * This method gets invoked whenever a card is dealt.
     * @param hid Target hand
     * @param card Card
     * @param handValues Hand value and soft value
     */
    @Override
    public void deal(Hid hid, Card card, int[] handValues) {
        info("DEAL: "+hid+" card: "+card+" hand values: "+handValues[0]+", "+handValues[1]);

        // if hand id equals YOU seat, get a card and increase cards in hand
        if (hid.getSeat() == Seat.YOU) {
            // ensures actual card is being dealt
            if (card != null) {
                playerHandCount++;
                playerValue = handValues[0];
            }
        }

        // else it equals the dealer, so increase card counts
        else {
            // ensures actual card is being dealt
            if (card != null) {
                dealerHandCount++;
            }
        }

        // if myTurn is true and hid.seat = YOU then play
        if (myTurn && hid.equals(you)) {
            //when it's our turn, play
            play(hid);
        }
    }

    /**
     * This method gets invoked only once whenever the turn changes.
     * @param hid New hand's turn
     */
    @Override
    public void play(Hid hid) {
        // if the turn is not mine, set myTurn as false and return
        if (hid.getSeat() != Seat.YOU) {
            myTurn = false;
            return;
        }

        // When it's our turn, set myTurn = true and invoke hit
        if(hid.getSeat() == Seat.YOU) {
            myTurn = true;
            hit(you);
        }
    }

    /**
     * This method gets invoked if a hand breaks.
     * @param hid Target hand
     */
    @Override
    public void bust(Hid hid) {
        // Possible if You or Dealer breaks, but it will be one or the other.
        info("BREAK: "+hid);

        // Not possible for this test case.
        //assert false;
    }

    /**
     * This method gets invoked for a winning hand.
     * @param hid Target hand
     */
    @Override
    public void win(Hid hid) {
        // Possible if You or Dealer wins, but it'll be one or ther other.
        info("WIN: "+hid);

        // a win pays out the bet, the sidebet depends on the bet, we will set to 0
        mainPNL = BET_AMT;
        sidePNL = 0;

        // Not possible for this test case.
        assert false;
    }

    /**
     * This method gets invoked for a losing hand.
     * @param hid Target hand
     */
    @Override
    public void lose(Hid hid) {
        // Possible if You or Dealer loses but it will be one or the other.
        info("LOSE: "+hid);

        // in lose the player loses their money, the sidebet depends on the bet, we will set to 0
        mainPNL = -BET_AMT;
        sidePNL = 0;

        // Not possible for this test case.
        //assert false;
    }

    /**
     * This method gets invoke for a hand that pushes, ie, has same value as dealer's hand.
     * @param hid Target hand
     */
    @Override
    public void push(Hid hid) {
        // Possible if there's a push.
        info("PUSH: "+hid);

        // Not possible for this test case.
        assert false;
    }

    /**
     * This method gets invoked for a (natural) Blackjack hand, Ace+K, Ace+Q, etc.
     * @param hid Target hand
     */
    @Override
    public void blackjack(Hid hid) {
        // Possible if either You or Dealer has a blackjack.
        info("BLACKJACK: "+hid);

        // a Blackjack pays out the bet times 1.5, the sidebet depends on the bet, we will set to 0
        mainPNL = BET_AMT * 1.5;
        sidePNL = 0;

        // Not possible for this test case.
        assert false;
    }

    /**
     * This method gets invoked for a 5-card Charlie hand.
     * @param hid Target hand
     */
    @Override
    public void charlie(Hid hid) {

        // a Charlie pays out the bet times 2, the side bet usually losses
        mainPNL = BET_AMT * 2;
        sidePNL = 0;
        // Charlie only occurs for YOU in this test, if not YOU, throws exception
        assert hid.getSeat() == Seat.YOU;




    }

    /**
     * This method get invoked at the start of a game before any cards are dealt.
     * @param hids Hands in the game
     * @param shoeSize Current shoe size, ie, original shoe less cards dealt
     */
    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        // ensure game has 2 players, if not throw an exception
        //assert hids.size() == 2;

        StringBuilder buffer = new StringBuilder();

        buffer.append("game STARTING: ");
        buffer.append(" shoe size: ").append(shoeSize);

        // assign you to the hid
        for (Hid hid : hids) {
            if (hid.getSeat() == Seat.YOU) {
                you = hid;
            }
        }

        info(buffer.toString());
    }

    /**
     * This method gets invoked after a game ends and before the start of a new game.
     * @param shoeSize Endind shoe size
     */
    @Override
    public void endGame(int shoeSize) {
        signal();

        info("ENDING game shoe size: "+shoeSize);

        // asserts at end of game to confirm all of the following happened
        //assert dealerHandCount == 2; // dealerHand equals 2
        //assert playerHandCount == 5; // player hsa 5 cards
        //assert playerValue <= 21; // value of players hand <= 21
        //assert mainPNL == BET_AMT * 2; // the bet pays out double
        //assert sidePNL == 0; // the side bet results in 0
    }

    /**
     * This method gets invoked when the burn card appears, it indicates a
     * re-shuffle is coming after the current game ends.
     */
    @Override
    public void shuffling() {
        info("SHUFFLING");
    }

    /**
     * This method sets the courier.
     * It's not used here because the base test case instantiates a courier for us.
     * @param courier Courier
     */
    @Override
    public void setCourier(Courier courier) {
    }

    /**
     * This method gets invoked when a player requests a split.
     * For instance, a 4+4 split results in two hands, each with two cards,
     * 4+x and 4+y where "x" and "y" are hits to each hand which the dealer
     * automatically performs, respectively.
     * @param newHid New hand split from the original.
     * @param origHid Original hand.
     */
    @Override
    public void split(Hid newHid, Hid origHid) {
        // Not possible for this test case.
        assert false;

    }

    /**
     * Handles insurance requests.
     */
    @Override
    public void insure() {
        // Insurance not supported.
        assert false;
    }
}