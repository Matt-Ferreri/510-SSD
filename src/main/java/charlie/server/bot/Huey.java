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
    private volatile boolean workerRunning = false;
    private volatile boolean pendingPlay = false;

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
        workerRunning = false;
        pendingPlay = false;
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

        if (!isMySeat(hid) || handFinished) {
            return;
        }

        Hand h = hand;
        if (h == null || h.size() < 2) {
            return;
        }

        // after a hit, charlie does not call play() again if the hand is still alive
        // so if this card is for huey, we have to queue up the next move ourselves
        if (!h.isBroke() && !h.isBlackjack() && !h.isCharlie() && h.getValue() < 21) {
            boolean startNow = false;
            synchronized (playLock) {
                if (handFinished) {
                    return;
                }
                if (workerRunning) {
                    pendingPlay = true;
                } else {
                    startNow = true;
                }
            }

            if (startNow) {
                startWorker(hid);
            }
        }
    }

    @Override
    public void play(Hid hid) {
        if (!isMySeat(hid)) {
            return;
        }
        startWorker(hid);
    }

    private void startWorker(Hid hid) {
        synchronized (playLock) {
            if (workerRunning || handFinished) {
                return;
            }
            workerRunning = true;
            pendingPlay = false;
        }

        new Thread(() -> {
            boolean runAgain = false;
            try {
                humanDelay();
                executePlay(hid);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                synchronized (playLock) {
                    workerRunning = false;

                    // if a hit came in while this worker was still finishing,
                    // save that next move and start a new worker now
                    if (pendingPlay && !handFinished) {
                        pendingPlay = false;
                        runAgain = true;
                    }
                }
            }

            if (runAgain) {
                startWorker(hid);
            }
        }).start();
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

            if (h == null || h.getHid().getSeat() != hid.getSeat()) {
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