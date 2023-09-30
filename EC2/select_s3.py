#!/usr/bin/env/env python3
import json
import boto3
from fastapi import FastAPI
from pydantic import BaseModel
import requests
import pandas as pd
from datetime import datetime, time
from datetime import timedelta

app = FastAPI()
server_url = "http://34.222.9.107"
interval = 7

@app.get("/s3_query")
def query():
    s3 = boto3.client('s3',
                    aws_access_key_id='',
                    aws_secret_access_key='',
                    region_name='us-west-2')

    r = s3.select_object_content(
        Bucket='fyp-time-series-data',
        Key='test_chunk_2.csv',
        ExpressionType='SQL',
        Expression="Select * from S3Object s WHERE s._1 > '2022-09-29 11:54:02' AND s._2 = 'f5f33e3b42bc7b7d'",
        InputSerialization={
            'CSV': {
                "FileHeaderInfo": "NONE",
            },
            'CompressionType': 'NONE',
        },
        OutputSerialization={'CSV': {}},
    )

    rows = []
    for event in r['Payload']:
        if 'Records' in event:
            records = event['Records']['Payload'].decode('utf-8')
            print(records)
            split_record = records.split("\n")
            for record in split_record[:-1]:
                splits = record.split(",")
                print(splits)
                record_json_str = "{\"package_name\":\"" + splits[2] + "\",\"app_name\":\"" + splits[3] + "\",\"total_duration\":" + splits[4] + "}"
                # print(record_json_str)
                record_json = json.loads(record_json_str)
                # print(record_json)
                rows.append(record_json)
        elif 'Stats' in event:
            statsDetails = event['Stats']['Details']
            print("Stats details bytesScanned: ")
            print(statsDetails['BytesScanned'])
            print("Stats details bytesProcessed: ")
            print(statsDetails['BytesProcessed'])

    print(rows)
    # df = pd.read_json(rows)

    return rows

class Data(BaseModel):
    # device_id: str
    # start_time: str
    # end_time: str
    key: str
    sql: str

@app.post("/s3_query")
def query(data: Data):
    key = data.key
    sql = data.sql
    print(sql)
    s3 = boto3.client('s3',
                    aws_access_key_id='',
                    aws_secret_access_key='',
                    region_name='us-west-2')

    r = s3.select_object_content(
        Bucket='fyp-time-series-data',
        Key=key,
        ExpressionType='SQL',
        Expression=sql,
        InputSerialization={
            'CSV': {
                "FileHeaderInfo": "IGNORE",
            },
            'CompressionType': 'NONE',
        },
        OutputSerialization={'CSV': {}},
    )

    rows = []
    for event in r['Payload']:
        if 'Records' in event:
            records = event['Records']['Payload'].decode('utf-8')
            # print(records)
            split_record = records.split("\n")
            for record in split_record[:-1]:
                splits = record.split(",")
                # print(splits)

                if len(splits) != 5:
                    print("split error! continue")
                    continue

                flag = 0
                for i in range(len(rows)):
                    if splits[3] == rows[i]["app_name"]:
                        rows[i]["total_duration"] += int(splits[4])
                        flag = 1
                        break
                if flag == 0:
                    # print(splits)
                    record_json_str = "{\"package_name\":\"" + splits[2] + "\",\"app_name\":\"" + splits[3] + "\",\"total_duration\":" + splits[4] + "}"
                    # print(record_json_str)
                    record_json = json.loads(record_json_str)
                    # print(record_json)
                    rows.append(record_json)
        elif 'Stats' in event:
            statsDetails = event['Stats']['Details']
            print("Stats details bytesScanned: ")
            print(statsDetails['BytesScanned'])
            print("Stats details bytesProcessed: ")
            print(statsDetails['BytesProcessed'])

    print(rows)

    return rows


class TieringData(BaseModel):
    # device_id: str
    # start_time: str
    # end_time: str
    chunk: str
    file_name: str


