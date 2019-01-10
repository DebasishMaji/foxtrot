import requests
import json
import pprint
import time
import argparse

host = "http://stg-els601.phonepe.nm2:9200/"

r = requests.get(host + "foxtrot-*/_settings/index.creation_date")
data = r.json()
# pprint.pprint(data)

oneDaySeconds = 86400
oneMonthSeconds = 2592000
currentTime = int(time.time())

parser = argparse.ArgumentParser(description='Deleting foxtrot data beyond certain day')
parser.add_argument('--day', type=int, metavar='N', action='store',
                    help='Days beyond which data needs to be deleted',
                    required=True)
args = parser.parse_args()

for indexName in data:
    creation_date = data[indexName]["settings"]["index"]["creation_date"]
    if (currentTime*1000 - int(creation_date)) > args.day*oneDaySeconds*1000:
        r = requests.delete(host + indexName)