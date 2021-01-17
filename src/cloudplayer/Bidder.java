package cloudplayer;

import battlecode.common.GameActionException;

public class Bidder extends RobotPlayer {
    private static final int FIRST_ROUND_BID = 50;
    private static final int INCREMENT = 5;
    private int roundsLost = 0;
    private int lastBid;
    private int lastRoundVotes;

    public void bid() throws GameActionException {
        int bid;
        int thisRoundVotes = rc.getTeamVotes();
        if (thisRoundVotes > 1500) {
            return;
        }

        // is there a way to get around checking this every round?
        if (rc.getRoundNum() == 1) {
            bid = FIRST_ROUND_BID;
        } else {
            if (thisRoundVotes > lastRoundVotes) {
                roundsLost = 0;
                if (lastBid > 1) {
                    bid = lastBid - 1;
                } else {
                    bid = lastBid;
                }
            } else {
                ++roundsLost;
                bid = lastBid + (roundsLost * INCREMENT);
            }
        }

        if (bid > rc.getInfluence()) {
            bid = rc.getInfluence();
        }

        if (rc.canBid(bid)) {
            rc.bid(bid);
            lastBid = bid;
        }

        // System.out.printf("Round: %d, bid: %d, lastRoundVotes: %d, thisRoundVotes:
        // %d\n", rc.getRoundNum(), bid, lastRoundVotes, thisRoundVotes);
        lastRoundVotes = thisRoundVotes;
    }
}
