import json
import math
from time import perf_counter
import boto3
from fastapi import FastAPI
from pydantic import BaseModel
import requests
import pandas as pd
from datetime import datetime, time
from datetime import timedelta
import random as rnd

app = FastAPI()
server_url = "http://100.22.18.163"
max_cache_records = 1000000
default_cache_minutes = 10
volume_attach_threshold = 100000


class QueryInput(BaseModel):
    # device_id: str
    # start_time: str
    # end_time: str
    s_date_time: str
    e_date_time: str
    android_id: str

@app.post("/query_s3_all")
def query_s3_all(data: QueryInput):
    s_date_time_input = data.s_date_time
    e_date_time_input = data.e_date_time
    android_id = data.android_id
    s_date_time = datetime.strptime(s_date_time_input, "%Y-%m-%d %H:%M:%S")
    e_date_time = datetime.strptime(e_date_time_input, "%Y-%m-%d %H:%M:%S")
    now = datetime.now()
    date_time_now = now.strftime("%Y-%m-%d %H:%M:%S")
    now = datetime.strptime(date_time_now, "%Y-%m-%d %H:%M:%S")

    today = datetime.today()
    offset = (today.weekday() - 3) % 7
    last_thursday = today - timedelta(days=offset)
    last_thursday = datetime.combine(last_thursday, time()) # set to 00:00:00
    print(last_thursday)

    last_thursday = last_thursday - timedelta(days=7)  # prev of prev, because older than one week is for the end time of an interval
    print(last_thursday)

    now = str(now).replace(" ", "T")
    if e_date_time_input > now:
        e_date_time_input = now
    # sql = "Select * from S3Object s WHERE s._1 >= '" + s_date_time_input + "' AND s._1 < '" + e_date_time_input + "' AND s._2 = '" + android_id + "'"
    
    # determine EC2 or S3

    ec2 = 0
    s3 = 0
    s3_key_list = []

    if s_date_time >= e_date_time:
        pass
    elif last_thursday <= s_date_time:
        ec2 = 1
    elif last_thursday >= e_date_time:
        s3 = 1
    else:
        ec2 = 1
        s3 = 1

    if ec2 == 1 and s3 == 0:
        res = 0.023/30/24/3600
    else:
        res = perform_query(android_id, s_date_time_input, e_date_time_input, last_thursday)

    return res


def perform_query(android_id, s_date_time_input, e_date_time_input, last_thursday):
    time_interval_in_cache_url = server_url + ":3000/rpc/time_interval_in_cache"
    time_interval_in_cache_obj = {
        "device_id": android_id,
        "start_c": s_date_time_input,
        "end_c": e_date_time_input
    }
    time_interval_in_cache = requests.post(time_interval_in_cache_url, json=time_interval_in_cache_obj)
    cache_start_time = time_interval_in_cache.json()[0]["start_time"]
    cache_end_time = time_interval_in_cache.json()[0]["end_time"]
    last_thursday = str(last_thursday).replace(" ", "T")
    if cache_start_time <= last_thursday and cache_end_time >= e_date_time_input: # all in cache:
        res = 0.023/30/24/3600
    elif cache_start_time > last_thursday and cache_end_time >= e_date_time_input: # ec2 + s3 + cache
        ec2_res = 0.023/30/24/3600/2
        s3_res = 0.0067/2
        res = ec2_res + s3_res
    elif cache_start_time <= last_thursday and cache_end_time < e_date_time_input: # ec2 + cache + s3
        ec2_res = 0.023/30/24/3600/2
        s3_res = 0.0067/2
        res = ec2_res + s3_res
    else: # ec2 + s3 + cache + s3
        ec2_res = 0.023/30/24/3600/3
        s3_res_1 = 0.0067/3
        s3_res_2 = 0.0067/3
        res = ec2_res + s3_res_1 + s3_res_2

    return res



def query_app_usage_by_time_s3(android_id, s_date_time_input, e_date_time_input):
    cache_metadata_url = server_url + ':3000/cache_metadata'

    pk_now = datetime.now() + timedelta(hours=8) # Peking
    date_time_now = pk_now.strftime("%Y-%m-%d %H:%M:%S")
    pk_now = datetime.strptime(date_time_now, "%Y-%m-%d %H:%M:%S")
    print(str(pk_now).replace(" ", "T"))


    cache_metadata_obj = {
        "android_id": android_id,
        "start_time": s_date_time_input,
        "end_time": e_date_time_input,
        "num_records": 100,
        "upload_time": str(pk_now).replace(" ", "T"),
        "evict_time": calculate_eviction_time(e_date_time_input)
    }
    cache_metadata = requests.post(cache_metadata_url, json = cache_metadata_obj)

    remove_cache_metadata_duplicate_url = server_url + ":3000/rpc/remove_cache_metadata_duplicate"
    remove_cache_metadata_duplicate_obj = {
        "device_id": android_id
    }
    remove_cache_metadata_duplicate = requests.post(remove_cache_metadata_duplicate_url, json=remove_cache_metadata_duplicate_obj)




def calculate_eviction_time(e_date_time_input):
    e_date_time = datetime.strptime(e_date_time_input.replace("T", " "), "%Y-%m-%d %H:%M:%S")
    now = datetime.now()
    date_time_now = now.strftime("%Y-%m-%d %H:%M:%S")
    now = datetime.strptime(date_time_now, "%Y-%m-%d %H:%M:%S")
    base = 1737 # in seconds
    r = rnd.random()
    res = base # deterministic
    # res = base * math.log((math.e - 1) * r + 1) # probabalistic
    eviction_time = now + timedelta(seconds=res)
    print(eviction_time)
    return eviction_time