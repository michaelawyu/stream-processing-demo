# coding: utf-8

import sys
from setuptools import setup, find_packages

NAME = "example_events"
VERSION = "0.0.1"
DESCRIPTION = ""
CONTACT = ""
URL = ""

with open('requirements.txt') as f:
    requirements = f.read().splitlines()

# To install the library, run the following
#
# python install -e .
#
# prerequisite: setuptools
# http://pypi.python.org/pypi/setuptools

setup(
    name=NAME,
    version=VERSION,
    description=DESCRIPTION,
    author_email=CONTACT,
    url="",
    keywords=["Cloud Events", "example_events"],
    install_requires=requirements,
    packages=find_packages(),
    package_data={'': ['spec/events.yaml']},
    include_package_data=True
)
