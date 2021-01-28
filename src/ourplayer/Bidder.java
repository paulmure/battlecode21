package ourplayer;

import battlecode.common.GameActionException;

public class Bidder extends RobotPlayer {
    private static final int FIRST_ROUND_BID = 50;
    private static final int BASE_INCREMENT = 5;
    // WS = Win Streak, LS = Lost Streak
    private static int WS_ALPHA = 1;
    private static int WS_BETA = 10;
    private static final double WS_K = 10;
    private static final double LS_GROWTH = 1.5;
    private static final int INCREMENT_INFLUENCE_THRESHOLD = 1200;

    private int roundsLost = 0;
    private int roundsWon = 0;
    private int lastRoundVotes = 0;
    private int lastBid = FIRST_ROUND_BID;

    public void setLastBid(int bid) {
        lastBid = bid;
    }

    public int bid(int influence) throws GameActionException {
        // stop voting if we get majority
        int thisRoundVotes = rc.getTeamVotes();
        if (thisRoundVotes > 750) {
            return 0;
        }

        int bid = getNewBid(influence, thisRoundVotes > lastRoundVotes);

        // update state
        lastRoundVotes = thisRoundVotes;
        lastBid = bid;

        return bid;
    }

    private int getNewBid(int influence, boolean wonLastVote) {
        int bid = 0;

        if (wonLastVote) {
            roundsLost = 0;
            ++roundsWon;

            // the lowest we want to bid is 1
            if (lastBid > 1) {
                // decrement = [ α ^ (β * roundsWon) ] * k
                // β < α < 1
                // int decrement = (int) (Math.pow(WS_ALPHA, WS_BETA * roundsWon) * WS_K);
                double numer = WS_BETA-WS_ALPHA;
                // System.out.println("frac");
                // System.out.println(numer);
                double denom = Math.exp(5.0-(roundsWon/4.0))+1;
                // System.out.println(denom);
                // System.out.println("==");
                int decrement = (int) (numer/denom) + WS_ALPHA;
                bid = lastBid - decrement;
                // System.out.println("rounds lost "+ roundsLost);
                // System.out.println("ourplayer decrement by " + decrement);
            } else {
                bid = 1;
            }
        } else {
            roundsWon = 0;
            ++roundsLost;

            int increment = BASE_INCREMENT;
            if (influence > INCREMENT_INFLUENCE_THRESHOLD) {
                increment = (int) Math.min(Math.pow(LS_GROWTH, roundsLost), influence / 2);
            }

            // System.out.println("increment by " + increment);
            bid = lastBid + (roundsLost * increment);
        }

        if (bid > influence) {
            bid = influence;
        }
        
        // System.out.println("bid final: "+bid);
        // System.out.println("=======");

        return Math.max(bid, 0);
    }
}
