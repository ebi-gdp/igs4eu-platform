#!/bin/bash
set -e

pgs_ids_file_name="/data/pgs-ids-reference-date/pgs-ids-reference-date.txt"
pgs_ids_downloaded_file="pgs-ids.txt"
pub_data_downloaded_file="publication-data.csv"
reference_date=""

redis_host="redis.$NAMESPACE.svc.cluster.local"
redis_port=6379
key_prefix_pgs_ids=pgs_ids_set
key_prefix_pub_data=pub_data_set

populate_reference_date() {
  echo "Getting latest updated date for PGS Ids & Publication data >>>"
  reference_date=$(curl -s "https://www.pgscatalog.org/rest/release/current?format=json" | jq -r ".date")
  echo "Latest updated date for PGS Ids & Publication data retrieved ${reference_date} >>>"
}

save_reference_date_to_file() {
  echo "$reference_date" >$pgs_ids_file_name
  echo "Updated reference date for PGS Ids & Publication data saved to ${pgs_ids_file_name} >>>"
}

download_pgs_ids() {
  file_name="pgs_scores_list.txt"

  # Save latest list of PGS Ids to local file
  echo "Downloading PGS Ids to ${file_name} >>>"
  curl -o $pgs_ids_downloaded_file https://ftp.ebi.ac.uk/pub/databases/spot/pgs/$file_name
  echo "Downloaded PGS Ids; file has been saved! >>>"
}

update_pgs_ids_into_redis() {
  # Read each line from pgs-ids.txt and add it to the set in Redis
  echo "Reading pgs-ids.txt file & updating records in Redis >>>"
  while read -r line; do
    redis-cli -h $redis_host -p $redis_port SADD $key_prefix_pgs_ids "$line"
  done <$pgs_ids_downloaded_file
  echo "PGS Ids record(s) have been updated in Redis >>>"
}

update_publication_json_data_into_redis() {
  next="https://www.pgscatalog.org/rest/publication/all?limit=50&offset=0"
  #next="https://www.pgscatalog.org/rest/publication/all?format=json&limit=50&offset=650"
  #next="https://www.pgscatalog.org/rest/publication/all?filter_ids=PGP000308%2CPGP000001"

  no_of_iterations=0

  while [ "$next" != "null" ]; do
    json_response=$(curl -s "$next" -H "accept: application/json")
    next=$(jq -r '.next' <<<"$json_response")

    echo "Iteration:"$((++no_of_iterations))

    jq -r '.results[] | "\(.id)|\(.title)|\(.doi)|\(.PMID)|\(.associated_pgs_ids.development)|\(.associated_pgs_ids.evaluation)"' <<<"$json_response" |
      while IFS='|' read -r pub_id title doi pub_med_id development evaluation; do
        unique_array=()

        for element in "${development[@]}"; do
          if [ "$element" != "[]" ]; then
            unique_array+=("$element")
          fi
        done

        for element in "${evaluation[@]}"; do
          if [ "$element" != "[]" ]; then
            unique_array+=("$element")
          fi
        done

        count=$(jq -r 'length' <<<"$unique_array")
        pub_json_data="{\"pgpId\":\"$pub_id\",\"pgsIdsCount\":\"$count\"}"

        redis-cli -h $redis_host -p $redis_port MSET $key_prefix_pub_data:"$pub_id" "$pub_json_data" $key_prefix_pub_data:"$title" "$pub_json_data" \
          $key_prefix_pub_data:"$doi" "$pub_json_data" $key_prefix_pub_data:"$pub_med_id" "$pub_json_data"
      done
  done
}

check_for_update() {
  populate_reference_date
  if [ ! -f $pgs_ids_file_name ] || [ "$(cat $pgs_ids_file_name)" != "$reference_date" ]; then
    # Save reference date
    save_reference_date_to_file

    echo "Process is being started...!"

    # Download & update PGS Ids
    download_pgs_ids
    update_pgs_ids_into_redis

    # Download & update Publication data
    update_publication_json_data_into_redis

    echo "Process executed successfully!"
  else
    echo "Reference date is still the same; no need to execute update script!"
  fi
}

check_for_update
