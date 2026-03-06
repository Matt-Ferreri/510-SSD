package libby.test.bs.invalid;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import junit.framework.TestCase;
import libby.client.BasicStrategy;

/**
 Tests is the player's hand only has one card
 */

public class TestHandHasOneCard extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // Put one card in the hand, only rank matters, not suit.
        myHand.hit(new Card(2, Card.Suit.CLUBS));

        // Again, only up-card rank matters, not suit.
        Card upCard = new Card(2,Card.Suit.HEARTS);

        BasicStrategy strategy = new BasicStrategy ();

        // Play should match the basic strategy.
        Play play = strategy.getPlay(myHand, upCard);

        // Players must get dealt two cards and sum has to equal more than 2, since 2 is not an option, we should get PLAY.NONE
        // This throws an exception if play is not the expected Play NONE.
        assert play = Play.NONE;
    }
}
