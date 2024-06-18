#!/bin/fish

set latest (fd 'toteutus-kouta[0-9-]+-at-[0-9.]+\.json')
set jq_src_latest '.[0]._source | del(.timestamp)'
set jq_src '.[0]._source | del(.timestamp)'

jq -s $jq_src_latest $latest > /tmp/b_search.json

jq $jq_src /tmp/elastic_dump/kouta-external/orig-toteutus-kouta-search.json > /tmp/a_search.json

difft /tmp/a_search.json /tmp/b_search.json
