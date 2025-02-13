#!/usr/bin/env python3
import requests
import json
import time
from datetime import datetime
import random

BASE_URL = "http://localhost:8081/api/transactions"

def send_high_amount_transaction():
    account_id = "ACC-{}".format(random.randint(1000, 9999))
    response = requests.post(
        "{}/high-amount/{}".format(BASE_URL, account_id),
        params={"amount": 5000.0}
    )
    print("High amount transaction response: {}".format(response.text))

def send_multiple_transactions():
    account_id = "ACC-{}".format(random.randint(1000, 9999))
    response = requests.post(
        "{}/multiple/{}/5".format(BASE_URL, account_id),
        params={"delayMs": 500}
    )
    print("Multiple transactions response: {}".format(response.text))

def send_location_pattern():
    account_id = "ACC-{}".format(random.randint(1000, 9999))
    locations = ["New York", "Tokyo", "London", "Paris", "high-risk-location"]
    response = requests.post(
        "{}/locations/{}".format(BASE_URL, account_id),
        json=locations,
        headers={"Content-Type": "application/json"}
    )
    print("Location pattern response: {}".format(response.text))

def send_single_transaction():
    transaction = {
        "id": "TRX-{}".format(int(time.time() * 1000)),
        "accountId": "ACC-{}".format(random.randint(1000, 9999)),
        "amount": random.uniform(100.0, 5000.0),
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "merchantId": "MERCH-{}".format(random.randint(100, 999)),
        "location": random.choice(["New York", "Tokyo", "London", "high-risk-location"])
    }

    response = requests.post(
        "{}/send".format(BASE_URL),
        json=transaction,
        headers={"Content-Type": "application/json"}
    )
    print("Single transaction response: {}".format(response.text))

def main():
    while True:
        print("\nFraud Detection Test Script")
        print("1. Send high amount transaction")
        print("2. Send multiple transactions")
        print("3. Send location pattern")
        print("4. Send single transaction")
        print("5. Exit")

        choice = input("\nEnter your choice (1-5): ")

        if choice == "1":
            send_high_amount_transaction()
        elif choice == "2":
            send_multiple_transactions()
        elif choice == "3":
            send_location_pattern()
        elif choice == "4":
            send_single_transaction()
        elif choice == "5":
            break
        else:
            print("Invalid choice!")

        time.sleep(1)

if __name__ == "__main__":
    main()