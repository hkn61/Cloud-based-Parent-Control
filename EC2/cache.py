# m_d = 0.023 USD/GB/Month 
# m_c = 8.352 USD/GB/Month 
# f_d = $0.0007 per GB - for bytes returned by S3 Select in Standard, 
#       $0.00200 per GB - for bytes scanned by S3 Select in Standard,
#       $0.0044 per 10,000 GET
# f_c = 0

import requests
import json
from datetime import datetime, time
from datetime import timedelta
from apscheduler.schedulers.blocking import BlockingScheduler
import boto3

server_url = "http://100.22.18.163"
max_cache_records = 1000000
default_cache_minutes = 15
volume_attach_threshold = 100000

def get_records_sum():
    sum_records_url = server_url + ":3000/rpc/sum_records"
    res = requests.get(sum_records_url)
    print("records_sum: ", res.json()[0]["records_sum"])
    return res.json()[0]["records_sum"]

def get_volumes_num():
    ec2resource = boto3.resource('ec2', region_name='us-west-2',
                    aws_access_key_id='AKIAQ5HCDRDTFKG2RVYM',
                    aws_secret_access_key='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')
    volumes = ec2resource.volumes.all()
    volume_list = []
    for volume in volumes:
        volume_list.append(volume.id)
    return len(volume_list)

def evict_cache_data():
    print("evicting cached data...")
    get_data_to_evict_url = server_url + ":3000/rpc/get_data_to_evict"
    evict_data_url = server_url + ":3000/rpc/evict_data"
    evict_metadata_url = server_url + ":3000/rpc/evict_metadata"
    pk_now = datetime.now() + timedelta(hours=8) # Peking
    date_time_now = pk_now.strftime("%Y-%m-%d %H:%M:%S")
    pk_now = datetime.strptime(date_time_now, "%Y-%m-%d %H:%M:%S")
    now = str(pk_now).replace(" ", "T")
    get_data_to_evict_obj = {
        "now": now
    }
    get_data_to_evict = requests.post(get_data_to_evict_url, json=get_data_to_evict_obj)
    res = get_data_to_evict.json()
    for i in range(len(res)):
        evict = {
            "device_id": res[i]["android_id"],
            "start_evict": res[i]["start_time"],
            "end_evict": res[i]["end_time"]
        }
        evict_data = requests.post(evict_data_url, json=evict)
        print("evict_data: ", evict_data)
        evict_metadata = requests.post(evict_metadata_url, json=evict)
        print("evict_metadata: ", evict_metadata)

    while get_records_sum() / volume_attach_threshold + 1 < get_volumes_num():
        detach_and_delete_volume()

# evict_cache_data()
sched = BlockingScheduler()
sched.add_job(evict_cache_data, 'interval', minutes=default_cache_minutes, start_date='2022-11-10 00:00:00', end_date='2023-11-10 14:57:00')
sched.start()


def detach_and_delete_volume():
    
    InstanceID = "i-00baa8cc81205de07"
    region_name='us-west-2'

    # Using Boto3 resource to get list of disks

    ec2_client= boto3.client('ec2', region_name=region_name,
                    aws_access_key_id='AKIAQ5HCDRDTFKG2RVYM',
                    aws_secret_access_key='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')
    ec2resource = boto3.resource('ec2', region_name='us-west-2',
                    aws_access_key_id='AKIAQ5HCDRDTFKG2RVYM',
                    aws_secret_access_key='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')
    Instance = ec2resource.Instance(InstanceID)

    volumes = ec2resource.volumes.all()
        # for volume in volumes:
        #     print(volume.id)
    volume_list = []
    for volume in volumes:
        volume_list.append(volume.id)
    print(volume_list[-1])

    try:
        # Process output to get list of all disks
        for device in Instance.block_device_mappings:
            
            volume = device.get('Ebs')
            VolID = volume.get('VolumeId')
            DevID = device.get('DeviceName')
            print (" VolumeID :" , VolID , " Device :" , DevID)

    except Exception as ERR:
        print(ERR)
        exit()

    # Detaching a Volume 
    try:
        VolumeID = volume_list[-1]

        volume = ec2resource.Volume(VolumeID)
        volume.detach_from_instance(InstanceId=InstanceID, Force=True)
        waiter = ec2_client.get_waiter('volume_available')
        waiter.wait(VolumeIds=[VolumeID],)
        print (" INFO : Volume Detached")

        # Deleting Volume Device details already available
        # volume.delete()
        # print (" INFO : Volume Deleted")


    except Exception as ERR:
        print(ERR)
        # exit()

    # delete a volume
    try:
        volume = ec2resource.Volume(VolumeID)
        volume.delete()
        print (" INFO : Volume Deleted")

    except Exception as ERR:
        print(ERR)
        exit()




detach_and_delete_volume()