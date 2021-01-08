from collections import namedtuple
import math
from sys import argv
from typing import Callable, List
import numpy as np
from matplotlib import pyplot as pp

FIRST_LOWBALL_BID = 50

LOWBALL_INCREMENT = 5

PASSABILITY = 1

SURPLUS = 551

ROUNDS = int(argv[1])


BidOutcome = namedtuple('BidOutcome', ['price', 'outcome'])
BidderState = namedtuple('BidderState', ['current_influence', 'past_bidding_outcomes'])

def lowball_bid(state : BidderState) -> int:
    bid = 0

    if not state.past_bidding_outcomes:
        bid = FIRST_LOWBALL_BID
    else:
        last_bid = state.past_bidding_outcomes[-1]
        bid = last_bid.price

        # we won last bid, just lower the bid slightly
        if last_bid.outcome == 'won':
            if bid > 1:
                bid -= 1
        else:
            # we lost last bid
            # increment the price based on how many rounds we lost in a row
            num_rounds_lost = 0
            i = len(state.past_bidding_outcomes) - 1

            while i >= 0 and state.past_bidding_outcomes[i].outcome == 'lost':
                num_rounds_lost += 1
                i -= 1
            
            bid += LOWBALL_INCREMENT * num_rounds_lost
    
    return min(bid, state.current_influence)
    


def other_bid(state : BidderState) -> int:
    # the other bid
    desired_bid = math.floor(np.random.normal(state.current_influence-500, 100))

    return min(state.current_influence, desired_bid)


def bid_all(state: BidderState) -> int:
    return state.current_influence


def update_state(outcome : str, bid : int, state : BidderState) -> BidderState:
    outcome = BidOutcome(price=bid, outcome=outcome)
    new_outcomes = state.past_bidding_outcomes
    new_outcomes.append(outcome)

    new_influence = 0
    if outcome == 'win':
        new_influence = state.current_influence - bid
    else:
        new_influence = state.current_influence - math.ceil(bid / 2)

    return BidderState(new_influence, new_outcomes)


def run_economy(player_1: Callable[[BidderState], int], player_2: Callable[[BidderState], int]):
    p1_state = BidderState(150, [])
    p2_state = BidderState(150, [])

    p1_votes = 0
    p2_votes = 0
    
    for i in range(ROUNDS):
        # print(f'Starting round {i+1}:')
        p1_bank_inc = math.floor(SURPLUS * (PASSABILITY/2))
        p1_state = BidderState(p1_state.current_influence+p1_bank_inc, p1_state.past_bidding_outcomes)
        # print(f'p1 made {p1_bank_inc} this round')

        p2_bank_inc = math.floor(SURPLUS * (PASSABILITY/2))
        p2_state = BidderState(p2_state.current_influence+p2_bank_inc, p2_state.past_bidding_outcomes)
        # print(f'p2 made {p2_bank_inc} this round')

        
        p1_bid = player_1(p1_state)
        p2_bid = player_2(p2_state)
        # print(f'p1 bid: {p1_bid} ------ p2 bid: {p2_bid}')

        if p1_bid > p2_bid:
            p1_votes += 1
            p1_result = 'won'
            p2_result = 'lost'
            # print(f"p1 {p1_result}")
        elif p1_bid < p2_bid:
            p2_votes += 1
            p1_result = 'lost'
            p2_result = 'won'
            # print(f"p1 {p1_result}")
        else:
            p1_result = 'lost'
            p2_result = 'lost'
            # print("tie :)")
        

        
        # print()
        
        p1_state = update_state(p1_result, p1_bid, p1_state)
        p2_state = update_state(p2_result, p2_bid, p2_state)
    
    print(f'p1 got {p1_votes} votes, and is left with {p1_state.current_influence} influence')
    print(f'p2 got {p2_votes} votes, and is left with {p2_state.current_influence} influence')
    plot_bid(p1_state.past_bidding_outcomes, 'red')
    plot_bid(p2_state.past_bidding_outcomes, 'blue', 0.5)

    if p1_votes > p2_votes:
        return 'p1'
    if p1_votes < p2_votes:
        return 'p2'
    return 'tie'


def plot_bid(past_outcomes : List[BidOutcome], color : str, alpha=1):
    bid_prices = [entry.price for entry in past_outcomes]
    pp.plot(bid_prices, color=color, alpha=alpha)
    # ax = pp.gca()
    # ax.set_yscale('ax')


def run_multiple(num_games):
    p1 = 0
    p2 = 0
    tie = 0
    for _ in range(num_games):
        res = run_economy(lowball_bid, other_bid)
        if res == 'p1':
            p1 += 1
        elif res == 'p2':
            p2 += 1
        elif res == 'tie':
            tie += 1
        else:
            print(f"something wrong, res = {res}")
    
    print(f'p1 = {p1}, p2 = {p2}, tie = {tie}')
    print(f'p1 wins {(p1 / (p1+p2+tie)) * 100:.2f}% of the time')
    

                    
# run_economy(lowball_bid, other_bid)
# pp.show()
run_multiple(1000)