from recordclass import StructClass


class BidState(StructClass):
    round_number: int      # the current round number, starts at 1
    influence: int         # how much influence we currently have
    last_price: int        # the price we bid on last round
    last_outcome: int      # the result of the last bid: won = +1, lose = -1
    streak: int            # how many of the last outcome in a row, win = + , lose = -
    income_per_turn: int
    votes_won: int
    votes_lost: int
