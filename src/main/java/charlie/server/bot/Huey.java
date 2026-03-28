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

    // this makes sure the bot doesnt start more than one worker thread at a time
    private volatile boolean workerRunning = false;

    @Override
    public void run() {
        // this is empty on purpose
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
        // reset everything at the start of each game
        dealerUpCard = null;
        handFinished = false;
        workerRunning = false;
    }

    @Override
    public void endGame(int shoeSize) {
        // reset happens in startGame
    }

    @Override
    public void deal(Hid hid, Card card, int[] handValues) {
        // the first dealer card is the up card the bot uses for basic strategy
        if (hid.getSeat() == Seat.DEALER && dealerUpCard == null) {
            dealerUpCard = card;
        }
    }

    @Override
    public void play(Hid hid) {
        if (!isMySeat(hid)) {
            return;
        }

        synchronized (playLock) {
            // dont start another worker if this hand is already done or one is already running
            if (workerRunning || handFinished) {
                return;
            }
            workerRunning = true;
        }

        new Thread(() -> {
            try {
                // wait a little so the bot plays more like a person
                humanDelay();
                executePlay(hid);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // once the worker is done, let the bot be able to act again if needed
                workerRunning = false;
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

            // make sure this play callback is really for this bot's seat
            if (h == null || h.getHid().getSeat() != hid.getSeat()) {
                return;
            }

            // if the hand is already over, dont send anything else to the dealer
            if (h.isBroke() || h.isBlackjack() || h.isCharlie()) {
                return;
            }

            // basic strategy only works once the hand has 2 cards
            if (h.size() < 2) {
                return;
            }

            Card up = dealerUpCard;
            Play p = (up == null) ? Play.NONE : strategy.getPlay(h, up);

            if (p == Play.NONE) {
                // if no play comes back, just stay so the game doesnt get stuck
                d.stay(this, hid);
                handFinished = true;
                return;
            }

            // if the hand already got hit before, double is no longer valid so just hit
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