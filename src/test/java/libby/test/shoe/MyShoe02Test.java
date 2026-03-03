package libby.test.shoe;

import charlie.card.Card;
import charlie.plugin.IShoe;
import junit.framework.TestCase;
import libby.plugin.MyShoe02;


public class MyShoe02Test extends TestCase {
    public void test() {
        IShoe shoe = new MyShoe02();
        shoe.init();

        assert shoe.size() == 10;

        Card card1 = shoe.next();
        assert card1.getRank() == 2;

        Card card2 = shoe.next();
        assert card2.getRank() == Card.QUEEN;

        Card card3 = shoe.next();
        assert card3.getRank() == 3;

        Card card4 = shoe.next();
        assert card4.getRank() == 7;

        Card card5 = shoe.next();
        assert card5.getRank() == 4;

        Card card6 = shoe.next();
        assert card6.getRank() == 5;

        Card card7 = shoe.next();
        assert card7.getRank() == 6;

        Card card8 = shoe.next();
        assert card8.getRank() == Card.KING;

        Card card9 = shoe.next();
        assert card9.getRank() == 9;

        Card card10 = shoe.next();
        assert card10.getRank() == Card.JACK;
    }
}
