import boto3
import requests
import pandas as pd
from apscheduler.schedulers.blocking import BlockingScheduler

server_url = "http://<EC2_public_IP_address>"


def get_old_chunks():
    url = server_url + ":3000/rpc/query_old_chunks"
    old_chunks = requests.get(url)
    old_chunks = old_chunks.json()
    old_chunk_list = []
    for i in range(len(old_chunks)):
        for key, value in old_chunks[i].items():
            old_chunk_list.append(value)

    return old_chunk_list


def get_chunk_time_range(chunk_name):
    url = server_url + ":3000/rpc/get_chunk_time_range"
    param = {"chunk_input": chunk_name}
    chunk_time_range = requests.post(url, json=param)
    chunk_time_range = chunk_time_range.json()
    start_date_time = ""
    for key, value in chunk_time_range[0].items():
        if key == "start_time":
            start_date_time = value
            break
    print(start_date_time)
    start_date = start_date_time.split("T")[0]

    return start_date


def upload_to_s3(old_chunk, file_name):
    url = server_url + ':3000/rpc/get_chunk_data'
    myobj = {'chunk': old_chunk}

    chunk_data = requests.post(url, json=myobj)
    # chunk_data_json = json.loads(chunk_data.text)
    chunk_data_df = pd.DataFrame.from_records(chunk_data.json())

    chunk_data_csv = chunk_data_df.to_csv(index=False)

    s3 = boto3.client('s3',
                      aws_access_key_id='',
                      aws_secret_access_key='',
                      region_name='us-west-2')
    url_s3 = s3.generate_presigned_url(
        'put_object', Params={'Bucket': 'fyp-time-series-data', 'Key': file_name})

    put_s3_res = requests.put(url_s3, data=chunk_data_csv.encode('utf-8'))

    print(put_s3_res.content)


def remove_old_chunks():
    url = server_url + ":3000/rpc/remove_old_chunks"
    res = requests.post(url)
    print(res)


def data_tiering():
    old_chunk_list = get_old_chunks()

    for old_chunk in old_chunk_list:
        split_chunk = old_chunk.split(".")
        chunk_name = split_chunk[1]

        start_date = get_chunk_time_range(chunk_name)
        file_name = start_date + ".csv"

        upload_to_s3(old_chunk, file_name)

    remove_old_chunks()


data_tiering()
# sched = BlockingScheduler()
# sched.add_job(data_tiering, 'interval', days=7, start_date='2022-11-10 00:00:00', end_date='2023-11-10 14:57:00')
# sched.start()
