import math
from matplotlib import pyplot as pp

def ec_money(turn):
    return math.ceil(0.2 * math.sqrt(turn))

magic = [0, 21, 41, 63, 85, 107, 130, 154, 178, 203, 228, 255, 282, 310, 339, 368, 399, 431, 463, 497, 532, 568, 605, 643, 683, 724, 766, 810, 855, 902, 949, 2**31 - 1]

turn = 1
slanderers = []

moneys = []
money = 150
speed = 25

while turn < 1500:

    if turn % 10 == 0:

        print(money)
    if turn % speed == 1:
        if len(slanderers) == 50:
            print(len(slanderers))
            slanderers.pop(0)
        i = 0
        while magic[i] < money:
            i += 1
        money = money - magic[i-1]
        slanderers.append(i-1)
        if i-1 == 30:
            print("max level reached on turn", turn)
    money += ec_money(turn)
    for income in slanderers:
        money += income
    turn += 1
    moneys.append(money)


pp.plot(moneys)

axes = pp.gca()

axes.set_yscale('log')

pp.show()