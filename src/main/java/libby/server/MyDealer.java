package libby.server;

import charlie.actor.House;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Dealer;
import charlie.plugin.IPlayer;
import charlie.util.Play;
import org.apache.log4j.Logger;


public class MyDealer extends Dealer {

    private static final Logger LOG = Logger.getLogger(MyDealer.class);

    public MyDealer(House house) {
        super(house);
    }

    /**
     * Moves to the next hand at the table.
     */
    @Override
    protected void goNextHand() {
        LOG.info("hand sequence index = " + nextHandIndex + " hand sequence size = " + handSequence.size());

        // Get next hand and inform player
        if (nextHandIndex < handSequence.size()) {
            // Did we "hit" a split hand this time
            boolean firstSplitHit = false;

            Hid hid = handSequence.get(nextHandIndex++);

            active = players.get(hid);
            LOG.info("active player = " + active);

            // Check for isBlackjack before moving on
            Hand hand = this.hands.get(hid);

            // If hand has Blackjack, it's not automatic hand wins
            // since the dealer may also have isBlackjack
            if (hand.isBlackjack()) {
                goNextHand();
                return;
            }

            // Is this hand created from a "split" AND about to be new turn?
            // If so, we need to "HIT" the hand with its first card.
            if (hid.isSplit() && hand.size() == 1) {
                // Need to request a delay or it comes out too fast.
                try {
                    Thread.sleep(DEAL_DELAY);

                    Card card = deal();
                    hand.hit(card);
                    firstSplitHit = true;
                } catch (InterruptedException ex) {
                    LOG.error(ex.getMessage());
                }
            }

            // If mandatory follow-up made A+10, pay/notify like hit() and advance (not in stock Dealer)
            if (hand.isBlackjack() && firstSplitHit) {
                hid.request(Play.STAY);
                // deal the next card to the hand
                for (IPlayer player : playerSequence) {
                    player.deal(hid, hand.getCard(1), hand.getValues());
                }
                updateBankroll(hid, BLACKJACK_PAYS);
                for (IPlayer p : playerSequence) {
                    p.blackjack(hid);
                }
                goNextHand();
                return;
            }

            // If already 2-card 21 and not just handled above
            if (hand.isBlackjack()) {
                goNextHand();
                return;
            }

            // Unless the player got a isBlackjack, tell the player they're
            // to start playing this hand
            for (IPlayer player : playerSequence) {

                // If the hand is a split, lets tell everyone a deal happened.
                // Do this here to prevent using the same 'for loop' twice.
                if (firstSplitHit) {
                    // tell players about hit
                    player.deal(hid, hand.getCard(1), hand.getValues());
                }

                LOG.info("sending turn " + hid + " to " + player);
                player.play(hid);
            }
        } else
            // If there are no more hands, close out game with dealer
            // making last play.
            closeGame();
    }

    /**
     * Hits player hand upon request only AFTER the initial rounds.
     * @param iplayer Player requesting a hit.
     * @param hid     Player's hand id
     */
    @Override
    public void hit(IPlayer iplayer, Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        if (hand == null) {
            LOG.error("got invalid HIT player = " + iplayer);
            return;
        }

        // Deal a card
        Card card = deal();
        hand.hit(card);

        hid.request(Play.HIT);

        LOG.info("hit hid = " + hid + " with " + card);

        // All players MUST test for charlie. Otherwise they will
        // not know they have this hand and may try to hit if hand<21.
        for (IPlayer player : playerSequence) {
            player.deal(hid, card, hand.getValues());
        }

        // If the hand isBroke, we're done with this hand
        if (hand.isBroke()) {
            updateBankroll(hid, LOSS);

            // Tell everyone what happened
            for (IPlayer _player : playerSequence) {
                _player.bust(hid);
            }

            LOG.info("going to next hand");
            goNextHand();
        }
        // If hand got a Charlie or Blackjack, we're done with this hand
        else if (hand.isCharlie()) {
//            hid.multiplyAmt(CHARLIE_PAYS);
            hid.request(Play.STAY);

            updateBankroll(hid, CHARLIE_PAYS);

            // Tell everyone what happened
            for (IPlayer _player : playerSequence) {
                _player.charlie(hid);
            }

            goNextHand();
        } else if (hand.isBlackjack()) {
            hid.request(Play.STAY);

            updateBankroll(hid, BLACKJACK_PAYS);

            // Tell everyone what happened
            for (IPlayer _player : playerSequence) {
                // Stock Dealer wrongly called charlie(hid) here; use blackjack.
                _player.blackjack(hid);
            }

            goNextHand();
        }
        // Player has 21: don't force player to break!
        else if (hand.getValue() == 21) {
            goNextHand();
        }
    }
}
