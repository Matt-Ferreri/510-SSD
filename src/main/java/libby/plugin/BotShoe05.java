package libby.plugin;

import charlie.card.Card;

public class BotShoe05 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        // game 5
        // Huey: 10+9 stay/win
        // You: 10+10 stay/win
        // Dewey: 6+7+5 hit/push
        // Dealer: 6+4+8
        cards.add(new Card(10, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.HEARTS));
        cards.add(new Card(6, Card.Suit.SPADES));
        cards.add(new Card(6, Card.Suit.HEARTS));

        cards.add(new Card(9, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.DIAMONDS));
        cards.add(new Card(7, Card.Suit.SPADES));
        cards.add(new Card(4, Card.Suit.SPADES));

        cards.add(new Card(5, Card.Suit.SPADES));
        cards.add(new Card(8, Card.Suit.SPADES));
    }
}
