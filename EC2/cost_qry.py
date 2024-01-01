import random as rnd
import math
import numpy
import matplotlib.pyplot as plt
import pandas as pd


def ebs_cost(l):
    return 0.123/30/24/3600 * l


def s3_cost(l):
    return 0.023/30/24/3600 * l + 0.0067


def prob_cache_hit(l):
    base = 1737  # in seconds
    r = rnd.random()
    # res = base # deterministic
    res = base * math.log((math.e - 1) * r + 1)  # probabalistic
    return res


def prob_cache_hit_old(l):
    base = 1737  # in seconds
    r = rnd.random()
    # res = base # deterministic
    res = base * math.log((pow(math.e, 0.693) - 1) * r + 1)  # probabalistic
    return res


def plot_samples(l_list):
    plt.rcParams['axes.unicode_minus'] = False

    plt.hist(x=l_list, bins=20,
             color="steelblue",
             edgecolor="black")

    plt.xlabel("l (seconds)")
    plt.ylabel("number of times that l falls into the time interval")

    plt.title("distribution of l")

    plt.show()


baseT = 1737


# recent data cache
l_list_recent = []

for i in range(2000):
    l_list_recent.append(baseT * 0.15)

for i in range(1500):
    l_list_recent.append(baseT * 0.3)

for i in range(1000):
    l_list_recent.append(baseT * 0.4)

for i in range(1000):
    l_list_recent.append(baseT * 0.6)

for i in range(1000):
    l_list_recent.append(baseT * 0.8)

for i in range(500):
    l_list_recent.append(baseT * 0.9)

for i in range(2000):
    r = rnd.uniform(1, 3)
    l_list_recent.append(baseT * r)

for i in range(1000):
    r = rnd.uniform(3, 5)
    l_list_recent.append(baseT * r)


l_list_old = []
# old data cache
for i in range(3000):
    r = rnd.uniform(0, 1)
    l = baseT * 0.002
    l_list_old.append(l)


# old data s3
for i in range(7000):
    rand = numpy.random.exponential(0.2)
    while (rand <= 1):
        rand = numpy.random.exponential(0.2)
    r = rnd.random()
    if (r < 0.1):
        rand = rand * 1.5
    elif (r < 0.2):
        rand = rand * 2
    elif r < 0.4:
        rand = rnd.uniform(1, 2)
    elif r < 0.6:
        rand = rnd.uniform(1, 3)
    elif r < 0.8:
        rand = rnd.uniform(1, 4)
    else:
        rand = rnd.uniform(1, 5)
    l = baseT * rand
    l_list_old.append(l)


plot_samples(l_list_old)
plot_samples(l_list_recent)

# DET cost
det_cost_r = 0
for l in l_list_recent:
    if l < baseT:
        det_cost_r = det_cost_r + ebs_cost(l)
    else:
        det_cost_r = det_cost_r + ebs_cost(baseT) + s3_cost(l-baseT)
det_cost_o = 0
for l in l_list_old:
    if l < baseT:
        det_cost_o = det_cost_o + ebs_cost(l)
    else:
        det_cost_o = det_cost_o + ebs_cost(baseT) + s3_cost(l-baseT)

print(det_cost_r, det_cost_o)

# PROB cost
prob_cost_r = 0
for l in l_list_recent:
    p = prob_cache_hit(l)
    if l <= baseT and l <= p:
        prob_cost_r = prob_cost_r + ebs_cost(l)
    else:
        prob_cost_r = prob_cost_r + ebs_cost(p) + s3_cost(l-p)

prob_cost_o = 0
for l in l_list_old:
    p = prob_cache_hit_old(l)
    if l <= baseT and l <= p:
        prob_cost_o = prob_cost_o + ebs_cost(l)
    else:
        prob_cost_o = prob_cost_o + ebs_cost(p) + s3_cost(l-p)

print(prob_cost_r, prob_cost_o)
