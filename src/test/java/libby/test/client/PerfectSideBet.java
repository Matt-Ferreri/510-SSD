package libby.test.client;

import charlie.actor.Courier;
import charlie.card.Card;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IUi;
import charlie.test.framework.Perfect;

import java.util.List;

public class PerfectSideBet extends Perfect implements IUi {
    Hid you;

    boolean myTurn = false; // set myTurn to false

    // keep track of number of cards in hand
    int playerHandCount = 0;
    int dealerHandCount = 0;
    // keep track of value of players hand
    int playerValue = 0;

    int gameNumber = 1;

    double bankroll = 0;

    int BET_AMT = 0;
    int SIDE_BET_AMT = 0;

    // declare the variables for main and side PNL
    double mainPNL;

    public void test() throws Exception {

        // Starts the server and logs in using only defaults
        go(this);

        bankroll = 1000;

        BET_AMT = 5;
        SIDE_BET_AMT = 0;

        /*for(; gameNumber <= 10; gameNumber++){
            // set the bet amounts for game 1
            BET_AMT = 5;
            SIDE_BET_AMT = 0;

            bankroll = 1000;
        }*/

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
        }
    }

    /**
     * This method gets invoke for a hand that pushes, ie, has same value as dealer's hand.
     * @param hid Target hand
     */
    @Override
    public void push(Hid hid) {
        System.out.println("HERE");
        // Possible if there's a push.
        info("PUSH: "+hid);

        // game 1 outcome is a push
        if (gameNumber == 1){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == 0 && mainPNL == 0;
            // bankroll = 1000
            assert hid.getSideAmt() == 0;
        } else {
            assert false;
        }
    }

    /**
     * This method gets invoked for a winning hand.
     * @param hid Target hand
     */
    @Override
    public void win(Hid hid) {
        // Possible if You or Dealer wins, but it'll be one or the other.
        info("WIN: "+hid);

        if (gameNumber == 2){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == 3 * 10;
            // bankroll = 1055
            assert bankroll == bankroll + (25 + 30);
        } else {
            assert false;
        }

        if (gameNumber == 3){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == -10;
            // bankroll = 1070
            assert bankroll == bankroll + (25 - 10);
        } else {
            assert false;
        }

        if (gameNumber == 6){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == 25 * 10;
            // bankroll = 1315
            assert bankroll == bankroll + (25 + (25 * 10));
        } else {
            assert false;
        }

        if (gameNumber == 7){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == -10;
            // bankroll = 1330
            assert bankroll == bankroll + (25 - 10);
        } else {
            assert false;
        }

        if (gameNumber == 8){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == 10;
            // bankroll = 1365
            assert bankroll == bankroll + (25 + 10);
        } else {
            assert false;
        }

        if (gameNumber == 9){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == 3 * 10;
            // bankroll = 1420
            assert bankroll == bankroll + (25 + 30);
        } else {
            assert false;
        }
    }

    /**
     * This method gets invoked for a losing hand.
     * @param hid Target hand
     */
    @Override
    public void lose(Hid hid) {
        // Possible if You or Dealer loses, but it will be one or the other.
        info("LOSE: "+hid);

        if (gameNumber == 4){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == -25;
            assert hid.getSideAmt() == 3 * 10;
            // bankroll = 1075
            assert bankroll == bankroll + (-25 + 30);
        } else {
            assert false;
        }

        if (gameNumber == 5){
            assert hid.getSeat() == Seat.YOU;
            assert hid.getAmt() == -25;
            assert hid.getSideAmt() == -10;
            // bankroll = 1040
            assert bankroll == bankroll + (-25 - 10);
        } else {
            assert false;
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

        // Not possible for this test case.
        assert false;
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
     * This method gets invoked for a 5-card Charlie hand.
     * @param hid Target hand
     */
    @Override
    public void charlie(Hid hid) {
        // Possible if either You or Dealer has a Charlie.
        info("CHARLIE: "+hid);

        // Not possible for this test case.
        assert false;
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
     * @param shoeSize Ending shoe size
     */
    @Override
    public void endGame(int shoeSize) {
        if(gameNumber == 10) {
            assert shoeSize == 0;
            assert bankroll == 1435;
            signal();
        }

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
}
