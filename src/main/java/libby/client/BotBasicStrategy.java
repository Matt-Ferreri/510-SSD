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
            // if the play is not a split, then do the play
            Play play = super.getPlay(hand, upCard);
            if (play != Play.SPLIT) {
                return play;
            }
            // if it is a split, do the correct action based on basic strategy other than split
            return resolveWithoutSplit(hand, upCard);
        }
        /**
         * Maps a pair that would split to a hit/stay/double using Sections 1 and 2 only.
         */
        private Play resolveWithoutSplit(Hand hand, Card upCard) {
            int value = hand.getValue();
            // refer to section 1 if value is greater than 12
            if (value >= 12) {
                return doSection1(hand, upCard);
            }
            if (value == 4) {
                // 2+2: Section 2 starts at 5 so we just hit
                return Play.HIT;
            }
            // if the value is between 5 and 11, refer to section 2
            if (value >= 5 && value <= 11) {
                return doSection2(hand, upCard);
            }
            return Play.NONE;
        }
        /**
         * Looks up a play from section2Rules for a given row vs. dealer up-card.
         *
         * @param rowIndex 0 = 11, 1 = 10, 2 = 9, 3 = 5–8
         */
        private Play playSection2Row(int rowIndex, Card upCard) {
            Play[] row = section2Rules[rowIndex];
            int colIndex = upCard.getRank() - 2;
            if (upCard.isFace()) {
                colIndex = 10 - 2;
            } else if (upCard.isAce()) {
                colIndex = 9;
            }
            return row[colIndex];
        }
    }
