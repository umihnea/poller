import * as React from "react";
import {useInterval} from "react-interval-hook";
import {API} from "../api";

const POLLING_INTERVAL = 5000; // 5 seconds (in milliseconds)
const HISTORY_WINDOW = 60 * 1000 // 1 minute (in milliseconds)

const useLatestStatus = () => {
  const [records, setRecords] = React.useState([]);

  useInterval(() => {
    const end = new Date(Date.now());
    const start = new Date(Date.now() - HISTORY_WINDOW);

    API.getHistory(start, end).then(data => {
      if (data && !data?.error) {
        setRecords(data);
        return;
      }

      console.log("Failed history poll.", data?.error);
    });
  }, POLLING_INTERVAL);

  // Parse records only once to find out the latest status and delay for each service
  const latestRecord = {};
  for (let index = 0; index < records.length; index++) {
    const serviceId = records[index].nodeId;
    if (!Object.keys(latestRecord).includes(serviceId)) {
      latestRecord[serviceId] = records[index];
      continue;
    }

    const existingRecord = latestRecord[serviceId];
    if (existingRecord.createdAt < records[index].createdAt) {
      latestRecord[serviceId] = records[index];
    }
  }

  return latestRecord;
};

export default useLatestStatus;
