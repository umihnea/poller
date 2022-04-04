import * as React from "react";
import {useDispatch, useSelector} from "react-redux";

import {selectServices, updateService} from "./state";
import Form from "./form";
import useFetchServices from "./useFetchServices";

const UpdateForm = ({serviceId}) => {
  const dispatch = useDispatch();
  const services = useSelector(selectServices);
  useFetchServices();

  const serviceId_int = parseInt(serviceId, 10);
  const service = services.find(service => service.id === serviceId_int);

  const onSubmit = React.useCallback((serviceData) => {
    dispatch(updateService(serviceId, serviceData));
  }, [serviceId, dispatch]);

  if (!service) {
    return (<p>{`No service exists with this ID (${serviceId}).`}</p>);
  }

  return (
    <Form loadedData={service} onSubmit={onSubmit}/>
  );
};

export default UpdateForm;
