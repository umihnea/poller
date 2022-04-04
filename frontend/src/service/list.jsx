import * as React from "react";
import {useDispatch, useSelector} from "react-redux";
import {useNavigate} from "react-router";

import {removeService, selectServices} from "./state";
import useFetchServices from "./useFetchServices";
import {useLatestStatus} from "../status";

const ServiceItem = ({service, status, delay}) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const onRemove = React.useCallback(serviceId => {
    dispatch(removeService(serviceId));
  }, [dispatch]);

  const onEdit = React.useCallback(serviceId => {
    navigate(`service/update/${serviceId}`);
  }, [navigate]);

  return (
    <li>
      <span>{service.name} ({service.url})</span>
      <button onClick={() => onRemove(service.id)}>Remove</button>
      <button onClick={() => onEdit(service.id)}>Edit</button>
      {`status: ${status ? status : '-'}, `}
      {`latency: ${delay ? delay + 'ms' : '-'}`}
    </li>
  );
};

const List = () => {
  const services = useSelector(selectServices);
  useFetchServices();

  const statusData = useLatestStatus();
  const statuses = Object.fromEntries(
    services.map(service => {
      const latestCode = statusData?.[service.id]?.statusCode ?? null;
      return [service.id, latestCode];
    })
  );
  const delays = Object.fromEntries(
    services.map(service => {
      const latestDelay = statusData?.[service.id]?.delay ?? 0;
      return [service.id, latestDelay];
    })
  );

  return (
    (services?.length) ? (<ul>
      {services.map(
        service => (
          <ServiceItem service={service} status={statuses[service.id]} delay={delays[service.id]}
                       key={`serviceItem-${service.id}`}
          />)
      )}
    </ul>) : (<p>No services.</p>)
  );
};

export default List;
