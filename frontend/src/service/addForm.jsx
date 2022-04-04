import * as React from "react";
import {useDispatch} from "react-redux";
import {addService} from "./state";
import Form from "./form";

const AddForm = () => {
  const dispatch = useDispatch();
  const onSubmit = React.useCallback((serviceData) => {
    dispatch(addService(serviceData));
  }, [dispatch]);

  return (
    <Form onSubmit={onSubmit}/>
  );
};

export default AddForm;
