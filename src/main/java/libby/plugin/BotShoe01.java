package libby.plugin;

import charlie.card.Card;

public class BotShoe01 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        // game 1
        // Huey: K+6+2 hit/win
        // You: 10+5+4 hit/win
        // Dewey: 10+10 stay/win
        // Dealer: K+7
        cards.add(new Card(Card.KING, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.HEARTS));
        cards.add(new Card(Card.KING, Card.Suit.HEARTS));

        cards.add(new Card(6, Card.Suit.SPADES));
        cards.add(new Card(5, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.DIAMONDS));
        cards.add(new Card(7, Card.Suit.SPADES));

        cards.add(new Card(2, Card.Suit.SPADES));
        cards.add(new Card(4, Card.Suit.SPADES));
    }
}
