import { configureStore } from "@reduxjs/toolkit";
import servicesReducer from "../service/state";

export default configureStore({
    reducer: {
      services: servicesReducer,
    }
  }
);