@app.post("/data_tiering")
def query(data: TieringData):
    chunk = data.chunk
    file_name = data.file_name
    url = server_url + ':3000/rpc/get_chunk_data'
    myobj = {'chunk': chunk}

    chunk_data = requests.post(url, json = myobj)
    # print(chunk_data.json())

    # chunk_data_json = json.loads(chunk_data.text)
    chunk_data_df = pd.DataFrame.from_records(chunk_data.json())
    # print(chunk_data_df)

    chunk_data_csv = chunk_data_df.to_csv(index=False)
    print(chunk_data_csv)

    s3 = boto3.client('s3',
                    aws_access_key_id='',
                    aws_secret_access_key='',
                    region_name='us-west-2')
    url_s3 = s3.generate_presigned_url('put_object', Params={'Bucket': 'fyp-time-series-data', 'Key': file_name})
    print(url_s3)

    # url_s3 = "https://fyp-time-series-data.s3.us-west-2.amazonaws.com/" + file_name
    put_s3_res = requests.put(url_s3, data = chunk_data_csv.encode('utf-8'))

    print(put_s3_res)
    print(put_s3_res.content)



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
    sql = "Select * from S3Object s WHERE s._1 >= '" + s_date_time_input + "' AND s._1 < '" + e_date_time_input + "' AND s._2 = '" + android_id + "'"
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

    # find S3 key list
    last_thursday = last_thursday + timedelta(days=7) # prev
    
    while True:
        if last_thursday > e_date_time:
            last_thursday = last_thursday - timedelta(days=7)
            print("last_thursday 0", last_thursday)
        else:
            break

    if ec2: # EC2 can store at most 2 weeks
        last_thursday = last_thursday - timedelta(days=7)
        print("last_thursday ec2", last_thursday)

    if s3 and not ec2:
        key = last_thursday.strftime("%Y-%m-%d") + ".csv"
        s3_key_list.append(key)

    while True:
        print("last_thursday 1", last_thursday)
        if last_thursday <= s_date_time:
            break
        else:
            last_thursday = last_thursday - timedelta(days=7)
            key = last_thursday.strftime("%Y-%m-%d") + ".csv"
            print("key", key)
            s3_key_list.append(key)
            print("list", s3_key_list)

    # s3_key_json = []
    # if len(s3_key_list) > 0:
    #     i = 0
    #     s3_key_json.append({
    #         i: s3_key_list[i]
    #     })
    #     for i in range(1, len(s3_key_list)):
    #         tmp_key_json = {
    #             i: s3_key_list[i]
    #         }
    #         s3_key_json.append(tmp_key_json)

    # result = {
    #     "EC2": ec2,
    #     "S3": s3,
    #     "S3KeyList": s3_key_json
    # }


    s3 = boto3.client('s3',
                    aws_access_key_id='',
                    aws_secret_access_key='',
                    region_name='us-west-2')


    rows = []
    print("used s3 key list", s3_key_list)
    for i in range(len(s3_key_list)):

        r = s3.select_object_content(
        Bucket='fyp-time-series-data',
        Key=s3_key_list[i],
        ExpressionType='SQL',
        Expression=sql,
        InputSerialization={
            'CSV': {
                "FileHeaderInfo": "IGNORE",
            },
            'CompressionType': 'NONE',
        },
        OutputSerialization={'CSV': {}},
        )

        for event in r['Payload']:
            if 'Records' in event:
                records = event['Records']['Payload'].decode('utf-8', 'ignore')
                # print(records)
                split_record = records.split("\n")
                for record in split_record[:-1]:
                    splits = record.split(",")
                    # print(splits)

                    if len(splits) != 5:
                        print("split error! continue")
                        continue

                    flag = 0
                    for i in range(len(rows)):
                        if splits[3] == rows[i]["app_name"]:
                            rows[i]["total_duration"] += int(splits[4])
                            flag = 1
                            break
                    if flag == 0:
                        # print(splits)
                        record_json_str = "{\"package_name\":\"" + splits[2] + "\",\"app_name\":\"" + splits[3] + "\",\"total_duration\":" + splits[4] + "}"
                        # print(record_json_str)
                        record_json = json.loads(record_json_str)
                        # print(record_json)
                        rows.append(record_json)
            elif 'Stats' in event:
                statsDetails = event['Stats']['Details']
                print("Stats details bytesScanned: ")
                print(statsDetails['BytesScanned'])
                print("Stats details bytesProcessed: ")
                print(statsDetails['BytesProcessed'])


    return rows


class QueryTimeRange(BaseModel):
    # device_id: str
    # start_time: str
    # end_time: str
    s_date_time: str
    e_date_time: str

@app.post("/query_instruction")
def query_instruction(data: QueryTimeRange):
    s_date_time_input = data.s_date_time
    e_date_time_input = data.e_date_time
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

    result = {
        "EC2": 1,
        "S3": 0
    }

    return result
