import {createSelector, createSlice} from '@reduxjs/toolkit';
import {API} from '../api';
import {EMPTY_ARRAY} from "../empty";

export const servicesSlice = createSlice({
  name: 'services',
  initialState: {
    data: null,
    loading: false,
    loaded: false,
    error: null,
  },
  reducers: {
    setLoaded: (state, action) => {
      state.loaded = action.payload;
    },
    setLoading: (state, action) => {
      state.loading = action.payload;
    },
    setData: (state, action) => {
      state.data = action.payload;
    },
    pushItem: (state, action) => {
      if (state.data) {
        state.data.push(action.payload);
      } else {
        state.data = [action.payload];
      }
    },
    popItem: (state, action) => {
      if (state.data?.length) {
        const index = state.data.findIndex(item => item.id === action.payload);
        state.data = [
          ...state.data.slice(0, index),
          ...state.data.slice(index + 1),
        ];
      }
    },
    updateItem: (state, action) => {
      const payload = action.payload;
      if (state.data?.length) {
        const updatedData = [...state.data];
        const index = state.data.findIndex(item => item.id === payload.id);
        updatedData[index] = payload.updatedItem;

        return {...state, data: updatedData};
      } else {
        return state;
      }
    },
    setError: (state, action) => {
      state.error = action.payload;
    },
  },
});

export const {setLoaded, setLoading, setError, setData, pushItem, popItem, updateItem} = servicesSlice.actions;

export const fetchServices = () => (dispatch, getState) => {
  dispatch(setLoading(true));
  API.fetchAllServices().then(data => {
    if (data.error != null) {
      dispatch(setError(data.error));
    } else if (data?.length) {
      dispatch(setData(data));
    } else {
      dispatch(setError("unknown error"));
    }

    dispatch(setLoading(false));
    dispatch(setLoaded(true));
  });
};

export const addService = (serviceData) => (dispatch, getState) => {
  API.addService(serviceData).then(data => {
    if (!data.error) {
      dispatch(pushItem(data));
    } else {
      dispatch(setError(data.error));
    }
  });
};

export const updateService = (serviceId, serviceData) => (dispatch, getState) => {
  API.updateService(serviceId, serviceData).then(data => {
    if (!data.error) {
      dispatch(updateItem({id: serviceId, updatedItem: serviceData}));
    } else {
      dispatch(setError(data.error));
    }
  });
};

export const removeService = (serviceId) => (dispatch, getState) => {
  API.removeService(serviceId).then(data => {
    if (data.error != null) {
      dispatch(setError(data.error));
    } else {
      dispatch(popItem(serviceId));
    }
  });
};

export const selectServices = createSelector(state => state.services, services => services.data || EMPTY_ARRAY);
export const selectServicesLoaded = createSelector(state => state.services, services => services.loaded ?? false);

export default servicesSlice.reducer;
