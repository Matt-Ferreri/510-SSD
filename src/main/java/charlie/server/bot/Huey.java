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

    // true only after play() is called for huey's seat; prevents deal() from
    // firing a worker during the initial deal before it's huey's turn
    private volatile boolean myTurn = false;

    // increments each game so old sleeping workers know they belong to a past round
    private volatile int gameId = 0;

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
        // reset everything at the start of each game and bump the round id
        synchronized (playLock) {
            gameId++;
            dealerUpCard = null;
            handFinished = false;
            workerRunning = false;
            pendingPlay = false;
            myTurn = false;
        }
    }

    @Override
    public void endGame(int shoeSize) {
        // stop any queued work so old threads from this round dont bleed into the next game
        synchronized (playLock) {
            handFinished = true;
            pendingPlay = false;
            myTurn = false;
        }
    }

    @Override
    public void deal(Hid hid, Card card, int[] handValues) {
        // the first dealer card is the up card the bot uses for basic strategy
        if (hid.getSeat() == Seat.DEALER && dealerUpCard == null) {
            dealerUpCard = card;
        }

        // myTurn guards against firing a worker during the initial deal phase
        if (!isMySeat(hid) || handFinished || !myTurn) {
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
        // only set myTurn here so deal() doesnt fire a premature worker during the initial deal
        myTurn = true;
        startWorker(hid);
    }

    private void startWorker(Hid hid) {
        // capture the current game id so the worker can check it later
        final int workerGameId;
        synchronized (playLock) {
            if (workerRunning || handFinished) {
                return;
            }
            workerRunning = true;
            pendingPlay = false;
            workerGameId = gameId;
        }

        new Thread(() -> {
            boolean runAgain = false;
            try {
                humanDelay();
                // if the game changed while we were sleeping, dont act
                if (workerGameId == gameId) {
                    executePlay(hid);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                synchronized (playLock) {
                    workerRunning = false;

                    // if a hit came in while this worker was still finishing,
                    // and we are still in the same game, queue another move
                    if (pendingPlay && !handFinished && workerGameId == gameId) {
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

    // appendix 1 wants about 2.5 sec on average so use 2000-3000 ms
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
