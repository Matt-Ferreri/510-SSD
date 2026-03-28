

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
 * Table bot driven by  BotBasicStrategy. Responds to the dealer only from
 * play(Hid) on a short-lived worker thread (one dealer request per thread).
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
        // Gameplay is driven by dealer callbacks on the house thread.
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
        // Outcome already delivered via bust/win/lose/etc.; state resets next startGame.
    }

    @Override
    public void deal(Hid hid, Card card, int[] handValues) {
        if (hid.getSeat() == Seat.DEALER && dealerUpCard == null) {
            dealerUpCard = card;
        }
    }

    @Override
    public void play(Hid hid) {
        if (!isMySeat(hid)) {
            return;
        }
        Thread worker = new Thread(() -> {
            try {
                humanDelay();
                executePlay(hid);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        worker.start();
        try {
            // Wait until the worker finishes its Dealer request so the round cannot stall
            // when executePlay would otherwise return without hit/stay/double.
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
                // Must still advance the hand or the table can hang before the human's turn.
                d.stay(this, hid);
                handFinished = true;
                return;
            }
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

    /** Random delay with mean ~2.5 s (uniform 2000–3000 ms). */
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
