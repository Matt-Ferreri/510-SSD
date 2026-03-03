package libby.test.bs.section4;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.util.Play;
import libby.client.BasicStrategy;

/**
 * Tests my 5,5 vs dealer 7 which should be DOUBLE_DOWN.
 * <p>Except this version is only a starter that fails without
 * completing BasicStrategy.</p>
 * @author Ron.Coleman
 */
public class Test_P5_7_00 extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // Put two cards in the hand, only rank matters, not suit.
        myHand.hit(new Card(5, Card.Suit.CLUBS));
        myHand.hit(new Card(5, Card.Suit.DIAMONDS));

        // Again, only up-card rank matters, not suit.
        Card upCard = new Card(7,Card.Suit.HEARTS);

        //refactored BasicStrategyStarter -> BasicStrategy.
        BasicStrategy strategy = new BasicStrategy ();

        // Play should match the basic strategy.
        Play play = strategy.getPlay(myHand, upCard);

        // This throws an exception if play is not the expected Play.
        assert play == Play.DOUBLE_DOWN;
    }
}