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

    boolean waitingToStayAfterHit = false;
    double bankroll;
    int gameNumber;

    public void test() throws Exception {
        // launch the server and login
        go(this);

        // start the bankroll and 1000 and gameNumber = 1
        bankroll = 1000;
        gameNumber = 1;

        // Start only game 1 here. The remaining games are chained from endGame().
        bet(25, 0);
        info("game 1 bet amt: 25, side bet: 0");

        // 20 second timeout is too short, so we use 60 seconds
        assert await(60000);

        info("DONE !");
    }

    @Override
    public void deal(Hid hid, Card card, int[] handValues) {
        info("DEAL: " + hid + " card: " + card + " hand values: " + handValues[0] + ", " + handValues[1]);

        // For games 1-5, we hit once and then explicitly stay after that hit card arrives.
        // Without this, the test can stall waiting for a second play callback that may never come.
        if (hid.getSeat() == Seat.YOU && waitingToStayAfterHit) {
            waitingToStayAfterHit = false;
            stay(you);
        }
    }

    /**
     * Games 1-5: player has 16, must hit to reach 19.
     * Games 6-7: player has 20, stay.
     * Games 8-10: player has 13 or 14, stay because the dealer busts.
     */
    @Override
    public void play(Hid hid) {
        // We receive callbacks for dealer/player hands; only act on our own turn.
        if (hid.getSeat() != Seat.YOU)
            return;

        // if the game number is between 1 and 5, hit once then stay from deal().
        if (gameNumber >= 1 && gameNumber <= 5) {
            hit(you);
            waitingToStayAfterHit = true;
        } else {
            stay(you);
        }
    }

    @Override
    public void push(Hid hid) {
        // ensure the hid is the player's
        assert hid.getSeat() == Seat.YOU;

        info("PUSH: " + hid);

        if (gameNumber == 1) {
            // Game 1 is a push with nothing gained or lost
            assert hid.getAmt() == 0;
            assert hid.getSideAmt() == 0;
        } else {
            assert false : "unexpected push at game " + gameNumber;
        }
    }

    // update the bankroll if someone wins
    @Override
    public void win(Hid hid) {
        // ensure the hid is the player's
        assert hid.getSeat() == Seat.YOU;

        info("WIN: " + hid);

        // update the bankroll based on the game number and the results of the games
        if (gameNumber == 2) {
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == 3 * 10;
            // Hid carries round P&L, so update cumulative bankroll from callback values.
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1055;
        } else if (gameNumber == 3) {
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == -10;
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1070;
        } else if (gameNumber == 6) {
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == 25 * 10;
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1315;
        } else if (gameNumber == 7) {
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == -10;
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1330;
        } else if (gameNumber == 8) {
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == 10;
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1365;
        } else if (gameNumber == 9) {
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == 3 * 10;
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1420;
        } else if (gameNumber == 10) {
            assert hid.getAmt() == 25;
            assert hid.getSideAmt() == -10;
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1435;
        } else {
            assert false : "unexpected win at game " + gameNumber;
        }
    }

    // update the bankroll if someone loses
    @Override
    public void lose(Hid hid) {
         // ensure the hid is the player's
         assert hid.getSeat() == Seat.YOU;

        info("LOSE: " + hid);

        if (gameNumber == 4) {
            assert hid.getAmt() == -25;
            assert hid.getSideAmt() == 3 * 10;
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1075;
        } else if (gameNumber == 5) {
            assert hid.getAmt() == -25;
            assert hid.getSideAmt() == -10;
            bankroll += hid.getAmt() + hid.getSideAmt();
            assert bankroll == 1040;
        } else {
            assert false : "unexpected lose at game " + gameNumber;
        }
    }

    @Override
    public void bust(Hid hid) {
        info("BREAK: " + hid);

        // Dealer busts in games 8-10; player bust is never expected.
        assert hid.getSeat() != Seat.YOU : "unexpected player bust at game " + gameNumber;
    }

    // unexpected results for blackjack, split, or charlie, all assert false
    @Override
    public void blackjack(Hid hid) {
        info("BLACKJACK: " + hid);

        assert false : "unexpected blackjack at game " + gameNumber;
    }

    @Override
    public void split(Hid newHid, Hid origHid) {
        assert false : "unexpected split at game " + gameNumber;
    }

    @Override
    public void charlie(Hid hid) {
        info("CHARLIE: " + hid);

        assert false : "unexpected charlie at game " + gameNumber;
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        info("game " + gameNumber + " STARTING shoe size: " + shoeSize);
        waitingToStayAfterHit = false;

        // Capture the player's Hid once per round for play() actions.
        for (Hid hid : hids) {
            if (hid.getSeat() == Seat.YOU) {
                you = hid;
            }
        }
    }

    @Override
    public void endGame(int shoeSize) {
        info("ENDING game " + gameNumber + " shoe size: " + shoeSize);

        if (gameNumber == 10) {
            // Shoe must be empty after all the games, finish the test
            assert shoeSize == 0;
            assert bankroll == 1435;
            signal();
            return;
        }

        // go to the next game
        gameNumber++;
        // game 1 has side bet 0, games 2-10 use side bet 10, so use that amount
        bet(25, 10);
    }

    @Override
    public void shuffling() {
        info("SHUFFLING");
    }

    @Override
    public void setCourier(Courier courier) {
    }
}
