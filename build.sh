#!/bin/bash

# Copyright 2014 Groupon.com
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Arguments
project=
current_branch=
release_branch=

# Extract arguments
OPTIND=1
while getopts "p:c:r:" opt; do
  case "$opt" in
    p)
      project=$OPTARG
      ;;
    c)
      current_branch=$OPTARG
      ;;
    r)
      release_branch=$OPTARG
      ;;
  esac
done

if [[ -z "$project" ]]; then echo "project not specified"; exit 1; fi
if [[ -z "$current_branch" ]]; then echo "current branch not specified"; exit 1; fi
if [[ -z "$release_branch" ]]; then echo "release branch not specified"; exit 1; fi

# Please see build.sh in the root of the repository for how this script is invoked.
../../build/mavenBuild.sh -p "$project" -c "$current_branch" -r "$release_branch" -b "mvn --update-snapshots clean install" -d "mvn --update-snapshots deploy -Dmaven.test.skip=true" -v
result=$?
exit $result

