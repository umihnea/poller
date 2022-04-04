import {createSelector, createSlice} from '@reduxjs/toolkit';
import {API} from '../api';

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
    setError: (state, action) => {
      state.error = action.payload;
    },
  },
});

export const {setLoaded, setLoading, setError, setData, pushItem} = servicesSlice.actions;

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

export const selectServices = createSelector(state => state.services, services => services.data);
export const selectServicesLoaded = createSelector(state => state.services, services => services.loaded ?? false);

export default servicesSlice.reducer;
