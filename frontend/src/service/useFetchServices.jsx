import * as React from "react";
import {useDispatch, useSelector} from "react-redux";

import {fetchServices, selectServicesLoaded} from "./state";

const useFetchServices = () => {
  const loaded = useSelector(selectServicesLoaded);
  const dispatch = useDispatch();

  React.useEffect(() => {
    if (!loaded) {
      dispatch(fetchServices());
    }
  }, [loaded, dispatch]);
};

export default useFetchServices;
