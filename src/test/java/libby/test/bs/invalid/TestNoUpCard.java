package libby.test.bs.invalid;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.util.Play;
import libby.client.BasicStrategy;

/**
 Tests is there is no up card
 */

public class TestNoUpCard extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // cards are dealt
        myHand.hit(new Card(4, Card.Suit.CLUBS));
        myHand.hit(new Card(3, Card.Suit.HEARTS));

        // assign a null up card
        Card upCard = null;

        BasicStrategy strategy = new BasicStrategy ();

        // Play should match the basic strategy
        Play play = strategy.getPlay(myHand, upCard);


        // This throws an exception if there's no upCard
        // We can't have more than 21 with 2 cards in hand so there should be no play
        assert play == Play.NONE;

    }
}