from abc import ABC, abstractmethod


class MDP(ABC):

    def __init__(self, discount) -> None:
        self._discount = discount

    @property
    def discount(self) -> float:
        """
        Return the discount factor
        """
        return self._discount

    @discount.setter
    def discount(self, value) -> None:
        if value < 0 or value > 1:
            raise ValueError("Discount should be between 0 and 1")
        self._discount = value

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
