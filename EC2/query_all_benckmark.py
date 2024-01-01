import requests
import time
from fastapi import FastAPI
from pydantic import BaseModel
import json
import boto3
import requests
import pandas as pd
from datetime import datetime, time
from datetime import timedelta

app = FastAPI()
server_url = "http://<EC2_public_IP_address>"

# android_users = ['0a7670ac17fc74ff', 'c26095f2a686f811', '5aae5dfa704c8d4a', '2255f8c14fc03fa1', '9e6999ec35e540c8', 'ad8d27cbaf3f5ac6', '72de9ee95258df0a', '66d1fee697063101', '4f40efe96805ba4b', '08ead5465d9c7e3a', '187e28405e646e59', 'f923ad2af516cadf', '812bdcc4b4bc0fbb', 'e43512961eaf8386', '8caf06b7f30eaa47', '1375adc126602d83', '744f64595e72a8e5', 'a530dff861774a91', '4a2805008646ca3e', 'b7b1f2a04e83c563', '9ff8e7ee718b3a1c', '0bff19779179f4f6', '70e14699f0baf473', '94f9e4958ac9e2df', 'e35e0deca8e041ee', 'bc64ed0865576b82', '16780993015946fb', 'c6de69bf28620ead', 'bc50c611e8b40cae', '0b33630e1f6c8085', '8d84086dc7d5db0e', '95d68b27e20e115b', '3fd0cba9e1d333e8', '421bf8400b4b9960', '58b00812d629cf9d', 'f2c3dfc44318196a', '254383581098ff77', '07fd4b9efa9c5a31', '3a2530b7f0e21150', '2887c9aca7b9b160', '1debdd1b60685850', 'ca0df0c04b793091', 'd1ffa9a45450cc91', 'e6740bcd41421f66', '92d207f7bdbbdf16', 'da8d8600b788104b', '8f96ec853cca16a9', 'affe2f01cae67256', 'd75f726255df4eb7', 'c07bd832432ecc28', '322259001a79ce07', '1c5be355a11ce2bc', 'd137418dd03e6620', 'ca4a9b7ca6a66218', '8ade5fea00e073e9', 'cd61cf6bf4f0f915', '46f95867db40b1dd', '703bb9c56e94aa0b', '040464544ab4be1d', 'a41385c491617a8f', '6a5b2d408c82e071', '954baedd1d26cd00', 'b4d1ef2715322edf', '2d70fb7c4ef7fe9d', 'd150dbf09a1affa0', '7f4edbef3b98abb7', '2e52d0b7d40f34d1', 'bc9c066a051b5468', '9fe159db74209210', '4ebf793d45bd2da8', '34a0aa9f32bface2', '52d2270b908f41b5', 'fd5177e5b267429a', '18e4c9834d80e194', '98ac535537968a51', '42e9b65fe3485f43', '0e3828a3aaf38af8', 'b558a0872fa80705', '65ad985d204fd884', 'a42e35968a4b6c9e', '56eb9fd9d7d79384', '54ae7944ca2f43f4', '08d1b445e221dc54', '17b646c1ca8b0464', 'e93eada17cc97473', '1c147ef058928e21', '329bd3e954cf8b03', '86b0bf9dd2fb4fd7', '5bad97fd72cacf1b', '82d2863b49cf7951', '3c0140b57cfab5a4', '152cf0e56952d3e8', '6f8da4e9cce9dce6', '2e9bb4435922942b', 'c00e5fc1ca6debb2', '226051d41d1fb969', '80c5d66cf1b7be84', '40de3469f007935a', '6ccfb9f0c47517a1', 'e22729019df1bd42']
# android_id = '0a7670ac17fc74ff'


def query_app_usage_by_time(device_id, start_time, end_time):
    query_app_usage_by_time_url = server_url + ":3000/rpc/query_app_usage_by_time"
    param = {"device_id": device_id,
             "start_time": start_time, "end_time": end_time}
    res = requests.post(query_app_usage_by_time_url, json=param).json()

    return res


def query_s3_all(s3_end_time_input, start_time, end_time, android_id):
    query_s3_all_url = server_url + "/query_s3_all"
    if s3_end_time_input > end_time:
        param = {"s_date_time": start_time,
                 "e_date_time": s3_end_time_input, "android_id": android_id}
    else:
        param = {"s_date_time": start_time,
                 "e_date_time": s3_end_time_input, "android_id": android_id}
    query_s3_all = requests.post(query_s3_all_url, json=param).json()
    return query_s3_all


def merge(ec2_res, s3_res):
    for i in range(len(s3_res)):
        flag = 0
        for ec2_record in ec2_res:
            if s3_res[i]["package_name"] == ec2_record["package_name"]:
                s3_res[i]["total_duration"] += ec2_record["total_duration"]
                flag = 1
                break
        if flag == 0:
            s3_res.append(ec2_record)

    return s3_res


class Benchmark(BaseModel):
    start: str
    end: str
    android_id: str


