import * as React from "react";
import {useDispatch, useSelector} from "react-redux";
import {fetchServices, selectServices, selectServicesLoaded} from "./state";

const ServiceItem = ({service}) => {
  return (<li>
    <span>{service.name} ({service.url})</span>
    <button>delete</button>
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
