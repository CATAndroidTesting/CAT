# Table of Content

- [Table of Content](#table-of-content)
- [Artifact package for the CAT paper](#artifact-package-for-the-cat-paper)
  - [Option 1. Running in the virtual machine (recommended)](#option-1-running-in-the-virtual-machine-recommended)
    - [Prerequisites](#prerequisites)
    - [Running CAT](#running-cat)
    - [Interpreting the result](#interpreting-the-result)
  - [Option 2. Running on your local environment](#option-2-running-on-your-local-environment)
    - [Prerequisites](#prerequisites-1)
    - [Installing CAT](#installing-cat)
    - [Running CAT and interpreting the result](#running-cat-and-interpreting-the-result)

# Artifact package for the CAT paper

We provide two ways to run the experiment: pre-configured environment in a VirtualBox image and guidance on running the experiment on your local environment.

## Option 1. Running in the virtual machine (recommended)

Virtual VM download links: via Google Drive: [Download](https://drive.google.com/file/d/14K7R1qwWSTJDzpj1NwRj1rMQGjwQ1a0T/view?usp=sharing)

The provided image can be run on VirtualBox 6.1.6 onwards as this version starts supporting nested virtualisation for running Android Emulator on both Intel and AMD CPUs.

### Prerequisites

- VirtualBox 6.1.6 onwards. Download from [https://www.virtualbox.org/](https://www.virtualbox.org/)
- Ensure your CPU supports nested virtualisation.
- Download the *ova* file from this repository.

### Running CAT

I) VirtualBox Image Setup
   1. Install and open VirtualBox.
   2. Click **Tools** and then **Import** the downloaded *ova* file.
   3. Start the imported virtual machine and input the user credentials: username=cat and password=catdroid


II) Run Experiments
   1. Open **Terminal** from the side bar.
   2. Start the emulator by typing the following command:

```
emulator -avd Nexus_5_API_22
```
   3. Open another **Terminal** and go to the CAT folder:

```
cd CAT/dataset
ls
```
   4. Select one of the interested subject app folder and go into the folder
   5. Run the following command to test the app with CAT:

```
catdroid -a API_FILE_NAME -script script.json -o OUTPUT_FOLDER
```

### Interpreting the result

   - Target states: during the execution, once the target state is entered, a message showing target state with index is shown.
   - Target events: All event sequences are stored in the OUTPUT_FOLDER/events. File names ending with *_KEY_EVENT.json* are those target events.

## Option 2. Running on your local environment

### Prerequisites

1. [Java 8 SDK](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)
2. [Android SDK](https://developer.android.com/studio). These components needs to be installed in the SDK manager: An Android SDK Platform, Android SDK Command-line Tools, Android Emulator, Android SDK Platform-Tools and HAXM Accelerator.
3. Python3 with Pip3

### Installing CAT

Clone this repo, enter the *cat-droidbot* folder and run

```
pip3 install -e .
```

### Running CAT and interpreting the result

This is same as Option 1. [Running CAT](#running-cat) and [Interpret the Result](#interpret-the-result)