@app.post("/query_benckmark")
def query(data: Benchmark):
    start_time_input = data.start
    end_time_input = data.end
    android_id = data.android_id
    query_instruction_url = server_url + "/query_instruction"
    param = {"s_date_time": "2022-11-10 00:00:00",
             "e_date_time": "2022-11-17 23:59:00"}
    query_instruction = requests.post(query_instruction_url, json=param).json()
    query_ec2 = query_instruction["EC2"]
    query_s3 = query_instruction["S3"]

    if query_ec2 == 1 and query_s3 == 0:
        res = query_app_usage_by_time(
            android_id, start_time_input, end_time_input)
    elif query_ec2 == 0 and query_s3 == 1:
        s3_end_time_input = "2022-11-16 23:59:00"
        res = query_s3_all(s3_end_time_input, start_time_input,
                           end_time_input, android_id)
    else:
        s3_end_time_input = "2022-11-16 23:59:00"
        ec2_res = query_app_usage_by_time(
            android_id, start_time_input, end_time_input)
        s3_res = query_s3_all(
            s3_end_time_input, start_time_input, end_time_input, android_id)
        res = merge(ec2_res, s3_res)

    return res


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
                record_json_str = "{\"package_name\":\"" + \
                    splits[2] + "\",\"app_name\":\"" + splits[3] + \
                    "\",\"total_duration\":" + splits[4] + "}"
                record_json = json.loads(record_json_str)
                rows.append(record_json)
        elif 'Stats' in event:
            statsDetails = event['Stats']['Details']
            print("Stats details bytesScanned: ")
            print(statsDetails['BytesScanned'])
            print("Stats details bytesProcessed: ")
            print(statsDetails['BytesProcessed'])

    print(rows)

    return rows


class Data(BaseModel):
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
            split_record = records.split("\n")
            for record in split_record[:-1]:
                splits = record.split(",")

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
                    record_json_str = "{\"package_name\":\"" + \
                        splits[2] + "\",\"app_name\":\"" + splits[3] + \
                        "\",\"total_duration\":" + splits[4] + "}"
                    record_json = json.loads(record_json_str)
                    rows.append(record_json)
        elif 'Stats' in event:
            statsDetails = event['Stats']['Details']

    print(rows)

    return rows


class TieringData(BaseModel):
    chunk: str
    file_name: str


@app.post("/data_tiering")
def query(data: TieringData):
    chunk = data.chunk
    file_name = data.file_name
    url = server_url + ':3000/rpc/get_chunk_data'
    myobj = {'chunk': chunk}

    chunk_data = requests.post(url, json=myobj)

    # chunk_data_json = json.loads(chunk_data.text)
    chunk_data_df = pd.DataFrame.from_records(chunk_data.json())

    chunk_data_csv = chunk_data_df.to_csv(index=False)
    print(chunk_data_csv)

    s3 = boto3.client('s3',
                      aws_access_key_id='',
                      aws_secret_access_key='',
                      region_name='us-west-2')
    url_s3 = s3.generate_presigned_url(
        'put_object', Params={'Bucket': 'fyp-time-series-data', 'Key': file_name})

    put_s3_res = requests.put(url_s3, data=chunk_data_csv.encode('utf-8'))


class QueryInput(BaseModel):
    s_date_time: str
    e_date_time: str
    android_id: str


@app.post("/query_s3_all")
def query_s3_all(data: QueryInput):
    s_date_time_input = data.s_date_time
    e_date_time_input = data.e_date_time
    android_id = data.android_id
    sql = "Select * from S3Object s WHERE s._1 >= '" + s_date_time_input + \
        "' AND s._1 < '" + e_date_time_input + "' AND s._2 = '" + android_id + "'"
    s_date_time = datetime.strptime(s_date_time_input, "%Y-%m-%d %H:%M:%S")
    e_date_time = datetime.strptime(e_date_time_input, "%Y-%m-%d %H:%M:%S")
    now = datetime.now()
    date_time_now = now.strftime("%Y-%m-%d %H:%M:%S")
    now = datetime.strptime(date_time_now, "%Y-%m-%d %H:%M:%S")

    today = datetime.today()
    offset = (today.weekday() - 3) % 7
    last_thursday = today - timedelta(days=offset)
    last_thursday = datetime.combine(last_thursday, time())  # set to 00:00:00
    print(last_thursday)

    # prev of prev, because older than one week is for the end time of an interval
    last_thursday = last_thursday - timedelta(days=7)
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
    last_thursday = last_thursday + timedelta(days=7)  # prev

    while True:
        if last_thursday > e_date_time:
            last_thursday = last_thursday - timedelta(days=7)
            print("last_thursday 0", last_thursday)
        else:
            break

    if ec2:  # EC2 can store at most 2 weeks
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
                split_record = records.split("\n")
                for record in split_record[:-1]:
                    splits = record.split(",")

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
                        record_json_str = "{\"package_name\":\"" + \
                            splits[2] + "\",\"app_name\":\"" + splits[3] + \
                            "\",\"total_duration\":" + splits[4] + "}"
                        record_json = json.loads(record_json_str)
                        rows.append(record_json)
            elif 'Stats' in event:
                statsDetails = event['Stats']['Details']
                print("Stats details bytesScanned: ")
                print(statsDetails['BytesScanned'])
                print("Stats details bytesProcessed: ")
                print(statsDetails['BytesProcessed'])

    return rows


class QueryTimeRange(BaseModel):
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
    last_thursday = datetime.combine(last_thursday, time())  # set to 00:00:00
    print(last_thursday)

    # prev of prev, because older than one week is for the end time of an interval #benchmark
    last_thursday = last_thursday - timedelta(days=7)
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
        "EC2": ec2,
        "S3": s3
    }

    return result
