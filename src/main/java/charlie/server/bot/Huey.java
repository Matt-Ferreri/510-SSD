package charlie.server.bot;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Dealer;
import charlie.dealer.Seat;
import charlie.plugin.IBot;
import charlie.util.Play;
import libby.client.BotBasicStrategy;

import java.util.List;

/**
 * this is the huey bot that uses BotBasicStrategy
 * it only talks to the dealer from play(Hid) on a worker thread, one dealer request per thread
 */
public class Huey implements IBot, Runnable {

    private static final int DELAY_MS_MIN = 2000;
    private static final int DELAY_MS_SPAN = 1001;

    private final BotBasicStrategy strategy = new BotBasicStrategy();
    private final Object playLock = new Object();

    private volatile Dealer dealer;
    private volatile Hand hand;
    private volatile Seat seat;
    private volatile Card dealerUpCard;
    private volatile boolean handFinished;

    @Override
    public void run() {
        // nothing here on purpose, the dealer calls the ibot methods when stuff happens
    }

    @Override
    public Hand getHand() {
        return hand;
    }

    @Override
    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }

    @Override
    public void sit(Seat seat) {
        this.seat = seat;
        this.hand = new Hand(new Hid(seat));
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        dealerUpCard = null;
        handFinished = false;
    }

    @Override
    public void endGame(int shoeSize) {
        // startGame will reset flags next hand
    }

    @Override
    public void deal(Hid hid, Card card, int[] handValues) {
        // first card to the dealer seat is the up card for basic strategy
        if (hid.getSeat() == Seat.DEALER && dealerUpCard == null) {
            dealerUpCard = card;
        }
    }

    @Override
    public void play(Hid hid) {
        if (!isMySeat(hid)) {
            return;
        }
        new Thread(() -> {
            try {
                humanDelay();
                executePlay(hid);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        // dont join() the worker — if the house thread waits here and the worker calls dealer.hit/stay,
        // charlie can deadlock and nothing moves
    }

    private void executePlay(Hid hid) {
        Dealer d = dealer;
        if (d == null) {
            return;
        }
        synchronized (playLock) {
            if (handFinished) {
                return;
            }
            Hand h = hand;
            if (h == null || !h.getHid().equals(hid)) {
                return;
            }
            if (h.isBroke() || h.isBlackjack() || h.isCharlie()) {
                return;
            }
            if (h.size() < 2) {
                return;
            }

            Card up = dealerUpCard;
            Play p = (up == null) ? Play.NONE : strategy.getPlay(h, up);
            if (p == Play.NONE) {
                // still have to call stay or the round can get stuck before it gets to the human
                d.stay(this, hid);
                handFinished = true;
                return;
            }
            // double only works with 2 cards so fall back to hit
            if (p == Play.DOUBLE_DOWN && h.size() != 2) {
                p = Play.HIT;
            }

            switch (p) {
                case STAY:
                    d.stay(this, hid);
                    handFinished = true;
                    break;
                case DOUBLE_DOWN:
                    d.doubleDown(this, hid);
                    handFinished = true;
                    break;
                case HIT:
                    d.hit(this, hid);
                    break;
                default:
                    d.stay(this, hid);
                    handFinished = true;
                    break;
            }
        }
    }

    private boolean isMySeat(Hid hid) {
        return seat != null && hid.getSeat() == seat;
    }

    // appendix 1 wants about 2.5 sec on average so use 2000–3000 ms
    private static void humanDelay() throws InterruptedException {
        int ms = DELAY_MS_MIN + (int) (Math.random() * DELAY_MS_SPAN);
        Thread.sleep(ms);
    }

    @Override
    public void bust(Hid hid) {
        if (isMySeat(hid)) {
            handFinished = true;
        }
    }

    @Override
    public void win(Hid hid) {
        if (isMySeat(hid)) {
            handFinished = true;
        }
    }

    @Override
    public void blackjack(Hid hid) {
        if (isMySeat(hid)) {
            handFinished = true;
        }
    }

    @Override
    public void charlie(Hid hid) {
        if (isMySeat(hid)) {
            handFinished = true;
        }
    }

    @Override
    public void lose(Hid hid) {
        if (isMySeat(hid)) {
            handFinished = true;
        }
    }

    @Override
    public void push(Hid hid) {
        if (isMySeat(hid)) {
            handFinished = true;
        }
    }

    @Override
    public void shuffling() {
    }

    @Override
    public void split(Hid newHid, Hid origHid) {
    }

    @Override
    public void insure() {
    }
}
