package libby.plugin;

import charlie.card.Card;

public class MyShoe02 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        cards.add(new Card(2, Card.Suit.SPADES));
        cards.add(new Card(Card.QUEEN, Card.Suit.HEARTS));
        cards.add(new Card(3, Card.Suit.HEARTS));
        cards.add(new Card(7, Card.Suit.CLUBS));
        cards.add(new Card(4, Card.Suit.SPADES));
        cards.add(new Card(5, Card.Suit.CLUBS));
        cards.add(new Card(6, Card.Suit.DIAMONDS));
        cards.add(new Card(Card.KING, Card.Suit.CLUBS));
        cards.add(new Card(9, Card.Suit.DIAMONDS));
        cards.add(new Card(Card.JACK, Card.Suit.CLUBS));
    }
}
