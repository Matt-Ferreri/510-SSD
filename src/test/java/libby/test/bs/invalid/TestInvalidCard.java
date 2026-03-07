package libby.test.bs.invalid;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.util.Play;
import libby.client.BasicStrategy;

/**
 Tests is the player's hand has an invalid card
 */

public class TestInvalidCard extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        myHand.hit(new Card(4, Card.Suit.CLUBS));
        // 30 is an invalid rank
        myHand.hit(new Card(5, Card.Suit.HEARTS));

        // Again, only up-card rank matters, not suit.
        Card upCard = new Card(30,Card.Suit.HEARTS);

        BasicStrategy strategy = new BasicStrategy ();

        // Play should match the basic strategy
        Play play = strategy.getPlay(myHand, upCard);

        // This throws an exception if play is not the expected Play NONE.
        // We can't have a card of rank 30, so we expect the Play to be NONE
        assert play == Play.NONE;
    }
}