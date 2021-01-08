import math
from matplotlib import pyplot as pp 

def plot_and_show_list(l):
    pp.plot(l)
    pp.show()

def ec_money(turn):
    return math.ceil(0.2 * math.sqrt(turn))

ideal_slanderer_influence = [0, 21, 41, 63, 85, 107, 130, 154, 178, 203, 228, 255, 282, 310, 339, 368, 399, 431, 463, 497, 532, 568, 605, 643, 683, 724, 766, 810, 855, 902, 949, 2**31 - 1]

TC = 250
turn = 1
slanderers = []

moneys = []
money = 150
speed = 20
ectotal = 0

for turn in range(TC):

    # if turn % 10 == 0:
        # print(money)
        # print("ec money: ", ectotal)

    if len(slanderers) == 50:
        slanderers.pop(0)

    if turn % speed == 1:
        i = 0
        while ideal_slanderer_influence[i] < money:
            i += 1
        print("built slanderer of size", ideal_slanderer_influence[i-1]," turn", turn)
        money = money - ideal_slanderer_influence[i-1]
        slanderers.append(i-1)
        # if i-1 == 30:
            # print("max level reached on turn", turn)
    else:
        slanderers.append(0)

    money += ec_money(turn)
    ectotal += ec_money(turn)

    for income in slanderers:
        money += income
        
    moneys.append(money)

plot_and_show_list(moneys)


