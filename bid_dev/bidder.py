from collections import namedtuple
import math
from sys import argv
from typing import Callable, List
import numpy as np
from matplotlib import pyplot as pp
from multiprocessing import Pool
from functools import partial
from tqdm import tqdm

FIRST_LOWBALL_BID = 50

LOWBALL_INCREMENT = 5

PASSABILITY = 1

SURPLUS = 551

BID_ALL_AFTER_ROUND = 1400

ROUNDS = int(argv[1])

bid_mod = 4

wacky_bids = []

num_rounds_lost = 0
num_rounds_won = 0

BidOutcome = namedtuple('BidOutcome', ['price', 'outcome'])
BidderState = namedtuple('BidderState', ['current_influence', 'past_bidding_outcomes'])

def lowball_bid(state : BidderState) -> int:
    global num_rounds_lost
    global num_rounds_won
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
            num_rounds_won += 1
            num_rounds_lost = 0
        else:
            # we lost last bid
            # increment the price based on how many rounds we lost in a row
            num_rounds_lost += 1
            num_rounds_won = 0
            # print("we've lost:")
            # print(num_rounds_lost)
            bid += LOWBALL_INCREMENT * num_rounds_lost
    
    return min(bid, state.current_influence)
    
def bid_all_at_round(state: BidderState) -> int:

    global BID_ALL_AFTER_ROUND
    round_num = BID_ALL_AFTER_ROUND
    r = len(state.past_bidding_outcomes)
    bid = 0

    curr_inf = state.current_influence
    if r == round_num:
        print("BIDDING ALL NOW")
    if r >= round_num:
        bid = math.floor((SURPLUS * (PASSABILITY/2)) * 1.5)
    
    return min(state.current_influence, bid)

def other_bid(state : BidderState) -> int:
    # the other bid
    r = len(state.past_bidding_outcomes)
    global bid_mod
    bid_mean = state.current_influence-np.random.normal(250, 50)
    desired_bid = math.floor(bid_mean)
    desired_bid = max(1, desired_bid)
    if desired_bid > state.current_influence:
        print(desired_bid)
        print("exceeded max bid!")
        bid_mod *= 2
    return min(state.current_influence, desired_bid)


def bid_all(state: BidderState) -> int:
    return state.current_influence


def bid_half_of_income(state: BidderState) -> int:
    return int(math.floor(SURPLUS * (PASSABILITY/2))) // 2


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
        wacky_bids.append(p2_bid)
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
    # return p1_votes / ROUNDS


def plot_bid(past_outcomes : List[BidOutcome], color : str, alpha=1):
    bid_prices = [entry.price for entry in past_outcomes]
    pp.plot(bid_prices, color=color, alpha=alpha)
    # ax = pp.gca()
    # ax.set_yscale('ax')


def adjustment_agent():
    outcomes = []
    for i in tqdm(range(10)):
        LOWBALL_INCREMENT = i
        results = []
        for i in range(100):
            results.append(run_economy(lowball_bid, other_bid))
        outcomes.append(np.mean(results))
    pp.plot(range(1, 11), outcomes)


def run_multiple(num_games):
    p1 = 0
    p2 = 0
    tie = 0
    
    with Pool() as pool:
        func = partial(run_economy, lowball_bid)
        bs = [other_bid] * num_games
        reses = list(tqdm(pool.imap(func, bs)))
    
    for res in reses:
        if res == 'p1':
            p1 += 1
        elif res == 'p2':
            p2 += 1
        else:
            tie += 1

    print(f'p1 = {p1}, p2 = {p2}, tie = {tie}')
    print(f'p1 wins {(p1 / (p1+p2+tie)) * 100:.2f}% of the time')
    

if __name__ == "__main__":
    print(run_economy(lowball_bid, bid_all_at_round), "won")
    # adjustment_agent()
    pp.show()
    # pp.clf()
    # pp.hist(wacky_bids)
    # pp.show()
    # run_multiple(1000)
