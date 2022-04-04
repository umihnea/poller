import * as React from "react";
import {useDispatch, useSelector} from "react-redux";
import {fetchServices, removeService, selectServices, selectServicesLoaded} from "./state";

const ServiceItem = ({service}) => {
  const dispatch = useDispatch();
  const onRemove = React.useCallback(serviceId => {
    dispatch(removeService(serviceId));
  }, [dispatch]);

  return (<li>
    <span>{service.name} ({service.url})</span>
    <button onClick={() => onRemove(service.id)}>Remove</button>
    <button>edit</button>
  </li>);
};

const List = () => {
  const services = useSelector(selectServices);
  const loaded = useSelector(selectServicesLoaded);
  const dispatch = useDispatch();

  React.useEffect(() => {
    if (!loaded) {
      dispatch(fetchServices());
    }
  }, [loaded, dispatch]);

  return (
    (loaded && services?.length) ? (<ul>
      {services.map(
        service => (<ServiceItem service={service} key={`serviceItem-${service.id}`}/>)
      )}
    </ul>) : (<p>No services.</p>)
  );
};

export default List;
