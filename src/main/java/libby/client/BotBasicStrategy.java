package libby.client;
import charlie.util.Play;
import charlie.card.Card;
import charlie.card.Hand;

/**
 * this class is for the basic strategy that the bots will use
 * this is different from the normal basic strategy because in no situation can the bots split
 * the bot basic strategy will have to refer to basic strategy to in most sitations, other than when its a split
 */
    public class BotBasicStrategy extends BasicStrategy {
        @Override
        public Play getPlay(Hand hand, Card upCard) {
            if (hand == null || upCard == null || hand.size() < 2) {
                return Play.NONE;
            }

            Card card1 = hand.getCard(0);
            Card card2 = hand.getCard(1);

            Play play;
            // case for pairs, refer to section 4
            if (hand.isPair()) {
                play = doSection4(hand, upCard);
            } else if (hand.size() == 2 && (card1.getRank() == Card.ACE || card2.getRank() == Card.ACE)) {
                play = Play.HIT;
            } else if (hand.getValue() >= 5 && hand.getValue() < 12) {
                play = doSection2(hand, upCard);
            } else if (hand.getValue() >= 12) {
                play = doSection1(hand, upCard);
            } else {
                play = Play.NONE;
            }

            if (play != Play.SPLIT) {
                return play;
            }
            // case for split
            int value = hand.getValue();
            // refer to section 1 if value is greater than 12
            if (value >= 12) {
                return doSection1(hand, upCard);
            }
            // case for 2+2
            if (value == 4) {
                return Play.HIT;
            }
            // case for 5-11
            if (value >= 5 && value <= 11) {
                return doSection2(hand, upCard);
            }
            return Play.NONE;
        }

    
    }
