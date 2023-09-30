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
    base = 1737 # in seconds
    r = rnd.random()
    # res = base # deterministic
    res = base * math.log((math.e - 1) * r + 1) # probabalistic
    return res

def prob_cache_hit_old(l):
    base = 1737 # in seconds
    r = rnd.random()
    # res = base # deterministic
    res = base * math.log((pow(math.e, 0.693) - 1) * r + 1) # probabalistic
    return res

def plot_samples(l_list):
    # plt.rcParams["font.sans-serif"]='SimHei'
    plt.rcParams['axes.unicode_minus']=False
    # df = pd.DataFrame({'col': l_list})

    plt.hist(x=l_list,bins=20,
            color="steelblue",
            edgecolor="black")

    #添加x轴和y轴标签
    plt.xlabel("l (seconds)")
    plt.ylabel("number of times that l falls into the time interval")

    #添加标题
    plt.title("distribution of l")

    #显示图形
    plt.show()

baseT = 1737


# recent data cache
l_list_recent = []
# for i in range(6000):
#     rand = numpy.random.exponential(1)
#     if rand < 1:
#         r = rnd.random()
#         if r < 0.65:
#             l = baseT * min(rand, 1 - rand)
#         else:
#             l = baseT * rand * 0.6
#     else:
#         l = baseT * ((math.floor(rand)-0.5)/(rand-0.5))
#     l_list_recent.append(l)

# # recent data s3
# for i in range(4000):
#     rand = numpy.random.exponential(0.2)
#     while(rand <= 1):
#         rand = numpy.random.exponential(0.2)
#     l = baseT * rand
#     l_list_recent.append(l)

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
    # sample next visit time
    # rand = numpy.random.exponential(2)
    # if rand < 1:
    #     r = rnd.random()
    #     if r < 0.9:
    #         l = baseT * min(rand, 1 - rand) * 0.02
    #     else:
    #         l = baseT * rand * 0.05
    # else:
    #     l = baseT * ((rand-math.floor(rand))/math.floor(rand))
    r = rnd.uniform(0, 1)
    # if r < 0.98:
    #     l = baseT * 0.0008
    # else:
    #     rand = rnd.uniform(0, 1)
    #     l = baseT * rand
    l = baseT * 0.002
    l_list_old.append(l)


# old data s3
for i in range(7000):
    rand = numpy.random.exponential(0.2)
    while(rand <= 1):
        rand = numpy.random.exponential(0.2)
    r = rnd.random()
    if(r < 0.1):
        rand = rand * 1.5
    elif(r < 0.2):
        rand = rand * 2
    # elif r < 0.31:
    #     rand = rnd.uniform(0, 1)
    elif r < 0.4:
        rand = rnd.uniform(1, 2)
    elif r < 0.6:
        rand = rnd.uniform(1, 3)
    elif r < 0.8:
        rand = rnd.uniform(1, 4)
    else:
        rand = rnd.uniform(1, 5)
    # elif r < 0.5:
    #     rand = rand * 2.5
    # elif r < 0.6:
    #     rand = rand * 3
    l = baseT * rand
    l_list_old.append(l)

# for i in range(1000):
#     l_list_old.append(baseT * 0.0001)

# for i in range(2000):
#     l_list_old.append(baseT * 0.0002)

# for i in range(2000):
#     l_list_old.append(baseT * 0.0008)

# for i in range(1000):
#     l_list_old.append(baseT * 0.02)

# for i in range(500):
#     l_list_old.append(baseT * 0.3)

# for i in range(500):
#     l_list_old.append(baseT * 0.4)

# for i in range(500):
#     l_list_old.append(baseT * 0.6)

# for i in range(500):
#     l_list_old.append(baseT * 0.8)

# for i in range(2000):
#     r = rnd.uniform(1, 5)
#     l_list_old.append(baseT * r)

plot_samples(l_list_old)
plot_samples(l_list_recent)

# DET cost
det_cost_r = 0
for l in l_list_recent:
    if l < baseT:
        det_cost_r  = det_cost_r + ebs_cost(l)
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
        prob_cost_r  = prob_cost_r + ebs_cost(l)
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