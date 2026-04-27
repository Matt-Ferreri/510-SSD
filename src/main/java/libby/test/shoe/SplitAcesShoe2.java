package libby.test.shoe;

import charlie.card.Card;

/**
 * {You 10+10+P!{A,A} | Dealer 10+6 >> Win{7.5}, Win{7.5}}
 * You gets 10, then the dealer gets face card (which is down)
 * then you get 10, then the dealer gets 6 (which is up card)
 * After split tens, follow-up A, A for 2 blackjacks
 */
public class SplitAcesShoe2 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        // Round 1: player 10, dealer hole 10
        cards.add(new Card(10, Card.Suit.HEARTS));
        cards.add(new Card(Card.QUEEN, Card.Suit.CLUBS));

        // Round 2: player 10, dealer up 6
        cards.add(new Card(10, Card.Suit.DIAMONDS));
        cards.add(new Card(6, Card.Suit.SPADES));

        // Split: hit to first 10-hand, mandatory deal to second 10-hand
        cards.add(new Card(Card.ACE, Card.Suit.HEARTS));
        cards.add(new Card(Card.ACE, Card.Suit.DIAMONDS));

    }

    @Override
    public boolean shuffleNeeded() {
        return false;
    }
}
