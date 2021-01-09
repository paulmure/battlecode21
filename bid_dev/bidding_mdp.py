from recordclass import StructClass
from mdp import MDP


class BidState(StructClass):
    round_number: int
    last_price: int
    last_result: str
    streak: int


class BiddingMDP(MDP):

    @staticmethod
    def start_state():
        """
        Give the start state
        """
        return BidState(round_number=1,
                        last_price=None,
                        last_result=None,
                        streak=None)

    @staticmethod
    def is_end(state):
        """
        Check if a state is the end state
        """
        return state.round_number > 3000

    def discount(self):
        """
        Return the discount factor
        """
        pass

    def actions(self, state):
        """
        Given a state, return a list of possible actions
        """
        pass

    def succ_prob_reward(self, state, action):
        """
        Given a state and action, return list of possible outcomes,
        each as a tuple of (successor state, probability, reward)
        """
        pass


if __name__ == "__main__":
    mdp = BiddingMDP()
