from abc import ABC, abstractmethod

class MDP(ABC):

    @abstractmethod
    def start_state(self):
        """
        Give the start state
        """
        pass

    @abstractmethod
    def is_end(self, state) -> bool:
        """
        Check if a state is the end state
        """
        pass

    @abstractmethod
    def discount(self) -> float:
        """
        Return the discount factor
        """
        pass

    @abstractmethod
    def actions(self, state):
        """
        Given a state, return a list of possible actions
        """
        pass

    @abstractmethod
    def succ_prob_reward(self, state, action):
        """
        Given a state and action, return list of possible outcomes,
        each as a tuple of (successor state, probability, reward)
        """
        pass

