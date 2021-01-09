from recordclass import StructClass


class BidState(StructClass):
    round_number: int      # the current round number, starts at 1
    influence: int         # how much influence we currently have
    last_price: int        # the price we bid on last round
    last_outcome: str      # the result of the last bid: { 'won', 'lost' }
    streak: int            # how many times in a row did the last result happen
