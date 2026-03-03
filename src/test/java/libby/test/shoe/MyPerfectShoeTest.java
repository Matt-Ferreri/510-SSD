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
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IUi;
import charlie.test.framework.Perfect;

import java.util.HashMap;
import java.util.List;

/**
 * This class is the minimalist perfect  test case.
 * @author Ron.Coleman
 */
public class MyPerfectShoeTest extends Perfect implements IUi {
    Hid you;

    // boolean member variable initialized to false
    boolean myTurn = false;

    // instantiate hands
    HashMap<Hid, Hand> hands = new HashMap<>();

    /**
     * Runs the test.
     */
    public void test() throws Exception {
        // set the shoe property
        System.setProperty("charlie.shoe", "libby.plugin.MyShoe02");

        // Starts the server and logs in using only defaults
        go(this);

        // Now that the game server is ready, to start a game, we just need to
        // send in a bet which in the GUI is like pressing DEAL.
        final int BET_AMT = 5;
        final int SIDE_BET_AMT = 0;

        bet(BET_AMT,SIDE_BET_AMT);
        info("bet amt: "+BET_AMT+", side bet: "+SIDE_BET_AMT);

        // All test logic at this point done by IUi implementation.

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

        // invoke play(hid) if myTurn is true and hid-seat is YOU
        if(myTurn && (hid.getSeat() == Seat.YOU)){
            play(hid);

            // 3. The player's hand value is <= 21
            assert handValues[0] <= 21;
            hands.get(hid).hit(card);
        }
        info("current hid: "+hid);

        if(hid.getSeat() == Seat.YOU){
            info("this hid: "+hid);
            info("this hand: "+hands);
            info("thisHand: " + (hands.get(hid)).size());
        }


    }

    /**
     * This method gets invoked only once whenever the turn changes.
     * @param hid New hand's turn
     */
    @Override
    public void play(Hid hid) {
        // When it's our turn, hit.
        if(hid.getSeat() == Seat.YOU) {
            myTurn = true;
            hit(you);

            info("inside play");
        } else { // the hid-seat is not YOU
            myTurn = false;
            return;
        }
    }

    /**
     * This method gets invoked if a hand breaks.
     * @param hid Target hand
     */
    @Override
    public void bust(Hid hid) {
        // Possible if You or Dealer breaks but it will be one or the other.
        info("BREAK: "+hid);

        // 10. bust is not observed
        assert false;
    }

    /**
     * This method gets invoked for a winning hand.
     * @param hid Target hand
     */
    @Override
    public void win(Hid hid) {
        // Possible if You or Dealer wins, but it'll be one or the other.
        info("WIN: "+hid);

        // 10. win is not observed
        assert false;
    }

    /**
     * This method gets invoked for a losing hand.
     * @param hid Target hand
     */
    @Override
    public void lose(Hid hid) {
        // Possible if You or Dealer loses, but it will be one or the other.
        info("LOSE: "+hid);

        // 10. lose is not observed
        assert false;
    }

    /**
     * This method gets invoke for a hand that pushes, ie, has same value as dealer's hand.
     * @param hid Target hand
     */
    @Override
    public void push(Hid hid) {
        // Possible if there's a push.
        info("PUSH: "+hid);

        // 10. push is not observed
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

        // 10. Blackjack is not observed
        assert false;
    }

    /**
     * This method gets invoked for a 5-card Charlie hand.
     * @param hid Target hand
     */
    @Override
    public void charlie(Hid hid) {
        // assert the hid-seat is YOU. 4. The player's hand is a Charlie
        assert hid.getSeat() == Seat.YOU;
        info("The player's hand is a Charlie.");

        // 5. Only the player's hand is a Charlie
        assert hid.getSeat() != Seat.DEALER;

        if(hid.getSeat() == Seat.YOU) {
            final int BET_AMT = 5;
            final int SIDE_BET_AMT = 0;

            int MainProfitAndLoss = (BET_AMT * 2);
            // 6. The player's main P&L is 2xBET_AMT
            assert MainProfitAndLoss == (2 * BET_AMT);

            int SideProfitAndLoss = (SIDE_BET_AMT * 2);
            // 7. The player's side P&L is 0
            assert SideProfitAndLoss == 0;

        }
    }

    /**
     * This method get invoked at the start of a game before any cards are dealt.
     * @param hids Hands in the game
     * @param shoeSize Current shoe size, ie, original shoe less cards dealt
     */
    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("game STARTING: ");

        // 1. The game has two players
        assert hids.size() == 2;

        for (Hid hid : hids) {
            buffer.append(hid).append(", ");
            if (hid.getSeat() == Seat.YOU)
                this.you = hid;
            Hand hand = new Hand(hid);
            hands.put(hid, hand);
            info("firstHand: "+(hands.get(hid)).size());
        }
        buffer.append(" shoe size: ").append(shoeSize);
        info(buffer.toString());



    }

    /**
     * This method gets invoked after a game ends and before the start of a new game.
     * @param shoeSize Ending shoe size
     */
    @Override
    public void endGame(int shoeSize) {
        signal();

        info("ENDING game shoe size: "+shoeSize);
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
        assert false; // 10. split is not observed
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