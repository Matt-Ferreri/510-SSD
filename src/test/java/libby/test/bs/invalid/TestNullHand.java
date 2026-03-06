package libby.test.bs.invalid;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.util.Play;
import libby.client.BasicStrategy;

/**
 Tests is the player's hand is NULL
 */

public class TestNullHand extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // player does not get any cards

        // Again, only up-card rank matters, not suit.
        Card upCard = new Card(2,Card.Suit.HEARTS);

        //refactored BasicStrategyStarter -> BasicStrategy.
        BasicStrategy strategy = new BasicStrategy ();

        // Play should match the basic strategy
        Play play = strategy.getPlay(myHand, upCard);

        // This throws an exception if play is not the expected Play NONE.
        // We didn't put any cards in the player's hand so we expect the Play to be NONE
        assert play == Play.NONE;
    }
}
