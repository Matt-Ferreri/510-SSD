package libby.test.shoe;

import charlie.card.Card;
import charlie.plugin.IShoe;
import junit.framework.TestCase;
import libby.plugin.MyShoe02;


public class MyShoe02Test extends TestCase {
    public void test() {
        IShoe shoe = new MyShoe02();
        shoe.init();

        //assert shoe.size() == 10;

        Card card1 = shoe.next();
        assert card1.getRank() == Card.KING;

        Card card2 = shoe.next();
        assert card2.getRank() == 2;

        Card card3 = shoe.next();
        assert card3.getRank() == Card.KING;

        Card card4 = shoe.next();
        assert card4.getRank() == Card.QUEEN;

        Card card5 = shoe.next();
        assert card5.getRank() == 6;

        Card card6 = shoe.next();
        assert card6.getRank() == 3;

        Card card7 = shoe.next();
        assert card7.getRank() == 6;

        Card card8 = shoe.next();
        assert card8.getRank() == 5;

        Card card9 = shoe.next();
        assert card9.getRank() == 8;

        Card card10 = shoe.next();
        assert card10.getRank() == 4;

        Card card11 = shoe.next();
        assert card11.getRank() == 8;

        Card card12 = shoe.next();
        assert card12.getRank() == 5;

        Card card13 = shoe.next();
        assert card13.getRank() == 2;

        Card card14 = shoe.next();
        assert card14.getRank() == 2;
    }
}
