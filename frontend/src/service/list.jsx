import * as React from "react";
import {useDispatch, useSelector} from "react-redux";
import {useNavigate} from "react-router";

import {removeService, selectServices} from "./state";
import useFetchServices from "./useFetchServices";

const ServiceItem = ({service}) => {
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
    </li>
  );
};

const List = () => {
  const services = useSelector(selectServices);
  useFetchServices();

  return (
    (services?.length) ? (<ul>
      {services.map(
        service => (<ServiceItem service={service} key={`serviceItem-${service.id}`}/>)
      )}
    </ul>) : (<p>No services.</p>)
  );
};

export default List;
