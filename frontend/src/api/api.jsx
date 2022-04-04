import axios from 'axios';

const JSON_CONTENT_TYPE = {
  headers: {
    "Content-Type": "application/json",
  },
};

class API {
  constructor() {
    this.baseUrl = 'http://localhost:8080/api';
  }

  fetchAllServices() {
    return axios.get(`${this.baseUrl}/nodes`)
      .then(response => {
        const code = response.status;
        if (200 <= code && code <= 299) {
          return response.data;
        } else {
          return this.handleStatusCodeFailure(response);
        }
      })
      .catch(error => this.handleInternalError(error));
  }

  handleStatusCodeFailure(response) {
    const code = response.status;
    console.warn(`API call rejected with status code ${code}.`);

    return {
      error: response.statusText,
      statusCode: code,
    };
  }

  handleInternalError(error) {
    console.error(`API call failed: ${error}.`);

    return {
      error: "internal failure",
      statusCode: 400,
    };
  }

  addService(serviceData) {
    const payload = serviceData?.createdAt ? {...serviceData} : {
      ...serviceData,
      createdAt: new Date(Date.now()).toISOString()
    };

    return axios.post(`${this.baseUrl}/node`, payload, JSON_CONTENT_TYPE)
      .then(response => {
        if (response.status !== 201) {
          return this.handleStatusCodeFailure(response);
        }

        return response.data;
      })
      .catch(error => this.handleInternalError(error));
  }

  removeService(serviceId) {
    return axios.delete(`${this.baseUrl}/node/${serviceId}`)
      .then(response => {
        if (response.status !== 200) {
          return this.handleStatusCodeFailure(response);
        }

        return serviceId;
      })
      .catch(error => this.handleInternalError(error));
  }

  updateService(serviceId, updatedData) {
    return axios.put(`${this.baseUrl}/node/${serviceId}`, updatedData, JSON_CONTENT_TYPE)
      .then(response => {
        if (response.status !== 200) {
          return this.handleStatusCodeFailure(response);
        }

        return response.data;
      })
      .catch(error => this.handleInternalError(error));
  }

  getHistory(start, end) {
    const query = { start, end };
    return axios.post(`${this.baseUrl}/history`, query, JSON_CONTENT_TYPE)
      .then(response => {
        if (response.status !== 200) {
          return this.handleStatusCodeFailure(response);
        }

        return response.data;
      })
      .catch(error => this.handleInternalError(error));
  }
}

export default new API();
