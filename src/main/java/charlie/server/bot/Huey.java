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
 * huey is a bot that plays blackjack using basic strategy.
 * it implements IBot so the dealer can treat it like any other player,
 * and Runnable because the framework requires it even though the run()
 * method isn't used directly here.
 *
 * threading overview:
 *   the dealer calls play() on huey when it's huey's turn. huey spawns
 *   a short-lived worker thread that sleeps for a human-like delay and
 *   then sends exactly one action (hit, stay, or double down) to the dealer.
 *   after a hit, the dealer does NOT call play() again -- instead it calls
 *   deal() with the new card. huey uses that callback to queue the next move.
 *
 * state is protected by playLock. volatile flags keep things visible across
 * threads without needing to hold the lock for every read.
 */
public class Huey implements IBot, Runnable {

    // delay range: 2000-3000 ms, so average is about 2500 ms as required by appendix 1
    private static final int DELAY_MS_MIN = 2000;
    private static final int DELAY_MS_SPAN = 1001;

    // the strategy huey uses to decide hit/stay/double -- no splits allowed for bots
    private final BotBasicStrategy strategy = new BotBasicStrategy();

    // guards workerRunning, pendingPlay, handFinished, and gameId so workers
    // don't race each other when deciding whether to start or queue the next move
    private final Object playLock = new Object();

    // set once when the dealer connects; used to send hit/stay/doubleDown requests
    private volatile Dealer dealer;

    // the hand the dealer tracks for huey; the dealer updates this object directly
    // when it deals cards, so h.size() and h.getValue() always reflect reality
    private volatile Hand hand;

    // the seat huey is sitting in; assigned once in sit() and never changes
    private volatile Seat seat;

    // the dealer's first visible card, captured in deal() so basic strategy can use it
    private volatile Card dealerUpCard;

    // set to true once huey busts, wins, loses, etc. -- tells workers to stop sending moves
    private volatile boolean handFinished;

    // true while a worker thread is sleeping or executing a move
    private volatile boolean workerRunning = false;

    // true when a hit card arrived while the current worker was still finishing up,
    // meaning another move needs to be sent once the worker clears out
    private volatile boolean pendingPlay = false;

    // true only after play() is called for huey's seat; prevents deal() from
    // firing a worker during the initial deal before it's actually huey's turn
    private volatile boolean myTurn = false;

    // increments at the start of each game so sleeping workers from the previous
    // round can detect they're stale and exit without sending any moves
    private volatile int gameId = 0;


     // required by Runnable but not used -- the dealer framework handles threading
     
    @Override
    public void run() {
    }

    
     //returns huey's hand so the dealer can store it and deal cards into it directly
    
    @Override
    public Hand getHand() {
        return hand;
    }

