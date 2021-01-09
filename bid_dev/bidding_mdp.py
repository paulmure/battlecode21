from recordclass import StructClass
from mdp import MDP
from typing import List


class BidState(StructClass):
    round_number: int      # the current round number, starts at 1
    influence: int         # how much influence we currently have
    last_price: int        # the price we bid on last round
    last_outcome: str      # the result of the last bid: { 'won', 'lost' }
    streak: int            # how many times in a row did the last result happen


class BiddingMDP(MDP):

    def __init__(self, discount, starting_influence, price_range, price_delta) -> None:
        super().__init__(discount)
        self.starting_influence = starting_influence
        self.price_range = price_range
        self.price_delta = price_delta

    def start_state(self) -> BidState:
        return BidState(round_number=1,
                        influence=self.starting_influence,
                        last_price=0,
                        last_outcome='',
                        streak=0)

    @staticmethod
    def is_end(state: BidState) -> bool:
        return state.round_number > 3000

    def actions(self, state: BidState) -> List[int]:
        """
        Action gives you a list of bid prices for the next round
        """
        pass

    def succ_prob_reward(self, state, action):
        pass


def main():
    pass


if __name__ == "__main__":
    main()
