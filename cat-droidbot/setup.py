# set up basic requirements for cat based on droidbot
from setuptools import setup, find_packages, findall
import os

setup(
    name='catdroid',
    packages=find_packages(include=['droidbot', 'droidbot.adapter']),
    # this must be the same as the name above
    version='1.0.2b4',
    description='A lightweight UI-guided test input generator for Android.',
    author='Anonymous',
    license='Apache 2.0, MIT',
    author_email='cattestingdroid@gmail.com',
    url='https://github.com/CATAndroidTesting/CAT',  # use the URL to the github repo
    download_url='https://github.com/CATAndroidTesting/CAT',
    keywords=['testing', 'monkey', 'exerciser'],  # arbitrary keywords
    classifiers=[
        # How mature is this project? Common values are
        #   3 - Alpha
        #   4 - Beta
        #   5 - Production/Stable
        'Development Status :: 4 - Beta',

        # Indicate who your project is intended for
        'Intended Audience :: Developers',
        'Topic :: Software Development :: Testing',

        # Pick your license as you wish (should match "license" above)
        'License :: OSI Approved :: MIT License',

        # Specify the Python versions you support here. In particular, ensure
        # that you indicate whether you support Python 2, Python 3 or both.
        'Programming Language :: Python',
    ],
    entry_points={
        'console_scripts': [
            'catdroid=droidbot.start:main',
        ],
    },
    package_data={
        'droidbot': [os.path.relpath(x, 'droidbot') for x in findall('droidbot/resources/')]
    },
    # androidviewclient doesnot support pip install, thus you should install it with easy_install
    install_requires=['androguard', 'networkx', 'Pillow'],
)