    // called once when huey connects to the server; stores the dealer reference
    // so huey can call hit/stay/doubleDown later
    @Override
    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }

    // called once when huey joins the table; stores the seat and creates the hand
    // the dealer will use throughout all games
    @Override
    public void sit(Seat seat) {
        this.seat = seat;
        this.hand = new Hand(new Hid(seat));
    }

    // called at the start of every game before any cards are dealt.
    // resets all per-round state and bumps the gameId so any still-sleeping
    // workers from the last round know they're no longer valid.
    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
        synchronized (playLock) {
            gameId++;
            dealerUpCard = null;
            handFinished = false;
            workerRunning = false;
            pendingPlay = false;
            myTurn = false;
        }
    }

    /**
     * called after outcomes are announced, before the next game starts.
     * marks the hand as finished and clears myTurn so any late-arriving
     * worker threads shut down cleanly instead of sending moves for a
     * game that's already over.
     */
    @Override
    public void endGame(int shoeSize) {
        synchronized (playLock) {
            handFinished = true;
            pendingPlay = false;
            myTurn = false;
        }
    }

    /**
     * called every time any card is dealt to any player or the dealer.
     *
     * two jobs:
     *   1. capture the dealer's first up card so basic strategy can use it
     *   2. after huey hits and the dealer deals huey a card, queue the next move
     *      (the dealer won't call play() again after a hit, so huey has to self-queue)
     *
     * the myTurn guard is critical -- without it, deal() would fire a premature
     * worker during the initial deal phase (when huey gets his first two cards)
     * before play() is ever called, which would block the real worker from starting.
     */
    @Override
    public void deal(Hid hid, Card card, int[] handValues) {
        // capture the first card the dealer shows so basic strategy knows what to play against
        if (hid.getSeat() == Seat.DEALER && dealerUpCard == null) {
            dealerUpCard = card;
        }

        // ignore cards dealt to other seats, cards dealt before huey's turn starts,
        // and cards dealt after the hand is already over
        if (!isMySeat(hid) || handFinished || !myTurn) {
            return;
        }

        Hand h = hand;
        // need at least 2 cards before making any decisions
        if (h == null || h.size() < 2) {
            return;
        }

        // only queue a follow-up move if the hand is still alive and not at 21
        // (broke/blackjack/charlie/21 are all terminal -- the dealer handles the outcome)
        if (!h.isBroke() && !h.isBlackjack() && !h.isCharlie() && h.getValue() < 21) {
            boolean startNow = false;
            synchronized (playLock) {
                if (handFinished) {
                    return;
                }
                // if the previous worker is still winding down, mark the pending move
                // so the worker's finally block picks it up and starts a new worker
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

    /**
     * called by the dealer when it's huey's turn to act.
     * sets myTurn so deal() callbacks can safely queue follow-up moves,
     * then launches the first worker for this turn.
     */
    @Override
    public void play(Hid hid) {
        if (!isMySeat(hid)) {
            return;
        }
        // myTurn must be set before startWorker so deal() can queue follow-up moves
        // if a hit card arrives while the first worker is still sleeping
        myTurn = true;
        startWorker(hid);
    }

    /**
     * starts a worker thread that will sleep for a human-like delay and then
     * send one play to the dealer. if a worker is already running or the hand
     * is over, does nothing.
     *
     * the worker captures gameId at launch. if the game ends before the worker
     * wakes up, the id won't match and the worker exits without sending anything.
     *
     * when the worker finishes, it checks pendingPlay. if a hit card arrived
     * while it was sleeping, it kicks off another worker for the next decision.
     */
    private void startWorker(Hid hid) {
        final int workerGameId;
        synchronized (playLock) {
            // don't start if a worker is already active or the hand is done
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
                // wait a random 2-3 seconds so huey plays at a human pace
                humanDelay();

                // if the game changed while we were sleeping, this worker is stale -- skip the move
                if (workerGameId == gameId) {
                    executePlay(hid);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                synchronized (playLock) {
                    workerRunning = false;

                    // if deal() flagged a pending move while this worker was finishing,
                    // and we're still in the same game with an active hand, chain another worker
                    if (pendingPlay && !handFinished && workerGameId == gameId) {
                        pendingPlay = false;
                        runAgain = true;
                    }
                }
            }

            // start outside the lock so startWorker can acquire playLock without deadlocking
            if (runAgain) {
                startWorker(hid);
            }
        }).start();
    }

    /**
     * reads the current hand state, asks basic strategy what to do, and sends
     * exactly one action to the dealer. all checks are inside the lock so the
     * hand state can't change between the guard checks and the dealer call.
     *
     * if basic strategy returns NONE (e.g. dealer up card was null), huey stays
     * so the game doesn't freeze waiting for a move that never comes.
     *
     * double down is only legal on the first two cards; if basic strategy says
     * double but the hand has more than 2 cards, huey hits instead.
     */
    private void executePlay(Hid hid) {
        Dealer d = dealer;
        if (d == null) {
            return;
        }

        synchronized (playLock) {
            // hand could have ended while the worker was sleeping
            if (handFinished) {
                return;
            }

            Hand h = hand;

            // sanity check: make sure this hid really belongs to huey's seat
            if (h == null || h.getHid().getSeat() != hid.getSeat()) {
                return;
            }

            // if the hand already reached a terminal state, don't send another move
            if (h.isBroke() || h.isBlackjack() || h.isCharlie()) {
                return;
            }

            // basic strategy needs at least 2 cards to make a decision
            if (h.size() < 2) {
                return;
            }

            Card up = dealerUpCard;
            Play p = (up == null) ? Play.NONE : strategy.getPlay(h, up);

            // fallback: if no valid play came back, stay to keep the game moving
            if (p == Play.NONE) {
                d.stay(this, hid);
                handFinished = true;
                return;
            }

            // double down is only valid on the opening two cards
            if (p == Play.DOUBLE_DOWN && h.size() != 2) {
                p = Play.HIT;
            }

            switch (p) {
                case STAY:
                    d.stay(this, hid);
                    handFinished = true;
                    break;

                case DOUBLE_DOWN:
                    // dealer automatically doubles the bet; huey just sends the request
                    d.doubleDown(this, hid);
                    handFinished = true;
                    break;

                case HIT:
                    // hand is still alive after this; deal() will queue the next move
                    d.hit(this, hid);
                    break;

                default:
                    // unexpected play -- stay to avoid freezing the game
                    d.stay(this, hid);
                    handFinished = true;
                    break;
            }
        }
    }

    /**
     * returns true if the given hid belongs to huey's seat
     */
    private boolean isMySeat(Hid hid) {
        return seat != null && hid.getSeat() == seat;
    }

    /**
     * sleeps for a random duration between 2000 and 3000 ms so huey plays
     * at a human pace as required by appendix 1 (average ~2.5 seconds)
     */
    private static void humanDelay() throws InterruptedException {
        int ms = DELAY_MS_MIN + (int) (Math.random() * DELAY_MS_SPAN);
        Thread.sleep(ms);
    }

    // the outcome callbacks below all do the same thing: mark the hand as finished
    // for huey's seat so the worker knows not to send any more moves

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

    // these events don't affect huey's state but must be implemented for IBot

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
