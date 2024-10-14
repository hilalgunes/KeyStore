# Keystore Project

This project provides an encryption and decryption manager for Android applications. It uses AES encryption to securely store and retrieve user data.

## Features

- Encrypt and save user data to a file
- Decrypt and display encrypted data
- Manage user IDs

## Installation

1. Clone this project:
    ```sh
    git clone https://github.com/username/keystore.git
    ```
2. Open Android Studio and import the project.
3. Run Gradle sync to download the necessary dependencies.

## Usage

1. Run the application.
2. Enter a username and password.
3. Click the `Encrypt` button to encrypt and save the data.
4. Click the `Decrypt` button to decrypt and display the encrypted data.

## Data Storage

The encrypted data is stored in a `.txt` file located in the device's internal storage under the following path:
`/data/data/com.example.keystore/files/user_data.txt`
